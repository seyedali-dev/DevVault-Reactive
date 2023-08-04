package com.dev.vault.scheduler;

import com.dev.vault.model.domain.task.Task;
import com.dev.vault.model.enums.TaskStatus;
import com.dev.vault.repository.task.TaskReactiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

import static com.dev.vault.model.enums.TaskStatus.IN_PROGRESS;
import static com.dev.vault.model.enums.TaskStatus.OVERDUE;

/**
 * This class schedules the marking of overdue tasks and sends notifications.<br>
 * The method "markTaskOverDue" runs every 10 minutes and iterates over all tasks in the database with a status of "IN_PROGRESS".<br>
 * If a task is overdue, its status is updated to "OVERDUE", its "hasOverdue" flag is set to true, and its "completionDate" is set to the current date and time.<br>
 * The method also sends a notification to the task assignee that the task is overdue (TODO).<br>
 * <p>
 * To change the scheduling time, use the following pattern:<br>
 * second, minute, hour, day, month, weekday.<br>
 * Examples of different scheduling patterns include:<br>
 * <p>
 * 1. "@Scheduled(cron = '0 0 18,21,0 * * *')": runs every day at 6:00 PM, 9:00 PM, and 12:00 AM.<br>
 * 2. "@Scheduled(cron = '0 0 0 * * *')": runs every day at midnight.<br>
 * 3. "@Scheduled(cron = '0 0 6-9 * * *')": runs every day at 6:00 AM, 7:00 AM, 8:00 AM, and 9:00 AM.<br>
 * <p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectTaskScheduler {

    private final TaskReactiveRepository taskReactiveRepository;

    /**
     * This method marks overdue tasks as {@link TaskStatus#OVERDUE OVERDUE} and saves them to the database.<br>
     * It iterates over all tasks in the database with a status of {@link TaskStatus#IN_PROGRESS IN_PROGRESS} and checks if their due date is before the current date and time.<br>
     * If a task is overdue, its status is updated to {@link TaskStatus#OVERDUE OVERDUE}, its "{@link Task#hasOverdue hasOverdue}" flag is set to true, and its "{@link Task#completionDate completionDate}" is set to the current date and time.<br>
     * The method runs every 10 minutes.<br><br>
     * <p>
     * You can change the scheduling time according to your needs using the following pattern:<br>
     * {{ second, minute, hour, day, month, weekday }}.<br>
     * Examples of different scheduling patterns include:<br>
     * <p>
     * 1. "@Scheduled(cron = '0 0 18,21,0 * * *')": runs every day at 6:00 PM, 9:00 PM, and 12:00 AM.<br>
     * 2. "@Scheduled(cron = '0 0 0 * * *')": runs every day at midnight.<br>
     * 3. "@Scheduled(cron = '0 0 6-9 * * *')": runs every day at 6:00 AM, 7:00 AM, 8:00 AM, and 9:00 AM.<br>
     * <p>
     * Use the appropriate scheduling pattern to suit your needs.<br>
     */
    @Scheduled(fixedRateString = "PT10M")
    public void markTaskOverDue() {
        taskReactiveRepository.findAll()
                // TODO: SEND NOTIFICATION OF OVERDUE TASK
                .flatMap(task -> {

                    // Check if the task is already marked as overdue -> do nothing if it is
                    if (task.getTaskStatus().equals(OVERDUE))
                        return Flux.empty();

                    // Check if the task is in progress -> do nothing if it is (since there's no need to check OVERDUE & COMPLETED tasks)
                    if (!task.getTaskStatus().equals(IN_PROGRESS))
                        return Flux.empty();

                    // Check if the task is overdue -> the real check of overdue tasks
                    if (task.getDueDate().isBefore(LocalDateTime.now())) {
                        log.info("------------------");
                        log.info("Scheduling task...");
                        log.warn("⌚⌚⌚Scheduler::: Task: {{}}, is overdue!⌚⌚⌚", task.getTaskName());

                        // Update the task status, hasOverdue flag, and completionDate and save to db
                        task.setTaskStatus(OVERDUE);
                        task.setHasOverdue(true);
                        task.setCompletionDate(LocalDateTime.now());

                        log.info("------------------");

                        // Return a Mono that saves the updated task to the database
                        return taskReactiveRepository.save(task);
                    }
                    return Flux.empty();
                }).subscribe();
    }
}

//TODO: REMINDER SYSTEM FOR TASKS THAT ARE APPROACHING THERE DUE DATE.
