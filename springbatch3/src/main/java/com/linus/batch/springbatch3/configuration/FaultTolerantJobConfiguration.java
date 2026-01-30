package com.linus.batch.springbatch3.configuration;

import com.linus.batch.components.chunk.SampleProcessor;
import com.linus.batch.components.chunk.SampleReader;
import com.linus.batch.components.chunk.SampleWriter;
import com.linus.batch.components.listener.JobListener;
import com.linus.batch.components.tasklet.SleepTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.NeverSkipItemSkipPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class FaultTolerantJobConfiguration {
    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job faultTolerantJob() throws Exception {
        Step step1 = new StepBuilder("step1", jobRepository).tasklet(new SleepTasklet(), transactionManager).build();

        SampleReader reader = new SampleReader();
        SampleProcessor processor = new SampleProcessor();
        SampleWriter writer = new SampleWriter();

        RepeatTemplate repeatTemplate = new RepeatTemplate();
//        repeatTemplate.setTaskExecutor(threadPoolTaskExecutor());

        Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<Class<? extends Throwable>, Boolean>(1);
        skippableExceptions.put(Exception.class, true);
        Step step2 = new StepBuilder("step2", jobRepository).<String, String>chunk(10, transactionManager)
                .faultTolerant()
                .retry(Exception.class)
//                .skipPolicy(new LimitCheckingItemSkipPolicy(1, skippableExceptions))
                .backOffPolicy(new NoBackOffPolicy())
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .reader(reader).processor(processor).writer(writer)
                .listener(writer)
                .taskExecutor(threadPoolTaskExecutor())
                .stepOperations(repeatTemplate)
                .build();

        Step step3 = new StepBuilder("step3", jobRepository).tasklet(new SleepTasklet(), transactionManager).build();
        
        return new JobBuilder("faultTolerantJob", jobRepository)
                .listener(new JobListener())
                .incrementer(new RunIdIncrementer()).start(step1).next(step2).next(step3).build();
    }

    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(10);
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
