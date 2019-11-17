package cn.sinjinsong.chat.server.exception.factory;

import java.util.concurrent.ThreadFactory;

/**
 * Created by SinjinSong on 2017/5/25.
 */

/**
 * ����쳣������
 */
public class ExceptionHandlingThreadFactory implements ThreadFactory {
    private Thread.UncaughtExceptionHandler handler;
	public ExceptionHandlingThreadFactory(Thread.UncaughtExceptionHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r);
		//�����������쳣������
		thread.setUncaughtExceptionHandler(handler);
		return thread;
	}
}
