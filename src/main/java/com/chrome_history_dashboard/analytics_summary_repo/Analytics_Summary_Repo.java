package com.chrome_history_dashboard.analytics_summary_repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chrome_history_dashboard.analytics_summary_entity.Analytics_Summary_Entity;

@Repository
public interface Analytics_Summary_Repo extends JpaRepository<Analytics_Summary_Entity, Integer> {
	
	 @Query("SELECT a FROM Analytics_Summary_Entity a WHERE a.user.users_Id = :userId ORDER BY a.periodStart DESC")
	    List<Analytics_Summary_Entity> findByUserId(@Param("userId") Integer userId);

	    @Query("SELECT a FROM Analytics_Summary_Entity a WHERE a.periodType = :periodType " +
	           "AND a.periodStart = :periodStart AND a.periodEnd = :periodEnd")
	    List<Analytics_Summary_Entity> findByPeriod(@Param("periodType") String periodType,
	                                                  @Param("periodStart") LocalDateTime periodStart,
	                                                  @Param("periodEnd") LocalDateTime periodEnd);

}
