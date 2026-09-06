package com.chrome_history_dashboard.browser_history_tag_service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrome_history_dashboard.browser_history_entity.Browser_History_Entity;
import com.chrome_history_dashboard.browser_history_tag_entity.Browser_History_Tag_Entity;
import com.chrome_history_dashboard.browser_history_tag_repo.Browser_History_Tag_Repo;
import com.chrome_history_dashboard.tag_entity.Tag_Entity;
import com.chrome_history_dashboard.tag_repo.Tag_Repo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Browser_History_Tag_Service {
	
	private static final double DOMAIN_MATCH_CONFIDENCE = 1.0;
    private static final double CONTENT_MATCH_CONFIDENCE = 0.7;
    private static final double MANUAL_TAG_CONFIDENCE = 1.0;

    private final Browser_History_Tag_Repo browserHistoryTagRepo;
    private final Tag_Repo tagRepo;
    private final Validator validator;

    // ---------------- Auto-tagging ----------------

    /**
     * Wipes existing links for this history entry and recomputes against the current
     * tag set. Called once per Browser_History_Entity during each sync — and again
     * whenever a tag's keywords change, so new/edited tags apply retroactively.
     */
    @Transactional
    public int applyTagsToHistory(Browser_History_Entity history, List<Tag_Entity> activeTags) {
        List<Browser_History_Tag_Entity> existing =
                browserHistoryTagRepo.findByBrowserHistoryId(history.getBrowser_history_Id());
        browserHistoryTagRepo.deleteAll(existing);

        String haystack = (safe(history.getDomain()) + " " + safe(history.getTitle()) + " " + safe(history.getUrl()))
                .toLowerCase();

        int created = 0;
        for (Tag_Entity tag : activeTags) {
            List<String> keywords = tag.getKeywordList();
            if (keywords.isEmpty()) continue;

            boolean matchedDomain = keywords.stream()
                    .anyMatch(k -> safe(history.getDomain()).toLowerCase().contains(k));
            boolean matchedElsewhere = !matchedDomain && keywords.stream().anyMatch(haystack::contains);

            if (matchedDomain || matchedElsewhere) {
                double confidence = matchedDomain ? DOMAIN_MATCH_CONFIDENCE : CONTENT_MATCH_CONFIDENCE;
                saveValidated(Browser_History_Tag_Entity.builder()
                        .browserHistory(history)
                        .tag(tag)
                        .confidenceScore(confidence)
                        .build());
                created++;
            }
        }
        return created;
    }

    /** Re-runs auto-tagging for every history entry — call this after a tag's keywords change. */
    @Transactional
    public int retagAllHistory(List<Browser_History_Entity> allHistory) {
        List<Tag_Entity> activeTags = tagRepo.findAll();
        return allHistory.stream().mapToInt(h -> applyTagsToHistory(h, activeTags)).sum();
    }

    // ---------------- Manual tag CRUD (for the frontend correcting/adding tags by hand) ----------------

    @Transactional
    public Browser_History_Tag_Entity addManualTag(Browser_History_Entity history, Tag_Entity tag) {
        return browserHistoryTagRepo
                .findByBrowserHistoryIdAndTagId(history.getBrowser_history_Id(), tag.getTag_Id())
                .orElseGet(() -> saveValidated(Browser_History_Tag_Entity.builder()
                        .browserHistory(history)
                        .tag(tag)
                        .confidenceScore(MANUAL_TAG_CONFIDENCE)
                        .build()));
    }

    @Transactional
    public void removeTagLink(Integer browserHistoryTagId) {
        if (!browserHistoryTagRepo.existsById(browserHistoryTagId)) {
            throw new NoSuchTagLinkException("Tag link not found with id: " + browserHistoryTagId);
        }
        browserHistoryTagRepo.deleteById(browserHistoryTagId);
    }

    public List<Browser_History_Tag_Entity> getTagsForHistory(Integer browserHistoryId) {
        return browserHistoryTagRepo.findByBrowserHistoryId(browserHistoryId);
    }

    public List<Browser_History_Tag_Entity> getHistoryForTag(Integer tagId) {
        return browserHistoryTagRepo.findByTagId(tagId);
    }

    // ---------------- Calculations ----------------

    /** Overall count of history entries per tag, across all time. */
    public Map<String, Long> getTagDistribution() {
        return browserHistoryTagRepo.findVisitCountsByTag().stream()
                .collect(Collectors.toMap(
                        Browser_History_Tag_Repo.TagVisitCount::getTagName,
                        Browser_History_Tag_Repo.TagVisitCount::getVisitCount));
    }

    public List<Browser_History_Tag_Repo.TagVisitCount> getTopTags(int limit) {
        return browserHistoryTagRepo.findVisitCountsByTag().stream().limit(Math.max(limit, 1)).toList();
    }

    /** Average confidence score per tag — a low average suggests that tag's keywords are too loose. */
    public Map<String, Double> getAverageConfidenceByTag() {
        return browserHistoryTagRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        bht -> bht.getTag().getTag_Name(),
                        Collectors.averagingDouble(Browser_History_Tag_Entity::getConfidenceScore)));
    }

    // ---------------- Validation ----------------

    private Browser_History_Tag_Entity saveValidated(Browser_History_Tag_Entity entity) {
        Set<ConstraintViolation<Browser_History_Tag_Entity>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Invalid Browser_History_Tag_Entity: " + message);
        }
        return browserHistoryTagRepo.save(entity);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public static class NoSuchTagLinkException extends RuntimeException {
        public NoSuchTagLinkException(String message) {
            super(message);
        }
    }

}
