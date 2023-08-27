package com.xzt.simulate.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xzt
 * @version 1.0
 * 模拟Spring中ComponentScan注解
 */

@Retention(RetentionPolicy.RUNTIME)   // 指明注解生效的时间
@Target(ElementType.TYPE)  // 指明注解使用的位置
public @interface Component {
    String value() default "";  // 给当前Bean指定一个名字
}
