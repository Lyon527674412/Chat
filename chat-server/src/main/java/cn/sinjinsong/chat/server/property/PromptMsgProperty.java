package cn.sinjinsong.chat.server.property;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by SinjinSong on 2017/5/24.
 */
public class PromptMsgProperty {
    public static final String LOGIN_SUCCESS = "��¼�ɹ�����ǰ����%dλ�����û�";
    public static final String LOGIN_FAILURE = "�û��������������ظ���¼����¼ʧ��";
    public static final String LOGOUT_SUCCESS = "ע���ɹ�";
    public static final String RECEIVER_LOGGED_OFF = "�����߲����ڻ�������";
    public static final String TASK_FAILURE = "����ִ��ʧ�ܣ�������";
    public static final String LOGIN_BROADCAST = "%s�û�������";
    public static final String LOGOUT_BROADCAST = "%s�û�������";
    public static final String SERVER_ERROR = "�������ڲ����ִ���������";
    public static final Charset charset = StandardCharsets.UTF_8;
}
