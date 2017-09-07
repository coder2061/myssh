package com.github.tools.rabbitmq.test.workqueue;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 工作者 - 接收处理任务
 * 
 * @author jiangyf
 * @date 2017年9月5日 下午6:01:55
 */
public class WorkerTest {
	// 开启公平转发（Fair dispatch，提醒RabbitMQ不要在同一时间给一个消费者超过一条消息，只有在消费者空闲的时候会发送下一条信息）
	private final static int PREFETCH_COUNT = 1;
	// 队列名称
	private final static String QUEUE_NAME = "workqueue";

	public static void main(String[] argv) throws IOException, InterruptedException, ShutdownSignalException {
		// 区分不同工作进程的输出
		int hashCode = WorkerTest.class.hashCode();
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明队列，主要为了防止消息接收者先运行此程序，队列还不存在时创建队列。
		channel.queueDeclare(PREFETCH_COUNT, QUEUE_NAME, false);
		System.out.println(hashCode + " [*] Waiting for messages. To exit press CTRL+C");
		QueueingConsumer consumer = new QueueingConsumer(channel);
		// false指开启消息应答（message acknowledgments，保证消息不会因为消费者被杀死而导致消息丢失，没有处理完成消息会重新转发给别的消费者）
		boolean ack = false;
		// 指定消费队列
		channel.basicConsume(PREFETCH_COUNT, QUEUE_NAME, ack, consumer);
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			System.out.println(hashCode + " [x] Received '" + message + "'");
			delTask(message);
			System.out.println(hashCode + " [x] '" + message + "' Done");
			// 开启消息应答机制，需要在每次处理完成一个消息后，手动发送一次应答
			channel.basicAck(1000, false);
		}
	}

	/**
	 * 处理任务，每个点耗时1s
	 * 
	 * @param task
	 * @throws InterruptedException
	 */
	private static void delTask(String task) throws InterruptedException {
		for (char ch : task.toCharArray()) {
			if (ch == '.')
				Thread.sleep(1000);
		}
	}
}
