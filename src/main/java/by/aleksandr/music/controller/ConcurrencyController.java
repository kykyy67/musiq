package by.aleksandr.music.controller;

import by.aleksandr.music.dto.request.AsyncTaskRequest;
import by.aleksandr.music.dto.response.AsyncTaskStatusResponse;
import by.aleksandr.music.dto.response.AsyncTaskSubmissionResponse;
import by.aleksandr.music.dto.response.CounterResponse;
import by.aleksandr.music.dto.response.RaceConditionDemoResponse;
import by.aleksandr.music.service.AsyncBusinessTaskService;
import by.aleksandr.music.service.CounterService;
import by.aleksandr.music.service.RaceConditionDemoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/concurrency")
@RequiredArgsConstructor
public class ConcurrencyController {

    private final AsyncBusinessTaskService asyncBusinessTaskService;
    private final CounterService counterService;
    private final RaceConditionDemoService raceConditionDemoService;

    @Operation(summary = "Start async business task")
    @PostMapping("/tasks")
    public ResponseEntity<AsyncTaskSubmissionResponse> startAsyncTask(@Valid @RequestBody AsyncTaskRequest request) {
        long taskId = asyncBusinessTaskService.startTask(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new AsyncTaskSubmissionResponse(taskId, "SUBMITTED"));
    }

    @Operation(summary = "Get async task status")
    @GetMapping("/tasks/{taskId}")
    public AsyncTaskStatusResponse getTaskStatus(@PathVariable Long taskId) {
        return asyncBusinessTaskService.getTaskStatus(taskId);
    }

    @Operation(summary = "Increment thread-safe counters")
    @PostMapping("/counter/increment")
    public CounterResponse incrementCounter(@RequestParam(defaultValue = "1") @Min(1) @Max(10_000) int delta) {
        int atomicValue = counterService.incrementAtomic(delta);
        int synchronizedValue = counterService.incrementSynchronized(delta);
        return new CounterResponse(atomicValue, synchronizedValue);
    }

    @Operation(summary = "Get thread-safe counter values")
    @GetMapping("/counter")
    public CounterResponse getCounter() {
        return new CounterResponse(counterService.getAtomicValue(), counterService.getSynchronizedValue());
    }

    @Operation(summary = "Reset thread-safe counters")
    @PostMapping("/counter/reset")
    public ResponseEntity<Void> resetCounter() {
        counterService.reset();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Demonstrate race condition and fixes")
    @PostMapping("/race-demo")
    public RaceConditionDemoResponse raceDemo(
            @RequestParam(name = "threads", defaultValue = "64") @Min(50) @Max(500) int threadCount,
            @RequestParam(name = "incrementsPerThread", defaultValue = "5000")
            @Min(1_000) @Max(100_000) int iterationsPerThread) {
        return raceConditionDemoService.runDemo(threadCount, iterationsPerThread);
    }
}
