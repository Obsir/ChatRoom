package client;

import bean.User;
import listener.MessageListener;
import protocol.Protocol;

import javax.naming.ldap.SortKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Obser on 2017/5/2.
 */
public class MessageThread extends Thread {
    private BufferedReader in;
    private boolean flag;
    private MessageListener listener;
    private PrintWriter out;
    private Socket socket;
    public void setMessageListener(MessageListener listener){
        this.listener = listener;
    }

    public MessageThread(BufferedReader in, PrintWriter out, Socket socket) {
        this.in = in;
        this.out = out;
        this.socket = socket;
        flag = true;
    }

    public void setFlag(boolean flag){
        this.flag = flag;
    }

    /**
     * 接收来自服务器的消息,并进行相应处理
     */
    @Override
    public void run() {
        String json = "";
        while(flag){
            try{
                json = in.readLine();
                if(json == null)
                    continue;
                Protocol.Message message = Protocol.unPackMessage(json);
                if(message.getId().equals(Protocol.INIT)){//初始化消息
                    if(listener != null)
                        listener.onConnect(message);
                } else if (message.getId().equals(Protocol.CLOSE)){//服务器关闭消息
                    if(listener != null)
                        listener.onClose();
                } else if(message.getId().equals(Protocol.MSG_ALL)){//群发消息
                    if(listener != null)
                        listener.onReceiveAll(message);
                } else if(message.getId().equals(Protocol.LIST)){//更新在线列表消息
                    if(listener != null)
                        listener.onReceiveList(message);
                } else if(message.getId().equals(Protocol.MSG_PRIVATE_TO)){//私聊消息(目的地)
                    if(listener != null)
                        listener.onReceivePrivate(message, true);
                } else if(message.getId().equals(Protocol.MSG_PRIVATE_FROM)){//私聊(来源)
                    if(listener != null){
                        listener.onReceivePrivate(message, false);
                    }
                } else if(message.getId().equals(Protocol.FAILED)){//登录失败消息
                    if(listener != null)
                        listener.onFailed(message);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public BufferedReader getIn() {
        return in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public Socket getSocket() {
        return socket;
    }
}
