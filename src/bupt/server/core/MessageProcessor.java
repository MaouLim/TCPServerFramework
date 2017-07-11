package bupt.server.core;

import bupt.server.protocol.MessageBase;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface MessageProcessor {

    void onMessage(MessageBase message, Object sender);
}
