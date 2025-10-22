package femcum.modernfloyd.clients.util.process;

public class ThreadUtil {
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}
