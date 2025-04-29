package com.linus.batch.springbatch3.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.repeat.*;
import org.springframework.batch.repeat.policy.DefaultResultCompletionPolicy;
import org.springframework.batch.repeat.support.*;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Simplest implementation of {@link RepeatOperations} that uses a {@link ThreadPoolTaskExecutor} to execute
 */
public class SimpleThreadPoolTaskExecutorRepeatTemplate implements RepeatOperations {

  private static final Logger log = LoggerFactory.getLogger(SimpleThreadPoolTaskExecutorRepeatTemplate.class);
  private CompletionPolicy completionPolicy = new DefaultResultCompletionPolicy();

  private ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

  // setter of taskExecutor
  public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
    Assert.notNull(taskExecutor, "taskExecutor must not be null");
    this.taskExecutor = taskExecutor;
  }

  private RepeatStatus status = RepeatStatus.CONTINUABLE;

  @Override
  public RepeatStatus iterate(RepeatCallback callback) throws RepeatException {
    RepeatContext outer = RepeatSynchronizationManager.getContext();
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

    try {
      do {
        try {
          // Check if the task executor is not full， if it is not full, then submit the task to the task executor
          // If the task executor is full, then wait for a while and try again
          if (this.taskExecutor.getActiveCount() < this.taskExecutor.getCorePoolSize()) {
            this.taskExecutor.submit(new Runnable() {
              @Override
              public void run() {
                RepeatStatus result = null;
                try {
                  result = callback.doInIteration(outer);
                } catch (Exception e) {
                  if (e instanceof RepeatException) {
                    throw (RepeatException) e;
                  } else {
                    throw new RepeatException("Error in iteration", e);
                  }
                } finally {
                  if (result == null) {
                    result = RepeatStatus.FINISHED;
                  }
                }

                if (result != null) {
                  status = status.and(result.isContinuable());
                }
              }
            });
          }
        } catch (TaskRejectedException e) {
          // ignore TaskRejectedException
        }
      } while (!completionPolicy.isComplete(outer, status));
    } finally {
      RepeatSynchronizationManager.clear();
      if (outer != null) {
        RepeatSynchronizationManager.register(outer);
      }
    }

    while (taskExecutor.getActiveCount() > 0) {
      // wait for all tasks to finish
    }

    return status;
  }

}