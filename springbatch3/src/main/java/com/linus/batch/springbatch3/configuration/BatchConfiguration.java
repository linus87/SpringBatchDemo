package com.linus.batch.springbatch3.configuration;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {
    
  /**
   * Create a datasource required by spring batch to store job context.
   * Use default one may lead to CannotAcquireLockException. But this one didn't set the isolation.
   */
  @Bean
  @BatchDataSource
  public DataSource getDataSource() {
    BasicDataSource dataSource = new BasicDataSource();
    dataSource.setUrl("jdbc:hsqldb:mem:jobRepo;sql.enforce_strict_size=true");
    dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  @Bean
  @ConditionalOnMissingBean({PlatformTransactionManager.class})
  public DataSourceTransactionManager transactionManager(Environment environment, @BatchDataSource DataSource dataSource, ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
    DataSourceTransactionManager transactionManager = this.createTransactionManager(environment, dataSource);
    return transactionManager;
  }

  private DataSourceTransactionManager createTransactionManager(Environment environment, DataSource dataSource) {
    return (DataSourceTransactionManager)((Boolean)environment.getProperty("spring.dao.exceptiontranslation.enabled", Boolean.class, Boolean.TRUE) ? new JdbcTransactionManager(dataSource) : new DataSourceTransactionManager(dataSource));
  }

}
