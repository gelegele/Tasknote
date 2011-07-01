package jp.co.baykraft.app.tasknote.entity;

/**
 * 設定情報エンティティ
 */
public class Config {

    /**
     * ID
     */
    private int id;
    /**
     * ユーザー名
     */
    private String username;
    /**
     * パスワード
     */
    private String password;
    /**
     * 対象タグ名
     */
    private String tagName;
    /**
     * プロキシ使用
     */
    private boolean viaProxy;
    /**
     * プロキシホスト名
     */
    private String proxyHost;
    /**
     * プロキシポート番号
     */
    private int proxyPort;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTagName() {
        return tagName;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String gettTagName() {
        return tagName;
    }
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    public boolean isViaProxy() {
        return viaProxy;
    }
    public void setViaProxy(boolean viaProxy) {
        this.viaProxy = viaProxy;
    }
    public String getProxyHost() {
        return proxyHost;
    }
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }
    public int getProxyPort() {
        return proxyPort;
    }
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
