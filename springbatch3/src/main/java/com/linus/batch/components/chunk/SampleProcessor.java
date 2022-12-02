package com.linus.batch.components.chunk;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.dao.QueryTimeoutException;

public class SampleProcessor implements ItemProcessor<String, String> {

    public String process(String item) throws Exception {
        
        System.out.println(String.format("Thread %d: Process: %s", Thread.currentThread().getId(), item));
        
//        if ("a".equalsIgnoreCase(item) || "kind".equalsIgnoreCase(item)) {
//            throw new QueryTimeoutException("Timeout");
//        }
        
        return item;
    }

}