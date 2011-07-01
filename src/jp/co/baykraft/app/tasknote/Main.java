package jp.co.baykraft.app.tasknote;


import java.awt.AWTException;
import java.awt.SystemTray;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jp.co.baykraft.app.tasknote.dao.ConfigDao;
import jp.co.baykraft.app.tasknote.dao.TaskNoteDao;
import jp.co.baykraft.app.tasknote.entity.Config;
import jp.co.baykraft.app.tasknote.gui.MainFrame;
import jp.co.baykraft.app.tasknote.gui.TrayIconWithMenu;


public class Main {

    private static MainFrame mainFrame;

    public static MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * DBコネクションを返します
     * @return
     */
    public static Connection getConnection() {
        Properties props = new Properties();
        props.put("user", "sa");
        try {
            return DriverManager.getConnection("jdbc:h2:ChatDB", props);
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        System.out.println("main : " + SwingUtilities.isEventDispatchThread());

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e1) {
            System.exit(-1);
        }
        try {
            Main.createTables();
        } catch (SQLException e) {
            System.exit(-1);
        }
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createMainFrame();
                }
        });
    }
    /**
     * 初回起動時にテーブルを生成します。
     * @throws SQLException
     */
    private static void createTables() throws SQLException {
        Connection connection = Main.getConnection();
        try {
            PreparedStatement stmt = null;
            List<String> tableNames = new ArrayList<String>();
            try {
                stmt = connection.prepareStatement("SHOW TABLES");
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString("TABLE_NAME"));
                }
            }
            finally {
                if (null != stmt) {
                    stmt.close();
                }
            }
            // 設定情報テーブルの生成
            if (!tableNames.contains("config") && !tableNames.contains("CONFIG")) {
                System.out.println("creating config table");
                ConfigDao configDao = new ConfigDao(connection);
                configDao.createTable();

// TODO 暫定
                Config config = new Config();
                config.setUsername("ryo500209");
                config.setPassword("password");
                config.setTagName("TODO");
                config.setViaProxy(true);
                config.setProxyHost("proxy.css.fujitsu.com");
                config.setProxyPort(8080);
                configDao.insert(config);
            }
            // タスクノートテーブルの生成
            if (!tableNames.contains("tasknote") && !tableNames.contains("TASKNOTE")) {
                System.out.println("creating tasknote table");
                TaskNoteDao taskNoteDao = new TaskNoteDao(connection);
                taskNoteDao.createTable();
            }
        } finally {
            if (null != connection) {
                connection.close();
            }
        }
    }
    /**
     * ToDoリストの生成と表示を行います。
     */
    private static void createMainFrame() {

        System.out.println("createAndShowTodoList :" + SwingUtilities.isEventDispatchThread());

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        catch (IllegalAccessException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        catch (ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

        // システムトレイ
        TrayIconWithMenu trayIconWithMenu = new TrayIconWithMenu();
        try {
            SystemTray.getSystemTray().add(trayIconWithMenu);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        // メイン画面
        Main.mainFrame = new MainFrame();
        trayIconWithMenu.activate();
    }
}
