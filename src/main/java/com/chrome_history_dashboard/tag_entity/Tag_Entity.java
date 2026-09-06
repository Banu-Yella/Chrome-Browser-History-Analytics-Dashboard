package com.chrome_history_dashboard.tag_entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.chrome_history_dashboard.browser_history_entity.Browser_History_Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "tag")
public class Tag_Entity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tag_Id")
	private Integer tag_Id;
	
	@Column(name = "tag_name")
	private String tag_Name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "category")
	private String category;
	
	@Column(name = "match_keywords", length = 1024)
	private String matchKeywords; // comma-separated, e.g. "github,stackoverflow,leetcode"
	
	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	//	JPA Relationship Mapping
	@ManyToMany(mappedBy = "tags", cascade = CascadeType.ALL)
	private List<Browser_History_Entity> browserHistoryTags;
	
		
}
