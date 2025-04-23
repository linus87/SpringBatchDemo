package com.linus.batch.springbatch3.configuration;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.batch.JobLauncherApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.StringUtils;

@Configuration
public class BatchConfiguration {
    
    /**
     * Create a datasource required by spring batch to store job context.
     */
//    @Bean
//    @BatchDataSource
//    public DataSource getDataSource() {
//        BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setUrl("jdbc:hsqldb:mem:jobRepo;sql.enforce_strict_size=true");
//        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
//        dataSource.setUsername("sa");
//        dataSource.setPassword("");
//        return dataSource;
//    }

//    @Bean
//    @ConditionalOnMissingBean
//    @ConditionalOnProperty(
//            prefix = "spring.batch.job",
//            name = {"enabled"},
//            havingValue = "true",
//            matchIfMissing = true
//    )
//    public JobLauncherApplicationRunner jobLauncherApplicationRunner(JobLauncher jobLauncher, JobExplorer jobExplorer, JobRepository jobRepository, BatchProperties properties) {
//        JobLauncherApplicationRunner runner = new JobLauncherApplicationRunner(jobLauncher, jobExplorer, jobRepository);
//        String jobName = properties.getJob().getName();
//        if (StringUtils.hasText(jobName)) {
//            runner.setJobName(jobName);
//        }
//
//        return runner;
//    }

}
