package com.github.tools.rabbitmq.test.routing;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 发送者-发送消息
 * 
 * 接收者可选择性订阅感兴趣类型的消息。即发送消息时可以设置routing_key，接收队列与转发器间可以设置binding_key，
 * 接收者接收与binding_key与routing_key相同的消息
 * 
 * 
 * @author jiangyf
 * @date 2017年9月7日 上午11:01:51
 */
public class SenderTest {
	// 转发器
	private final static String EXCHANGE_NAME = "ex_logs_direct";
	// 日志级别
	private static final String[] LOG_LEVELS = { "debug", "info", "warning", "error" };

	public static void main(String[] args) throws IOException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明转发器和类型
		channel.exchangeDeclare(1, EXCHANGE_NAME, "direct");
		// 多重绑定
		for (int i = 0; i < 10; i++) {
			// 路由选择键，这里作为日志级别
			String routingKey = getLogLevel();
			String message = routingKey + "_log :" + UUID.randomUUID().toString();
			// 发布消息至转发器，指定routingkey
			channel.basicPublish(1, EXCHANGE_NAME, routingKey, null, message.getBytes());
			System.out.println(" [x] Sent '" + message + "'");
		}

		// 关闭频道和资源
		// channel.close();
		// connection.close();
	}

	/**
	 * 随机获取一种日志类型
	 * 
	 * @return
	 */
	private static String getLogLevel() {
		int ranVal = new Random().nextInt(LOG_LEVELS.length);
		return LOG_LEVELS[ranVal];
	}

}