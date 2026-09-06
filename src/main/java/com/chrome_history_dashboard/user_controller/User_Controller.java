package com.chrome_history_dashboard.user_controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chrome_history_dashboard.user_entity.User_Entity;
import com.chrome_history_dashboard.user_service.User_Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class User_Controller {
	
	  private final User_Service userService;

	    @GetMapping("/user")
	    public User_Entity getCurrentUser() {
	        return userService.getOrCreateDefaultUser();
	    }

}
