package server;

import bean.User;
import listener.ClientConnectListener;
import protocol.Protocol;
import utils.DBUtils;
import utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Obser on 2017/5/2.
 */
public class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private User user;
    private ClientConnectListener listener;
    private ArrayList<ClientThread> clients;
    private boolean flag;
    public void setClientConnectListener(ClientConnectListener listener){
        this.listener = listener;
    }

    public User getUser() {
        return user;
    }
    public PrintWriter getOut() {
        return out;
    }
    public BufferedReader getIn() {
        return in;
    }


    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    /**
     * 初始化客户端服务线程
     * @param socket
     * @param clients
     */
    public ClientThread(Socket socket, ArrayList<ClientThread> clients){
        flag = true;
        this.socket = socket;
        this.clients = clients;
        initStream();
    }

    /**
     * 初始化输入输出流
     */
    private void initStream(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不断地接收客户端的消息,进行处理
     */
    public void run(){
        String json = null;
        while(flag){
            try {
                json = in.readLine();
                if(json == null)
                    continue;
                Protocol.Message message = Protocol.unPackMessage(json);
                if(message.getId().equals(Protocol.INIT)){
                    //反馈连接成功信息
                    initClientThread(message);
                } else if(message.getId().equals(Protocol.CLOSE)){
                    //下线命令
                    closeClientThread(message);
                } else if(message.getId().equals(Protocol.MSG_ALL)){
                    //转发多人消息
                    sendAll(json);
                } else if(message.getId().equals(Protocol.MSG_PRIVATE_TO)){
                    //转发私人消息
                    sendPrivate(message);
                } else if(message.getId().equals(Protocol.CONFIRM))
                    //注册账号
                    registerAccount(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 注册账户
     */
    private void registerAccount(Protocol.Message message) {
        DBUtils dbUtils = DBUtils.newInstance();
        boolean confirmed = dbUtils.add(message.getName(), message.getPassword());//获取注册的结果
        message.setConfirmed(confirmed);
        //发送注册结果
        out.println(Protocol.packMessage(message));
        out.flush();
        //关闭线程
        flag = false;
        closeStream();
    }

    /**
     * 私聊
     * @param message
     */
    private void sendPrivate(Protocol.Message message) {
        for(ClientThread client : clients){
            if(client.getUser().getName().equals(message.getToUser())){
                //私聊对象
                message.setId(Protocol.MSG_PRIVATE_TO);
                client.getOut().println(Protocol.packMessage(message));
                client.getOut().flush();
            } else if(client.getUser().getName().equals(message.getName())){
                //私聊来源
                message.setId(Protocol.MSG_PRIVATE_FROM);
                client.getOut().println(Protocol.packMessage(message));
                client.getOut().flush();
            }
        }
    }

    /**
     * 对客户端发送基本信息
     * @param message
     */
    private void initClientThread(Protocol.Message message){
        //接收客户端的基本用户信息
        user = new User(message.getName(), message.getIp());
        DBUtils dbUtils = DBUtils.newInstance();
        boolean checked = dbUtils.check(message);
        if(checked){
            if(listener != null)
                listener.onConnect();
            message.setId(Protocol.INIT);
            dbUtils.updateState(true, message.getName());
        } else {
            message.setId(Protocol.FAILED);
        }
        out.println(Protocol.packMessage(message));
        out.flush();
        //向所有在线用户发送该用户上线命令
        sendList(message);
    }

    /**
     * 群发消息
     * @param json
     */
    private void sendAll(String json){
        for(ClientThread client : clients){
            client.getOut().println(json);
            client.getOut().flush();
        }
    }
    /**
     * 发送在线用户列表
     * @param message
     */
    private void sendList(Protocol.Message message){
        List<String> nameList = new ArrayList<>();
        for(ClientThread client : clients){
            nameList.add(client.getUser().getName());
        }
        message.setId(Protocol.LIST);
        message.setNameList(nameList);

        sendAll(Protocol.packMessage(message));
    }
    /**
     * 关闭当前客户端服务线程
     * @param message
     */
    public void closeClientThread(Protocol.Message message){
        //更新UI
        if(listener != null)
            listener.onDisconnect();
        //更新数据库中用户的状态
        DBUtils dbUtils = DBUtils.newInstance();
        dbUtils.updateState(false, user.getName());
        //断开连接释放资源
        closeStream();
        //向所有在线用户发送该用户的下线命令
        sendList(message);
        //删除此条客户端服务线程
        setFlag(false);
    }

    /**
     * 关闭输入输出流
     */
    private void closeStream(){
        Utils.closeQuietly(in);
        Utils.closeQuietly(out);
        Utils.closeQuietly(socket);
    }

    /**
     * 通知客户端服务器已关闭
     */
    public void closeClientService(){
        Protocol.Message message = new Protocol.Message(Protocol.CLOSE);
        //更新数据库中用户状态
        DBUtils dbUtils = DBUtils.newInstance();
        dbUtils.updateState(false, user.getName());
        out.println(Protocol.packMessage(message));
        out.flush();
        //关闭当前线程
        closeStream();
        setFlag(false);
    }

}
