package com.chrome_history_dashboard.analytics_summary_service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chrome_history_dashboard.analytics_summary_entity.Analytics_Summary_Entity;
import com.chrome_history_dashboard.analytics_summary_entity.PeriodType;
import com.chrome_history_dashboard.analytics_summary_repo.Analytics_Summary_Repo;
import com.chrome_history_dashboard.browser_history_entity.Chrome_Url_Entity;
import com.chrome_history_dashboard.browser_history_entity.Visit_Entity;
import com.chrome_history_dashboard.browser_history_repo.Visit_Repo;
import com.chrome_history_dashboard.browser_history_tag_repo.Browser_History_Tag_Repo;
import com.chrome_history_dashboard.user_entity.User_Entity;
import com.chrome_history_dashboard.user_service.User_Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Analytics_Summary_Service {

	private final Visit_Repo visitRepo;
	private final Browser_History_Tag_Repo browserHistoryTagRepo;
	private final Analytics_Summary_Repo analyticsSummaryRepo;
	private final User_Service userService;

	@Transactional
	public Analytics_Summary_Entity generateSummary(PeriodType periodType, LocalDate referenceDate) {
		LocalDate ref = referenceDate == null ? LocalDate.now() : referenceDate;
		LocalDateTime[] range = periodType.resolveRange(ref);
		LocalDateTime periodStart = range[0];
		LocalDateTime periodEnd = range[1];

		User_Entity user = userService.getOrCreateDefaultUser();

		// No duplicates: reuse the existing row for this exact user + period if one
		// exists.
		Analytics_Summary_Entity summary = analyticsSummaryRepo.findByPeriod(periodType.name(), periodStart, periodEnd)
				.stream().filter(s -> s.getUser().getUsers_Id().equals(user.getUsers_Id())).findFirst()
				.orElseGet(Analytics_Summary_Entity::new);

		List<Visit_Entity> visits = visitRepo.findByVisitTimeBetweenWithUrl(periodStart, periodEnd);
		long totalVisits = visits.size();

		Map<String, List<Visit_Entity>> byDomain = visits.stream()
				.collect(Collectors.groupingBy(v -> safeDomain(v.getUrl())));
		long uniqueDomains = byDomain.size();

		String topDomain = byDomain.entrySet().stream().max(Comparator.comparingInt(e -> e.getValue().size()))
				.map(Map.Entry::getKey).orElse("N/A");

		Integer mostActiveHour = visits.stream()
				.collect(Collectors.groupingBy(Visit_Entity::getHourOfDay, Collectors.counting())).entrySet().stream()
				.max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);

		BigDecimal totalTimeSpent = visits.stream()
				.map(v -> BigDecimal.valueOf(v.getVisitDurationSeconds() == null ? 0 : v.getVisitDurationSeconds()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal averageTimeSpent = totalVisits == 0 ? BigDecimal.ZERO
				: totalTimeSpent.divide(BigDecimal.valueOf(totalVisits), 2, RoundingMode.HALF_UP);

		BigDecimal minTimeSpent = visits.stream()
				.map(v -> BigDecimal.valueOf(v.getVisitDurationSeconds() == null ? 0 : v.getVisitDurationSeconds()))
				.min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

		BigDecimal maxTimeSpent = visits.stream()
				.map(v -> BigDecimal.valueOf(v.getVisitDurationSeconds() == null ? 0 : v.getVisitDurationSeconds()))
				.max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

		List<Integer> perDomainVisitCounts = byDomain.values().stream().map(List::size).toList();
		int minVisitCount = perDomainVisitCounts.stream().mapToInt(Integer::intValue).min().orElse(0);
		int maxVisitCount = perDomainVisitCounts.stream().mapToInt(Integer::intValue).max().orElse(0);
		double averageVisitCount = perDomainVisitCounts.stream().mapToInt(Integer::intValue).average().orElse(0.0);

		String topTag = browserHistoryTagRepo.findTagCountsInPeriod(periodStart, periodEnd).stream().findFirst()
				.map(Browser_History_Tag_Repo.TagCountInPeriod::getTagName).orElse("N/A");

		summary.setUser(user);
		summary.setPeriodType(periodType.name());
		summary.setPeriodStart(periodStart);
		summary.setPeriodEnd(periodEnd);
		summary.setMostActiveHour(mostActiveHour);
		summary.setTotalVisits(totalVisits);
		summary.setUniqueDomains(uniqueDomains);
		summary.setTopDomain(topDomain);
		summary.setTopTag(topTag);
		summary.setTotalTimeSpent(totalTimeSpent);
		summary.setAverageTimeSpent(averageTimeSpent);
		summary.setMinTimeSpent(minTimeSpent);
		summary.setMaxTimeSpent(maxTimeSpent);
		summary.setMinVisitCount(minVisitCount);
		summary.setMaxVisitCount(maxVisitCount);
		summary.setAverageVisitCount(averageVisitCount);

		return analyticsSummaryRepo.save(summary);
	}

	private String safeDomain(Chrome_Url_Entity url) {
		return url == null || url.getDomain() == null ? "unknown" : url.getDomain();
	}

}
