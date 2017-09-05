package com.globo.ateam.taurina.services;

import com.globo.ateam.taurina.model.Result;
import com.globo.ateam.taurina.model.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;

@Service
@EnableScheduling
public class QueueExecutorService {

    private static final Integer TASK_LIMIT = Integer.parseInt(System.getProperty("task.limit", "0"));

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentLinkedQueue<Callable<Result>> queue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final FilesService filesService;
    private final JmeterService jmeterService;

    @Autowired
    public QueueExecutorService(FilesService filesService, JmeterService jmeterService) throws IOException {
        log.info("Using " + filesService.tmpDir() + " as tmpdir");
        this.filesService = filesService;
        this.jmeterService = jmeterService;
    }

    public void put(long testId, Scenario scenario) {
        if (taskQueueOverflow(testId)) return;
        queue.add(() -> {
            log.info("executing task id " + testId);
            try {
                return jmeterService.start(testId, scenario);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return new Result(-1L, new byte[0]);
        });
        log.info("added task id " + testId);
    }

    private boolean taskQueueOverflow(long testId) {
        try {
            filesService.createDir(filesService.pathIdDirectory(testId));
            if (TASK_LIMIT != 0 && queue.size() >= TASK_LIMIT) {
                String resultFile = filesService.pathResultFile(testId);
                String errorQueueOverflowMessage = "ERROR: Task Queue Overflow. Try again later";
                filesService.writeToFile(("{\"status\":\"" + errorQueueOverflowMessage + "\"}").getBytes(Charset.defaultCharset()), resultFile);
                log.error(errorQueueOverflowMessage);
                log.warn("task id " + testId + " NOT executed");
                return true;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return true;
        }
        return false;
    }

    @Scheduled(fixedDelay = 5000)
    public void run() throws ExecutionException, InterruptedException, IOException {
        if (!queue.isEmpty()) {
            final Callable<Result> task = queue.poll();
            final Result result = executor.submit(task).get();
            long id = result.getId();
            String resultFile = filesService.pathResultFile(id);
            Files.write(Paths.get(resultFile), result.getResult());
            log.info("task id " + id + " executed");
        }
    }

}
