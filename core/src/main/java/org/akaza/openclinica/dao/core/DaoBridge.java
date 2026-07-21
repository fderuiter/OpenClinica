package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.core.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

public class DaoBridge {
    public static <T> T getDao(Class<T> clazz) {
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        if (context == null) {
            throw new IllegalStateException("ApplicationContext is not initialized");
        }
        return context.getBean(clazz);
    }
}
