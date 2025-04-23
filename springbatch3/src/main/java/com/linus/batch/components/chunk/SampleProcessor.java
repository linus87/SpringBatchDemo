package com.linus.batch.components.chunk;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.dao.QueryTimeoutException;

public class SampleProcessor implements ItemProcessor<String, String> {

    public String process(String item) throws Exception {
        
        System.out.println(String.format("%s: Process: %s", Thread.currentThread().getName(), item));
        
//        if ("a".equalsIgnoreCase(item) || "kind".equalsIgnoreCase(item)) {
//            throw new QueryTimeoutException("Timeout");
//        }
        
        return item;
    }

}