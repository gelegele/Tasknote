package jp.co.baykraft.app.tasknote.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.management.RuntimeErrorException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jp.co.baykraft.app.tasknote.edam.EdamService;

import org.apache.thrift.TException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Note;

public class MainPane extends JSplitPane implements ActionListener, ListSelectionListener, MouseListener {

    /**
     * EDAMサービス
     */
    private EdamService edamService;
    /**
     * 対象となるNoteリスト
     */
    private List<Note> notes;
    /**
     * リストモデル
     */
    private DefaultListModel noteListModel;
    /**
     * リストポップアップメニュー
     */
    private JPopupMenu listPopupMenu;
    /**
     * 更新ボタン
     */
    private JButton btnUpdate;

    /**
     * リストコンポーネント
     */
    private JList noteList;
    /**
     * 本文テキストエリア
     */
    private JTextArea noteText;

    private static final String BUTTON_UPDATE = "更新";
    private static final String BUTTON_UPLOAD = "登録";
    private static final String MENU_DELETE =   "削除";

    /**
     * コンストラクタ
     */
    public MainPane() {
        super(VERTICAL_SPLIT);
        this.setDividerLocation(200);

        // スプリット上部
        JPanel topPanel = new JPanel(new BorderLayout());

        // ボタンパネル
        btnUpdate = new JButton(BUTTON_UPDATE);
        btnUpdate.addActionListener(this);
        JButton btnUploadNote = new JButton(BUTTON_UPLOAD);
        btnUploadNote.addActionListener(this);
        JPanel pnlButtons = new JPanel(new GridLayout(1, 2));
        pnlButtons.add(btnUpdate);
        pnlButtons.add(btnUploadNote);
        topPanel.add(pnlButtons, BorderLayout.NORTH);

        // リスト
        noteListModel = new DefaultListModel();
        noteList = new JList(noteListModel);
        noteList.setCellRenderer(new NoteListCellRenderer());
        noteList.addListSelectionListener(this);
        noteList.addMouseListener(this);
        JScrollPane noteListScrollPane = new JScrollPane(noteList);
        topPanel.add(noteListScrollPane, BorderLayout.CENTER);

        // リストポップアップメニュー
        listPopupMenu = new JPopupMenu();
        JMenuItem miDelete = new JMenuItem(MENU_DELETE);
        miDelete.addActionListener(this);
        listPopupMenu.add(miDelete);

        this.setTopComponent(topPanel);

        // スプリット下部
        noteText = new JTextArea();
        JScrollPane noteTextScrollPane = new JScrollPane(noteText);
        this.setBottomComponent(noteTextScrollPane);

        edamService = new EdamService();
        try {
            edamService.authenticate();
            this.updateNoteList();
        } catch (TException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (EDAMUserException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    /**
     * 更新
     */
    private void updateNoteList() {
        btnUpdate.setEnabled(false);
        try {
            notes = edamService.findNoteList();
            noteListModel.removeAllElements();
            noteText.setText(null);
            for (Note note : notes) {
                note.setContent(edamService.findNoteContent(note.getGuid()));
                noteListModel.addElement(note);
                System.out.println(" " + note.getContent());
            }
        } catch (EDAMUserException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (EDAMSystemException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (EDAMNotFoundException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (TException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        btnUpdate.setEnabled(true);
    }

    /**
     *
     * @param update
     */
    private void uploadNote(boolean update) {
        Note note = new Note();
        Date date = new Date();
        //note.setTitle(date.toString());
        note.setTitle("This is TITLE!");
        note.setContent("This is CONTENT!");
        edamService.createOrUploadNote(note);
        if (update) {
            this.updateNoteList();
        }
    }

    private void deleteNotes() {
        while (true) {
            if (0 == noteList.getSelectedIndices().length) {
                break;
            }
            int index = noteList.getSelectedIndices()[0];
            Note note = (Note) noteListModel.get(index);
            edamService.deleteNote(note.getGuid());
            noteListModel.remove(index);
        }
    }

    /**
     * ボタンアクションハンドラ
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        String actionCommand = event.getActionCommand();
        if (BUTTON_UPLOAD.equals(actionCommand)) {
            this.uploadNote(true);
        } else if (BUTTON_UPDATE.equals(actionCommand)) {
            this.updateNoteList();
        } else if (MENU_DELETE.equals(actionCommand)) {
            this.deleteNotes();
        }
    }

    /**
     * リスト選択変更ハンドラ
     */
    @Override
    public void valueChanged(ListSelectionEvent event) {
        if (event.getValueIsAdjusting()) {
            System.out.println("valueIsAdjusting");
            return;
        }
        if (0 == noteList.getSelectedIndices().length){
            System.out.println("setText(null)");
            noteText.setText(null);
        } else {
            System.out.println("setText()");
            // 選択されたノートの本文を本文テキストエリアに表示
            Note note = (Note) noteList.getSelectedValue();
//            String text = parseByDom(note.getContent());
//            noteText.setText(text);
            parseBySax(note.getContent());
        }
    }

    private String parseByDom(String sourceXml) {
        DocumentBuilder builder = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        ByteArrayInputStream is =
            new ByteArrayInputStream(sourceXml.getBytes());
        Document document = null;
        try {
            document = builder.parse(is); // 重いな
        } catch (SAXException e) {
            // 文法エラーが発生した場合
            e.printStackTrace();
        } catch (IOException e) {
            // ファイルが読み込めなかった場合
            e.printStackTrace();
        }
        Element element = document.getDocumentElement();
        return element.getTextContent();
    }

    private void parseBySax(String sourceXml) {
        try {
            // SAXパーサーファクトリを生成
            SAXParserFactory spfactory = SAXParserFactory.newInstance();
            // SAXパーサーを生成
            SAXParser parser = spfactory.newSAXParser();
            // XMLファイルを指定されたデフォルトハンドラーで処理します
            ByteArrayInputStream is =
                new ByteArrayInputStream(sourceXml.getBytes());
            parser.parse(is, new MySAXHandler());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO 自動生成されたメソッド・スタブ
        if (e.isPopupTrigger()) {
            listPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    class MySAXHandler extends DefaultHandler {
        private StringBuilder stringBuilder;
        // ContentHandlerの実装
        public void startDocument() throws SAXException {
            System.out.println("startDocument()");
        }
        public void endDocument() throws SAXException {
            System.out.println("endDocument()");
        }
        public void startElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName,
                           Attributes atts)
                    throws SAXException {
            System.out.println("startElement()");
            System.out.println("\tnamespace=" + uri);
            System.out.println("\tlocal name=" + localName);
            System.out.println("\tqualified name=" + qName);
            for (int i = 0; i < atts.getLength(); i++) {
                System.out.println("\tattribute name=" + atts.getLocalName(i));
                System.out.println("\tattribute qualified name=" + atts.getQName(i));
                System.out.println("\tattribute value=" + atts.getValue(i));
            }
        }
        public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName)
                    throws SAXException {
            System.out.println("endElement()");
            if ("en-note".equals(qName)) {
                noteText.setText(stringBuilder.toString());
            }
        }
        public void characters(char[] ch, int start, int length) throws SAXException {
            String str = new String(ch, start, length);
            System.out.println("characters()" + str);
            stringBuilder.append(str);
        }
        // ErrorHandlerの実装
        public void warning(SAXParseException e) {
            System.out.println("警告: " + e.getLineNumber() +"行目");
            System.out.println(e.getMessage());
        }
        public void error(SAXParseException e) {
            System.out.println("エラー: " + e.getLineNumber() +"行目");
            System.out.println(e.getMessage());
        }
        public void fatalError(SAXParseException e) {
            System.out.println("深刻なエラー: " + e.getLineNumber() +"行目");
            System.out.println(e.getMessage());
        }
    }
}
