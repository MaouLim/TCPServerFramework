package bupt.networks.tcp.behaviors;

/*
 * Created by Maou Lim on 2017/7/11.
 */
public interface MessageArrivedHandler {

    void handleMessageArrived(String content, Object sender);
}
