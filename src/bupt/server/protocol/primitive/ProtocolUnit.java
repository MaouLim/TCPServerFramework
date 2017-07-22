package bupt.server.protocol.primitive;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public class ProtocolUnit {

    private String       sourceCommunicatorId  = null;
    private List<String> targetCommunicatorIds = null;

    public ProtocolUnit() {
        this.targetCommunicatorIds = new ArrayList<>();
    }

    public ProtocolUnit(String sourceCommunicatorId) {
        this.sourceCommunicatorId = sourceCommunicatorId;
        this.targetCommunicatorIds = new ArrayList<>();
    }

    public void setSourceCommunicator(@NotNull String sourceCommunicatorId) {
        this.sourceCommunicatorId = sourceCommunicatorId;
    }

    public String getSourceCommunicator() {
        return this.sourceCommunicatorId;
    }

    public void addTargetCommunicator(@NotNull String targetCommunicatorId) {
        this.targetCommunicatorIds.add(targetCommunicatorId);
    }

    public void addTargetCommunicators(@NotNull String[] communicatorIds) {
        for (String each : communicatorIds) {
            if (null == each) {
                continue;
            }

            this.targetCommunicatorIds.add(each);
        }
    }

    public void addTargetCommunicators(@NotNull Collection<String> communicatorIds) {
        for (String each : communicatorIds) {
            if (null == each) {
                continue;
            }

            this.targetCommunicatorIds.add(each);
        }
    }

    public int removeTargetCommunicator(@NotNull String targetCommunicatorId) {
        boolean needRemove = false;
        int count = 0;
        do {
            needRemove =
                    this.targetCommunicatorIds.remove(targetCommunicatorId);
            if (needRemove) {
                ++count;
            }
        } while (needRemove);

        return count;
    }

    public List<String> getTargetCommunicators() {
        return this.targetCommunicatorIds;
    }
}
