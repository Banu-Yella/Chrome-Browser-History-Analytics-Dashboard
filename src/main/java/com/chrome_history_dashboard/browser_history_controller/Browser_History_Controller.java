package com.chrome_history_dashboard.browser_history_controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.chrome_history_dashboard.browser_history_entity.Browser_History_Entity;
import com.chrome_history_dashboard.browser_history_repo.Browser_History_Repo;
import com.chrome_history_dashboard.browser_history_service.Browser_History_Service;
import com.chrome_history_dashboard.browser_history_service.Chrome_History_Reader_Service;
import com.chrome_history_dashboard.browser_history_tag_entity.Browser_History_Tag_Entity;
import com.chrome_history_dashboard.browser_history_tag_service.Browser_History_Tag_Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/browser-history")
@RequiredArgsConstructor
public class Browser_History_Controller {

	private final Chrome_History_Reader_Service readerService;
    private final Browser_History_Service browserHistoryService;
    private final Browser_History_Repo browserHistoryRepo;
    private final Browser_History_Tag_Service browserHistoryTagService;

    /** The "Generate" button: pulls Chrome's raw SQLite history, then auto-tags it. */
    @PostMapping("/sync")
    public SyncResponse sync() throws IOException, SQLException {
        Chrome_History_Reader_Service.SyncResult rawResult = readerService.syncFromChrome();
        Browser_History_Service.TaggingResult taggingResult = browserHistoryService.syncAndTag();
        return new SyncResponse(rawResult, taggingResult, LocalDateTime.now());
    }

    @GetMapping
    public List<Browser_History_Entity> getAllHistory() {
        return browserHistoryRepo.findAll();
    }

    @GetMapping("/{id}")
    public Browser_History_Entity getHistory(@PathVariable Integer id) {
        return browserHistoryRepo.findById(id)
                .orElseThrow(() -> new NoSuchHistoryException("Browser history not found with id: " + id));
    }

    @GetMapping("/{id}/tags")
    public List<Browser_History_Tag_Entity> getTagsForHistory(@PathVariable Integer id) {
        return browserHistoryTagService.getTagsForHistory(id);
    }

    @GetMapping("/top-domains")
    public List<Browser_History_Repo.DomainVisitCount> getTopDomains() {
        return browserHistoryRepo.findTopDomains();
    }

    @GetMapping("/stats")
    public Browser_History_Repo.VisitCountStats getVisitCountStats() {
        return browserHistoryRepo.getVisitCountStats();
    }

    public record SyncResponse(
            Chrome_History_Reader_Service.SyncResult rawSync,
            Browser_History_Service.TaggingResult tagging,
            LocalDateTime completedAt
    ) {}

    public static class NoSuchHistoryException extends RuntimeException {
        public NoSuchHistoryException(String message) {
            super(message);
        }
    }
	
}
