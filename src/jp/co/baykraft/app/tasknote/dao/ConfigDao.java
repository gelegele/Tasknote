package jp.co.baykraft.app.tasknote.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jp.co.baykraft.app.tasknote.entity.Config;

/**
 * 設定情報テーブルDAO
 */
public class ConfigDao {

    /**
     * DBコネクション
     */
    private Connection connection;

    public ConfigDao(Connection con) {
        connection = con;
    }
    /**
     * テーブルを生成する
     * @throws SQLException
     */
    public void createTable() {
        PreparedStatement stmt = null;
        try {
            try {
                stmt = connection.prepareStatement(
                        "CREATE TABLE config ("
                        + " id INT IDENTITY PRIMARY KEY"
                        + ",username VARCHAR(20) NOT NULL UNIQUE"
                        + ",password VARCHAR(20) NOT NULL"
                        + ",tagname VARCHAR(30)"
                        + ",via_proxy BOOLEAN NOT NULL DEFAULT FALSE"
                        + ",proxyhost VARCHAR(50)"
                        + ",proxyport INT"
                        + ")");
                stmt.execute();
            } finally {
                if (null != stmt) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * 設定情報を生成する
     * @param config 設定情報
     */
    public void insert(Config config) {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("INSERT INTO config (username, password, tagname, via_proxy, proxyhost, proxyport) VALUES (?, ?, ?, ?, ?, ?)");
                int i = 1;
                stmt.setString(i++, config.getUsername());
                stmt.setString(i++, config.getPassword());
                stmt.setString(i++, config.gettTagName());
                stmt.setBoolean(i++, config.isViaProxy());
                stmt.setString(i++, config.getProxyHost());
                stmt.setInt(i++, config.getProxyPort());
                stmt.execute();
            } finally {
                if (null != stmt) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * 設定情報を取得する
     * @return 設定情報
     */
    public Config find() {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("SELECT * FROM config");
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    Config config = new Config();
                    config.setId(resultSet.getInt("id"));
                    config.setUsername(resultSet.getString("username"));
                    config.setPassword(resultSet.getString("password"));
                    config.setTagName(resultSet.getString("tagname"));
                    config.setViaProxy(resultSet.getBoolean("via_proxy"));
                    config.setProxyHost(resultSet.getString("proxyhost"));
                    config.setProxyPort(resultSet.getInt("proxyPort"));
                    if (resultSet.next()) {
                        assert false; // 仕様上、レコードは最大1件
                    }
                    return config;
                }
            } finally {
                if (null != stmt) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 設定情報を更新する
     * @param config 設定情報
     */
    public void update(Config config) {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("UPDATE config SET username = ?, password = ?, tagname = ?, via_proxy = ?, proxyhost = ?, proxyport = ? WHERE id = ?");
                int i = 1;
                stmt.setString(i++, config.getUsername());
                stmt.setString(i++, config.getPassword());
                stmt.setString(i++, config.gettTagName());
                stmt.setBoolean(i++, config.isViaProxy());
                stmt.setString(i++, config.getProxyHost());
                stmt.setInt(i++, config.getProxyPort());
                stmt.setInt(i++, config.getId());
                stmt.executeUpdate();
            } finally {
                if (null != stmt) {
                    stmt.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
