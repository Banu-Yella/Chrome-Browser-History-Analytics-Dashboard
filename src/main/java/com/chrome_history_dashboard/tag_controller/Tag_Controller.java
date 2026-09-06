package com.chrome_history_dashboard.tag_controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chrome_history_dashboard.tag_entity.Tag_Entity;
import com.chrome_history_dashboard.tag_service.Tag_Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class Tag_Controller {

	
	private final Tag_Service tagService;

    @GetMapping
    public List<Tag_Entity> getAllTags() {
        return tagService.getAllTags();
    }

    @GetMapping("/{id}")
    public Tag_Entity getTag(@PathVariable Integer id) {
        return tagService.getTagById(id);
    }

    @PostMapping
    public ResponseEntity<Tag_Entity> createTag(@Valid @RequestBody TagRequest request) {
        Tag_Entity created = tagService.createTag(
                request.tagName(), request.description(), request.category(), request.matchKeywords());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Tag_Entity updateTag(@PathVariable Integer id, @Valid @RequestBody TagRequest request) {
        return tagService.updateTag(
                id, request.tagName(), request.description(), request.category(), request.matchKeywords());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Integer id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    /** Keeps clients from having to send id/timestamps when creating or editing a tag. */
    public record TagRequest(
            @NotBlank(message = "tagName is required") String tagName,
            String description,
            String category,
            @NotBlank(message = "matchKeywords is required") String matchKeywords
    ) {}
	
}
