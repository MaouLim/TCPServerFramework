package bupt.server.protocol.data;

import bupt.server.protocol.primitive.ProtocolUnit;

/*
 * Created by Maou Lim on 2017/7/22.
 */
public abstract class MessageBase extends ProtocolUnit {

    public MessageBase() { }

    public MessageBase(String sourceCommunicatorId) {
        super(sourceCommunicatorId);
    }

    public abstract Object get(String property);
}
