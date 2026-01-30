package com.linus.batch.components.chunk.prod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class CampaignProcessor implements ItemProcessor<Campaign, Campaign> {
  private final Logger log = LoggerFactory.getLogger(CampaignProcessor.class);
  public Campaign process(Campaign item) throws Exception {

    log.info(String.format("%s: Process: %s", Thread.currentThread().getName(), item));

    if (Integer.parseInt(item.getId()) % 5 == 0) {
        log.error("Simulating process exception at item {}", item);
        throw new Exception("Simulated process exception at item " + item);
    }

//    Thread.sleep(100);

    return item;
  }

}