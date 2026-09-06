package com.chrome_history_dashboard.browser_history_entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Mirrors Chrome's native `visits` table. One row = a single visit event to a URL.
 *
 * Native Chrome columns: id, url (FK), visit_time, from_visit, transition, segment_id, visit_duration
 */
@Entity
@Table(name = "visit_entity", indexes = {
        @Index(name = "idx_visit_time", columnList = "visit_time"),
        @Index(name = "idx_visit_chrome_id", columnList = "chrome_visit_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit_Entity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Original primary key from Chrome's `visits.id` */

    @Column(nullable = false, unique = true)
    private Long chromeVisitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false)
    private Chrome_Url_Entity url;

    @Column(nullable = false)
    private LocalDateTime visitTime;

    /** Chrome's `from_visit` — id of the visit that navigated here (0 = direct/typed/bookmark) */
    private Long fromVisit;

    /** Raw Chrome transition code (link, typed, reload, form_submit, etc.) */
    private Integer transitionType;

    private Long segmentId;

    /** Duration of the visit in seconds (Chrome stores this in microseconds natively) */
    private Long visitDurationSeconds;

    // ---- Additional analytics columns ----

    /** Day-of-week bucket (MONDAY..SUNDAY), precomputed for fast dashboard grouping */
    @Column(length = 20)
    private String dayOfWeek;

    /** Hour-of-day bucket (0-23), precomputed for "browsing pattern by hour" charts */
    private Integer hourOfDay;

    /** True if transitionType corresponds to a user-typed navigation (vs link click) */
    private Boolean isTypedNavigation;

}
