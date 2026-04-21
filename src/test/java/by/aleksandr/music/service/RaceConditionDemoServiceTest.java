package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import by.aleksandr.music.dto.response.RaceConditionDemoResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

class RaceConditionDemoServiceTest {

    private final RaceConditionDemoService raceConditionDemoService = new RaceConditionDemoService();

    @Test
    void runDemoShouldShowUnsafeCounterDivergenceAndSafeCountersCorrectness() {
        RaceConditionDemoResponse response = null;

        for (int attempt = 0; attempt < 3; attempt++) {
            response = raceConditionDemoService.runDemo(64, 20_000);
            if (response.unsafeActual() != response.expectedTotal()) {
                break;
            }
        }

        assertThat(response).isNotNull();
        assertThat(response.atomicActual()).isEqualTo(response.expectedTotal());
        assertThat(response.synchronizedActual()).isEqualTo(response.expectedTotal());
        assertThat(response.unsafeActual()).isLessThan(response.expectedTotal());
        assertThat(response.elapsedMillis()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void awaitStartShouldWrapInterruptedException() throws Exception {
        Method method = RaceConditionDemoService.class.getDeclaredMethod("awaitStart", CountDownLatch.class);
        method.setAccessible(true);
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> invokeAwaitStart(method))
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(InterruptedException.class)
                .hasMessage("Race condition demo was interrupted");

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
        Thread.interrupted();
    }

    private void invokeAwaitStart(Method method) throws Throwable {
        try {
            method.invoke(raceConditionDemoService, new CountDownLatch(1));
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getCause();
        }
    }
}
