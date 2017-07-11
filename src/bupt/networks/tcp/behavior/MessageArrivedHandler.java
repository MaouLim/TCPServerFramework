package bupt.networks.tcp.behavior;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface MessageArrivedHandler {

    void handleMessageArrived(String content, Object sender);
}
