package cn.dgkj.rabbitmq.routing;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

/**
 * @author mawt
 */
public class ReceiveLogsDirect {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        /**
         * 创建一个交换机
         *
         * @param exchange 交换机名称
         * @param type 交换机类型：4种，AMQP规范里还提到另外两种，分别为system与自定义
         *             direct：direct类型的Exchange路由规则也很简单，它会把消息路由到那些binding key与routing key完全匹配的Queue中
         *             topic：
         *             headers：
         *             fanout：fanout类型的Exchange路由规则非常简单，它会把所有发送到该Exchange的消息路由到所有与它绑定的Queue中
         */
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        /**
         * 创建一个队列
         */
        String queueName = channel.queueDeclare().getQueue();

        if (argv.length < 1) {
            System.err.println("Usage: ReceiveLogsDirect [info] [warning] [error]");
            System.exit(1);
        }

        for (String severity : argv) {
            /**
             * 将队列绑定到交换机上，并指定routingKey，可以有多个routingKey
             * @param queue 队列名称
             * @param exchange 交换机名称
             * @param routingKey 路由键
             */
            channel.queueBind(queueName, EXCHANGE_NAME, severity);
        }
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        /**
         * 消费
         *
         * 通过basic.consume命令，订阅某一个队列中的消息,channel会自动在处理完上一条消息之后，接收下一条消息。
         * （同一个channel消息处理是串行的）。除非关闭channel或者取消订阅，否则客户端将会一直接收队列的消息。
         * @param queue 消息队列的名称
         * @param autoAck 是否自动应答
         * @param deliverCallback
         * @param cancelCallback
         */
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

}

