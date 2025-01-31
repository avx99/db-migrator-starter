package com.example.demo.app.jobs.v1_0_0;

import com.avx.migration.annotations.Executable;
import com.avx.migration.jobs.spec.Job;
import com.example.demo.app.domain.postgres.Users;
import com.example.demo.app.repository.postgres.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Executable(version = "1.0.0", order = "1")
@Component("UserJobV1.0.0")
@Slf4j
@RequiredArgsConstructor
public class UserJob implements Job<Users> {
    private final UserRepository userRepository;
    @PersistenceContext(unitName = "mysql")
    private EntityManager mysqlEntityManager;


    @Override
    public void migrate(int page, int size, String version) {
        log.info("########### migrate UserJobV1.0.0");
        var offset = page * size;
        var userBuilder = Users.builder();
        var sql = String.format("SELECT * FROM user LIMIT %d OFFSET %d", size, offset);
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        for (var record : results) {
            var recordRows = (Object[]) record;

            userBuilder.firstName((String) recordRows[4]);
            userBuilder.lastName((String) recordRows[5]);
            userBuilder.city((String) recordRows[6]);
            userBuilder.job((String) recordRows[7]);
            userBuilder.email((String) recordRows[8]);

            userBuilder.version(version);

            userRepository.save(userBuilder.build());
        }
    }

    @Override
    public Long getSize() {
        log.info("########### getSize UserJobV1.0.0");
        var sql = "SELECT count(*) FROM user";
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        return (results != null && !results.isEmpty()) ? (Long) results.getFirst() : 0L;
    }

    @Override
    @Transactional(transactionManager = "postgresTransactionManager")
    public void rollback(String version) {
        log.info("########### rollback UserJobV1.0.0");
        userRepository.deleteUsersByVersion(version);
    }
}
