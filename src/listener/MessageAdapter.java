package listener;

import protocol.Protocol;

/**
 * Created by Obser on 2017/5/6.
 */
public class MessageAdapter implements MessageListener{
    @Override
    public void onReceiveAll(Protocol.Message message) {

    }

    @Override
    public void onClose() throws Exception {

    }

    @Override
    public void onConnect(Protocol.Message message) {

    }

    @Override
    public void onReceiveList(Protocol.Message message) {

    }

    @Override
    public void onReceivePrivate(Protocol.Message message, boolean flag) {

    }

    @Override
    public void onFailed(Protocol.Message message) {

    }
}
