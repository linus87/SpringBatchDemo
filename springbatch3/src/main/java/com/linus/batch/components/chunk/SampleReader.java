package com.linus.batch.components.chunk;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class SampleReader implements ItemReader<String> {
    
    private String[] datasource = {"you", "are", "a", "kind", "man"};
    private volatile int currentIndex = 0;
    
    public synchronized String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (datasource.length > 0 && currentIndex < datasource.length) {
            String result = datasource[currentIndex++];
            System.out.println(String.format(String.format("%s: Reader: %s", Thread.currentThread().getName(), result)));
            return result;
        }
        
        return null;
    }

}