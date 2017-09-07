package com.github.tools.rabbitmq.test.topic;

import java.io.IOException;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 发送者-发送消息
 * 
 * 主题转发：能够基于多重条件进行路由选择。
 * 
 * @author jiangyf
 * @date 2017年9月7日 上午11:01:51
 */
public class SenderTest {
	// 转发器
	private final static String EXCHANGE_NAME = "topic_logs";

	public static void main(String[] args) throws IOException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明转发器和类型
		channel.exchangeDeclare(1, EXCHANGE_NAME, "topic");

		String[] routing_keys = new String[] { "kernal.info", "cron.warning", "auth.info",
				"kernel.critical" };
		for (String routing_key : routing_keys) {
			String msg = UUID.randomUUID().toString();
			channel.basicPublish(1, EXCHANGE_NAME, routing_key, null, msg.getBytes());
			System.out.println(" [x] Sent routingKey = " + routing_key + " ,msg = " + msg + ".");
		}

		// 关闭频道和资源
		// channel.close();
		// connection.close();
	}

}