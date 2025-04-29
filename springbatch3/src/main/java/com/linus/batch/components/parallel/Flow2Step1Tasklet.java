package com.linus.batch.components.parallel;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class Flow2Step1Tasklet implements Tasklet {

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    // Your tasklet logic here
    for (int i = 0; i < 50; i++) {
      System.out.println("Executing Flow2: " + i);
      Thread.sleep(200); // Simulate some work
    }
    return RepeatStatus.FINISHED;
  }
}
