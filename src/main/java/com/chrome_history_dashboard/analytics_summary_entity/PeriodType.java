package com.chrome_history_dashboard.analytics_summary_entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

public enum PeriodType {
	
	 DAILY, WEEKLY, MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY;

	    /** Resolves this period's [start, end] LocalDateTime range around a reference date. */
	    public LocalDateTime[] resolveRange(LocalDate referenceDate) {
	        LocalDate start;
	        LocalDate end;
	        switch (this) {
	            case DAILY -> { start = referenceDate; end = referenceDate; }
	            case WEEKLY -> {
	                start = referenceDate.with(DayOfWeek.MONDAY);
	                end = start.plusDays(6);
	            }
	            case MONTHLY -> {
	                start = referenceDate.withDayOfMonth(1);
	                end = referenceDate.with(TemporalAdjusters.lastDayOfMonth());
	            }
	            case QUARTERLY -> {
	                int quarterIndex = (referenceDate.getMonthValue() - 1) / 3;
	                Month startMonth = Month.of(quarterIndex * 3 + 1);
	                start = LocalDate.of(referenceDate.getYear(), startMonth, 1);
	                end = start.plusMonths(3).minusDays(1);
	            }
	            case HALF_YEARLY -> {
	                boolean firstHalf = referenceDate.getMonthValue() <= 6;
	                start = LocalDate.of(referenceDate.getYear(), firstHalf ? 1 : 7, 1);
	                end = start.plusMonths(6).minusDays(1);
	            }
	            case YEARLY -> {
	                start = referenceDate.withDayOfYear(1);
	                end = referenceDate.with(TemporalAdjusters.lastDayOfYear());
	            }
	            default -> throw new IllegalStateException("Unhandled period: " + this);
	        }
	        return new LocalDateTime[] { start.atStartOfDay(), end.atTime(23, 59, 59) };
	    }

}
