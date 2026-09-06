package com.chrome_history_dashboard.browser_history_repo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chrome_history_dashboard.browser_history_entity.Chrome_Url_Entity;


@Repository
public interface Chrome_Url_Repo extends JpaRepository<Chrome_Url_Entity, Integer> {
	// was: JpaRepository<Chrome_Url_Entity, Long> — didn't match the entity's Integer @Id

    Optional<Chrome_Url_Entity> findByChromeUrlId(Integer chromeUrlId);

    List<Chrome_Url_Entity> findByDomainIgnoreCase(String domain);

    @Query("SELECT u.domain AS domain, SUM(u.visitCount) AS totalVisits, " +
           "SUM(u.totalTimeSpentSeconds) AS totalSeconds " +
           "FROM Chrome_Url_Entity u WHERE u.domain IS NOT NULL " +
           "GROUP BY u.domain ORDER BY totalVisits DESC")
    List<DomainAggregate> findTopDomains();

    interface DomainAggregate {
        String getDomain();
        Long getTotalVisits();
        java.math.BigDecimal getTotalSeconds();
    }

    @Query("SELECT u FROM Chrome_Url_Entity u ORDER BY u.visitCount DESC")
    List<Chrome_Url_Entity> findMostVisited();
}


