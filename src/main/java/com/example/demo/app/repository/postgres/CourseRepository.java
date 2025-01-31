package com.example.demo.app.repository.postgres;


import com.example.demo.app.domain.postgres.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    Optional<Course> findByName(String name);

    @Modifying
    @Query(value = "DELETE FROM course WHERE version = :version", nativeQuery = true)
    void deleteCoursesByVersion(@Param("version") String version);

    @Modifying
    @Query(value = "UPDATE course SET location = NULL WHERE version = :version", nativeQuery = true)
    void resetLocation(@Param("version") String version);

    @Modifying
    @Query(value = "UPDATE course SET version = :newVersion WHERE version = :oldVersion", nativeQuery = true)
    void resetVersion(@Param("oldVersion") String oldVersion, @Param("newVersion") String newVersion);
}
