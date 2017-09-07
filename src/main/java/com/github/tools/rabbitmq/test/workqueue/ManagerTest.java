package com.github.tools.rabbitmq.test.workqueue;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * 负责人 - 发送任务
 * 
 * @author jiangyf
 * @date 2017年9月5日 下午6:01:55
 */
public class ManagerTest {
	private final static int PREFETCH_COUNT = 1;
	// 队列名称
	private final static String QUEUE_NAME = "workqueue";

	public static void main(String[] args) throws IOException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 开启消息持久化（Message durability，保证消息不会因为rabbitMQ退出或异常推出而丢失）
		boolean durable = true;
		// 声明队列，主要为了防止消息接收者先运行此程序，队列还不存在时创建队列。
		channel.queueDeclare(PREFETCH_COUNT, QUEUE_NAME, durable, false, false, false, null);
		// 发送messageNum条消息，依次在消息后面附加1-messageNum个点
		int messageNum = 10;
		for (int i = 0; i < messageNum; i++) {
			String dots = "";
			for (int j = 0; j <= i; j++) {
				dots += ".";
			}
			String message = "helloworld" + dots + dots.length();
			// 开启消息持久化，需要标识消息为持久化的，MessageProperties.PERSISTENT_TEXT_PLAIN
			channel.basicPublish(PREFETCH_COUNT, "", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN,
					message.getBytes());
			System.out.println(" [x] Sent '" + message + "'");
		}
		// 关闭频道和资源
		// channel.close();
		// connection.close();
	}

}