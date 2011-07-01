package jp.co.baykraft.app.tasknote.entity;

/**
 * タスクノートエンティティ
 */
public class TaskNote {
    /**
     * ID
     */
    private String guid;
    /**
     * タイトル
     */
    private String title;
    /**
     * 本文
     */
    private String text;

    public String getGuid() {
        return guid;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}
