package cn.dgkj.rabbitmq.routing;

import com.rabbitmq.client.*;

import java.util.HashMap;
import java.util.Map;

public class EmitLogHeader {

    private static final String EXCHANGE_NAME = "header_test";

    public static void main(String[] argv) throws Exception {
        if (argv.length < 1) {
            System.err.println("Usage: EmitLogHeader message queueName [headers]...");
            System.exit(1);
        }

        // The API requires a routing key, but in fact if you are using a header exchange the
        // value of the routing key is not used in the routing. You can store information
        // for the receiver here as the routing key is still available in the received message.
        String routingKey = "ourTestRoutingKey";

        // Argument processing: the first arg is the message, the rest are
        // key value pairs for headers.
        String message = argv[0];

        // The map for the headers.
        Map<String, Object> headers = new HashMap<String, Object>();

        // The rest of the arguments are key value header pairs.  For the purpose of this
        // example, we are assuming they are all strings, but that is not required by RabbitMQ
        for (int i = 1; i < argv.length; i++) {
            System.out.println("Adding header " + argv[i] + " with value " + argv[i + 1] + " to Map");
            headers.put(argv[i], argv[i + 1]);
            i++;
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

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
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.HEADERS);

            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

            // MessageProperties.PERSISTENT_TEXT_PLAIN is a static instance of AMQP.BasicProperties
            // that contains a delivery mode and a priority. So we pass them to the builder.
            builder.deliveryMode(MessageProperties.PERSISTENT_TEXT_PLAIN.getDeliveryMode());
            builder.priority(MessageProperties.PERSISTENT_TEXT_PLAIN.getPriority());

            // Add the headers to the builder.
            builder.headers(headers);

            // Use the builder to create the BasicProperties object.
            AMQP.BasicProperties theProps = builder.build();

            /**
             * 发布消息
             * @param exchange 交换机名称
             * @param routingKey 路由键
             * @param props 消息头
             * @param body 消息体
             */
            // Now we add the headers.  This example only uses string headers, but they can also be integers
            channel.basicPublish(EXCHANGE_NAME, routingKey, theProps, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent message: '" + message + "'");
        }
    }
}

