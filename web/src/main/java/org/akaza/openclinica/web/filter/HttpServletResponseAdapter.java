package org.akaza.openclinica.web.filter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseAdapter {
    public static javax.servlet.http.HttpServletResponse adapt(final HttpServletResponse response) {
        return (javax.servlet.http.HttpServletResponse) Proxy.newProxyInstance(
            HttpServletResponseAdapter.class.getClassLoader(),
            new Class<?>[] { javax.servlet.http.HttpServletResponse.class },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Method jakartaMethod = HttpServletResponse.class.getMethod(method.getName(), method.getParameterTypes());
                    return jakartaMethod.invoke(response, args);
                }
            }
        );
    }
}
