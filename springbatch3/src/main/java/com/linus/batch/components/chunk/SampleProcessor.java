package com.linus.batch.components.chunk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.dao.QueryTimeoutException;

public class SampleProcessor implements ItemProcessor<String, String> {
  private final Logger log = LoggerFactory.getLogger(SampleProcessor.class);
  public String process(String item) throws Exception {

    log.info(String.format("%s: Process: %s", Thread.currentThread().getName(), item));

    if (Integer.parseInt(item) % 5 == 0) {
        log.error("Simulating process exception at item {}", item);
        throw new Exception("Simulated process exception at item " + item);
    }

//    Thread.sleep(100);

    return item;
  }

}