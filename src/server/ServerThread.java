package server;

import listener.ClientConnectListener;
import listener.ServerConnectListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Obser on 2017/5/2.
 */
public class ServerThread extends Thread {

    private ServerSocket serverSocket;
    private ArrayList<ClientThread> clients;
    private boolean flag;
    private ServerConnectListener listener;

    public void setServerConnectListener(ServerConnectListener listener){
        this.listener = listener;
    }


    public ServerThread(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
        clients = new ArrayList<>();
        flag = true;
    }

    public void setFlag(boolean flag){
        this.flag = flag;
    }


    public void run(){
        while(flag){//不停地等待客户端的连接
            try {
                Socket socket = serverSocket.accept();
                //接收客户端的基本用户信息
                //反馈连接成功信息
                ClientThread client = new ClientThread(socket, clients);
                client.setClientConnectListener(new ClientConnectListener() {
                    @Override
                    public void onDisconnect() {
                        clients.remove(client);
                        if(listener != null)
                            listener.onDisconnect(clients, client);
                    }

                    @Override
                    public void onConnect() {
                        clients.add(client);
                        if(listener != null){
                            listener.onConnect(clients, client);
                        }
                    }

                });
                client.start();//开启对此客户端服务的线程
                //更新UI
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
