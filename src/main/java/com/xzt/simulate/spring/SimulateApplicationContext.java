package com.xzt.simulate.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xzt
 * @version 1.0
 * 模拟SpringApplicationContext Spring容器
 */
public class SimulateApplicationContext {
    private Class configClass;

    /**
     * 保存所有beanName对应的Bean的类型以及单例/多例
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    /**
     * 单例池：保证创建的Bean是单例的
     */
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /**
     * 存储所有实现了BeanPostProcessor接口的类
     */
    private ArrayList<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * 构造函数
     *
     * @param configClass 传入需要加载的配置文件
     */
    public SimulateApplicationContext(Class configClass) {
        this.configClass = configClass;
        // 启动Spring容器后，首先需要扫描 然后创建单例Bean

        // 1.扫描 - 得到所有的Bean对象 BeanDefinition，并保存到beanDefinitionMap中。
        // 判断给定的配置类上有没有ComponentScan注解
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            // 有ComponentScan注解，直接拿到注解中的扫描路径
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();  // 扫描路径-包名：com.xzt.simulate.service

            path = path.replace(".", "/");  // 相对路径：com/xzt/simulate/service

            // 扫描target下path路径中的资源
            // D:\Desktop\SpringSmulate\target\classes\com\xzt\simulate\service
            ClassLoader classLoader = SimulateApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);

            // D:\Desktop\SpringSmulate\target\classes\com\xzt\simulate\service
            File file = new File(resource.getFile());

            if (file.isDirectory()) {  // 是一个文件夹
                File[] files = file.listFiles();  // 获取到file目录下的所有文件
                for (File f : files) { // 遍历所有文件，过滤掉不是.class的文件
                    String fileName = f.getAbsolutePath();

                    if (fileName.endsWith(".class")) {  // 当前文件是否以.class结尾
                        // com\\xzt\\simulate\\userService   下面的写法是写死了，需要改写 TODO
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        // com.xzt.simulate.userService
                        className = className.replace("\\", ".");

                        // 需要使用反射判断.class文件对应的类上是否有Component注解
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {

                                if(BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    // clazz 实现了BeanPostProcessor接口
                                    BeanPostProcessor instance = (BeanPostProcessor) clazz.newInstance();
                                    beanPostProcessorList.add(instance);
                                }

                                // 1.获取beanName
                                Component componentAnnotation = clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();

                                if(beanName.equals("")) { // 没有给定beanName，生成一个beanName
                                    // 使用Introspector.decapitalize()函数 首字母会小写
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                // 2.生成一个BeanDefinition对象
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)) { // 是否存在Scope注解
                                    // 获取Scope注解的value属性的值
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    String scopeType = scopeAnnotation.value();
                                    beanDefinition.setScope(scopeType);
                                } else {
                                    beanDefinition.setScope("singleton");
                                }

                                beanDefinitionMap.put(beanName, beanDefinition);

                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        // 2.实例化单例Bean - 创建单例Bean对象 - 并保存在singletonObjects中
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);  // 加入单例池
            }
        }
    }

    /**
     * 创建Bean对象
     * 会模拟bean的生命周期
     * @param beanName bean对象对应的beanName
     * @param beanDefinition beanName对应的beanDefinition
     * @return
     */
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        // 获取需要创建的bean对象的类型
        Class clazz = beanDefinition.getType();

        try {
            // 1.实例化，得到一个Bean对象
            // 通过反射调用构造方法创建对象 - 有个前提，需要保证该类中有无参构造函数
            Object instance  = clazz.getConstructor().newInstance();

            // 2.进行依赖注入
            // 遍历当前类的所有属性Field
            for (Field f : clazz.getDeclaredFields()) {
                if(f.isAnnotationPresent(Autowired.class)) {
                    // 属性上存在Autowired注解
                    f.setAccessible(true);  // 开启给属性赋值的权限
                    f.set(instance, getBean(f.getName()));  // byName 通过属性的名字获得bean对象, 存在问题 TODO
                }
            }

            // 3.回调 - Aware回调
            if(instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // 初始化之前执行方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
            }

            // 4.初始化
            if(instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // 5.初始化后执行方法 BeanPostProcessor
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            }

            return instance;

        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 模拟application容器中的getBean函数
     *
     * @param beanName Bean的名字
     * @return Bean对象
     */
    public Object getBean(String beanName) {
        // 需要根据beanName来找到对应的类，并且需要判断是单例还是多例
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanName == null) {  // 不存在beanName对应的bean
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if(scope.equals("singleton")) {
                // 单例
                Object bean = singletonObjects.get(beanName);
                if(bean == null) {
                    // 没有创建出来bean时，需要创建bean并加入单例池中。
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            } else {
                // 多例, 每一次都创建一个Bean
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
