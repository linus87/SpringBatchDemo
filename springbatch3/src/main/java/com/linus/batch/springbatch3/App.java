package com.linus.batch.springbatch3;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Hello world!
 * 
 */
@SpringBootApplication
public class App {
	public static void main(String[] args) {
		System.out.println("Hello World!");
		System.exit(SpringApplication.exit(SpringApplication.run(
		        App.class, args)));
	}
}
