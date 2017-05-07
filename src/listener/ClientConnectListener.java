package listener;

/**
 * Created by Obser on 2017/5/4.
 */
public interface ClientConnectListener {
    void onDisconnect();
    void onConnect();
}
