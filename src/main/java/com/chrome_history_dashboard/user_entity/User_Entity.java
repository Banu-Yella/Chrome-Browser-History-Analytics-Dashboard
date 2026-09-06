package com.chrome_history_dashboard.user_entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.chrome_history_dashboard.analytics_summary_entity.Analytics_Summary_Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "Users")
public class User_Entity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "users_Id")
	private Integer users_Id;

	@Column(name = "email")
	private String email;
	
	@Column(name = "name")
	private String name;
	
	@CreationTimestamp
	@Column(name = "created_At", updatable = false)
	private LocalDateTime createdAt;
	
	@UpdateTimestamp
	@Column(name = "updated_At")
	private LocalDateTime updatedAt;
	
	//	JPA RELATIONSHIP MAPPING
	@OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Analytics_Summary_Entity> analyticsSummaries;
	
	
}
