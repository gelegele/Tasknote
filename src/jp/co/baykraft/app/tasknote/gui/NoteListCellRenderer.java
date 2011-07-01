package jp.co.baykraft.app.tasknote.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.evernote.edam.type.Note;

/**
 * TODOリストレンダラー
 */
public class NoteListCellRenderer extends JLabel implements ListCellRenderer {

    /**
     * コンストラクタ
     */
    public NoteListCellRenderer() {
        this.setOpaque(true);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Note note = (Note)value;
        this.setText(note.getTitle()); // Noteタイトルを表示

        if (isSelected){
            setForeground(Color.white);
            setBackground(Color.blue);
        }else{
            setForeground(Color.black);
            // 一行ごとに色を変える
            if (index % 2 == 0){
                setBackground(Color.white);
            }else{
                setBackground(Color.lightGray);
            }
        }
        return this;
    }
}
