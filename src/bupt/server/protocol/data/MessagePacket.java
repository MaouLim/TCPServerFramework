package bupt.server.protocol.data;

import java.util.HashMap;
import java.util.Map;

/*
 * Created by Maou Lim on 2017/7/22.
 */
public class MessagePacket extends MessageBase {

    private Map<String, Object> properties = null;

    public MessagePacket() {
        super();
        this.properties = new HashMap<>();
    }

    public MessagePacket(Map<String, Object> properties) {
        super();
        this.properties = properties;
    }

    public MessagePacket(String sourceCommunicatorId) {
        super(sourceCommunicatorId);
        this.properties = new HashMap<>();
    }

    public Object put(String property, Object value) {
        return properties.put(property, value);
    }

    @Override
    public Object get(String property) {
        return properties.get(property);
    }
}
