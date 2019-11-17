package cn.sinjinsong.common.enumeration;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public enum MessageType {
    LOGIN(1,"��¼"),
    LOGOUT(2,"ע��"),
    NORMAL(3,"����"),
    BROADCAST(4,"Ⱥ��"),
    TASK(4,"����");
    
    private int code;
    private String  desc;

    MessageType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
