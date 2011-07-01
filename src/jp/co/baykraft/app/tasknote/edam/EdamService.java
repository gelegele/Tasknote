package jp.co.baykraft.app.tasknote.edam;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.co.baykraft.app.tasknote.Main;
import jp.co.baykraft.app.tasknote.dao.ConfigDao;
import jp.co.baykraft.app.tasknote.entity.Config;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import com.evernote.edam.error.EDAMErrorCode;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.UserStore;

public class EdamService extends SwingWorker<Object, Object> {

    private UserStore.Client userStore;
    private AuthenticationResult authResult;
    private NoteStore.Client noteStore;

    /**
     * コンストラクタ
     */
    public EdamService() {
    }
    /**
     * Userを返す。
     * @return User
     */
    public User getUser() {
        if (authResult == null) {
            throw new IllegalStateException("AuthenticationResultが未取得");
        }
        return authResult.getUser();
    }

    /**
     * ログインします
     * @throws TException
     * @throws EDAMSystemException
     * @throws EDAMUserException
     */
    public void authenticate() throws TException, EDAMSystemException, EDAMUserException {
        // システム設定情報の読み込み
        ResourceBundle resBundle = ResourceBundle.getBundle("jp.co.baykraft.app.tasknote.resources.edam");
        String evernoteHost = resBundle.getString("evernote.host");      // 接続先
        String consumerKey = resBundle.getString("consumer.key");        // APIキー
        String consumerSecret = resBundle.getString("consumer.secret");  // APIパスワード
        // ユーザー設定情報の読み込み
        ConfigDao configDao = new ConfigDao(Main.getConnection());
        Config config = configDao.find();

        String userStoreUrl = "https://" + evernoteHost + "/edam/user";

        THttpClient userStoreTrans = new THttpClient(userStoreUrl);
        if (config.isViaProxy()) {
            // プロキシ使用
            userStoreTrans.setProxy(config.getProxyHost(), config.getProxyPort());
        }
        TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
        userStore = new UserStore.Client(userStoreProt, userStoreProt);
        boolean versionOk = userStore.checkVersion("Evernote's EDAMDemo (Java)",
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
                com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
        if (!versionOk) {
            throw new RuntimeException("Incomatible EDAM client protocol version");
        }

        try {
            authResult = userStore.authenticate(config.getUsername(), config.getPassword(), consumerKey, consumerSecret);
        } catch (EDAMUserException ex) {
            String parameter = ex.getParameter();
            EDAMErrorCode errorCode = ex.getErrorCode();
            System.err.println("Authentication failed (parameter: " + parameter + " errorCode: " + errorCode + ")");
            if (errorCode == EDAMErrorCode.INVALID_AUTH) {
                if (parameter.equals("consumerKey")) {
                    if (consumerKey.equals("en-edamtest")) {
                        System.err.println("You must replace the variables consumerKey and consumerSecret with the values you received from Evernote.");
                    } else {
                        System.err.println("Your consumer key was not accepted by " + evernoteHost);
                    }
                    System.err.println("If you do not have an API Key from Evernote, you can request one from http://www.evernote.com/about/developer/api");
                } else if (parameter.equals("username")) {
                    System.err.println("You must authenticate using a username and password from " + evernoteHost);
                    if (evernoteHost.equals("www.evernote.com") == false) {
                        System.err.println("Note that your production Evernote account will not work on " + evernoteHost + ",");
                        System.err.println("you must register for a separate test account at https://" + evernoteHost + "/Registration.action");
                    }
                } else if (parameter.equals("password")) {
                    System.err.println("The password that you entered is incorrect");
                }
            }
            throw ex;
        }

        String noteStoreUrlBase = "http://" + evernoteHost + "/edam/note/";
        String noteStoreUrl = noteStoreUrlBase + authResult.getUser().getShardId();
        THttpClient noteStoreTrans = new THttpClient(noteStoreUrl);
        if (config.isViaProxy()) {
            // プロキシ使用
            noteStoreTrans.setProxy(config.getProxyHost(), config.getProxyPort());
        }
        TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
        noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);
    }

