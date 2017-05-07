package client;

import bean.User;
import listener.MessageListener;
import protocol.Protocol;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

/**
 * Created by Obser on 2017/5/2.
 */
public class Client {
    private JTextArea textArea;
    private JTextField textField;
    private JTextField txt_port;
    private JTextField txt_hostIp;
    private JButton btn_start;
    private JButton btn_stop;
    private JButton btn_send;
    private DefaultListModel listModel;
    private JList userList;
    private JPanel northPanel;
    private JScrollPane rightScroll;
    private JPanel southPanel;
    private JScrollPane leftScroll;
    private JSplitPane centerSplit;
    private JFrame frame;
    private boolean isConnected;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageThread messageThread;
    private User user;
    private JComboBox<String> comboBox;
    private static final String ALL = "所有人";
    private MessageListener messageListener;

    public Client(User user, MessageThread messageThread){
        initData(user, messageThread);
        initView();
        initListener();
    }
    private void initData(User user, MessageThread messageThread){
        isConnected = true;
        this.user = user;
        this.messageThread = messageThread;
        this.in = messageThread.getIn();
        this.out = messageThread.getOut();
        this.socket = messageThread.getSocket();
    }
    private void initView() {
        textArea = new JTextArea();
        textArea.setEnabled(false);
        textField = new JTextField();
        txt_port = new JTextField("6666");
        txt_hostIp = new JTextField("127.0.0.1");
        btn_start = new JButton("连接");
        btn_stop = new JButton("断开");
        btn_send = new JButton("发送");
        listModel = new DefaultListModel();
        userList = new JList(listModel);
        comboBox = new JComboBox<>();
        comboBox.addItem(ALL);

        refreshButton(!isConnected);

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 7));
        northPanel.add(new JLabel("端口"));
        northPanel.add(txt_port);
        northPanel.add(new JLabel("服务器IP"));
        northPanel.add(txt_hostIp);
        northPanel.add(btn_start);
        northPanel.add(btn_stop);
        northPanel.setBorder(BorderFactory.createTitledBorder("连接信息"));

        rightScroll = new JScrollPane(textArea);
        rightScroll.setBorder(BorderFactory.createTitledBorder("消息显示区"));
        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("在线用户"));
        southPanel = new JPanel(new BorderLayout());
        southPanel.add(comboBox, BorderLayout.WEST);
        southPanel.add(textField, BorderLayout.CENTER);
        southPanel.add(btn_send, BorderLayout.EAST);
        southPanel.setBorder(BorderFactory.createTitledBorder("写消息"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        centerSplit.setDividerLocation(100);

        frame = new JFrame(user.getName());
        frame.setLayout(new BorderLayout());
        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(centerSplit, BorderLayout.CENTER);
        frame.add(southPanel, BorderLayout.SOUTH);
        frame.setSize(600, 400);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
        frame.setVisible(true);
    }
    private void initListener() {
        messageListener = new MessageListener() {
            @Override
            public void onReceiveAll(Protocol.Message message) {
                appendText("\t" + message.getTime() + "\r\n");
                appendText("[所有人]  " + message.getName() + " 说:\r\n" + message.getMessage() + "\r\n");
            }

            @Override
            public void onClose() throws Exception {
                close();
                appendText("服务器已关闭!\r\n");
            }

            @Override
            public void onConnect(Protocol.Message message) {
                appendText("与端口号为:" + txt_port.getText().trim() + "   IP地址为:" + txt_hostIp.getText().trim() + "的服务器连接成功!" + "\r\n");
                listModel.removeAllElements();
            }

            @Override
            public void onReceiveList(Protocol.Message message) {
                refreshListModel(message);
            }

            @Override
            public void onReceivePrivate(Protocol.Message message, boolean flag) {
                appendText("\t" + message.getTime() + "\r\n");
                if(flag)
                    appendText("[悄悄话]   " + message.getName() + " 对 你 说:\r\n" + message.getMessage() + "\r\n");
                else
                    appendText("[悄悄话]  你 对 " +message.getToUser() + " 说:\r\n" + message.getMessage() + "\r\n");
            }

            @Override
            public void onFailed(Protocol.Message message) {

            }
        };

        messageThread.setMessageListener(messageListener);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(isConnected){
                    try {
                        closeConnection();//关闭连接
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });

        btn_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port;
                try{
                    try{
                        port = Integer.parseInt(txt_port.getText().trim());
                    } catch(NumberFormatException e1){
                        throw new Exception("端口号不符合要求!端口号为整数");
                    }
                    String hostIp = txt_hostIp.getText().trim();
                    if(Utils.isEmpty(hostIp)){
                        throw new Exception("服务器IP不能为空!");
                    }
                    connectServer(port, hostIp);
                    //反馈
                } catch (Exception exc){
                    JOptionPane.showMessageDialog(frame, exc.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
                refreshButton(!isConnected);
            }
        });

        btn_stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    closeConnection();//断开连接
                }catch (Exception exc){
                    JOptionPane.showMessageDialog(frame, exc.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
                refreshButton(!isConnected);
            }
        });

        //写消息的文本框中按回车键时事件
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(comboBox.getSelectedItem().equals(ALL)){
                    send("", Protocol.MSG_ALL);
                } else {
                    send((String)comboBox.getSelectedItem(), Protocol.MSG_PRIVATE_TO);
                }
            }
        });

        //单击发送按钮时事件
        btn_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(comboBox.getSelectedItem().equals(ALL))
                    send("", Protocol.MSG_ALL);
                else
                    send((String) comboBox.getSelectedItem(), Protocol.MSG_PRIVATE_TO);
            }
        });
    }

    /**
     * 客户端断开连接
     * @throws Exception
     */
    private void closeConnection() throws Exception{
        Protocol.Message message = new Protocol.Message(Protocol.CLOSE);
        sendMessage(Protocol.packMessage(message));//发送断开连接的命令给服务器
        close();
    }

    /**
     * 断开连接
     * @throws Exception
     */
    private synchronized void close() throws Exception{
        try{
//            messageThread.stop();//停止接收消息的线程
            messageThread.setFlag(false);//停止接收消息的线程
            //释放资源
            if(in != null){
                in.close();
            }
            if(out != null){
                out.close();
            }
            if(socket != null){
                socket.close();
            }
            isConnected = false;
            listModel.removeAllElements();
        } catch (IOException e1){
            e1.printStackTrace();
            isConnected = true;
            throw new Exception("断开连接发生异常!");
        }
        finally {
            refreshButton(!isConnected);
        }
    }

    /**
     * 发送消息
     * @param message
     */
    private void sendMessage(String message) {
        out.println(message);
        out.flush();
    }

    /**
     * 执行发送
     */
    private void send(String toUser, String id){
        if(!isConnected){
            JOptionPane.showMessageDialog(frame, "还没有连接服务器,无法发送消息", "错误", JOptionPane.ERROR_MESSAGE);
            return ;
        }
        String message = textField.getText().trim();
        if(Utils.isEmpty(message)){
            return ;
        }
        Protocol.Message msg = new Protocol.Message(user.getName(), user.getIp(), toUser, message, id);
        sendMessage(Protocol.packMessage(msg));
        textField.setText("");
    }


    /**
     * 连接服务器
     * @param port 端口号
     * @param hostIp 主机地址
     * @throws Exception
     */
    public void connectServer(int port, String hostIp) throws Exception{
        try {
            socket = new Socket(hostIp, port);//根据端口号和服务器ip建立连接
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            //发送客户端基本用户信息
            Protocol.Message message = new Protocol.Message(user.getName(), user.getIp(), Protocol.INIT);
            message.setPassword(user.getPassword());
            sendMessage(Protocol.packMessage(message));
            //开启接收消息的线程
            messageThread = new MessageThread(in, out, socket);
            messageThread.setMessageListener(messageListener);
            messageThread.start();
            isConnected = true;//已经连接上
        } catch (IOException e) {
            isConnected = false;//未连接上
            appendText("与端口号为:" + port + "   IP地址为:" + hostIp + "的服务器连接失败!" + "\r\n");
            throw new Exception("与服务器连接失败!");
        }
    }

    /**
     * 更新textArea
     * @param text
     */
    private void appendText(String text){
        textArea.append(text);
        textArea.setCaretPosition(textArea.getText().length());
    }

    /**
     * 刷新列表
     * @param message
     */
    private void refreshListModel(Protocol.Message message){
        listModel.removeAllElements();
        comboBox.removeAllItems();
        comboBox.addItem(ALL);
        for(String name : message.getNameList()){
            listModel.addElement(name);
            if(name.equals(user.getName()))
                continue;
            comboBox.addItem(name);
        }

    }

    /**
     * 刷新按钮
     * @param flag
     */
    private void refreshButton(boolean flag){
        btn_start.setEnabled(flag);
        btn_stop.setEnabled(!flag);
        txt_hostIp.setEnabled(flag);
        txt_port.setEnabled(flag);
    }

}
