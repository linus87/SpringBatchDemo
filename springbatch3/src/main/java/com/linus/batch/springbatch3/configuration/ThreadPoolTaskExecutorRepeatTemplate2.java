package com.linus.batch.springbatch3.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.repeat.*;
import org.springframework.batch.repeat.exception.DefaultExceptionHandler;
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.batch.repeat.policy.DefaultResultCompletionPolicy;
import org.springframework.batch.repeat.support.*;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Try to rewrite ThreadPoolTaskExecutorRepeatTemplate by implements RepeatOperations directly.
 */
public class ThreadPoolTaskExecutorRepeatTemplate2 implements RepeatOperations {

  private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTaskExecutorRepeatTemplate2.class);
  private RepeatListener[] listeners = new RepeatListener[0];

  private CompletionPolicy completionPolicy = new DefaultResultCompletionPolicy();
  private ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
  private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

  // setter of taskExecutor
  public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
    Assert.notNull(taskExecutor, "taskExecutor must not be null");
    this.taskExecutor = taskExecutor;
  }

  private RepeatStatus status = RepeatStatus.CONTINUABLE;

  @Override
  public RepeatStatus iterate(RepeatCallback callback) throws RepeatException {
    RepeatContext outer = RepeatSynchronizationManager.getContext(); // save outer repeat context
    RepeatStatus result = RepeatStatus.CONTINUABLE;

    try {
      result = this.executeInternal(callback);
    } finally {
      RepeatSynchronizationManager.clear();
      if (outer != null) {
        // restore outer repeat context
        RepeatSynchronizationManager.register(outer);
      }
    }

    return result;
  }

  private RepeatStatus executeInternal(final RepeatCallback callback) {
    RepeatContext context = this.start();
    // check if repeat is complete according to context alone
    boolean running = !this.isMarkedComplete(context);

    for(RepeatListener interceptor : this.listeners) {
      interceptor.open(context);
      running = running && !this.isMarkedComplete(context);
      if (!running) {
        break;
      }
    }

    RepeatStatusInternalState internalState = (RepeatStatusInternalState)this.createInternalState(context);
    Collection<Throwable> throwables = internalState.getThrowables();
    Collection<Throwable> deferred = new ArrayList();

    for(RepeatListener interceptor : this.listeners) {
      interceptor.before(context);
      running = running && !this.isMarkedComplete(context);
    }

    if (running) {
      do {
        if (!running) {
          status = status.and(this.waitForResults(internalState));

          // handle exception if there is any and break out of the loop
          for(Throwable throwable : throwables) {
            this.doHandle(throwable, context, deferred);
          }
          break;
        }

        try {
          // Check if the task executor is not full， if it is not full, then submit the task to the task executor
          // If the task executor is full, then wait for a while and try again
          if (this.taskExecutor.getActiveCount() < this.taskExecutor.getCorePoolSize()) {
            this.taskExecutor.submit(new ExecutingRunnable(callback, context, internalState));
            this.update(context);
          }
        } catch (TaskRejectedException e) {
          // ignore TaskRejectedException
        }
      } while (!completionPolicy.isComplete(context, status));

      while (taskExecutor.getActiveCount() > 0) {
        // wait for all tasks to finish
      }

      try {
        int i = this.listeners.length;

        while(i-- > 0) {
          RepeatListener interceptor = this.listeners[i];
          interceptor.close(context);
        }
      } finally {
        context.close();
      }
    }

    return status;
  }

  protected boolean waitForResults(RepeatInternalState state) {
    return true;
  }

  protected RepeatContext start() {
    RepeatContext parent = RepeatSynchronizationManager.getContext();
    RepeatContext context = this.completionPolicy.start(parent);
    RepeatSynchronizationManager.register(context);
    this.logger.debug("Starting repeat context.");
    return context;
  }

  protected void update(RepeatContext context) {
    this.completionPolicy.update(context);
  }

  protected void executeAfterInterceptors(final RepeatContext context, RepeatStatus value) {
    if (value != null && value.isContinuable()) {
      int i = this.listeners.length;

      while(i-- > 0) {
        RepeatListener interceptor = this.listeners[i];
        interceptor.after(context, value);
      }
    }

  }

  protected RepeatInternalState createInternalState(RepeatContext context) {
    return new RepeatStatusInternalState();
  }

  private void doHandle(Throwable throwable, RepeatContext context, Collection<Throwable> deferred) {
    Throwable unwrappedThrowable = unwrapIfRethrown(throwable);

    try {
      RepeatListener interceptor;
      for(int i = this.listeners.length; i-- > 0; interceptor.onError(context, unwrappedThrowable)) {
        interceptor = this.listeners[i];
        if (this.logger.isDebugEnabled()) {
          this.logger.debug("Exception intercepted (" + (i + 1) + " of " + this.listeners.length + ")", unwrappedThrowable);
        }
      }

      if (this.logger.isDebugEnabled()) {
        StringBuilder message = (new StringBuilder("Handling exception: ")).append(throwable.getClass().getName());
        if (unwrappedThrowable != null) {
          message.append(", caused by: ").append(unwrappedThrowable.getClass().getName()).append(": ").append(unwrappedThrowable.getMessage());
        }

        this.logger.debug(message.toString());
      }

      this.exceptionHandler.handleException(context, unwrappedThrowable);
    } catch (Throwable handled) {
      deferred.add(handled);
    }

  }

  private static Throwable unwrapIfRethrown(Throwable throwable) {
    return throwable instanceof RepeatException ? throwable.getCause() : throwable;
  }

  public void setListeners(RepeatListener[] listeners) {
    this.listeners = (RepeatListener[]) Arrays.asList(listeners).toArray(new RepeatListener[listeners.length]);
  }

  public void registerListener(RepeatListener listener) {
    List<RepeatListener> list = new ArrayList(Arrays.asList(this.listeners));
    list.add(listener);
    this.listeners = (RepeatListener[])list.toArray(new RepeatListener[list.size()]);
  }

  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  public void setCompletionPolicy(CompletionPolicy terminationPolicy) {
    Assert.notNull(terminationPolicy, "CompletionPolicy is required");
    this.completionPolicy = terminationPolicy;
  }

  private boolean isMarkedComplete(RepeatContext context) {
    boolean complete = context.isCompleteOnly();
    if (context.getParent() != null) {
      complete = complete || this.isMarkedComplete(context.getParent());
    }

    if (complete) {
      this.logger.debug("Repeat is complete according to context alone.");
    }

    return complete;
  }

  private class ExecutingRunnable implements Runnable {
    private final RepeatCallback callback;
    private final RepeatContext context;
    private volatile RepeatStatusInternalState internalState;
    private volatile Throwable error;

    public ExecutingRunnable(RepeatCallback callback, RepeatContext context, RepeatStatusInternalState internalState) {
      this.callback = callback;
      this.context = context;
      this.internalState = internalState;
    }

    public void run() {
      boolean clearContext = false;
      RepeatStatus result = null;
      try {
        if (RepeatSynchronizationManager.getContext() == null) {
          clearContext = true;
          RepeatSynchronizationManager.register(this.context);
        }

        if (ThreadPoolTaskExecutorRepeatTemplate2.this.logger.isDebugEnabled()) {
          ThreadPoolTaskExecutorRepeatTemplate2.this.logger.debug("Repeat operation about to start at count=" + this.context.getStartedCount());
        }

        result = result = callback.doInIteration(context);
        ThreadPoolTaskExecutorRepeatTemplate2.this.executeAfterInterceptors(context, result);
      } catch (Throwable e) {
        this.error = e;
      } finally {
        if (result == null) {
          result = RepeatStatus.FINISHED;
        }

        internalState.setStatus(status.and(result.isContinuable()));

        if (clearContext) {
          RepeatSynchronizationManager.clear();
        }
      }

    }

    public Throwable getError() {
      return this.error;
    }

    public RepeatContext getContext() {
      return this.context;
    }
  }

  private static class RepeatStatusInternalState extends RepeatInternalStateSupport {
    private RepeatStatus status = RepeatStatus.CONTINUABLE;

    public void setStatus(RepeatStatus status) {
      this.status = status;
    }

    public RepeatStatus getStatus() {
      return status;
    }
  }
}