package cn.dgkj.rabbitmq.rpc;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static int fib(int n) {
        if (n == 0 || n == 1) {
            return 0;
        }
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

            /**
             * 声明一个队列
             * @param queue 消息队列的名称
             * @param durable 是否可持久化
             * @param exclusive 是否是排外的；如果设置为true，这个消息队列只能被当前信道访问，并且连接关闭时该消息队列会被自动删除。
             * @param autoDelete 是否自动删除 消费完自动删
             * @param arguments 扩展参数
             */
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            /**
             *
             * @param queue 队列名称
             */
            channel.queuePurge(RPC_QUEUE_NAME);

            /**
             *
             * @param prefetchCount 通过设置prefetchCount来限制Queue每次发送给每个消费者的消息数
             *                      比如我们设置prefetchCount=1，则Queue每次给每个消费者发送一条消息；消费者处理完这条消息后Queue会再给该消费者发送一条消息
             *                      prefetchCount的默认值为0，即没有限制，队列会将所有消息尽快发给消费者。
             *
             */
            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Object monitor = new Object();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                String response = "";

                try {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    int n = Integer.parseInt(message);

                    System.out.println(" [.] fib(" + message + ")");
                    response += fib(n);
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {

                    /**
                     * 发布消息
                     * @param exchange 交换机名称
                     * @param routingKey 路由键
                     * @param props 消息体
                     * @param body 消息体
                     */
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));
                    /**
                     * 应答
                     */
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            /**
             * 消费
             *
             * @param queue 消息队列的名称
             * @param autoAck 是否自动应答
             * @param deliverCallback
             * @param cancelCallback
             */
            channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> { }));
            // Wait and be prepared to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        /**
                         * 放弃monitor的锁，进入阻塞状态
                         */
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}