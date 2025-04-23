package com.linus.batch.components.chunk;

import java.util.List;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.dao.QueryTimeoutException;

public class SampleWriter implements ItemWriter<String> {

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        for (String item : chunk) {
            System.out.println(String.format("%s: Writer: %s", Thread.currentThread().getName(), item));
//            if ("kind".equalsIgnoreCase(item)) {
//                throw new QueryTimeoutException("Timeout");
//            }
        }
    }
}
