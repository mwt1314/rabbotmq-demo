package cn.dgkj.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class ReceiveLogsTopic {

    private static final String EXCHANGE_NAME = "topic_logs";

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
         *             topic：topic类型的Exchange在匹配规则上进行了扩展，它与direct类型的Exchage相似，也是将消息路由到binding key与routing key相匹配的Queue中，但这里的匹配规则有些不同，它约定：
         *                      1.routingKey为一个句点号“.”分隔的字符串
         *                      2.bindingKey与routingKey一样也是句点号“.”分隔的字符串
         *                      3.bindingKey中可以存在两种特殊字符“*”与“#”，用于做模糊匹配，其中“*”用于匹配一个单词，“#”用于匹配多个单词（可以是零个）
         *             headers：
         *             fanout：fanout类型的Exchange路由规则非常简单，它会把所有发送到该Exchange的消息路由到所有与它绑定的Queue中
         */
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        /**
         * 创建一个队列
         */
        String queueName = channel.queueDeclare().getQueue();

        if (argv.length < 1) {
            System.err.println("Usage: ReceiveLogsTopic [binding_key]...");
            System.exit(1);
        }

        for (String bindingKey : argv) {
            /**
             * 将队列绑定到交换机上，并指定routingKey
             *
             * @param queue 队列名称
             * @param exchange 交换机名称
             * @param routingKey 路由键
             */
            channel.queueBind(queueName, EXCHANGE_NAME, bindingKey);
        }

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        /**
         * 消费指定的队列消息
         *
         * @param queue 消息队列的名称
         * @param autoAck 是否自动应答
         * @param deliverCallback
         * @param cancelCallback
         */
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}

