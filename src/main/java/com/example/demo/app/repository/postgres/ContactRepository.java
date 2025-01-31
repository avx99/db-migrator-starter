package com.example.demo.app.repository.postgres;


import com.example.demo.app.domain.postgres.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
    Optional<Contact> findByFirstName(String firstName);

    @Modifying
    @Query(value = "DELETE FROM contact c WHERE c.version = :version", nativeQuery = true)
    void deleteContactsByVersion(@Param("version") String version);

    @Modifying
    @Query(value = "UPDATE contact SET company = NULL WHERE version = :version", nativeQuery = true)
    void resetCompany(@Param("version") String version);

    @Modifying
    @Query(value = "UPDATE contact SET version = :newVersion WHERE version = :oldVersion", nativeQuery = true)
    void resetVersion(@Param("oldVersion") String oldVersion, @Param("newVersion") String newVersion);

}
