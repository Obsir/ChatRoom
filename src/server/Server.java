package server;

import listener.ServerConnectListener;
import utils.DBUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * Created by Obser on 2017/5/2.
 */
public class Server {
    private JFrame frame;
    private JTextArea contentArea;
    private JTextField txt_port;
    private JButton btn_start;
    private JButton btn_stop;
    private DefaultListModel<String> listModel;
    private JList userList;
    private JScrollPane leftPanel;
    private JScrollPane rightPanel;
    private JSplitPane centerSplit;
    private JPanel northPanel;
    private boolean isStart;
    private ServerSocket serverSocket;
    private ArrayList<ClientThread> clients;
    private ServerThread serverThread;

    public Server(){
        initData();
        initView();
        initListener();
    }

    private void initData(){
        isStart = false;
        DBUtils.newInstance();
    }
    private void initListener(){
        //关闭窗口事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isStart) {
                    closeServer();
                }
                System.exit(0);
            }
        });

        //单击启动服务器按钮事件
        btn_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port;
                try {
                    try {
                        port = Integer.parseInt(txt_port.getText());
                    } catch (Exception e1) {
                        throw new Exception("端口号为正整数!");
                    }
                    if(port <= 0){
                        throw new Exception("端口号为正整数!");
                    }
                    serverStart(port);
                    appendText("服务器已启动成功!端口:" + port + "\r\n");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
                refreshButton(!isStart);
            }
        });

        //单击停止服务器按钮事件
        btn_stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    closeServer();
                    appendText("服务器成功停止!\r\n");
                }catch (Exception exc){
                    JOptionPane.showMessageDialog(frame, "停止服务器发生异常!", "错误", JOptionPane.ERROR_MESSAGE);
                }
                refreshButton(!isStart);
            }
        });
    }

    /**
     * 关闭服务器
     */
    private void closeServer() {
        try{
            if(serverThread != null)
                serverThread.setFlag(false);//停止服务器线程
            for(ClientThread client : clients){
                client.closeClientService();
            }
            if(serverSocket != null){
                serverSocket.close();//关闭服务器端连接
            }
            listModel.removeAllElements();//清空用户列表
            clients.clear();
            isStart = false;
        } catch (IOException e){
            e.printStackTrace();
            isStart = true;
        }
        finally {
            refreshButton(!isStart);
        }
    }

    private void initView(){
        frame = new JFrame("服务器");
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(Color.blue);
        txt_port = new JTextField("6666");
        btn_start = new JButton("启动");
        btn_stop = new JButton("停止");
        refreshButton(true);
        listModel = new DefaultListModel();
        userList = new JList(listModel);

        leftPanel = new JScrollPane(userList);
        leftPanel.setBorder(BorderFactory.createTitledBorder("在线用户"));

        rightPanel = new JScrollPane(contentArea);
        rightPanel.setBorder(BorderFactory.createTitledBorder("消息显示区"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        centerSplit.setDividerLocation(100);

        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 4));
        northPanel.add(new JLabel("             端口号:"));
        northPanel.add(txt_port);
        northPanel.add(btn_start);
        northPanel.add(btn_stop);
        northPanel.setBorder(BorderFactory.createTitledBorder("配置信息"));

        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.setSize(600, 400);
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
        frame.setVisible(true);
    }

    private void refreshList(ArrayList<ClientThread> clients){
        this.clients = clients;
        listModel.removeAllElements();
        for(ClientThread client : clients){
            listModel.addElement(client.getUser().getName());
        }
    }
    /**
     * 启动服务器
     * @param port
     * @throws BindException
     */
    private void serverStart(int port) throws BindException {
        clients = new ArrayList<>();
        try {
            serverSocket = new ServerSocket(port);
            serverThread = new ServerThread(serverSocket);
            serverThread.setServerConnectListener(new ServerConnectListener() {
                @Override
                public void onConnect(ArrayList<ClientThread> clients, ClientThread client) {
                    refreshList(clients);
                    appendText(client.getUser().getName() + client.getUser().getIp() + "上线!\r\n");
                }

                @Override
                public void onDisconnect(ArrayList<ClientThread> clients, ClientThread client) {
                    refreshList(clients);
                    appendText(client.getUser().getName() + client.getUser().getIp() + "下线!\r\n");
                }
            });
            serverThread.start();
            isStart = true;
        } catch (BindException e) {
            e.printStackTrace();
            isStart = false;
            throw new BindException("端口号已被占用,请更改!");
        } catch (Exception e1){
            e1.printStackTrace();
            isStart = false;
            throw new BindException("启动服务器异常!");
        }
    }

    /**
     * 更新contentArea
     * @param text
     */
    private void appendText(String text){
        contentArea.append(text);
        contentArea.setCaretPosition(contentArea.getText().length());
    }

    /**
     * 刷新Button
     * @param flag
     */
    private void refreshButton(boolean flag){
        btn_start.setEnabled(flag);
        btn_stop.setEnabled(!flag);
        txt_port.setEnabled(flag);
    }

    public static void main(String[] args){
        new Server();
    }
}
