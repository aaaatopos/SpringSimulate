package com.xzt.simulate.service;

import com.xzt.simulate.spring.*;

/**
 * @author xzt
 * @version 1.0
 * 模拟Spring项目中的main函数。
 */
@Component("userService")
@Scope("prototype")  // 多例
public class UserService implements UserInterface {

    @Autowired
    private OrderService orderService;

    public void test() {
        System.out.println(orderService);
    }

}