    /**
     * 対象タグが設定されているNoteリストを取得します。
     * @return Noteリスト
     * @throws EDAMUserException
     * @throws EDAMSystemException
     * @throws EDAMNotFoundException
     * @throws TException
     */
    public List<Note> findNoteList() throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        if (noteStore == null) {
            throw new IllegalStateException("NoteStoreが未取得");
        }
        ConfigDao configDao = new ConfigDao(Main.getConnection());
        Config config = configDao.find();
        String tagId = null;
        List<Tag> tagList = findTagList();
        for (Tag tag : tagList) {
            if (tag.getName().equals(config.getTagName())) {
                tagId = tag.getGuid();
                break;
            }
        }

        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.CREATED.getValue());
        filter.setAscending(true);
        List<String> tagGuidList = new ArrayList<String>();
        tagGuidList.add(tagId);
        filter.setTagGuids(tagGuidList);
        NoteList noteList = noteStore.findNotes(authResult.getAuthenticationToken(), filter, 0, 100);
        return (List<Note>) noteList.getNotes();
    }
    /**
     * Tagリストを取得します。
     * @return Tagリスト
     * @throws EDAMUserException
     * @throws EDAMSystemException
     * @throws TException
     */
    private List<Tag> findTagList() throws EDAMUserException, EDAMSystemException, TException {
        if (noteStore == null) {
            throw new IllegalStateException("NoteStoreが未取得");
        }
        return noteStore.listTags(authResult.getAuthenticationToken());
    }

    /**
     * ノートの本文を取得します。
     * @param guid Noteのguid
     * @return ノート本文
     * @throws EDAMUserException
     * @throws EDAMSystemException
     * @throws EDAMNotFoundException
     * @throws TException
     */
    public String findNoteContent(String guid) throws EDAMUserException, EDAMSystemException, EDAMNotFoundException, TException {
        String noteContents = noteStore.getNoteContent(authResult.getAuthenticationToken(), guid);
        // TODO en-noteタグを抽出し、改行のみ残してhtml装飾を排除したものにする
        return noteContents;
    }

    public void uploadNoteList(List<Note> noteList) {
    }

    public void createOrUploadNote(Note note) {
        File noteFile = this.createNoteFile(note.getContent(), note.getTitle());
        try {
            // デフォルトノートブックのノートとして更新する
            List<Notebook> noteBollList = this.findNotebookList();
            Notebook defaultNotebook = noteBollList.get(0);
            this.uploadNote(defaultNotebook.getGuid(), noteStore, authResult.getAuthenticationToken(), noteFile);
        } catch (Exception e1) {
            // TODO 自動生成された catch ブロック
            e1.printStackTrace();
        }
    }
    /**
     * Notebookリストを取得します。
     * @return Notebookリスト
     * @throws EDAMUserException
     * @throws EDAMSystemException
     * @throws TException
     */
    private List<Notebook> findNotebookList() throws EDAMUserException, EDAMSystemException, TException {
        if (noteStore == null) {
            throw new IllegalStateException("NoteStoreが未取得");
        }
        return (List<Notebook>) noteStore.listNotebooks(authResult.getAuthenticationToken());
    }

    public File createNoteFile(String text, String title) {
        if (title == null || title.length() == 0) {
            title = "無題ノート";
        }

        try {
            DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
            //Document document = docbuilder.newDocument();
            DOMImplementation domImpl = docbuilder.getDOMImplementation();
            DocumentType doctype = domImpl.createDocumentType("en-note", "SYSTEM", "http://xml.evernote.com/pub/enml2.dtd");
            Document document = domImpl.createDocument("", "en-note", doctype);

            /*
             * root要素として en-note要素を生成
             */
            /*
            Element elementEnNote = document.createElement("en-note");
            elementEnNote.setTextContent(text);
            document.appendChild(elementEnNote);
            */

            /*
             * DOMオブジェクトを文字列として出力
             */
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer transformer = tfactory.newTransformer();
            File outfile = new File(title);
            transformer.transform(new DOMSource(document), new StreamResult(outfile));
            return outfile;
        } catch (ParserConfigurationException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
        return null;
    }

    public void uploadNote(String noteGuid, NoteStore.Client noteStore, String authToken, File noteFile) throws Exception {
        // Create a note containing a little text, plus the "enlogo.png" image
        Note note = new Note();
        note.setTitle(noteFile.getName());
        note.setCreated(System.currentTimeMillis());
        note.setUpdated(System.currentTimeMillis());
        note.setActive(true);
        note.setNotebookGuid(noteGuid);
        List<String> tagNameList = new ArrayList<String>();
        ConfigDao configDao = new ConfigDao(Main.getConnection());
        Config config = configDao.find();
        tagNameList.add(config.getTagName());
        note.setTagNames(tagNameList);         // 対象タグ名を付与する
        StringBuilder sb = new StringBuilder();
        FileReader fr = new FileReader(noteFile.getName());
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        fr.close();
        String content = //sb.toString();
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml.dtd\">"
            + "<en-note><div>notenotenote<br/>note</div></en-note>";

            /*"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml.dtd\">"
            + "<en-note>Here's the Evernote logo:<br/>"
            + "</en-note>";*/
        note.setContent(content);
        Note createdNote = noteStore.createNote(authToken, note);
        System.out.println("Note created. GUID: " + createdNote.getGuid());
    }

    public void deleteNote(String noteGuid) {
        try {
            noteStore.deleteNote(authResult.getAuthenticationToken(), noteGuid);
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
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : bytes) {
            int intVal = 0xff & hashByte;
            if (intVal < 0x10) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(intVal));
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doInBackground() throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void done() {

    }
}
