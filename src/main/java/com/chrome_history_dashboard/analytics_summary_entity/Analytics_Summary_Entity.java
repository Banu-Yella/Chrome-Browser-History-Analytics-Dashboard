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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analytics_Summary_Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "analytics_summary_id")
	private Integer analytics_summary_Id;	
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User_Entity userId;
	
	@NotBlank
	@Column(name = "period_type", length = 20, nullable = false)
	private String periodType; // e.g. DAILY, WEEKLY, MONTHLY
	
	@NotNull
	@Column(name = "period_start")
	private LocalDateTime periodStart;
	
	@NotNull
	@Column(name = "period_end")
	private LocalDateTime periodEnd;
	
	@Min(0) @jakarta.validation.constraints.Max(23)
	@Column(name = "most_active_hour")
	private Integer mostActiveHour;
	
	@Column(name = "time_spent_domain")
	private LocalDateTime time_spent_domain;
	
	@Column(name = "total_visits")
	@Min(0)private long totalVisits;
	
	@Column(name = "unique_domains")
	@Min(0)private long uniqueDomains;
	
	@Column(name = "top_domain", length = 255)
	private String topDomain;
	
	@Column(name = "top_tag", length = 100)
	private String topTag;
	
	@Column(name = "total_time_spent")
	@Min(0)private BigDecimal totalTimeSpent;
	
	@Column(name = "average_time_spent")
	@Min(0)private BigDecimal averageTimeSpent;
	
	@Column(name = "min_time_spent")
	@Min(0)private BigDecimal minTimeSpent;
	
	@Column(name = "max_time_spent")
	@Min(0)private BigDecimal maxTimeSpent;
	
	@Column(name = "min_visit_count")
	@Min(0)private Integer minVisitCount;
	
	@Column(name = "max_visit_count")
	@Min(0)private Integer maxVisitCount;
	
	@Column(name = "average_visit_count")
	@Min(0)private double averageVisitCount;
	
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
	
	 @PrePersist
	    protected void onCreate() {
	        this.createdAt = LocalDateTime.now();
	        this.updatedAt = LocalDateTime.now();
	    }

	    @PreUpdate
	    protected void onUpdate() {
	        this.updatedAt = LocalDateTime.now();
	    }

}
