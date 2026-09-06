package com.chrome_history_dashboard.analytics_summary_entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.chrome_history_dashboard.user_entity.User_Entity;

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
@Table(name = "analytics_summary")
public class Analytics_Summary_Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "analytics_summary_id")
	private Integer analytics_summary_Id;	
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	private User_Entity userId;
	
	@Column(name = "period_type")
	private String periodType;
	
	@Column(name = "period_start")
	private LocalDateTime periodStart;
	
	@Column(name = "period_end")
	private LocalDateTime periodEnd;
	
	@Column(name = "time_spent_domain")
	private LocalDateTime time_spent_domain;
	
	@Column(name = "total_visits")
	private Integer totalVisits;
	
	@Column(name = "unique_domains")
	private Integer uniqueDomains;
	
	@Column(name = "top_domain")
	private String topDomain;
	
	@Column(name = "top_tag")
	private String topTag;
	
	@Column(name = "total_time_spent")
	private BigDecimal totalTimeSpent;
	
	@Column(name = "average_time_spent")
	private BigDecimal averageTimeSpent;
	
	@Column(name = "min_time_spent")
	private BigDecimal minTimeSpent;
	
	@Column(name = "max_time_spent")
	private BigDecimal maxTimeSpent;
	
	@Column(name = "min_visit_count")
	private Integer minVisitCount;
	
	@Column(name = "max_visit_count")
	private Integer maxVisitCount;
	
	@Column(name = "average_visit_count")
	private Integer averageVisitCount;
	
	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	//	JpaRelationshipMapping
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	private User_Entity user;

}
