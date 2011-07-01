package jp.co.baykraft.app.tasknote.gui;

import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import jp.co.baykraft.app.tasknote.Main;



public class TrayIconWithMenu extends TrayIcon implements ActionListener {

    private static final String MENU_SHOW = "表示する";
    private static final String MENU_HIDE = "隠す";
    private static final String MENU_CONFIG = "設定";
    private static final String MENU_EXIT = "終了";

    public TrayIconWithMenu() {
        // タスクトレイアイコン
        super(Toolkit.getDefaultToolkit().getImage("./src/jp/co/baykraft/app/tasknote/resources/enlogo_sleep.png"), "えばーのーと");
        this.setImageAutoSize(true); // 画像リサイズ

        // タスクトレイポップアップメニュー
        PopupMenu popupMenu = new PopupMenu();
        popupMenu.add(MENU_EXIT);
        popupMenu.addActionListener(this);
        this.setPopupMenu(popupMenu);
    }
    public void activate() {
        this.setImage(Toolkit.getDefaultToolkit().getImage("./src/jp/co/baykraft/app/tasknote/resources/enlogo.png"));
        this.addActionListener(this);
        this.displayMessage("○件更新", "みたいな", MessageType.INFO);
    }
    @Override
    public void actionPerformed(ActionEvent event) {
        if (Main.getMainFrame() != null) {
System.out.println("MainFrame.visible " + (Main.getMainFrame().isVisible()?"true":"false"));
            if (event.getSource().getClass().equals(PopupMenu.class)) {
                if (MENU_EXIT.equals(event.getActionCommand())) {
                    System.exit(0);
                } else if (MENU_SHOW.equals(event.getActionCommand())) {
                    Main.getMainFrame().setVisible(true);
                } else if (MENU_HIDE.equals(event.getActionCommand())) {
                    Main.getMainFrame().setVisible(false);
                    Main.getMainFrame().dispatchEvent(new WindowEvent(Main.getMainFrame(), WindowEvent.WINDOW_CLOSING));
                } else if (MENU_CONFIG.equals(event.getActionCommand())) {
                    new ConfigFrame();
                }
            } else {
                Main.getMainFrame().setVisible(true);
            }
        }
    }

    /**
     * タスクトレイポップアップメニューの(再)表示
     */
    public void refreshPopupMenu() {
        if (Main.getMainFrame() != null) {
            this.getPopupMenu().removeAll();
            if (Main.getMainFrame().isVisible()) {
                this.getPopupMenu().add(MENU_HIDE);
            } else {
                this.getPopupMenu().add(MENU_SHOW);
            }
            this.getPopupMenu().add(MENU_CONFIG);
            this.getPopupMenu().addSeparator();
            this.getPopupMenu().add(MENU_EXIT);
        }
    }
    // TODO 新規作成

    // TODO 定期同期
}
