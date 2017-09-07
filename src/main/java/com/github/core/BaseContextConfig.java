package com.github.core;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.github.service.impl.UserServiceImpl;

/**
 * 
 * @author jiangyf
 * @date 2017年8月23日 上午11:27:06
 */
public class BaseContextConfig {

	public static void main(String[] args) {
		// 加载spring上下文
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:spring-context.xml");
		// 获取bean
		UserServiceImpl userServiceImpl = context.getBean(UserServiceImpl.class);
		userServiceImpl.login();
		// 关闭上下文
		context.close();
	}

}
