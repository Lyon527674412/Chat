package cn.sinjinsong.chat.server.task;

import cn.sinjinsong.chat.server.exception.factory.ExceptionHandlingThreadFactory;
import cn.sinjinsong.chat.server.handler.task.BaseTaskHandler;
import cn.sinjinsong.chat.server.http.HttpConnectionManager;
import cn.sinjinsong.chat.server.util.SpringContextUtil;
import cn.sinjinsong.common.domain.Task;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by SinjinSong on 2017/5/23.
 * ������
 * ���������������ȡ�������ύ���̳߳�
 */
@Slf4j
public class TaskManagerThread extends Thread {
    private ExecutorService taskPool;
    private BlockingQueue<Task> taskBlockingQueue;
    private HttpConnectionManager httpConnectionManager;

    private ExecutorService crawlerPool;


    public TaskManagerThread(BlockingQueue<Task> taskBlockingQueue) {
        this.taskPool = new ThreadPoolExecutor(
                5, 10, 1000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10),
                new ExceptionHandlingThreadFactory(SpringContextUtil.getBean("taskExceptionHandler")),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.taskBlockingQueue = taskBlockingQueue;
        this.httpConnectionManager = SpringContextUtil.getBean("httpConnectionManager");
        this.crawlerPool = new ThreadPoolExecutor(
                5, 10, 1000,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void shutdown() {
        taskPool.shutdown();
        crawlerPool.shutdown();
        Thread.currentThread().interrupt();
    }

    /**
     * �����ǰ�̱߳��жϣ���ôFuture���׳�InterruptedException��
     * ��ʱ����ͨ��future.cancel(true)���жϵ�ǰ�߳�
     * <p>
     * ��submit�����ύ������������׳����쳣����ô����ExecutionException�������׳�
     */
    @Override
    public void run() {
        Task task;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                task = taskBlockingQueue.take();
                log.info("{}�Ѵ�����������ȡ��",task.getReceiver().getRemoteAddress());
                BaseTaskHandler taskHandler = SpringContextUtil.getBean("BaseTaskHandler", task.getType().toString().toLowerCase());
                taskHandler.init(task,httpConnectionManager,this);
                System.out.println(taskHandler);
                taskPool.execute(taskHandler);
            }
        } catch (InterruptedException e) {
            //����Ҳ�޷���֪������Ϣ����˭������ֻ��ֱ���˳���
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ExecutorService getCrawlerPool() {
        return crawlerPool;
    }
}
