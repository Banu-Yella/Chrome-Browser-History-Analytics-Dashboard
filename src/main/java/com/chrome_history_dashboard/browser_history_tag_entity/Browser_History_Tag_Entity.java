package com.chrome_history_dashboard.browser_history_tag_entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Transient;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "browser_history_tag", uniqueConstraints = @UniqueConstraint(columnNames = { "browser_history_id",
		"tag_id" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Browser_History_Tag_Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "browser_history_tag_Id")
	private Integer browser_history_tag_Id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "browser_history_id", nullable = false)
	private Browser_History_Entity browserHistory;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tag_id", nullable = false)
	private Tag_Entity tag;

	@NotNull
	@DecimalMin(value = "0.0", message = "confidenceScore cannot be below 0.0")
	@DecimalMax(value = "1.0", message = "confidenceScore cannot exceed 1.0")
	private Double confidenceScore;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	@PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Calculation: used by the frontend to badge high-confidence tags differently. */
    @Transient
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 0.7;
    }

}
