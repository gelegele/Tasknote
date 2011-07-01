package jp.co.baykraft.app.tasknote.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.co.baykraft.app.tasknote.entity.TaskNote;

/**
 * タスクノートテーブルDAO
 */
public class TaskNoteDao {

    /**
     * DBコネクション
     */
    private Connection connection;

    public TaskNoteDao(Connection con) {
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
                        "CREATE TABLE tasknote ("
                        + " id INT PRIMARY KEY"
                        + ",title VARCHAR(100) NOT NULL"
                        + ",text VARCHAR(500)"
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
     * タスクノートを生成する
     * @param taskNote タスクノート
     */
    public void insert(TaskNote taskNote) {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("INSERT INTO tasknote (guid, title, text) VALUES (?, ?, ?)");
                int i = 1;
                stmt.setString(i++, taskNote.getGuid());
                stmt.setString(i++, taskNote.getTitle());
                stmt.setString(i++, taskNote.getText());
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
     * タスクノートを取得する
     * @param id id
     * @return タスクノート
     */
    public TaskNote find(int id) {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("SELECT * FROM tasknote WHERE guid = ?");
                stmt.setInt(1, id);
                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    TaskNote taskNote = new TaskNote();
                    taskNote.setGuid(resultSet.getString("guid"));
                    taskNote.setTitle(resultSet.getString("title"));
                    taskNote.setText(resultSet.getString("text"));
                    return taskNote;
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
     * 全てのタスクノートを取得する
     * @return 全てのタスクノート
     */
    public List<TaskNote> findAll() {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("SELECT * FROM tasknote");
                ResultSet resultSet = stmt.executeQuery();
                List<TaskNote> taskNoteList = new ArrayList<TaskNote>();
                if (resultSet.next()) {
                    TaskNote taskNote = new TaskNote();
                    taskNote.setGuid(resultSet.getString("guid"));
                    taskNote.setTitle(resultSet.getString("title"));
                    taskNote.setText(resultSet.getString("text"));
                    taskNoteList.add(taskNote);
                }
                return taskNoteList;
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
     * タスクノートを更新する
     * @param taskNote タスクノート
     */
    public void update(TaskNote taskNote) {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("UPDATE tasknote SET title = ?, text = ? WHERE guid = ?");
                int i = 1;
                stmt.setString(i++, taskNote.getTitle());
                stmt.setString(i++, taskNote.getText());
                stmt.setString(i++, taskNote.getGuid());
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

    /**
     * タスクノートを取得する
     * @param id id
     */
    public void delete(int id) {
        try {
            PreparedStatement stmt = null;
            try {
                stmt = connection.prepareStatement("DELETE tasknote WHERE guid = ?");
                stmt.setInt(1, id);
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
