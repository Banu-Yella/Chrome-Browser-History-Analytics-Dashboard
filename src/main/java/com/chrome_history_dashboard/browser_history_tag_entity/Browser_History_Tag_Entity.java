package com.chrome_history_dashboard.browser_history_tag_entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.chrome_history_dashboard.browser_history_entity.Browser_History_Entity;
import com.chrome_history_dashboard.tag_entity.Tag_Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "browser_history_tag")
public class Browser_History_Tag_Entity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "browser_history_tag_Id")
	private Integer browser_history_tag_Id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "browser_history_id")
	private Browser_History_Entity browserHistory;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "tag_id")
	private Tag_Entity tag;
	
	@Column(name = "confidence_score")
	private Double confidenceScore;
	
	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
