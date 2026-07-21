package org.akaza.openclinica.modern;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class LegacyHibernateBeanRemover implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (registry.containsBeanDefinition("transactionManager")) {
            registry.removeBeanDefinition("transactionManager");
        }
        
        org.springframework.beans.factory.support.RootBeanDefinition tmDef = new org.springframework.beans.factory.support.RootBeanDefinition(org.springframework.orm.jpa.JpaTransactionManager.class);
        registry.registerBeanDefinition("transactionManager", tmDef);
        
        if (registry.containsBeanDefinition("sessionFactory")) {
            registry.removeBeanDefinition("sessionFactory");
        }
        
        org.springframework.beans.factory.support.RootBeanDefinition sfDef = new org.springframework.beans.factory.support.RootBeanDefinition();
        sfDef.setFactoryBeanName("hibernateListenerConfig");
        sfDef.setFactoryMethodName("sessionFactory");
        registry.registerBeanDefinition("sessionFactory", sfDef);

        String[] beansToRemove = {
            "hibernateTemplate",
            "sharedTransactionTemplate",
            "liquibase",
            "schedulerFactoryBean",
            "notificationOutboxJobDetail",
            "notificationOutboxTrigger",
            "jaxb2Marshaller"
        };
        
        for (String beanName : beansToRemove) {
            if (registry.containsBeanDefinition(beanName)) {
                registry.removeBeanDefinition(beanName);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Nothing to do here
    }
}
