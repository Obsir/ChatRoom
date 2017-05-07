package client;

import bean.User;
import listener.MessageAdapter;
import listener.MessageListener;
import protocol.Protocol;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Obser on 2017/5/6.
 */
public class Logger {
    private JTextField txt_ip;
    private JPasswordField txt_pwd;
    private JButton btn_reg;
    private JButton btn_login;
    private JTextField txt_port;
    private JTextField txt_name;
    private JLabel label_ip;
    private JLabel label_port;
    private JLabel label_name;
    private JLabel label_pwd;
    private static JFrame frame;
    private JPanel panel;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private User user;
    private MessageThread messageThread;
    private ActionListener loginListener;

    public Logger(){
        initData();
        initListener();
    }

    private void initData(){
    }

    private static void initView(){
        frame = new JFrame("登录");
        frame.setContentPane(new Logger().panel);
        frame.pack();
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private void initListener() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        loginListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port;
                try{
                    try{
                        port = Integer.parseInt(txt_port.getText().trim());
                    } catch(NumberFormatException e1){
                        throw new Exception("端口号不符合要求!端口号为整数");
                    }
                    String hostIp = txt_ip.getText().trim();
                    String name = txt_name.getText().trim();
                    String password = String.valueOf(txt_pwd.getPassword());
                    if(Utils.isEmpty(hostIp) || Utils.isEmpty(name) || Utils.isEmpty(password)){
                        throw new Exception("姓名、服务器IP及密码不能为空!");
                    }
                    connectServer(port, hostIp, name, password);
                } catch (Exception exc){
                    JOptionPane.showMessageDialog(frame, exc.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        btn_login.addActionListener(loginListener);
        txt_pwd.addActionListener(loginListener);


        btn_reg.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port;
                try{
                    String hostIp = txt_ip.getText().trim();
                    if(Utils.isEmpty(hostIp)){
                        throw new Exception("服务器IP不能为空!");
                    }
                    try{
                        port = Integer.parseInt(txt_port.getText().trim());
                    } catch(NumberFormatException e1){
                        throw new Exception("端口号不符合要求!端口号为整数");
                    }

                    Register.initView(port, hostIp);
                } catch (Exception exc){
                    JOptionPane.showMessageDialog(frame, exc.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


    }

    /**
     * 连接服务器,请求登录
     * @param port
     * @param hostIp
     * @param name
     * @param password
     * @throws Exception
     */
    private void connectServer(int port, String hostIp, String name, String password) throws Exception{
        try {
            socket = new Socket(hostIp, port);//根据端口号和服务器ip建立连接
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            //发送客户端基本用户信息
            user = new User(name, password, socket.getLocalAddress().toString());
            //初始化消息包,发送用户名和密码
            Protocol.Message message = new Protocol.Message();
            message.setName(name);
            message.setPassword(password);
            message.setIp(user.getIp());
            message.setId(Protocol.INIT);
            sendMessage(Protocol.packMessage(message));
            //开启接收消息的线程
            messageThread = new MessageThread(in, out, socket);
            messageThread.setMessageListener(new MessageAdapter(){
                @Override
                public void onConnect(Protocol.Message message) {
                    new Client(user, messageThread);
                    frame.dispose();
                }
                @Override
                public void onFailed(Protocol.Message message) {
                    JOptionPane.showMessageDialog(frame, "登录失败!", "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
            messageThread.start();
        } catch (IOException e) {
            throw new Exception("与服务器连接失败!");
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

    public static void main(String[] args) {
        initView();
    }

}
