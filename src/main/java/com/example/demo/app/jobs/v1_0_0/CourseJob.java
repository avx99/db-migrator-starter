package com.example.demo.app.jobs.v1_0_0;

import com.avx.migration.annotations.Executable;
import com.avx.migration.jobs.spec.Job;
import com.example.demo.app.domain.postgres.Course;
import com.example.demo.app.repository.postgres.CourseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Executable(version = "1.0.0", order = "2")
@Component("CourseJobV1.0.0")
@Slf4j
@RequiredArgsConstructor
public class CourseJob implements Job<Course> {
    private final CourseRepository courseRepository;
    @PersistenceContext(unitName = "mysql")
    private EntityManager mysqlEntityManager;


    @Override
    public void migrate(int page, int size, String version) {
        log.info("########### migrate CourseJobV1.0.0");
        var offset = page * size;
        var courseBuilder = Course.builder();
        var sql = String.format("SELECT * FROM course LIMIT %d OFFSET %d", size, offset);
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        for (var record : results) {
            var recordRows = (Object[]) record;

            courseBuilder.name((String) recordRows[1]);

            courseBuilder.version(version);

            courseRepository.save(courseBuilder.build());
        }
    }

    @Override
    public Long getSize() {
        log.info("########### getSize CourseJobV1.0.0");
        var sql = "SELECT count(*) FROM course";
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        return (results != null && !results.isEmpty()) ? (Long) results.getFirst() : 0L;
    }

    @Override
    @Transactional(transactionManager = "postgresTransactionManager")
    public void rollback(String version) {
        log.info("########### rollback CourseJobV1.0.0");
        courseRepository.deleteCoursesByVersion(version);
    }
}
