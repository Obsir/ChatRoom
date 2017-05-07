package client;

import protocol.Protocol;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Obser on 2017/5/6.
 */
public class Register {
    private JPasswordField txt_confirm_pwd;
    private JButton btn_cancel;
    private JButton btn_confirm;
    private JTextField txt_name;
    private JLabel label_port;
    private JLabel label_pwd;
    private JLabel label_confirm_pwd;
    private static JFrame frame;
    private JPanel panel;
    private JPasswordField txt_pwd;
    private JLabel label_title;
    private Pattern pattern;
    private ActionListener confirmListener;
    private int port;
    private String hostIp;
    private BufferedReader in;
    private Socket socket;
    private PrintWriter out;

    public static void initView(int port, String hostIp){
        frame =  new JFrame("注册");
        frame.setContentPane(new Register(port, hostIp).panel);
        frame.pack();
        int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);
        frame.setResizable(false);
        frame.setVisible(true);
    }
    private void initData(int port, String hostIp){
        //初始化正则表达式
        pattern = Pattern.compile("[a-zA-Z0-9_]{1,20}");
        this.port = port;
        this.hostIp = hostIp;
    }
    private void initListener(){
        //建立确认侦听
        confirmListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = txt_name.getText();
                String pwd = String.valueOf(txt_pwd.getPassword());
                String confirm_pwd = String.valueOf(txt_confirm_pwd.getPassword());

                Matcher match_name = pattern.matcher(name);
                Matcher match_pwd = pattern.matcher(pwd);
                boolean flag_name = match_name.matches();
                boolean flag_pwd = match_pwd.matches();
                boolean flag_confirm = confirm_pwd.equals(pwd);

                try{
                    if(!flag_name || !flag_pwd)
                        throw new Exception("用户名和密码由20个字符以内的数字和字母组成!");
                    if(!flag_confirm)
                        throw new Exception("两次输入的密码不一致!");
                    connectServer(name, pwd);
                } catch (Exception exc){
                    JOptionPane.showMessageDialog(frame, exc.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        btn_confirm.addActionListener(confirmListener);
        txt_confirm_pwd.addActionListener(confirmListener);

        //注册取消事件
        btn_cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        //设置关闭侦听
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
            }
        });
    }

    /**
     * 向服务器请求注册
     * @param name
     * @param password
     * @throws Exception
     */
    private void connectServer(String name, String password) throws Exception{
        try{
            //根据端口号和服务器ip建立连接
            socket = new Socket(hostIp, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            //初始化注册账号消息包
            Protocol.Message message = new Protocol.Message();
            message.setName(name);
            message.setPassword(password);
            message.setId(Protocol.CONFIRM);
            //发送消息
            out.println(Protocol.packMessage(message));
            out.flush();
            //开启消息侦听线程
            new ConfirmThread().start();
        } catch (Exception e){
            e.printStackTrace();
            throw new Exception("与服务器连接失败!");
        }
    }

    /**
     * 接收来自服务器的注册消息
     */
    private class ConfirmThread extends Thread{
        @Override
        public void run() {
            String json = "";
            while(true){
                try {
                    json = in.readLine();
                    if(json == null)
                        continue;
                    Protocol.Message message = Protocol.unPackMessage(json);
                    if(message.getId().equals(Protocol.CONFIRM)){//注册成功
                        if(message.getConfirmed()){
                            JOptionPane.showMessageDialog(frame, "注册成功!", "错误", JOptionPane.INFORMATION_MESSAGE);
                            //关闭注册界面
                            frame.dispose();
                            closeSocket();
                            break;
                        }
                        else{//注册失败,关闭当前界面
                            JOptionPane.showMessageDialog(frame, "用户名已存在!", "错误", JOptionPane.ERROR_MESSAGE);
                            closeSocket();
                            break;
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 关闭当前连接
     */
    private void closeSocket(){
        Utils.closeQuietly(socket);
        Utils.closeQuietly(in);
        Utils.closeQuietly(out);
    }

    public Register(int port, String hostIp){
        initData(port, hostIp);
        initListener();
    }


}
