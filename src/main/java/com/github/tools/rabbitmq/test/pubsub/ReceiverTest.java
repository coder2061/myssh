package com.github.tools.rabbitmq.test.pubsub;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private final static String EXCHANGE_NAME = "ex_log";

	public static void main(String[] argv)
			throws java.io.IOException, java.lang.InterruptedException, ShutdownSignalException {
		// 创建连接到rabbitMQ
		ConnectionFactory factory = new ConnectionFactory();
		// 创建一个连接，设置rabbitMQ所在主机ip或者主机名
		Connection connection = factory.newConnection("localhost");
		// 创建一个频道
		Channel channel = connection.createChannel();
		// 声明转发器和类型
		channel.exchangeDeclare(1, EXCHANGE_NAME, "fanout");
		// 创建一个非持久的、唯一的且自动删除的临时队列
		String queueName = channel.queueDeclare(1).getQueue();
		System.out.println(" [*] queueName " + queueName);
		// 绑定键的意义依赖于转发器的类型。对于fanout类型，忽略此参数
		String bindingKey = "info";
		// 为转发器绑定队列，可理解为队列对该转发器上的消息感兴趣
		channel.queueBind(1, queueName, EXCHANGE_NAME, bindingKey);

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		// 创建消费者
		QueueingConsumer consumer = new QueueingConsumer(channel);
		// 指定接收者，第二个参数为自动应答，无需手动应答
		channel.basicConsume(1, queueName, true, consumer);
		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());
			print2File(message);
		}
	}

	/**
	 * 写入文件
	 * 
	 * @param msg
	 */
	private static void print2File(String msg) {
		try {
			// String dir = ReceiverTest.class.getClassLoader().getResource("").getPath();
			String dir = "D:\\";
			String logFileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
			File file = new File(dir, logFileName + ".log");
			FileOutputStream fos = new FileOutputStream(file, true);
			fos.write((msg + "\r\n").getBytes());
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}