package by.aleksandr.music.service;

import static org.assertj.core.api.Assertions.assertThat;

import by.aleksandr.music.dto.response.RaceConditionDemoResponse;
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
    }
}
