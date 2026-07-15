package org.akaza.openclinica.web.filter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HttpServletRequestAdapter implements InvocationHandler {
    private final jakarta.servlet.http.HttpServletRequest delegate;

    private HttpServletRequestAdapter(jakarta.servlet.http.HttpServletRequest delegate) {
        this.delegate = delegate;
    }

    public static javax.servlet.http.HttpServletRequest adapt(jakarta.servlet.http.HttpServletRequest request) {
        if (request == null) return null;
        return (javax.servlet.http.HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequestAdapter.class.getClassLoader(),
                new Class<?>[]{javax.servlet.http.HttpServletRequest.class},
                new HttpServletRequestAdapter(request)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Attempt to find the matching method on the Jakarta delegate
        try {
            Method delegateMethod = delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
            return delegateMethod.invoke(delegate, args);
        } catch (NoSuchMethodException e) {
            // For simple cases in JMesa, it mostly calls getParameter, getAttribute, etc.
            // If it calls something that expects a javax.servlet object back, we might get an error,
            // but JMesa typically just uses basic string/object methods.
            throw new UnsupportedOperationException("Method not adapted: " + method.getName(), e);
        }
    }
}
