### 此包中存放平时调用spring的业务代码
- `Test` 模拟Spring项目中的main方法
- `userService` 模拟Spring项目中的某个业务代码
- `OrderService` 模拟Spring项目中的某个业务代码
- `AppConfig` 模拟Spring项目中的配置类
- `xztBeanPostProcessor`实现`BeanPostProcessor`接口实现AOP
- `UserInterface`接口，因为JDK动态代理需要实现一个接口，所以给UserService提供了一个接口