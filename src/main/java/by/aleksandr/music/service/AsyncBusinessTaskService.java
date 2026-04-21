package by.aleksandr.music.service;

import by.aleksandr.music.dto.request.AsyncTaskRequest;
import by.aleksandr.music.dto.response.AsyncTaskStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncBusinessTaskService {

    private final AsyncTaskRegistry asyncTaskRegistry;
    private final AsyncBusinessWorker asyncBusinessWorker;

    public long startTask(AsyncTaskRequest request) {
        long taskId = asyncTaskRegistry.createTask(request.steps());
        asyncBusinessWorker.executeTask(
                taskId,
                request.steps(),
                request.delayMillis(),
                request.incrementPerStep());
        return taskId;
    }

    public AsyncTaskStatusResponse getTaskStatus(long taskId) {
        return asyncTaskRegistry.getTask(taskId);
    }
}
