package com.linus.batch.components.chunk.prod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class CampaignWriter implements ItemWriter<Campaign> {
  private final Logger log = LoggerFactory.getLogger(CampaignProcessor.class);

  @Override
  public void write(Chunk<? extends Campaign> chunk) throws Exception {
    log.warn(chunk.toString());
    for (Campaign item : chunk) {
      if (Integer.parseInt(item.getId()) % 3 == 0) {
        log.error("Simulating write exception at item {}", item);
        throw new Exception("Simulated write exception at item " + item);
      }
      log.info(String.format("%s: Writer: %s", Thread.currentThread().getName(), item));
    }
  }

  @AfterStep
  public void afterStep(StepExecution stepExecution) {
    log.info("Step completed. Performing cleanup in SampleWriter.");
    // Add any necessary cleanup logic here
    log.info(stepExecution.getSummary());
  }
}
