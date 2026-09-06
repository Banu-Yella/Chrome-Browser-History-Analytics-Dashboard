package com.chrome_history_dashboard.browser_history_repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chrome_history_dashboard.browser_history_entity.Visit_Entity;

@Repository
public interface Visit_Repo extends JpaRepository<Visit_Entity, Long> {
	
	@Query("SELECT v FROM Visit_Entity v JOIN FETCH v.url WHERE v.visitTime BETWEEN :start AND :end")
	List<Visit_Entity> findByVisitTimeBetweenWithUrl(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
	
	Optional<Visit_Entity> findByChromeVisitId(Long chromeVisitId);

    List<Visit_Entity> findByVisitTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT FUNCTION('DATE', v.visitTime) AS day, COUNT(v) AS visitCount " +
           "FROM Visit_Entity v GROUP BY FUNCTION('DATE', v.visitTime) ORDER BY day ASC")
    List<DailyTrend> findDailyVisitTrend();

    @Query("SELECT v.hourOfDay AS hour, COUNT(v) AS visitCount " +
           "FROM Visit_Entity v GROUP BY v.hourOfDay ORDER BY v.hourOfDay ASC")
    List<HourlyTrend> findHourlyVisitPattern();

    @Query("SELECT v.url.id AS urlId, COALESCE(SUM(v.visitDurationSeconds), 0) AS totalSeconds " +
           "FROM Visit_Entity v GROUP BY v.url.id")
    List<UrlTimeSpent> sumDurationGroupedByUrl();

    interface DailyTrend {
        String getDay();
        Long getVisitCount();
    }

    interface HourlyTrend {
        Integer getHour();
        Long getVisitCount();
    }

    interface UrlTimeSpent {
        Integer getUrlId();   // matches Chrome_Url_Entity.id (Integer)
        Long getTotalSeconds();
    }

}
