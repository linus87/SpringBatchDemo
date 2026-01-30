package com.linus.batch.springbatch3.configuration;

import com.linus.batch.components.chunk.prod.Campaign;
import com.linus.batch.components.chunk.prod.CampaignProcessor;
import com.linus.batch.components.chunk.prod.CampaignReader;
import com.linus.batch.components.chunk.prod.CampaignWriter;
import com.linus.batch.components.chunk.sample.SampleProcessor;
import com.linus.batch.components.chunk.sample.SampleReader;
import com.linus.batch.components.chunk.sample.SampleWriter;
import com.linus.batch.components.listener.JobListener;
import com.linus.batch.components.tasklet.SleepTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * A job configuration that defines a fault-tolerant job with three steps.
 * Fault tolerant can only run in single thread mode.
 */
@Configuration
public class CampaignFaultTolerantJobConfiguration {
    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job campaignFaultTolerantJob() throws Exception {
        Step step1 = new StepBuilder("step1", jobRepository).tasklet(new SleepTasklet(), transactionManager).build();

        CampaignReader reader = new CampaignReader();
        CampaignProcessor processor = new CampaignProcessor();
        CampaignWriter writer = new CampaignWriter();

        RepeatTemplate repeatTemplate = new RepeatTemplate();

        Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<Class<? extends Throwable>, Boolean>(1);
        skippableExceptions.put(Exception.class, true);
        Step step2 = new StepBuilder("step2", jobRepository).<Campaign, Campaign>chunk(10, transactionManager)
                .faultTolerant()
                .retry(Exception.class)
//                .skipPolicy(new LimitCheckingItemSkipPolicy(1, skippableExceptions))
                .backOffPolicy(new NoBackOffPolicy())
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .reader(reader).processor(processor).writer(writer)
                .listener(writer)
                .stepOperations(repeatTemplate)
                .build();

        Step step3 = new StepBuilder("step3", jobRepository).tasklet(new SleepTasklet(), transactionManager).build();
        
        return new JobBuilder("campaignFaultTolerantJob", jobRepository)
                .listener(new JobListener())
                .incrementer(new RunIdIncrementer()).start(step1).next(step2).next(step3).build();
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
