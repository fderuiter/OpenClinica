package org.akaza.openclinica.modern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class DataSourceWrapperConfig {

    @Bean
    public BeanPostProcessor dataSourceWrapper() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if ("dataSource".equals(beanName) && bean instanceof DataSource && !(bean instanceof TransactionAwareDataSourceProxy)) {
                    return new TransactionAwareDataSourceProxy((DataSource) bean);
                }
                return bean;
            }
        };
    }
}
