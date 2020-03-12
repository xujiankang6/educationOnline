package com.bdqn.jiankang.config;


import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    //密码验证器
    @Bean("credentialsMatcher")
    public CredentialsMatcher credentialsMatcher() {
        return new MyMatcher();
    }

    //权限验证器
    @Bean("myRealm")
    public MyRealm myRealm(@Qualifier("credentialsMatcher") CredentialsMatcher credentialsMatcher) {
        MyRealm myRealm = new MyRealm();
        //给权限验证器配置上自定义的密码验证器
        myRealm.setCredentialsMatcher(credentialsMatcher);
        return myRealm;
    }

    //桥梁，主要是Realm的管理认证配置
    @Bean("securityManager")
    public SecurityManager securityManager(@Qualifier("myRealm") MyRealm myRealm) {
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(myRealm);
        //自定义缓存实现，使用redis
//        defaultWebSecurityManager.setSessionManager(SessionManager());
        return defaultWebSecurityManager;
    }

    //进行全局配置，Filter工厂，设置对应的过滤条件和跳转条件
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") SecurityManager securityManager) {
        //shiro对象
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);
        bean.setLoginUrl("/login");
        bean.setSuccessUrl("/index");


       /* //自定义拦截器
        Map<String, Filter> filterMap=new LinkedHashMap<String,Filter>();
        //限制同一账号同时在线的个数
//        filterMap.put("kickout",kickoutSessionControlFilter());
        bean.setFilters(filterMap);*/
        //MAP
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
        /*
        认证顺序是从上往下执行。
         */
        linkedHashMap.put("/logout", "logout");//在这儿配置登出地址，不需要专门去写控制器。
        linkedHashMap.put("/static/**", "anon");
        //开启注册页面不需要权限
        linkedHashMap.put("/register","anon");
        linkedHashMap.put("/saveregister","anon");

        //验证phone唯一
        linkedHashMap.put("/solephone","anon");
        //获取验证码
        linkedHashMap.put("/getcode","anon");
        //验证码判断
        linkedHashMap.put("/comparecode","anon");
        linkedHashMap.put("/websocket","anon");//必须开启。
        linkedHashMap.put("/css/**", "anon");//不需要验证
        linkedHashMap.put("/js/**", "anon");//不需要验证
        //配置错误页面
        linkedHashMap.put("error","anon");//不需要验证
        linkedHashMap.put("/img/**", "anon");//不需要验证
        linkedHashMap.put("/layui/**", "anon");//不需要验证
        linkedHashMap.put("/video/**", "anon");//不需要验证
        linkedHashMap.put("/bower_components/**", "anon");//不需要验证
        linkedHashMap.put("/plugins/**", "anon");//不需要验证
        linkedHashMap.put("/dist/**", "anon");//不需要验证
        linkedHashMap.put("/**", "authc");//需要进行权限验证
        bean.setFilterChainDefinitionMap(linkedHashMap);
        return bean;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }



    //加入·注解的使用，不加入这个注解不生效
    //启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
    //     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)即可实现此功能
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager") SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor sourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        sourceAdvisor.setSecurityManager(securityManager);
        return sourceAdvisor;
    }


    //    进行权限认证的，没有会使得前台的shiro标签无法使用
    //shiro结合thymeleaf实现细粒度权限控制
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }


}



