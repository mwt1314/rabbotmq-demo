package cn.dgkj.rabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EmitLogTopic {

    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            /**
             * 创建一个交换机
             *
             * @param exchange 交换机名称
             * @param type 交换机类型：4种，AMQP规范里还提到另外两种，分别为system与自定义
             *             direct：direct类型的Exchange路由规则也很简单，它会把消息路由到那些bindingKey与routingKey完全匹配的Queue中
             *             topic：topic类型的Exchange在匹配规则上进行了扩展，它与direct类型的Exchage相似，也是将消息路由到bindingKey与routingKey相匹配的Queue中，但这里的匹配规则有些不同，它约定：
             *                      1.routingKey为一个句点号“.”分隔的字符串
             *                      2.bindingKey与routingKey一样也是句点号“.”分隔的字符串
             *                      3.bindingKey中可以存在两种特殊字符“*”与“#”，用于做模糊匹配，其中“*”用于匹配一个单词，“#”用于匹配多个单词（可以是零个）
             *             headers：headers类型的Exchange不依赖于routingKey与bindingKey的匹配规则来路由消息，而是根据发送的消息内容中的headers属性进行匹配
             *
             *             fanout：fanout类型的Exchange路由规则非常简单，它会把所有发送到该Exchange的消息路由到所有与它绑定的Queue中
             */
            channel.exchangeDeclare(EXCHANGE_NAME, "topic");

            String routingKey = getRouting(argv);
            String message = getMessage(argv);

            /**
             * 发布消息到指定的交换机中，并指定routingKey
             * @param exchange 交换机名称
             * @param routingKey 路由键
             * @param props 消息头
             * @param body 消息体
             */
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
        }
    }

    private static String getRouting(String[] strings) {
        if (strings.length < 1) {
            return "anonymous.info";
        }
        return strings[0];
    }

    private static String getMessage(String[] strings) {
        if (strings.length < 2) {
            return "Hello World!";
        }
        return joinStrings(strings, " ", 1);
    }

    private static String joinStrings(String[] strings, String delimiter, int startIndex) {
        int length = strings.length;
        if (length == 0) {
            return "";
        }
        if (length < startIndex) {
            return "";
        }
        StringBuilder words = new StringBuilder(strings[startIndex]);
        for (int i = startIndex + 1; i < length; i++) {
            words.append(delimiter).append(strings[i]);
        }
        return words.toString();
    }
}

