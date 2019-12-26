package cn.dgkj.rabbitmq.publishsubscribe;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author mawt
 */
public class EmitLog {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

            /**
             * 创建一个交换机
             *
             * @param exchange 交换机名称
             * @param type 交换机类型：4种，AMQP规范里还提到另外两种，分别为system与自定义
             *             direct：direct类型的Exchange路由规则也很简单，它会把消息路由到那些binding key与routing key完全匹配的Queue中
             *             topic：
             *             headers：
             *             fanout：fanout类型的Exchange路由规则非常简单，它会把所有发送到该Exchange的消息路由到所有与它绑定的Queue中
             * @param durable 是否可持久化
             * @param autoDelete 是否自动删除
             * @param internal
             * @param arguments 扩展参数
             */
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

            String message = argv.length < 1 ? "info: Hello World!" : String.join(" ", argv);

            /**
             * 消息发送到没有队列绑定的交换机时，消息将丢失，因为，交换机没有存储消息的能力，消息只能存在在队列中
             *
             * @param exchange 交换机名称
             * @param routingKey 路由键
             * @param mandatory
             * @param immediate
             * @param props 消息头
             * @param body 消息体
             *
             */
            channel.basicPublish(EXCHANGE_NAME, "", false, false, null, message.getBytes(StandardCharsets.UTF_8));

            System.out.println(" [x] Sent '" + message + "'");
        }
    }

}

