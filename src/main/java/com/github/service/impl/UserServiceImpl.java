package com.github.service.impl;

import com.github.entity.User;

/**
 * 用户
 * 
 * @author jiangyf
 * @date 2017年8月23日 上午11:13:29
 */
public class UserServiceImpl {

	private User user;

	public UserServiceImpl(User user) {
		this.user = user;
	}

	public void login() {
		user.print();
	}

}
