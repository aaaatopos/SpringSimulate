package com.xzt.simulate.service;

import com.xzt.simulate.spring.BeanPostProcessor;
import com.xzt.simulate.spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author xzt
 * @version 1.0
 * AOP
 */

@Component
public class xztBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        if(beanName.equals("userService"))
            System.out.println("初始化之前执行");
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        // 使用JDK动态代理，需要保证代理的类有实现的接口
        if(beanName.equals("userService")) {
            // 创建一个代理对象，JDK动态代理
            Object proxyInstance = Proxy.newProxyInstance(xztBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("切面逻辑");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
