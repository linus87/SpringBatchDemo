package com.linus.batch.springbatch3.configuration;

import com.linus.batch.components.chunk.SampleProcessor;
import com.linus.batch.components.chunk.SampleReader;
import com.linus.batch.components.chunk.SampleWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ParallelStepsJobsConfiguration {
  private JobRepository jobRepository;
  private PlatformTransactionManager transactionManager;

  private SampleReader reader = new SampleReader();
  private SampleProcessor processor = new SampleProcessor();
  private SampleWriter writer = new SampleWriter();

  private static Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<Class<? extends Throwable>, Boolean>(1);

  static {
    // Add skippable exceptions here
    skippableExceptions.put(QueryTimeoutException.class, true);
  }

  @Bean
  public Flow splitFlow() {
    // create 20 parallel flows, they share the same logic.

    List<Flow> flows = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      String flowName = "flow" + i;
      Flow flow = createFlow(flowName);
      flows.add(flow);
    }

    return new FlowBuilder<SimpleFlow>("splitFlow")
            .split(threadPoolTaskExecutor())
            .add(flows.toArray(new Flow[0]))
            .build();
  }

  public Flow createFlow(String flowName) {

    Step step1 = new StepBuilder(flowName + "_step1", jobRepository).<String, String>chunk(10, transactionManager)
            .faultTolerant().skipPolicy(new LimitCheckingItemSkipPolicy(1, skippableExceptions))
            .reader(reader).processor(processor).writer(writer)
            .build();
    return new FlowBuilder<SimpleFlow>(flowName)
            .start(step1)
            .build();
  }

//  @Bean
//  public Flow flow1() {
//    Step step1 = new StepBuilder("step1", jobRepository).<String, String>chunk(10, transactionManager)
//            .faultTolerant().skipPolicy(new LimitCheckingItemSkipPolicy(1, skippableExceptions))
//            .reader(reader).processor(processor).writer(writer)
//            .build();
//    return new FlowBuilder<SimpleFlow>("flow1")
//            .start(step1)
//            .build();
//  }
//
//
//  @Bean
//  public Flow flow2() {
//    Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<Class<? extends Throwable>, Boolean>(1);
//    skippableExceptions.put(QueryTimeoutException.class, true);
//
//    Step step1 = new StepBuilder("step1", jobRepository).<String, String>chunk(10, transactionManager)
//            .faultTolerant().skipPolicy(new LimitCheckingItemSkipPolicy(1, skippableExceptions))
//            .reader(reader).processor(processor).writer(writer)
//            .build();
//    return new FlowBuilder<SimpleFlow>("flow2")
//            .start(step1)
//            .build();
//  }

  @Bean
  public Job parallelJob() throws Exception {

    return new JobBuilder("parallel", jobRepository).incrementer(new RunIdIncrementer())
            .start(splitFlow()).build().build();

  }

  public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(20);
    taskExecutor.setMaxPoolSize(20);
    taskExecutor.setQueueCapacity(0);
    taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
//        taskExecutor.setAwaitTerminationSeconds(2);
    taskExecutor.setThreadNamePrefix("taskExecutor-");
    taskExecutor.initialize();

    return taskExecutor;
  }

  public SimpleAsyncTaskExecutor simpleAsyncTaskExecutor() {
    SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
    taskExecutor.setConcurrencyLimit(20);
    taskExecutor.setThreadNamePrefix("taskExecutor-");
    return taskExecutor;
  }

  @Autowired
  public void setJobRepository(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  @Autowired
  public void setTransactionManager(PlatformTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }
}
