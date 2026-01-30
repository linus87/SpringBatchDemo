package com.linus.batch.components.listener;

import com.linus.batch.components.chunk.SampleProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class JobListener implements JobExecutionListener {
  private final Logger log = LoggerFactory.getLogger(JobListener.class);

  public void afterJob(JobExecution jobExecution) {
    log.info("Job completed. Performing cleanup in SampleWriter.");
    log.info(jobExecution.toString());
    // Add any necessary cleanup logic here
  }
}
