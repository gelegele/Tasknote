package jp.co.baykraft.app.tasknote.gui;


import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class ConfigFrame extends JFrame {

    public ConfigFrame() {
        this.setTitle("設定");
        this.setBounds(200, 200, 230, 360); // TODO センター表示にしたい  this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        ImageIcon icon = new ImageIcon("./src/jp/co/baykraft/app/tasknote/resources/enlogo.png");
        this.setIconImage(icon.getImage());

        ConfigPanel configPanel = new ConfigPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        this.add(configPanel);
        this.setVisible(true);
    }
}
