package com.chrome_history_dashboard.browser_history_tag_repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chrome_history_dashboard.browser_history_tag_entity.Browser_History_Tag_Entity;

@Repository
public interface Browser_History_Tag_Repo extends JpaRepository<Browser_History_Tag_Entity, Integer> {
	
	// FIXED: was JpaRepository<Browser_History_Entity, Integer> — wrong entity type.
    // It compiled (both PKs are Integer) but would save/return the wrong table's rows.

    @Query("SELECT bht FROM Browser_History_Tag_Entity bht WHERE bht.browserHistory.browser_history_Id = :historyId")
    List<Browser_History_Tag_Entity> findByBrowserHistoryId(@Param("historyId") Integer historyId);

    @Query("SELECT bht FROM Browser_History_Tag_Entity bht WHERE bht.tag.tag_Id = :tagId")
    List<Browser_History_Tag_Entity> findByTagId(@Param("tagId") Integer tagId);

    @Query("SELECT bht FROM Browser_History_Tag_Entity bht " +
           "WHERE bht.browserHistory.browser_history_Id = :historyId AND bht.tag.tag_Id = :tagId")
    Optional<Browser_History_Tag_Entity> findByBrowserHistoryIdAndTagId(
            @Param("historyId") Integer historyId, @Param("tagId") Integer tagId);

    @Query("SELECT bht.tag.tag_Name AS tagName, COUNT(bht) AS visitCount " +
           "FROM Browser_History_Tag_Entity bht GROUP BY bht.tag.tag_Name ORDER BY visitCount DESC")
    List<TagVisitCount> findVisitCountsByTag();

    interface TagVisitCount {
        String getTagName();
        Long getVisitCount();
    }
    
    @Query("SELECT bht.tag.tag_Name AS tagName, COUNT(bht) AS cnt FROM Browser_History_Tag_Entity bht " +
    	       "WHERE bht.browserHistory.visitedAt BETWEEN :start AND :end GROUP BY bht.tag.tag_Name ORDER BY cnt DESC")
    	List<TagCountInPeriod> findTagCountsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    	interface TagCountInPeriod {
    	    String getTagName();
    	    Long getCnt();
    	}

}
