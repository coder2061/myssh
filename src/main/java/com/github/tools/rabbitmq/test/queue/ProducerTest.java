package com.github.tools.rabbitmq.test.queue;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 生产者 - 发送消息
 * 
 * @author jiangyf
 * @date 2017年9月5日 下午6:01:39
 */
public class ProducerTest {
	private final static int PREFETCH_COUNT = 1;
	// 队列名称
	private final static String QUEUE_NAME = "hello";

	public static void main(String[] argv) throws IOException {
		producing("hello word");
	}

	/**
	 * 发送消息
	 * 
	 * @param message
	 *            待发送的消息
	 * @throws IOException
	 */
	public static void producing(String message) throws IOException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明一个队列
		channel.queueDeclare(PREFETCH_COUNT, QUEUE_NAME, false);
		// 往队列中发布一条消息
		channel.basicPublish(PREFETCH_COUNT, "", QUEUE_NAME, null, message.getBytes());
		System.out.println(" [x] Sent '" + message + "'");
		// 关闭频道和连接
		// channel.close(PREFETCH_COUNT, QUEUE_NAME);
		// connection.close(PREFETCH_COUNT, QUEUE_NAME);
	}

}
