package listener;

import server.ClientThread;

import java.util.ArrayList;

/**
 * Created by Obser on 2017/5/4.
 */
public interface ServerConnectListener {
    /**
     * 与客户端建立连接时调用
     * @param clients
     * @param client
     */
    void onConnect(ArrayList<ClientThread> clients, ClientThread client);

    /**
     * 与客户端断开连接时调用
     * @param clients
     * @param client
     */
    void onDisconnect(ArrayList<ClientThread> clients, ClientThread client);
}
