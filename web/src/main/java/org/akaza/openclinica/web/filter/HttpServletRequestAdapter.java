package org.akaza.openclinica.web.filter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import jakarta.servlet.http.HttpServletRequest;

public class HttpServletRequestAdapter {
    public static javax.servlet.http.HttpServletRequest adapt(final HttpServletRequest request) {
        return (javax.servlet.http.HttpServletRequest) Proxy.newProxyInstance(
            HttpServletRequestAdapter.class.getClassLoader(),
            new Class<?>[] { javax.servlet.http.HttpServletRequest.class },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Method jakartaMethod = HttpServletRequest.class.getMethod(method.getName(), method.getParameterTypes());
                    return jakartaMethod.invoke(request, args);
                }
            }
        );
    }
}
