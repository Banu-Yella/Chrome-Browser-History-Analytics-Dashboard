package com.chrome_history_dashboard.browser_history_service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.chrome_history_dashboard.browser_history_entity.Visit_Entity;
import com.chrome_history_dashboard.browser_history_repo.Visit_Repo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Visit_Service {

	private static final int DEFAULT_SESSION_GAP_MINUTES = 30;

    private final Visit_Repo visitRepo;

    public List<Visit_Entity> getVisitsInRange(LocalDateTime start, LocalDateTime end) {
        return visitRepo.findByVisitTimeBetween(start, end);
    }

    /** Groups visits into sessions: a new session starts whenever the gap since the
     *  previous visit exceeds gapMinutes. Useful for "browsing session" views. */
    public List<List<Visit_Entity>> groupIntoSessions(List<Visit_Entity> visits, int gapMinutes) {
        List<Visit_Entity> sorted = visits.stream()
                .sorted((a, b) -> a.getVisitTime().compareTo(b.getVisitTime())).toList();

        List<List<Visit_Entity>> sessions = new ArrayList<>();
        List<Visit_Entity> current = new ArrayList<>();

        for (Visit_Entity visit : sorted) {
            if (!current.isEmpty()) {
                LocalDateTime lastVisitTime = current.get(current.size() - 1).getVisitTime();
                long gap = ChronoUnit.MINUTES.between(lastVisitTime, visit.getVisitTime());
                if (gap > gapMinutes) {
                    sessions.add(current);
                    current = new ArrayList<>();
                }
            }
            current.add(visit);
        }
        if (!current.isEmpty()) sessions.add(current);
        return sessions;
    }

    public List<List<Visit_Entity>> groupIntoSessions(List<Visit_Entity> visits) {
        return groupIntoSessions(visits, DEFAULT_SESSION_GAP_MINUTES);
    }
	
}
