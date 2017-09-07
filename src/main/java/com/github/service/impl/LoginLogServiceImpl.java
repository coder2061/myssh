package com.github.service.impl;

import java.io.PrintStream;

/**
 * 登录日志
 * 
 * @author jiangyf
 * @date 2017年8月23日 下午6:00:51
 */
public class LoginLogServiceImpl {

	private PrintStream stream;

	public LoginLogServiceImpl(PrintStream stream) {
		this.stream = stream;
	}

	public void beforeLogin() {
		System.out.println("-----登录前1");
		stream.println("-----登录前");
	}

	public void afterLogin() {
		System.out.println("-----登录后2");
		stream.println("-----登录后");
	}

}
