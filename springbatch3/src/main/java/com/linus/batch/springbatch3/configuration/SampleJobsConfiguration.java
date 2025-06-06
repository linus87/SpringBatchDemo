package com.linus.batch.springbatch3.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.dao.QueryTimeoutException;

import com.linus.batch.components.chunk.SampleProcessor;
import com.linus.batch.components.chunk.SampleReader;
import com.linus.batch.components.chunk.SampleWriter;
import com.linus.batch.components.tasklet.SleepTasklet;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SampleJobsConfiguration {
    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job job() throws Exception {
        Step step1 = new StepBuilder("step1", jobRepository).tasklet(new SleepTasklet(), transactionManager).build();

        SampleReader reader = new SampleReader();
        SampleProcessor processor = new SampleProcessor();
        SampleWriter writer = new SampleWriter();

        ThreadPoolTaskExecutorRepeatTemplate repeatTemplate = new ThreadPoolTaskExecutorRepeatTemplate();
        repeatTemplate.setTaskExecutor(threadPoolTaskExecutor());

        Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<Class<? extends Throwable>, Boolean>(1);
        skippableExceptions.put(QueryTimeoutException.class, true);
        Step step2 = new StepBuilder("step2", jobRepository).<String, String>chunk(10, transactionManager)
                .faultTolerant().skipPolicy(new LimitCheckingItemSkipPolicy(1, skippableExceptions))
                .reader(reader).processor(processor).writer(writer)
//                .stepOperations(new RepeatTemplate())
//                .taskExecutor(threadPoolTaskExecutor())
                .stepOperations(repeatTemplate)
                .build();

        Step step3 = new StepBuilder("step3", jobRepository).tasklet(new SleepTasklet(), transactionManager).build();
        
        return new JobBuilder("job1", jobRepository).incrementer(new RunIdIncrementer()).start(step1).next(step2).next(step3).build();
    }

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(20);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(0);
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
