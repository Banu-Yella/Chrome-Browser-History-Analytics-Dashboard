package com.chrome_history_dashboard.user_repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chrome_history_dashboard.user_entity.User_Entity;

@Repository
public interface User_Repo extends JpaRepository<User_Entity, Integer> {
	
	 Optional<User_Entity> findByEmailIgnoreCase(String email);
	    boolean existsByEmailIgnoreCase(String email);

}
