### 此包中存放模拟spring框架的代码
- `SimulateApplicationContext` 模拟`SpringApplicationContext`
- `ComponentScan` 模拟`ComponentScan`注解
- `Component` 模拟生成Bean注解
- `Autowired` 模拟Autowired注解，进行依赖注入
- `BeanDefinition` 指明当前Bean的类型以及单例/多例
- `Scope` 模拟 Bean类型单例/多例指定
- `BeanNameAware` 模拟Aware回调方法接口
- `InitializingBean` 模拟属性初始化的接口
- `BeanPostProcessor` 模拟`BeanPostProcessor`接口实现AOP


### 手写模拟Spring内容：
- 容器启动
- `BeanDefinition`扫描
- Bean的生命周期
- 单例与多例Bean
- 依赖注入 `Autowired`
- AOP
- Aware回调
- 初始化
- `BeanPostProcessor`

### Bean的创建的生命周期
```text
UserService.class --> 推断构造方法 --> 普通对象 --> 依赖注入 --> 
初始化前(@PostConstruct或者实现InitializingBean接口) --> 初始化 --> 初始化后(AOP) --> 
代理对象 --> 放入Map(单例池) --> Bean对象
```
#### 推断构造方法
默认使用无参构造方法\
有多个构造方法时，使用默认的无参构造方法，没有无参构造方法则会报错。\
需要使用某个构造方法时，则要在该构造方法上使用`@Autowired`\
仅有一个有参构造方法，并且参数是一个Bean，则会直接使用，
使用时会去`Map<beanName, Bean对象>`中去查找，没有则创建（单例情况下），多例情况下则直接创建。\
先byType再byName

#### 依赖注入
也是先byType再byName然后从Map单例池中查找

#### AOP
如果实现了一个接口，则会使用JDK动态代理，否则会使用CGLIB动态代理\
- CGLIB：其实就是父子类的原理，代理类继承需要代理的类；\
UserServiceProxy对象 --> UserService对象 --> UserService对象.target=普通对象
```java
class UserServiceProxy extends UserService {
    
    UserService target;

    // 重写 代理方法
    public void test() {
        // 切面逻辑@Before
        // @Transactional
        // 开启事务
        // 1.事务管理器新建一个数据库连接conn TreadLocal<Map<DataSource, conn>>
        // 2.conn.autocommit = false;
        
        target.test();
        
        // conn.commit(); conn.rollback();
        // 切面逻辑@After
    }
}
```

#### Spring循环依赖
**什么是循环依赖？**\
简单示例：
```text
AService的Bean的生命周期:
1.实例化-->普通对象
2.依赖注入：填充bService --> Map单例池找 --> 创建BService
    BService的Bean的生命周期：
    1.实例化 --> 普通对象
    2.依赖注入：填充aService --> Map单例池找 --> 创建AService
    3.填充其他属性
    4.做一些其他的事情（AOP）
    5.添加到单例池
3.填充其他属性
4.做一些其他的事情（AOP）
5.添加到单例池
```
在上述过程中就存在循环依赖的问题。

**循环依赖的解决办法**\
Spring中会使用**三级缓存**来解决循环依赖
- 第一级缓存：singletonObjects ---> 单例池Map<beanName, Bean对象>
- 第二级缓存：earlySingletonObjects ---> Map<beanName, 提前AOP的代理对象>
  - 主要用来保存在出现了循环依赖情况下，提前AOP生成的代理对象。同时保持单例。
- 第三级缓存：singletonFactories ---> 也是一个Map<beanName, () -> getEarlyBeanReference(beanName, mbd, 普通对象)>
  - 来打破循环，保存一个Lambda表达式，执行Lambda表达式后会得到一个普通对象。

若存在循环依赖，则需要提前进行AOP，来生成代理对象。\
```text
AService的Bean的生命周期:  --*--> 是为了解决循环依赖的步骤
1.实例化-->AService普通对象 --*--> singletonFactories.put(Bean, 普通对象)（三级缓存）
2.依赖注入：填充bService --> Map单例池找 --> 创建BService
    BService的Bean的生命周期：
    2.1.实例化 --> 普通对象 --*--> singletonFactories.put(Bean, 普通对象)
    2.2.依赖注入：填充aService --> Map单例池找 --*--> 循环依赖？ 
        --*--> 去earlySingletonObjects中查找 --*没找到--> singletonFactories
        --*--> 拿到lambda表达式 --*--> 提前进行AOP --*--> AService代理对象 --*--> 加入Map
    2.3.填充其他属性
    2.4.做一些其他的事情（AOP）
    2.5.添加到单例池
2.依赖注入：填充cService --> Map单例池找 --> 创建CService
    BService的Bean的生命周期：
    2.1.实例化 --> 普通对象 --*--> singletonFactories.put(Bean, 普通对象)
    2.2.依赖注入：填充aService --> Map单例池找 --*--> 循环依赖？ 
        --*--> 去earlySingletonObjects中查找 --*找到--> 直接get
    2.3.填充其他属性
    2.4.做一些其他的事情（AOP）
    2.5.添加到单例池
3.填充其他属性
4.做一些其他的事情（AOP） --> AService代理对象
    这里如果提前进行过AOP了，则不会在进行AOP，通过earlyProxyReferences来控制的。
4.5 earlySingletonObjects.get("AService");
5.添加到单例池
4.creatingSet.remove<'AService'>
```



