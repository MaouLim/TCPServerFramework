package bupt.server.protocol.primitive;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface ProtocolUnitDecoder {

    ProtocolUnit decode(String content);
}
