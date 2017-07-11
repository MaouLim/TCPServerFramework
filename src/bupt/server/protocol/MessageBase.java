package bupt.server.protocol;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public abstract class MessageBase {

    private String sourceCommunicatorId = null;

    public MessageBase() { }

    public MessageBase(String sourceCommunicatorId) {
        this.sourceCommunicatorId = sourceCommunicatorId;
    }

    public abstract Object get(String properties);

    public void setSourceCommunicatorId(String sourceCommunicatorId) {
        this.sourceCommunicatorId = sourceCommunicatorId;
    }

    public String getSourceCommunicatorId() {
        return sourceCommunicatorId;
    }
}
