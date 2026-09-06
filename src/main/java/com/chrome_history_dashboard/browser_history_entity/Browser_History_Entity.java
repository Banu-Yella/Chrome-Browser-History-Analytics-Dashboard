package com.chrome_history_dashboard.browser_history_entity;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "browser_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Browser_History_Entity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "browser_history_id")
	private Integer browser_history_Id;

	@NotBlank(message = "url must not be blank")
	@Column(nullable = false, unique = true, length = 2048)
	private String url;

	@Column(name = "title", length = 1024)
	private String title;

	@Column(name = "domain", length = 256)
	private String domain;

	@Column(name = "visited_at")
	private LocalDateTime visitedAt;

	@Column(name = "visit_count")
	@Min(value = 0, message = "visitCount cannot be negative")
	private Integer visitCount;

	@Column(name = "typed_count")
	@Min(value = 0, message = "typedCount cannot be negative")
	private Integer typedCount;

	@Column(name = "browser_name", length = 100)
	private String browserName;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// JPA RelationShip Mapping
	@OneToMany(mappedBy = "browserHistory", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Browser_History_Tag_Entity> browserHistoryTags;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		deriveDomainIfMissing();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
		deriveDomainIfMissing();
	}

	/**
	 * Calculation: fills `domain` from `url` automatically if the caller didn't set
	 * it.
	 */
	private void deriveDomainIfMissing() {
		if ((domain == null || domain.isBlank()) && url != null) {
			try {
				String host = URI.create(url).getHost();
				this.domain = host == null ? "unknown" : host.replaceFirst("^www\\.", "");
			} catch (Exception e) {
				this.domain = "unknown";
			}
		}
	}
}
