package org.akaza.openclinica.web.filter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HttpServletResponseAdapter implements InvocationHandler {
    private final jakarta.servlet.http.HttpServletResponse delegate;

    private HttpServletResponseAdapter(jakarta.servlet.http.HttpServletResponse delegate) {
        this.delegate = delegate;
    }

    public static javax.servlet.http.HttpServletResponse adapt(jakarta.servlet.http.HttpServletResponse response) {
        if (response == null) return null;
        return (javax.servlet.http.HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponseAdapter.class.getClassLoader(),
                new Class<?>[]{javax.servlet.http.HttpServletResponse.class},
                new HttpServletResponseAdapter(response)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Method delegateMethod = delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
            return delegateMethod.invoke(delegate, args);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("Method not adapted: " + method.getName(), e);
        }
    }
}
