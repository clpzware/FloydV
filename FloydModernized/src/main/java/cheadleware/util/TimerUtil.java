package cheadleware.util;

public class TimerUtil {
    private long lastMS = 0L;

    public void reset() {
        this.lastMS = System.currentTimeMillis();
    }

    public long getTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - this.lastMS;
    }

    public boolean hasTimeElapsed(long ms) {
        return this.getElapsedTime() >= ms;
    }

    public void setTime(long time) {
        this.lastMS = time;
    }
}