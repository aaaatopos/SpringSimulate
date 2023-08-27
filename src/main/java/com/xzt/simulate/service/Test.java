package com.xzt.simulate.service;

import com.xzt.simulate.spring.SimulateApplicationContext;

/**
 * @author xzt
 * @version 1.0
 */
public class Test {
    public static void main(String[] args) {
        // 1.加载Spring容器，并扫描配置文件
        SimulateApplicationContext applicationContext = new SimulateApplicationContext(AppConfig.class);
        // 2.通过getBean函数获取Bean对象
        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();
    }
}
