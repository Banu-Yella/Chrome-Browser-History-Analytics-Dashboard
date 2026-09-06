package com.chrome_history_dashboard.browser_history_entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "chrome_url_entity", indexes = {
        @Index(name = "idx_url_domain", columnList = "domain"),
        @Index(name = "idx_url_chrome_id", columnList = "chromeurlid", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chrome_Url_Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	/**
	 * Original primary key from Chrome's `urls.id` — used to detect duplicates on
	 * re-sync
	 */
	@Column(nullable = false, unique = true)
	private Integer chromeUrlId;

	@Column(nullable = false, length = 2048)
	private String url;

	@Column(length = 1024)
	private String title;

	@Column(nullable = false)
	private Integer visitCount;

	@Column(nullable = false)
	private Integer typedCount;

	/** Converted from Chrome's WebKit microsecond epoch into a normal timestamp */
	private LocalDateTime lastVisitTime;

	private Boolean hidden;

	// ---- Additional analytics columns ----

	/** Extracted host, e.g. "github.com" — used for grouping/aggregation */
	@Column(length = 255)
	private String domain;

	/**
	 * Coarse bucket derived from domain, e.g. "Social", "Dev Tools", "Search",
	 * "Other"
	 */
	@Column(length = 100)
	private String category;

	/** Sum of visitDuration across all associated visits, in seconds */
	private BigDecimal totalTimeSpentSeconds;

	/** Timestamp of when this row was last synced from Chrome */
	private LocalDateTime lastSyncedAt;

	@OneToMany(mappedBy = "url", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Visit_Entity> visits;

}
