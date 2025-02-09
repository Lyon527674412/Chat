package cn.sinjinsong.chat.server.handler.message;

import cn.sinjinsong.common.domain.Task;
import cn.sinjinsong.common.domain.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public abstract class MessageHandler {
    
    public static final String SYSTEM_SENDER = "ϵͳ��ʾ";
    abstract public void handle(Message message, Selector server, SelectionKey client, BlockingQueue<Task> queue, AtomicInteger onlineUsers) throws InterruptedException;
    
    protected void broadcast(byte[] data, Selector server) throws IOException {
        for (SelectionKey selectionKey : server.keys()) {
            Channel channel = selectionKey.channel();
            if (channel instanceof SocketChannel) {
                SocketChannel dest = (SocketChannel) channel;
                if (dest.isConnected()) {
                    dest.write(ByteBuffer.wrap(data));
                }
            }
        }
    }
}
