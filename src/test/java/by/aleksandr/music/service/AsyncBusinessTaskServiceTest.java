package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;

import by.aleksandr.music.dto.request.AsyncTaskRequest;
import by.aleksandr.music.dto.response.AsyncTaskStatusResponse;
import java.util.concurrent.locks.LockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:musicdb-async;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class AsyncBusinessTaskServiceTest {

    @Autowired
    private AsyncBusinessTaskService asyncBusinessTaskService;

    @Autowired
    private CounterService counterService;

    @BeforeEach
    void setUp() {
        counterService.reset();
    }

    @Test
    void startTaskShouldReturnIdAndEventuallyComplete() {
        long taskId = asyncBusinessTaskService.startTask(new AsyncTaskRequest(3, 20L, 2));

        AsyncTaskStatusResponse finalStatus = waitUntilCompleted(taskId);

        assertThat(taskId).isPositive();
        assertThat(finalStatus.status()).isEqualTo("COMPLETED");
        assertThat(finalStatus.completedSteps()).isEqualTo(3);
        assertThat(finalStatus.totalSteps()).isEqualTo(3);
        assertThat(finalStatus.totalAppliedIncrements()).isEqualTo(6);
        assertThat(finalStatus.safeCounterValue()).isEqualTo(6);
        assertThat(finalStatus.errorMessage()).isNull();
    }

    private AsyncTaskStatusResponse waitUntilCompleted(long taskId) {
        long deadline = System.currentTimeMillis() + 3_000;
        AsyncTaskStatusResponse currentStatus = asyncBusinessTaskService.getTaskStatus(taskId);
        while (!"COMPLETED".equals(currentStatus.status()) && System.currentTimeMillis() < deadline) {
            LockSupport.parkNanos(25_000_000L);
            currentStatus = asyncBusinessTaskService.getTaskStatus(taskId);
        }
        return currentStatus;
    }
}
