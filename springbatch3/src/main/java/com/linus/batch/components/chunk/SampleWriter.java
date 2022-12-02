package com.linus.batch.components.chunk;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.QueryTimeoutException;

public class SampleWriter implements ItemWriter<String> {

    public void write(List<? extends String> items) throws Exception {
        for (String item : items) {
            System.out.println(String.format("Thread %d: Writer: %s", Thread.currentThread().getId(), item));
            if ("kind".equalsIgnoreCase(item)) {
                throw new QueryTimeoutException("Timeout");
            }
        }
    }

}
