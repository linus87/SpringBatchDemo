package com.linus.batch.components.chunk.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

public class SampleReader implements ItemReader<String> {
  private final Logger log = LoggerFactory.getLogger(SampleReader.class);
  private String[] datasource = {
          "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
          "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"
          , "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"
          , "31", "32", "33", "34", "35", "36", "37", "38", "39", "40"
          , "41", "42", "43", "44", "45", "46", "47", "48", "49", "50"
          , "51", "52", "53", "54", "55", "56", "57", "58", "59", "60"
          , "61", "62", "63", "64", "65", "66", "67", "68", "69", "70"
          , "71", "72", "73", "74", "75", "76", "77", "78", "79", "80"
          , "81", "82", "83", "84", "85", "86", "87", "88", "89", "90"
          , "91", "92", "93", "94", "95", "96", "97", "98", "99", "100"
  };
  private volatile int currentIndex = 0;

  public synchronized String read() throws Exception {
    if (datasource.length > 0 && currentIndex < datasource.length) {
      String result = datasource[currentIndex++];
      if (currentIndex % 7 == 0) {
        log.error("Simulating read exception at index {}", currentIndex);
        throw new Exception("Simulated read exception at index " + currentIndex);
      }
      log.info(String.format(String.format("%s: Reader: %s", Thread.currentThread().getName(), result)));
      return result;
    }

    return null;
  }

}