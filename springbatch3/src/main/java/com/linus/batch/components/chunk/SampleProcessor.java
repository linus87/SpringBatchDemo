package com.linus.batch.components.chunk;

import org.springframework.batch.item.ItemProcessor;

public class SampleProcessor implements ItemProcessor<String, String> {

    public String process(String item) throws Exception {
        return item;
    }

}