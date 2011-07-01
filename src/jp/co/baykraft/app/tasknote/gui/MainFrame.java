package jp.co.baykraft.app.tasknote.gui;


import java.awt.BorderLayout;
import java.awt.SystemTray;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class MainFrame extends JFrame {

    public MainFrame() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                System.out.println("windowActivated");
                TrayIconWithMenu trayIcon = (TrayIconWithMenu) SystemTray.getSystemTray().getTrayIcons()[0];
                trayIcon.refreshPopupMenu();
            }
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("windowClosing");
                TrayIconWithMenu trayIcon = (TrayIconWithMenu) SystemTray.getSystemTray().getTrayIcons()[0];
                //TODO 間に合ってない
                trayIcon.refreshPopupMenu();
            }
        });

        this.setTitle("たすくのーと");
        this.setBounds(100, 100, 200, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //TODO this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        ImageIcon icon = new ImageIcon("./src/jp/co/baykraft/app/tasknote/resources/enlogo.png");
        this.setIconImage(icon.getImage());

        this.add(new MainPane(), BorderLayout.CENTER);
        this.setVisible(true);
    }
}
