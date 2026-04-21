package by.aleksandr.music.dto.response;

public record RaceConditionDemoResponse(
        int threadCount,
        int iterationsPerThread,
        int expectedTotal,
        int unsafeActual,
        int atomicActual,
        int synchronizedActual,
        long elapsedMillis) {
}
