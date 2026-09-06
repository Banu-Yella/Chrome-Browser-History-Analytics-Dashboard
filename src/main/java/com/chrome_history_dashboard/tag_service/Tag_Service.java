package com.chrome_history_dashboard.tag_service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrome_history_dashboard.browser_history_service.Browser_History_Service;
import com.chrome_history_dashboard.tag_entity.Tag_Entity;
import com.chrome_history_dashboard.tag_repo.Tag_Repo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Tag_Service {
	
	 private final Tag_Repo tagRepo;
	    private final Validator validator;
	    private final Browser_History_Service browserHistoryService;

	    public List<Tag_Entity> getAllTags() {
	        return tagRepo.findAll();
	    }

	    public Tag_Entity getTagById(Integer id) {
	        return tagRepo.findById(id)
	                .orElseThrow(() -> new NoSuchTagException("Tag not found with id: " + id));
	    }

	    @Transactional
	    public Tag_Entity createTag(String tagName, String description, String category, String matchKeywords) {
	        if (tagRepo.existsByTagName(tagName)) {
	            throw new IllegalArgumentException("A tag named '" + tagName + "' already exists");
	        }
	        Tag_Entity saved = saveValidated(Tag_Entity.builder()
	                .tag_Name(tagName)
	                .description(description)
	                .category(category)
	                .matchKeywords(matchKeywords)
	                .build());
	        browserHistoryService.syncAndTag(); // apply the new tag to already-synced history
	        return saved;
	    }

	    @Transactional
	    public Tag_Entity updateTag(Integer id, String tagName, String description, String category, String matchKeywords) {
	        Tag_Entity tag = getTagById(id);
	        tag.setTag_Name(tagName);
	        tag.setDescription(description);
	        tag.setCategory(category);
	        tag.setMatchKeywords(matchKeywords);
	        Tag_Entity saved = saveValidated(tag);
	        browserHistoryService.syncAndTag(); // keywords may have changed — reapply everywhere
	        return saved;
	    }

	    @Transactional
	    public void deleteTag(Integer id) {
	        if (!tagRepo.existsById(id)) {
	            throw new NoSuchTagException("Tag not found with id: " + id);
	        }
	        tagRepo.deleteById(id); // cascade = ALL on the entity removes its Browser_History_Tag_Entity links too
	    }

	    private Tag_Entity saveValidated(Tag_Entity tag) {
	        Set<ConstraintViolation<Tag_Entity>> violations = validator.validate(tag);
	        if (!violations.isEmpty()) {
	            String message = violations.stream().map(ConstraintViolation::getMessage)
	                    .collect(Collectors.joining("; "));
	            throw new IllegalArgumentException("Invalid tag: " + message);
	        }
	        return tagRepo.save(tag);
	    }

	    public static class NoSuchTagException extends RuntimeException {
	        public NoSuchTagException(String message) {
	            super(message);
	        }
	    }

}
