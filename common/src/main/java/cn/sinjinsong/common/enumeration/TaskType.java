package cn.sinjinsong.common.enumeration;

/**
 * Created by SinjinSong on 2017/5/25.
 */
public enum TaskType {
    FILE(1,"�ļ�"),
    CRAWL_IMAGE(2,"�����ӰͼƬ");
    private int code;
    private String desc;

    TaskType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
}
