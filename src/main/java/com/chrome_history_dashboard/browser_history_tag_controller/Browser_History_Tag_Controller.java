package com.chrome_history_dashboard.browser_history_tag_controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chrome_history_dashboard.browser_history_controller.Browser_History_Controller;
import com.chrome_history_dashboard.browser_history_entity.Browser_History_Entity;
import com.chrome_history_dashboard.browser_history_repo.Browser_History_Repo;
import com.chrome_history_dashboard.browser_history_tag_entity.Browser_History_Tag_Entity;
import com.chrome_history_dashboard.browser_history_tag_repo.Browser_History_Tag_Repo;
import com.chrome_history_dashboard.browser_history_tag_service.Browser_History_Tag_Service;
import com.chrome_history_dashboard.tag_entity.Tag_Entity;
import com.chrome_history_dashboard.tag_service.Tag_Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/browser-history-tags")
@RequiredArgsConstructor
public class Browser_History_Tag_Controller {
	
	 private final Browser_History_Tag_Service tagLinkService;
	    private final Browser_History_Repo browserHistoryRepo;
	    private final Tag_Service tagService;

	    @PostMapping
	    public ResponseEntity<Browser_History_Tag_Entity> addManualTag(@RequestBody ManualTagRequest request) {
	        Browser_History_Entity history = browserHistoryRepo.findById(request.browserHistoryId())
	                .orElseThrow(() -> new Browser_History_Controller.NoSuchHistoryException(
	                        "Browser history not found with id: " + request.browserHistoryId()));
	        Tag_Entity tag = tagService.getTagById(request.tagId());
	        Browser_History_Tag_Entity link = tagLinkService.addManualTag(history, tag);
	        return ResponseEntity.status(HttpStatus.CREATED).body(link);
	    }

	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> removeTag(@PathVariable Integer id) {
	        tagLinkService.removeTagLink(id);
	        return ResponseEntity.noContent().build();
	    }

	    @GetMapping("/distribution")
	    public Map<String, Long> getTagDistribution() {
	        return tagLinkService.getTagDistribution();
	    }

	    @GetMapping("/top")
	    public List<Browser_History_Tag_Repo.TagVisitCount> getTopTags(@RequestParam(defaultValue = "10") int limit) {
	        return tagLinkService.getTopTags(limit);
	    }

	    @GetMapping("/average-confidence")
	    public Map<String, Double> getAverageConfidence() {
	        return tagLinkService.getAverageConfidenceByTag();
	    }

	    public record ManualTagRequest(Integer browserHistoryId, Integer tagId) {}

}
