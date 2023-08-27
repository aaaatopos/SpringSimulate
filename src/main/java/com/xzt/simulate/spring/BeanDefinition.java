package com.xzt.simulate.spring;

/**
 * @author xzt
 * @version 1.0
 * 指明当前Bean的类型以及单例/多例
 */
public class BeanDefinition {
    private Class type;
    private String scope;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
