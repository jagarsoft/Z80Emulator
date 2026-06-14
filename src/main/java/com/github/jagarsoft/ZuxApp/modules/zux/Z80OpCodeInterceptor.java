package com.github.jagarsoft.ZuxApp.modules.zux;

import com.github.jagarsoft.Z80;
import com.github.jagarsoft.Z80OpCode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Z80OpCodeInterceptor {
    
    // Crear un proxy que intercepte solo el método que quieres cambiar
    public static Z80OpCode createInterceptor(Z80 originalZ80, String methodName, Runnable customImplementation) {
    return (Z80OpCode) Proxy.newProxyInstance(
            Z80OpCode.class.getClassLoader(),
            new Class[]{Z80OpCode.class},
            // lambda version
            // (proxy, method, args) -> {
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getName().equals(methodName)) {
                        // Ejecutar tu implementación personalizada
                        customImplementation.run();
                        return null;
                    } else {
                        // Delegar al objeto original
                        return method.invoke(originalZ80.clone(), args);
                    }
                }
            }
    );
    }
}
