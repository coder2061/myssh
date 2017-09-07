package com.github.entity;

import java.io.PrintStream;

/**
 * 用户
 * 
 * @author jiangyf
 * @date 2017年8月23日 上午11:15:05
 */
public class User {
	private String username;
	private String password;

	private PrintStream stream;

	public User(PrintStream stream) {
		System.out.println("-------init");
		this.stream = stream;
	}

	public void print() {
		System.out.println("-------print");
		stream.print("User [username=" + username + ", password=" + password + "]");
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
