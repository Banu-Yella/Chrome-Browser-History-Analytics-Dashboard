package com.chrome_history_dashboard.analytics_summary_controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.chrome_history_dashboard.analytics_summary_entity.Analytics_Summary_Entity;
import com.chrome_history_dashboard.analytics_summary_entity.PeriodType;
import com.chrome_history_dashboard.analytics_summary_repo.Analytics_Summary_Repo;
import com.chrome_history_dashboard.analytics_summary_service.Analytics_Summary_Service;
import com.chrome_history_dashboard.user_service.User_Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class Analytics_Summary_Controller {
	
	  private final Analytics_Summary_Service analyticsSummaryService;
	    private final Analytics_Summary_Repo analyticsSummaryRepo;
	    private final User_Service userService;

	    /** e.g. POST /api/analytics/summary?periodType=WEEKLY&referenceDate=2026-09-01
	     *  periodType is one of DAILY, WEEKLY, MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY.
	     *  referenceDate is optional — omit it to use today. Re-running for the same
	     *  period updates the existing row instead of duplicating it. */
	    @PostMapping("/summary")
	    public Analytics_Summary_Entity generateSummary(
	            @RequestParam PeriodType periodType,
	            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
	        return analyticsSummaryService.generateSummary(periodType, referenceDate);
	    }

	    @GetMapping("/summary/history")
	    public List<Analytics_Summary_Entity> getSummaryHistory() {
	        var user = userService.getOrCreateDefaultUser();
	        return analyticsSummaryRepo.findByUserId(user.getUsers_Id());
	    }

}
