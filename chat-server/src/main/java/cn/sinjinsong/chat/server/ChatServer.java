package cn.sinjinsong.chat.server;

import cn.sinjinsong.chat.server.exception.handler.InterruptedExceptionHandler;
import cn.sinjinsong.chat.server.handler.message.MessageHandler;
import cn.sinjinsong.chat.server.task.TaskManagerThread;
import cn.sinjinsong.chat.server.util.SpringContextUtil;
import cn.sinjinsong.common.domain.Message;
import cn.sinjinsong.common.domain.Task;
import cn.sinjinsong.common.util.ProtoStuffUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SinjinSong on 2017/3/25.
 */

/**
 * @Slf4j��Ϊ��ǰ������һ����Ϊlog����־����
 * Slf4j�����ڴ�ӡ���ַ��������ռλ�����Ա����ַ�����ƴ��
 */
@Slf4j
public class ChatServer {
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final int PORT = 9000;
    public static final String QUIT = "QUIT";
    private AtomicInteger onlineUsers;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private ExecutorService readPool;

    private BlockingQueue<Task> downloadTaskQueue;
    private TaskManagerThread taskManagerThread;
    private ListenerThread listenerThread;
    private InterruptedExceptionHandler exceptionHandler;
    public ChatServer() {
        log.info("����������");
        initServer();
    }

    private void initServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            //�л�Ϊ������ģʽ
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            //���ѡ����
            selector = Selector.open();
            //��channelע�ᵽselector��
            //�ڶ���������ѡ���������˵��selector���channel��״̬
            //���ܵ�ȡֵ��SelectionKey.OP_READ OP_WRITE OP_CONNECT OP_ACCEPT
            //��ص���channel�Ľ���״̬
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            this.readPool = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
            this.downloadTaskQueue = new ArrayBlockingQueue<>(20);
            this.taskManagerThread = new TaskManagerThread(downloadTaskQueue);
            this.taskManagerThread.setUncaughtExceptionHandler(SpringContextUtil.getBean("taskExceptionHandler"));
            this.listenerThread = new ListenerThread();
            this.onlineUsers = new AtomicInteger(0);
            this.exceptionHandler = SpringContextUtil.getBean("interruptedExceptionHandler");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * �����������߳���ò�Ҫ�ڹ��캯����������Ӧ����Ϊһ����������������ʹ�ù�������������ʵ��
     * ���⹹��δ��ɾ�ʹ�ó�Ա����
     */
    public void launch() {
        new Thread(listenerThread).start();
        new Thread(taskManagerThread).start();
    }

    /**
     * �Ƽ��Ľ����̵߳ķ�ʽ��ʹ���ж�
     * ��whileѭ����ʼ������Ƿ��жϣ����ṩһ�����������Լ��ж�
     * ��Ҫ���ⲿ���߳��ж�
     * <p>
     * ���⣬���Ҫ�ж�һ��������ĳ���ط����̣߳�����Ǽ̳���Thread���ȹر�����������Դ���ٹرյ�ǰ�߳�
     */
    private class ListenerThread extends Thread {

        @Override
        public void interrupt() {
            try {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                super.interrupt();
            }
        }

        @Override
        public void run() {
            try {
                //�����һ�������ϵĿͻ��˵�����׼������
                while (!Thread.currentThread().isInterrupted()) {
                    //��ע����¼�����ʱ���������أ�����,�÷�����һֱ����  
                    selector.select();
                    //��ȡ��ǰѡ����������ע��ļ����¼�
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey key = it.next();
                        //ɾ����ѡ��key,�Է��ظ����� 
                        it.remove();
                        //���"����"�¼��Ѿ���
                        if (key.isAcceptable()) {
                            //���ɽ����¼��Ĵ���������
                            handleAcceptRequest();
                        } else if (key.isReadable()) {
                            //���"��ȡ"�¼��Ѿ���
                            //ȡ���ɶ�������ǣ����δ������Ŵ򿪶�ȡ�¼����
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            //���ɶ�ȡ�¼��Ĵ���������
                            readPool.execute(new ReadEventHandler(key));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void shutdown() {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * �رշ�����
     */
    public void shutdownServer() {
        try {
            taskManagerThread.shutdown();
            listenerThread.shutdown();
            readPool.shutdown();
            serverSocketChannel.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ����ͻ��˵���������
     */
    private void handleAcceptRequest() {
        try {
            SocketChannel client = serverSocketChannel.accept();
            // ���յĿͻ���ҲҪ�л�Ϊ������ģʽ
            client.configureBlocking(false);
            // ��ؿͻ��˵Ķ������Ƿ����
            client.register(selector, SelectionKey.OP_READ);
            log.info("���������ӿͻ���:{}",client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * �����̳߳��е��̻߳������̳߳ص�shutdown�������ر�
     */
    private class ReadEventHandler implements Runnable {

        private ByteBuffer buf;
        private SocketChannel client;
        private ByteArrayOutputStream baos;
        private SelectionKey key;

        public ReadEventHandler(SelectionKey key) {
            this.key = key;
            this.client = (SocketChannel) key.channel();
            this.buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            this.baos = new ByteArrayOutputStream();
        }

        @Override
        public void run() {
            try {
                int size;
                while ((size = client.read(buf)) > 0) {
                    buf.flip();
                    baos.write(buf.array(), 0, size);
                    buf.clear();
                }
                if (size == -1) {
                    return;
                }
                log.info("��ȡ��ϣ���������");
                //����������ȡ�¼�
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                key.selector().wakeup();
                byte[] bytes = baos.toByteArray();
                baos.close();
                Message message = ProtoStuffUtil.deserialize(bytes, Message.class);
                MessageHandler messageHandler = SpringContextUtil.getBean("MessageHandler", message.getHeader().getType().toString().toLowerCase());
                try {
                    messageHandler.handle(message, selector, key, downloadTaskQueue, onlineUsers);
                } catch (InterruptedException e) {
                    log.error("�������̱߳��ж�");
                    exceptionHandler.handle(client, message);
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        System.out.println("Initialing...");
        ChatServer chatServer = new ChatServer();
        chatServer.launch();
        Scanner scanner = new Scanner(System.in, "UTF-8");
        while (scanner.hasNext()) {
            String next = scanner.next();
            if (next.equalsIgnoreCase(QUIT)) {
                System.out.println("������׼���ر�");
                chatServer.shutdownServer();
                System.out.println("�������ѹر�");
            }
        }
    }
}
