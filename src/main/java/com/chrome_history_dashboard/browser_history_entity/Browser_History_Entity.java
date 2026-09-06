package com.chrome_history_dashboard.browser_history_entity;

import java.time.LocalDateTime;
import java.util.List;


import com.chrome_history_dashboard.tag_entity.Tag_Entity;

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
@Table(name = "browser_history")
public class Browser_History_Entity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "browser_history_id")
	private Integer browser_history_Id;
	
	@Column(name = "url")
	private String url;
	
	@Column(name = "title")
	private String title;
	
	@Column(name = "domain")
	private String domain;
	
	@Column(name = "visited_at")
	private LocalDateTime visitedAt;
	
	@Column(name = "visit_count")
	private Integer visitCount;
	
	@Column(name = "typed_count")
	private Integer typedCount;
	
	@Column(name = "browser_name")
	private String browserName;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	//	JPA RelationShip Mapping
	@ManyToMany(mappedBy = "browserHistory", cascade = CascadeType.ALL)
	private List<Tag_Entity> Tags;
	
}
