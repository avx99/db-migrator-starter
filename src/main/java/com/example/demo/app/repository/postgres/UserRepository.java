package com.example.demo.app.repository.postgres;


import com.example.demo.app.domain.postgres.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    @Modifying
    @Query(value = "DELETE FROM users c WHERE c.version = :version", nativeQuery = true)
    void deleteUsersByVersion(@Param("version") String version);

    Optional<Users> findByEmail(String email);

    @Modifying
    @Query(value = "UPDATE users SET phone_number = NULL WHERE version = :version", nativeQuery = true)
    void resetPhoneNumber(@Param("version") String version);

    @Modifying
    @Query(value = "UPDATE users SET version = :newVersion WHERE version = :oldVersion", nativeQuery = true)
    void resetVersion(@Param("oldVersion") String oldVersion, @Param("newVersion") String newVersion);
}
