package com.chrome_history_dashboard.browser_history_repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chrome_history_dashboard.browser_history_entity.Browser_History_Entity;

@Repository
public interface Browser_History_Repo extends JpaRepository<Browser_History_Entity, Integer> {
	
	Optional<Browser_History_Entity> findByUrl(String url);

    boolean existsByUrl(String url);

    List<Browser_History_Entity> findByDomainIgnoreCase(String domain);

    List<Browser_History_Entity> findByVisitedAtBetween(LocalDateTime start, LocalDateTime end);

    /** Rows with no tags yet — this is what the tagging service will pick up each run. */
    @Query("SELECT b FROM Browser_History_Entity b WHERE b.browserHistoryTags IS EMPTY")
    List<Browser_History_Entity> findUntagged();

    @Query("SELECT b.domain AS domain, COUNT(b) AS totalVisits " +
           "FROM Browser_History_Entity b WHERE b.domain IS NOT NULL " +
           "GROUP BY b.domain ORDER BY totalVisits DESC")
    List<DomainVisitCount> findTopDomains();

    @Query("SELECT MIN(b.visitCount) AS minVisits, MAX(b.visitCount) AS maxVisits, " +
           "AVG(b.visitCount) AS avgVisits FROM Browser_History_Entity b")
    VisitCountStats getVisitCountStats();

    @Query("SELECT COUNT(DISTINCT b.domain) FROM Browser_History_Entity b WHERE b.domain IS NOT NULL")
    long countUniqueDomains();

    interface DomainVisitCount {
        String getDomain();
        Long getTotalVisits();
    }

    interface VisitCountStats {
        Integer getMinVisits();
        Integer getMaxVisits();
        Double getAvgVisits();
    }
    
    @Query("SELECT bht.tag.tag_Name AS tagName, COUNT(bht) AS cnt FROM Browser_History_Tag_Entity bht " +
    	       "WHERE bht.browserHistory.visitedAt BETWEEN :start AND :end GROUP BY bht.tag.tag_Name ORDER BY cnt DESC")
    	List<TagCountInPeriod> findTagCountsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    	interface TagCountInPeriod {
    	    String getTagName();
    	    Long getCnt();
    	}

}
