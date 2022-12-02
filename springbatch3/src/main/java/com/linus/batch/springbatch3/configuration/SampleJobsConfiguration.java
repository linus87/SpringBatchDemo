package com.linus.batch.springbatch3.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.QueryTimeoutException;

import com.linus.batch.components.chunk.SampleProcessor;
import com.linus.batch.components.chunk.SampleReader;
import com.linus.batch.components.chunk.SampleWriter;
import com.linus.batch.components.tasklet.SleepTasklet;

@Configuration
public class SampleJobsConfiguration {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    
    
    @Bean
    public Job job(TaskExecutor taskExecutor) throws Exception {
        Step step1 = stepBuilderFactory.get("step1").tasklet(new SleepTasklet()).build();

        SampleReader reader = new SampleReader();
        SampleProcessor processor = new SampleProcessor();
        SampleWriter writer = new SampleWriter();

        Map<Class<? extends Throwable>, Boolean> skippableExceptions = new HashMap<Class<? extends Throwable>, Boolean>(1);
        skippableExceptions.put(QueryTimeoutException.class, true);
        Step step2 = stepBuilderFactory.get("step2").<String, String>chunk(10).faultTolerant().skipPolicy(new LimitCheckingItemSkipPolicy(1, skippableExceptions)).reader(reader).processor(processor).writer(writer).taskExecutor(taskExecutor).throttleLimit(2)
                .build();
        
//        Step step2 = stepBuilderFactory.get("step2").<String, String>chunk(10).faultTolerant().reader(reader).processor(processor).writer(writer)
//                .build();
        
        return jobBuilderFactory.get("job1").incrementer(new RunIdIncrementer()).start(step1).next(step2).build();
    }
}
