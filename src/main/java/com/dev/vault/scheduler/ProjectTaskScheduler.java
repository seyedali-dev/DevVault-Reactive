package com.dev.vault.scheduler;

import com.dev.vault.repository.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    private final TaskRepository taskRepository;

    /**
     * This method marks overdue tasks as "overdue" and saves them to the database.<br>
     * It iterates over all tasks in the database with a status of "IN_PROGRESS" and checks if their due date is before the current date and time.<br>
     * If a task is overdue, its status is updated to "OVERDUE", its "hasOverdue" flag is set to true, and its "completionDate" is set to the current date and time.<br>
     * The method runs every 10 minutes.<br><br>
     * <p>
     * You can change the scheduling time according to your needs using the following pattern:<br>
     * second, minute, hour, day, month, weekday.<br>
     * Examples of different scheduling patterns include:<br>
     * <p>
     * 1. "@Scheduled(cron = '0 0 18,21,0 * * *')": runs every day at 6:00 PM, 9:00 PM, and 12:00 AM.<br>
     * 2. "@Scheduled(cron = '0 0 0 * * *')": runs every day at midnight.<br>
     * 3. "@Scheduled(cron = '0 0 6-9 * * *')": runs every day at 6:00 AM, 7:00 AM, 8:00 AM, and 9:00 AM.<br>
     * <p>
     * Use the appropriate scheduling pattern to suit your needs.<br>
     */
    /*@Scheduled(fixedRateString = "PT10M")
    public void markTaskOverDue() {
        List<Task> tasks = taskRepository.findAll();
        for (Task task : tasks) {
            // TODO: SEND NOTIFICATION OF OVERDUE TASK
            // Check if the task is already marked as overdue
            if (task.getTaskStatus().equals(OVERDUE))
                continue;

            // Check if the task is in progress
            if (!task.getTaskStatus().equals(IN_PROGRESS))
                continue;

            // Check if the task is overdue
            if (task.getDueDate().isBefore(LocalDateTime.now())) {
                log.info("⌚⌚⌚Scheduler::: Task: {}, is overdue!⌚⌚⌚", task.getTaskName());

                // Update the task status, hasOverdue flag, and completionDate and save to db
                task.setTaskStatus(TaskStatus.OVERDUE);
                task.setHasOverdue(true);
                task.setCompletionDate(LocalDateTime.now());

                taskRepository.save(task);
            }
        }
    }*/

    //TODO: REMINDER SYSTEM FOR TASKS THAT ARE APPROACHING THERE DUE DATE.
}
