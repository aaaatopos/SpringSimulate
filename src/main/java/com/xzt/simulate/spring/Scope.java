package com.xzt.simulate.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xzt
 * @version 1.0
 * 模拟Spring中的Scope注解来指定一个类是单例还是多例
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {
    String value() default ""; // prototype 多例 singleton 单例
}
