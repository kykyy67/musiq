package by.aleksandr.music.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RaceConditionDemoResponse(
        @JsonProperty("threads")
        int threadCount,
        @JsonProperty("incrementsPerThread")
        int iterationsPerThread,
        @JsonProperty("expected")
        int expectedTotal,
        @JsonProperty("unsafeResult")
        int unsafeActual,
        @JsonProperty("atomicResult")
        int atomicActual,
        @JsonProperty("synchronizedResult")
        int synchronizedActual,
        long elapsedMillis) {
}
