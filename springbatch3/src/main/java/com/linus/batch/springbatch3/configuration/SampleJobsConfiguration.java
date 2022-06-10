package com.linus.batch.springbatch3.configuration;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.linus.batch.components.tasklet.SleepTasklet;

@Configuration
public class SampleJobsConfiguration {
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job job() throws Exception {
	    Step step1 = stepBuilderFactory.get("step1").tasklet(new SleepTasklet()).build();
		return jobBuilderFactory.get("job1")
				.incrementer(new RunIdIncrementer()).start(step1).build();
	}
}
