package com.chrome_history_dashboard.tag_repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chrome_history_dashboard.tag_entity.Tag_Entity;

@Repository
public interface Tag_Repo extends JpaRepository<Tag_Entity, Integer> {
	
	  // Entity field is literally named "tag_Name" — explicit @Query avoids Spring Data
    // misreading the underscore as a nested-property separator.
    @Query("SELECT t FROM Tag_Entity t WHERE LOWER(t.tag_Name) = LOWER(:tagName)")
    Optional<Tag_Entity> findByTagName(@Param("tagName") String tagName);

    @Query("SELECT t FROM Tag_Entity t WHERE LOWER(t.category) = LOWER(:category)")
    List<Tag_Entity> findByCategory(@Param("category") String category);

    @Query("SELECT COUNT(t) > 0 FROM Tag_Entity t WHERE LOWER(t.tag_Name) = LOWER(:tagName)")
    boolean existsByTagName(@Param("tagName") String tagName);

}
