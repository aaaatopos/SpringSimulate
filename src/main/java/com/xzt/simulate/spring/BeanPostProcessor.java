package com.xzt.simulate.spring;

/**
 * @author xzt
 * @version 1.0
 * 模拟BeanPostProcessor接口
 * bean的后置处理器：主要帮助在bean实例化之后，初始化前后做一些事情。
 */
public interface BeanPostProcessor {
    public Object postProcessBeforeInitialization(String beanName, Object bean);
    public Object postProcessAfterInitialization(String beanName, Object bean);
}
