package com.chrome_history_dashboard.browser_history_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.chrome_history_dashboard.browser_history_entity.Chrome_Url_Entity;
import com.chrome_history_dashboard.browser_history_entity.Visit_Entity;
import com.chrome_history_dashboard.browser_history_repo.Chrome_Url_Repo;
import com.chrome_history_dashboard.browser_history_repo.Visit_Repo;
import com.chrome_history_dashboard.config.ChromeHistoryProperties;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.*;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Reads Chrome's native "History" SQLite file and syncs it into our own app database.
 *
 * IMPORTANT — the locking constraint:
 * While Chrome is running, it holds an exclusive lock on the History file, so a direct
 * JDBC connection to the live path will usually throw "SQLITE_BUSY: database is locked".
 * To read "live" data without asking the user to close Chrome, we copy the file to a
 * temp location immediately before each read (a cheap, near-instant OS-level copy),
 * then open sqlite-jdbc against that snapshot. This is the same trick tools like
 * SQLiteBrowser/DB Browser use, and it's the only reliable way to do this while Chrome
 * is open, on any OS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Chrome_History_Reader_Service {
	 private final ChromeHistoryProperties properties;
	    private final Chrome_Url_Repo urlRepository;
	    private final Visit_Repo visitRepository;

	    private static final long WEBKIT_EPOCH_DIFF_SECONDS = 11_644_473_600L;

	    public SyncResult syncFromChrome() throws IOException, SQLException {
	        Path historyFile = resolveHistoryPath();
	        if (!Files.exists(historyFile)) {
	            throw new IllegalStateException("Chrome History file not found at: " + historyFile
	                    + ". Set chrome.history.path in application.yml if your profile is non-default.");
	        }

	        Path snapshot = Files.createTempFile("chrome-history-snapshot-", ".sqlite");
	        Files.copy(historyFile, snapshot, StandardCopyOption.REPLACE_EXISTING);

	        int urlsSynced;
	        int visitsSynced;

	        String jdbcUrl = "jdbc:sqlite:" + snapshot.toAbsolutePath();
	        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
	            urlsSynced = syncUrls(conn);
	            visitsSynced = syncVisits(conn);
	        } finally {
	            Files.deleteIfExists(snapshot);
	        }

	        return new SyncResult(urlsSynced, visitsSynced, LocalDateTime.now());
	    }

	    private int syncUrls(Connection conn) throws SQLException {
	        String sql = "SELECT id, url, title, visit_count, typed_count, last_visit_time, hidden FROM urls";
	        int count = 0;
	        try (Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(sql)) {
	            while (rs.next()) {
	                Integer chromeId = rs.getInt("id");
	                String rawUrl = rs.getString("url");

	                Chrome_Url_Entity entity = urlRepository.findByChromeUrlId(chromeId)
	                        .orElseGet(Chrome_Url_Entity::new);

	                entity.setChromeUrlId(chromeId);
	                entity.setUrl(rawUrl);
	                entity.setTitle(rs.getString("title"));
	                entity.setVisitCount(rs.getInt("visit_count"));
	                entity.setTypedCount(rs.getInt("typed_count"));
	                entity.setLastVisitTime(fromWebKitTime(rs.getLong("last_visit_time")));
	                entity.setHidden(rs.getInt("hidden") == 1);
	                entity.setDomain(extractDomain(rawUrl));
	                entity.setCategory(categorize(entity.getDomain()));
	                entity.setLastSyncedAt(LocalDateTime.now());

	                urlRepository.save(entity);
	                count++;
	            }
	        }
	        return count;
	    }

	    private int syncVisits(Connection conn) throws SQLException {
	        String sql = "SELECT id, url, visit_time, from_visit, transition, segment_id, visit_duration FROM visits";
	        int count = 0;
	        try (Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(sql)) {
	            while (rs.next()) {
	                Long chromeVisitId = rs.getLong("id");   // was long — didn't match Long PK
	                Integer chromeUrlId = rs.getInt("url");    // was long — didn't match Chrome_Url_Repo signature

	                Chrome_Url_Entity url = urlRepository.findByChromeUrlId(chromeUrlId).orElse(null);
	                if (url == null) continue;

	                Visit_Entity entity = visitRepository.findByChromeVisitId(chromeVisitId)
	                        .orElseGet(Visit_Entity::new);

	                LocalDateTime visitTime = fromWebKitTime(rs.getLong("visit_time"));
	                long durationSeconds = rs.getLong("visit_duration") / 1_000_000L;

	                entity.setChromeVisitId(chromeVisitId);
	                entity.setUrl(url);
	                entity.setVisitTime(visitTime);
	                entity.setFromVisit(rs.getLong("from_visit"));
	                entity.setTransitionType(rs.getInt("transition"));
	                entity.setSegmentId(rs.getLong("segment_id"));
	                entity.setVisitDurationSeconds(durationSeconds);
	                entity.setDayOfWeek(visitTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
	                entity.setHourOfDay(visitTime.getHour());
	                entity.setIsTypedNavigation((rs.getInt("transition") & 0xFF) == 1);

	                visitRepository.save(entity);
	                count++;
	            }
	        }
	        recomputeTimeSpentPerUrl();
	        return count;
	    }

	    private void recomputeTimeSpentPerUrl() {
	        var totals = visitRepository.sumDurationGroupedByUrl();
	        totals.forEach(t -> urlRepository.findById(t.getUrlId()).ifPresent(u -> {
	            u.setTotalTimeSpentSeconds(BigDecimal.valueOf(t.getTotalSeconds())); // Long -> BigDecimal
	            urlRepository.save(u);
	        }));
	    }

	    private LocalDateTime fromWebKitTime(long webkitMicros) {
	        if (webkitMicros <= 0) return null;
	        long unixSeconds = (webkitMicros / 1_000_000L) - WEBKIT_EPOCH_DIFF_SECONDS;
	        return LocalDateTime.ofEpochSecond(unixSeconds, 0, ZoneOffset.UTC);
	    }

	    private String extractDomain(String rawUrl) {
	        try {
	            String host = URI.create(rawUrl).getHost();
	            return host == null ? "unknown" : host.replaceFirst("^www\\.", "");
	        } catch (Exception e) {
	            return "unknown";
	        }
	    }

	    private String categorize(String domain) {
	        if (domain == null) return "Other";
	        if (domain.contains("github") || domain.contains("stackoverflow") || domain.contains("localhost"))
	            return "Dev Tools";
	        if (domain.contains("youtube") || domain.contains("netflix"))
	            return "Entertainment";
	        if (domain.contains("google") || domain.contains("bing"))
	            return "Search";
	        if (domain.contains("linkedin") || domain.contains("twitter") || domain.contains("instagram") || domain.contains("facebook"))
	            return "Social";
	        return "Other";
	    }

	    private Path resolveHistoryPath() {
	        if (properties.getPath() != null && !properties.getPath().isBlank()) {
	            return Paths.get(properties.getPath());
	        }
	        String os = System.getProperty("os.name").toLowerCase();
	        String userHome = System.getProperty("user.home");
	        if (os.contains("win")) {
	            return Paths.get(userHome, "AppData", "Local", "Google", "Chrome", "User Data", "Default", "History");
	        } else if (os.contains("mac")) {
	            return Paths.get(userHome, "Library", "Application Support", "Google", "Chrome", "Default", "History");
	        } else {
	            return Paths.get(userHome, ".config", "google-chrome", "Default", "History");
	        }
	    }

	    public record SyncResult(int urlsSynced, int visitsSynced, LocalDateTime syncedAt) {}
	
}
