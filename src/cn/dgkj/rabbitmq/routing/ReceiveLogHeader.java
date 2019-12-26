package cn.dgkj.rabbitmq.routing;

import com.rabbitmq.client.*;

import java.util.HashMap;
import java.util.Map;

public class ReceiveLogHeader {
    private static final String EXCHANGE_NAME = "header_test";

    public static void main(String[] argv) throws Exception {
        if (argv.length < 1) {
            System.err.println("Usage: ReceiveLogsHeader queueName [headers]...");
            System.exit(1);
        }

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
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.HEADERS);

        // The API requires a routing key, but in fact if you are using a header exchange the
        // value of the routing key is not used in the routing. You can receive information
        // from the sender here as the routing key is still available in the received message.
        String routingKeyFromUser = "ourTestRoutingKey";

        // Argument processing: the first arg is the local queue name, the rest are
        // key value pairs for headers.
        String queueInputName = argv[0];

        // The map for the headers.
        Map<String, Object> headers = new HashMap<>();

        // The rest of the arguments are key value header pairs.  For the purpose of this
        // example, we are assuming they are all strings, but that is not required by RabbitMQ
        // Note that when you run this code you should include the x-match header on the command
        // line. Example:
        //    java -cp $CP ReceiveLogsHeader testQueue1  x-match any header1 value1
        for (int i = 1; i < argv.length; i++) {
            headers.put(argv[i], argv[i + 1]);
            System.out.println("Binding header " + argv[i] + " and value " + argv[i + 1] + " to queue " + queueInputName);
            i++;
        }
        /**
         * 声明一个队列
         * @param queue 消息队列的名称
         * @param durable 是否可持久化
         * @param exclusive 是否是排外的；如果设置为true，这个消息队列只能被当前信道访问，并且连接关闭时该消息队列会被自动删除。
         * @param autoDelete 是否自动删除 消费完自动删
         * @param arguments 扩展参数
         */
        String queueName = channel.queueDeclare(queueInputName, true, false, false, null).getQueue();


        /**
         *
         * @param queue 队列名称
         * @param exchange 交换机名称
         * @param routingKey 路由键
         * @param arguments 扩展参数
         *
         */
        channel.queueBind(queueName, EXCHANGE_NAME, routingKeyFromUser, headers);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}

