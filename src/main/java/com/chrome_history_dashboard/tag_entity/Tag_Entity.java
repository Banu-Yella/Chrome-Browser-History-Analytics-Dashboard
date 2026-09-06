package com.chrome_history_dashboard.tag_entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Transient;

import com.chrome_history_dashboard.browser_history_tag_entity.Browser_History_Tag_Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag_Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tag_Id")
	private Integer tag_Id;

	@NotBlank(message = "tag name must not be blank")
	@Size(max = 100, message = "tag name must be under 100 characters")
	@Column(nullable = false, unique = true, length = 100)
	private String tag_Name;

	@Size(max = 500, message = "description must be under 500 characters")
	@Column(length = 500)
	private String description;

	@Size(max = 100)
	@Column(length = 100)
	private String category;

	/**
	 * Comma-separated match terms, e.g. "github,stackoverflow,leetcode" — drives
	 * auto-tagging.
	 */
	@NotBlank(message = "at least one match keyword is required for auto-tagging to work")
	@Column(name = "match_keywords", length = 1024, nullable = false)
	private String matchKeywords;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Browser_History_Tag_Entity> browserHistoryTags;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	/**
	 * Splits matchKeywords into a clean, lowercased list for the tagging service to
	 * use.
	 */
	@Transient
	public List<String> getKeywordList() {
		if (matchKeywords == null || matchKeywords.isBlank())
			return List.of();
		return java.util.Arrays.stream(matchKeywords.split(",")).map(String::trim).filter(s -> !s.isEmpty())
				.map(String::toLowerCase).toList();
	}

}
