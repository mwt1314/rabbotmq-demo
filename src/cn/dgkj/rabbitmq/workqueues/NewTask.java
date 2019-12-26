package cn.dgkj.rabbitmq.workqueues;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.nio.charset.StandardCharsets;

public class NewTask {

    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            /**
             * 声明一个队列
             * @param queue 消息队列的名称
             * @param durable 是否可持久化
             * @param exclusive 是否是排外的；如果设置为true，这个消息队列只能被当前信道访问，并且连接关闭时该消息队列会被自动删除。
             * @param autoDelete 是否自动删除
             * @param arguments 扩展参数
             */
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            byte[] message = String.join(" ", argv).getBytes(StandardCharsets.UTF_8);
            /**
             * 发送消息
             * @param exchange 交换机名称，如果没有指定交换机，就会使用RabbitMQ提供的默认交换机；该交换机与所有消息队列绑定，并默认以消息队列名称作为绑定路由键来匹配消息的路由键。
             * @param routingKey 路由键
             * @param props 消息头
             * @param body 消息体
             */
            for(int i = 0; i < 100; i++){
                channel.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message);
                System.out.println(" [x] Sent '" + message + "'");
                Thread.sleep(i*10);
            }
        }
    }

}
