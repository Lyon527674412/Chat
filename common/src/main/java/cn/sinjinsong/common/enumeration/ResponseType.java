package cn.sinjinsong.common.enumeration;

/**
 * Created by SinjinSong on 2017/5/23.
 */
public enum ResponseType {
    NORMAL(1,"��Ϣ"),
    PROMPT(2,"��ʾ"),
    FILE(3,"�ļ�");
    
    private int code;
    private String desc;

    ResponseType(int code, String desc) {
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
