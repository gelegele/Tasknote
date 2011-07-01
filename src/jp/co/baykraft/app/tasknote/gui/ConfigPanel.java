package jp.co.baykraft.app.tasknote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import jp.co.baykraft.app.tasknote.Main;
import jp.co.baykraft.app.tasknote.dao.ConfigDao;
import jp.co.baykraft.app.tasknote.entity.Config;

public class ConfigPanel extends JPanel implements ActionListener, ItemListener {

    /**
     * 適用ボタン
     */
    private JButton btnCommit;

    private JTextField tfUsername;
    private JPasswordField tfPassword;
    private JTextField tfTagname;
    private JCheckBox  cbViaProxy;
    private JTextField tfProxyhost;
    private JTextField tfProxyport;

    private ConfigDao configDao;
    private Config config;

    private static final String BUTTON_COMMIT = "適用";

    /**
     * コンストラクタ
     */
    public ConfigPanel() {

        configDao = new ConfigDao(Main.getConnection());
        config = configDao.find();

        this.add(new JLabel("username"));
        tfUsername = new JTextField();
        tfUsername.setText(config.getUsername());
        this.add(tfUsername);

        this.add(new JLabel("password"));
        tfPassword = new JPasswordField();
        tfPassword.setText(config.getPassword());
        this.add(tfPassword);

        this.add(new JLabel("tagname"));
        tfTagname = new JTextField();
        tfTagname.setText(config.getTagName());
        this.add(tfTagname);

        cbViaProxy = new JCheckBox("via proxy");
        this.add(cbViaProxy);

        this.add(new JLabel("proxyhost"));
        tfProxyhost = new JTextField();
        tfProxyhost.setText(config.getProxyHost());
        this.add(tfProxyhost);

        this.add(new JLabel("proxyport"));
        tfProxyport = new JTextField();
        tfProxyport.setText(Integer.toString(config.getProxyPort()));
        this.add(tfProxyport);

        // 初期表示時にもitemStateChangedを走らせたいのでここ
        cbViaProxy.addItemListener(this);
        cbViaProxy.setSelected(config.isViaProxy());

        btnCommit = new JButton(BUTTON_COMMIT);
        btnCommit.addActionListener(this);
        this.add(btnCommit);
    }

    /**
     * ボタンアクションハンドラ
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (BUTTON_COMMIT.equals(event.getActionCommand())) {
            // 適用ボタンが押下されたら設定情報を更新する
            config.setUsername(tfUsername.getText());
            config.setPassword(new String(tfPassword.getPassword()));
            config.setTagName(tfTagname.getText());
            config.setViaProxy(cbViaProxy.isSelected());
            config.setProxyHost(tfProxyhost.getText());
            config.setProxyPort(Integer.parseInt(tfProxyport.getText()));
            configDao.update(config);

            // TODO タグ名以外が変更されたらログインしなおす必要がある
        }
    }

    /**
     * アイテムチェンジハンドラ
     */
    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.getSource() == cbViaProxy) {
            // プロキシ使用チェックボックスが変更されたらプロキシ設定を無効化する
            tfProxyhost.setText(config.getProxyHost());
            tfProxyhost.setEnabled(cbViaProxy.isSelected());
            tfProxyport.setText(Integer.toString(config.getProxyPort()));
            tfProxyport.setEnabled(cbViaProxy.isSelected());
        }
    }
}
