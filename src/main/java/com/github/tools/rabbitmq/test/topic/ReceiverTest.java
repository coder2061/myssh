package com.github.tools.rabbitmq.test.topic;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 接收者-接收消息
 * 
 * @author jiangyf
 * @date 2017年9月7日 上午11:02:27
 */
public class ReceiverTest {
	// 转发器
	private final static String EXCHANGE_NAME = "topic_logs";

	public static void main(String[] argv)
			throws java.io.IOException, java.lang.InterruptedException, ShutdownSignalException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明转发器和类型
		channel.exchangeDeclare(1, EXCHANGE_NAME, "topic");
		// 创建一个非持久的、唯一的且自动删除的临时队列
		String queueName = channel.queueDeclare(1).getQueue();
		System.out.println(" [*] queueName " + queueName);
		// 绑定键（*可以匹配一个标识符，#可以匹配0个或多个标识符）
		String bindingKey = "kernal.*";
		// 为转发器绑定队列，可理解为队列对该转发器上的消息感兴趣
		channel.queueBind(1, queueName, EXCHANGE_NAME, bindingKey);

		System.out.println(" [*] Waiting for messages about kernel. To exit press CTRL+C");
		// 创建消费者
		QueueingConsumer consumer = new QueueingConsumer(channel);
		// 指定接收者，第二个参数为自动应答，无需手动应答
		channel.basicConsume(1, queueName, true, consumer);
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			String routingKey = delivery.getEnvelope().getRoutingKey();
			System.out.println(" [x] Received routingKey = " + routingKey + ",msg = " + message + ".");
		}
	}
}