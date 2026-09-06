package com.chrome_history_dashboard.user_service;

import org.springframework.stereotype.Service;

import com.chrome_history_dashboard.user_entity.User_Entity;
import com.chrome_history_dashboard.user_repo.User_Repo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class User_Service {
	
	private static final String DEFAULT_EMAIL = "local-user@chrome-history-dashboard.local";

    private final User_Repo userRepo;

    @Transactional
    public User_Entity getOrCreateDefaultUser() {
        return userRepo.findByEmailIgnoreCase(DEFAULT_EMAIL)
                .orElseGet(() -> userRepo.save(User_Entity.builder()
                        .email(DEFAULT_EMAIL)
                        .name("Local User")
                        .build()));
    }

}
