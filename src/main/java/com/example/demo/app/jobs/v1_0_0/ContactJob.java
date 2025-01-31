package com.example.demo.app.jobs.v1_0_0;

import com.avx.migration.annotations.Executable;
import com.avx.migration.jobs.spec.Job;
import com.example.demo.app.domain.postgres.Contact;
import com.example.demo.app.repository.postgres.ContactRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Executable(version = "1.0.0", order = "3")
@Component("ContactJobV1.0.0")
@Slf4j
@RequiredArgsConstructor
public class ContactJob implements Job<Contact> {
    private final ContactRepository contactRepository;
    @PersistenceContext(unitName = "mysql")
    private EntityManager mysqlEntityManager;

    @Override
    public void migrate(int page, int size, String version) {
        log.info("########### migrate ContactJobV1.0.0");
        var offset = page * size;
        var contactBuilder = Contact.builder();
        var sql = String.format("SELECT * FROM contact LIMIT %d OFFSET %d", size, offset);
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        for (var record : results) {
            var recordRows = (Object[]) record;

            contactBuilder.firstName((String) recordRows[1]);
            contactBuilder.lastName((String) recordRows[3]);


            contactBuilder.version(version);

            contactRepository.save(contactBuilder.build());
        }
    }

    @Override
    public Long getSize() {
        log.info("########### getSize ContactJobV1.0.0");
        var sql = "SELECT count(*) FROM contact";
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        return (results != null && !results.isEmpty()) ? (Long) results.getFirst() : 0L;
    }


    @Override
    @Transactional(transactionManager = "postgresTransactionManager")
    public void rollback(String version) {
        log.info("########### rollback ContactJobV1.0.0");
        contactRepository.deleteContactsByVersion(version);
    }
}
