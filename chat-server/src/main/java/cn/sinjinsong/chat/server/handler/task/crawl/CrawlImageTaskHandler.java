package cn.sinjinsong.chat.server.handler.task.crawl;

import cn.sinjinsong.chat.server.handler.task.BaseTaskHandler;
import cn.sinjinsong.chat.server.task.TaskManagerThread;
import cn.sinjinsong.chat.server.util.ImageURLCrawlerUtil;
import cn.sinjinsong.chat.server.util.RequestParser;
import cn.sinjinsong.common.domain.MessageHeader;
import cn.sinjinsong.common.domain.Request;
import cn.sinjinsong.common.domain.Response;
import cn.sinjinsong.common.domain.ResponseHeader;
import cn.sinjinsong.common.enumeration.ResponseType;
import cn.sinjinsong.common.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by SinjinSong on 2017/5/25.
 */
@Component("BaseTaskHandler.crawl_image")
@Scope("prototype")
@Slf4j
public class CrawlImageTaskHandler extends BaseTaskHandler {
    private ExecutorService crawlerPool;

    /**
     * ����Executor�ύ����������ʱ������ϣ����������ɺ��ý���������FutureTask��
     * �����ѭ����ȡtask������future.get()ȥ��ȡ���������������taskû����ɣ���͵����������
     * ���ʵЧ�Բ��ߣ���ʵ�ںܶೡ�ϣ���ʵ���õ�һ��������ʱ����ʱ�����û�����ɲ�������
     * ��ʵ�������ڵ�һ������ʱ���ڶ���task�������Ѿ��������ˣ���Ȼ���������future task�����ʵģ�Ч��Ҳ���ߡ�
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected Response process() throws IOException, InterruptedException {
        MessageHeader header = info.getMessage().getHeader();
        Request request = RequestParser.parse(info.getDesc());
        List<String> urls = ImageURLCrawlerUtil.crawl(request);
        urls.forEach(System.out::println);
        String suffix = null;
        if (urls.size() > 0) {
            suffix = urls.get(0).substring(urls.get(0).lastIndexOf('.') + 1);
        }
        CompletionService<byte[]> completionService = new ExecutorCompletionService<>(crawlerPool);
        //���ύ����
        for (String url : urls) {
            completionService.submit(new ImageThread(url, manager));
        }
        List<byte[]> result = new ArrayList<>();
        //ȡ������˵�������
        byte[] image;
        for (int i = 0; i < urls.size(); i++) {
            Future<byte[]> future = completionService.take();
            try {
                image = future.get();
                result.add(image);
            } catch (ExecutionException e) {
                //��ʹ����������ʧ�ܣ�Ҳ��Ӱ�죬��������
                log.info("��ͼƬ����ʧ��");
            }
        }
        result.forEach((bytes) -> System.out.println(bytes.length));
        return new Response(ResponseHeader.builder()
                .type(ResponseType.FILE)
                .sender(header.getSender())
                .timestamp(header.getTimestamp())
                .build(),
                ZipUtil.zipCompress(result,suffix));
    }

    @Override
    protected void init(TaskManagerThread parentThread) {
        this.crawlerPool = parentThread.getCrawlerPool();
    }

}
