package bupt.server.core;

import bupt.server.protocol.primitive.ProtocolUnit;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface ProtocolUnitProcessor {

    void onMessage(ProtocolUnit unit, Object sender);
}
