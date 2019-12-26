package cn.dgkj.rabbitmq.helloworld;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author mawt
 * @decription 生产者
 * @date 2019.11.28
 */
public class Send {

    /**
     * AMQP高级消息队列协议
     * 虚拟主机（virtual host）：RabbitMQ当中，用户只能在虚拟主机的粒度进行权限控制。因此，如果需要禁止A组访问B组的交换机/队列/绑定，必须为A和B分别创 建一个虚拟主机。每一个RabbitMQ服务器都有一个默认的虚拟主机“/”。
     * 交换机（exchange）
     * 队列（queue）
     * 绑定（binding）
     *
     */
    private final static String QUEUE_NAME = "hello22";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

    //    factory.setVirtualHost("");  //vhost：虚拟主机，一个broker里可以开设多个vhost，用作不同用户的权限分离。

        /**
         * tcp连接
         * channel 消息通道，在客户端的每个连接里，可建立多个channel，每个channel代表一个会话任务
         */

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            /**
             * 声明一个队列
             * @param queue 消息队列的名称
             * @param durable 是否可持久化
             * @param exclusive 是否是排外的；如果设置为true，这个消息队列只能被当前信道访问，并且连接关闭时该消息队列会被自动删除。
             * @param autoDelete 是否自动删除 消费完自动删
             * @param arguments 扩展参数
             */
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            try {
                //开启事务
                channel.txSelect();

                /**
                 * 发送消息
                 * @param exchange 交换机名称，如果没有指定交换机，就会使用RabbitMQ提供的默认交换机；该交换机与所有消息队列绑定，并默认以消息队列名称作为绑定路由键来匹配消息的路由键。
                 * @param routingKey 路由键
                 * @param props 消息头
                 * @param body 消息体
                 */
                String message = "Hello World!";

                for (int i = 0; i < 10; i++) {
                    channel.basicPublish("", QUEUE_NAME, null, (message + i).getBytes(StandardCharsets.UTF_8));
                }
                System.out.println(" [x] Sent '" + message + "'");
            } catch (Exception e) {
                e.printStackTrace();

                channel.txRollback();
            } finally {
                //提交事务
                channel.txCommit();
            }
        }
    }

}
