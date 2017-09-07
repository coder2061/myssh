package com.github.tools.rabbitmq.test.pubsub;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 发送者-发送消息
 * 
 * 发布/订阅：把一个消息发给多个消费者
 * 
 * 转发器（Exchanges）：生产者将消息发送至转发器，转发器决定将消息发送至哪些队列，消费者绑定队列获取消息。
 * 
 * @author jiangyf
 * @date 2017年9月7日 上午11:01:51
 */
public class SenderTest {
	// 转发器
	private final static String EXCHANGE_NAME = "ex_log";

	public static void main(String[] args) throws IOException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明转发器和类型
		channel.exchangeDeclare(1, EXCHANGE_NAME, "fanout");

		String message = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " : log something";
		// 往转发器上发布消息
		channel.basicPublish(1, EXCHANGE_NAME, "", null, message.getBytes());

		System.out.println(" [x] Sent '" + message + "'");

		// 关闭频道和资源
		// channel.close();
		// connection.close();
	}

}