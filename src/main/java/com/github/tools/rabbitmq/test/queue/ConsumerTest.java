package com.github.tools.rabbitmq.test.queue;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 消费者 - 接收消息
 * 
 * @author jiangyf
 * @date 2017年9月5日 下午6:01:55
 */
public class ConsumerTest {
	// 开启公平转发（Fair dispatch,提醒RabbitMQ不要在同一时间给一个消费者超过一条消息，只有在消费者空闲的时候会发送下一条信息，消息没被分发，支持动态增加消费者）
	private final static int PREFETCH_COUNT = 1;
	// 队列名称
	private final static String QUEUE_NAME = "hello";

	public static void main(String[] argv) throws IOException, InterruptedException, ShutdownSignalException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明队列，主要为了防止消息接收者先运行此程序，队列还不存在时创建队列。
		channel.queueDeclare(PREFETCH_COUNT, QUEUE_NAME, false);
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		// 创建队列消费者
		QueueingConsumer consumer = new QueueingConsumer(channel);
		// 指定消费队列
		channel.basicConsume(PREFETCH_COUNT, QUEUE_NAME, true, consumer);
		while (true) {
			// nextDelivery是一个阻塞方法（内部实现其实是阻塞队列的take方法）
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			System.out.println(" [x] Received '" + message + "'");
		}

	}
}
