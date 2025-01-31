package com.example.demo.app.jobs.v2_0_0;

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

@Executable(version = "2.0.0", order = "2")
@Component("CourseJobV2.0.0")
@Slf4j
@RequiredArgsConstructor
public class CourseJob implements Job<Course> {
    private final CourseRepository courseRepository;
    @PersistenceContext(unitName = "mysql")
    private EntityManager mysqlEntityManager;


    @Override
    public void migrate(int page, int size, String version) {
        log.info("########### migrate CourseJobV2.0.0");
        var offset = page * size;
        var sql = String.format("SELECT * FROM course LIMIT %d OFFSET %d", size, offset);
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        for (var record : results) {
            var recordRows = (Object[]) record;

            var course = courseRepository.findByName((String) recordRows[1]).orElse(new Course());
            course.setLocation((String) recordRows[5]);
            course.setVersion(version);

            courseRepository.save(course);
        }
    }

    @Override
    public Long getSize() {
        log.info("########### getSize CourseJobV2.0.0");
        var sql = "SELECT count(*) FROM course";
        var query = mysqlEntityManager.createNativeQuery(sql);
        var results = query.getResultList();
        return (results != null && !results.isEmpty()) ? (Long) results.getFirst() : 0L;
    }

    @Override
    @Transactional(transactionManager = "postgresTransactionManager")
    public void rollback(String version) {
        log.info("########### rollback CourseJobV2.0.0");
        courseRepository.resetLocation(version);
        courseRepository.resetVersion(version, "1.0.0");

    }
}
