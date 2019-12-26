package cn.dgkj.rabbitmq.helloworld;

import com.rabbitmq.client.*;

public class Recv {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        /**
         * 声明一个队列
         *
         * @param queue 消息队列的名称
         * @param durable 是否可持久化
         * @param exclusive 是否是排外的；如果设置为true，这个消息队列只能被当前信道访问，并且连接关闭时该消息队列会被自动删除。
         * @param autoDelete 是否自动删除
         * @param arguments 扩展参数
         *
         */
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");

            /**
             * 应答
             * @param deliveryTag
             * @param multiple
             */
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            /**
             * 拒绝消息
             * @param deliveryTag
             * @param requeue  true: 消息服务器会重传该消息给下一个订阅者；
             *                 false: 则会直接删除该消息
             */
        //    channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);

        };

        /**
         * @param queue 消息队列的名称
         * @param autoAck 是否自动应答 ACK仅仅是通知服务器可以安全的删除该消息，而不是通知生产者，与RPC不同。如果消费者在接到消息以后还没来得及返回ACK就断开了连接，消息服务器会重传该消息给下一个订阅者，如果没有订阅者就会存储该消息
         *                true
         *                  只要消息从队列中获取，无论消费者获取到消息后是否成功消息，都认为是消息已经成功消费。
         *                false
         *                  消费者从队列中获取消息后，服务器会将该消息标记为不可用状态，等待消费者的反馈，如果消费者一直没有反馈，那么该消息将一直处于不可用状态。
         * @param deliverCallback
         * @param cancelCallback
         */
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });

        channel.close();
        connection.close();

    }

}
