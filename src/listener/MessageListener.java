package listener;

import protocol.Protocol;

/**
 * Created by Obser on 2017/5/4.
 */
public interface MessageListener {
    void onReceiveAll(Protocol.Message message);
    void onClose() throws Exception;
    void onConnect(Protocol.Message message);
    void onReceiveList(Protocol.Message message);
    void onReceivePrivate(Protocol.Message message, boolean flag);
    void onFailed(Protocol.Message message);
}
