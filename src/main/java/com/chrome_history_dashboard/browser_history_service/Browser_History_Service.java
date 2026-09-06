package com.chrome_history_dashboard.browser_history_service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.chrome_history_dashboard.browser_history_entity.Browser_History_Entity;
import com.chrome_history_dashboard.browser_history_entity.Chrome_Url_Entity;
import com.chrome_history_dashboard.browser_history_repo.Browser_History_Repo;
import com.chrome_history_dashboard.browser_history_repo.Chrome_Url_Repo;
import com.chrome_history_dashboard.browser_history_tag_entity.Browser_History_Tag_Entity;
import com.chrome_history_dashboard.browser_history_tag_repo.Browser_History_Tag_Repo;
import com.chrome_history_dashboard.browser_history_tag_service.Browser_History_Tag_Service;
import com.chrome_history_dashboard.tag_entity.Tag_Entity;
import com.chrome_history_dashboard.tag_repo.Tag_Repo;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Turns raw Chrome_Url_Entity rows into Browser_History_Entity + auto-applied tags.
 * Runs right after Chrome_History_Reader_Service.syncFromChrome() on every "Generate" click.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Browser_History_Service {
	
	private final Chrome_Url_Repo chromeUrlRepo;
    private final Browser_History_Repo browserHistoryRepo;
    private final Tag_Repo tagRepo;
    private final Browser_History_Tag_Repo browserHistoryTagRepo;
    private final Browser_History_Tag_Service browserHistoryTagService;

    @Transactional
    public TaggingResult syncAndTag() {
        List<Chrome_Url_Entity> rawUrls = chromeUrlRepo.findAll();
        List<Tag_Entity> activeTags = tagRepo.findAll();

        int historyUpserted = 0;
        int tagLinksCreated = 0;

        for (Chrome_Url_Entity raw : rawUrls) {
            Browser_History_Entity history = browserHistoryRepo.findByUrl(raw.getUrl())
                    .orElseGet(Browser_History_Entity::new);

            history.setUrl(raw.getUrl());
            history.setTitle(raw.getTitle());
            history.setDomain(raw.getDomain());
            history.setVisitedAt(raw.getLastVisitTime());
            history.setVisitCount(raw.getVisitCount());
            history.setTypedCount(raw.getTypedCount());
            history.setBrowserName("Chrome");

            Browser_History_Entity saved = browserHistoryRepo.save(history);
            historyUpserted++;

         // constructor: add `private final Browser_History_Tag_Service browserHistoryTagService;`
         // and remove browserHistoryTagRepo + tagRepo if nothing else in this class uses them directly

         tagLinksCreated += browserHistoryTagService.applyTagsToHistory(saved, activeTags);
        }

        return new TaggingResult(historyUpserted, tagLinksCreated, LocalDateTime.now());
    }

    /** Clears old tag links and recomputes against the current tag set — so a newly
     *  created tag automatically applies retroactively to already-synced history. */
    private int retagHistoryEntry(Browser_History_Entity history, List<Tag_Entity> activeTags) {
        List<Browser_History_Tag_Entity> existing =
                browserHistoryTagRepo.findByBrowserHistoryId(history.getBrowser_history_Id());
        browserHistoryTagRepo.deleteAll(existing);

        String haystack = (safe(history.getDomain()) + " " + safe(history.getTitle()) + " " + safe(history.getUrl()))
                .toLowerCase();

        int created = 0;
        for (Tag_Entity tag : activeTags) {
            List<String> keywords = tag.getKeywordList();
            if (keywords.isEmpty()) continue;

            boolean matchedDomain = keywords.stream().anyMatch(k -> safe(history.getDomain()).toLowerCase().contains(k));
            boolean matchedElsewhere = !matchedDomain && keywords.stream().anyMatch(haystack::contains);

            if (matchedDomain || matchedElsewhere) {
                browserHistoryTagRepo.save(Browser_History_Tag_Entity.builder()
                        .browserHistory(history)
                        .tag(tag)
                        .confidenceScore(matchedDomain ? 1.0 : 0.7)
                        .build());
                created++;
            }
        }
        return created;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public record TaggingResult(int historyUpserted, int tagLinksCreated, LocalDateTime completedAt) {}

    
    
}
