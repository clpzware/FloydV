package femcum.modernfloyd.clients.packetlog.api.manager;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.packetlog.Check;
import femcum.modernfloyd.clients.packetlog.impl.HostsFileCheck;
import femcum.modernfloyd.clients.packetlog.impl.ProxyCheck;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.interfaces.ThreadAccess;
import lombok.Getter;
import rip.vantage.commons.util.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
@Getter
public final class PacketLogManager implements Accessor, ThreadAccess {

    private final List<Check> checkList = new ArrayList<>();

    private final StopWatch stopWatch = new StopWatch();

    public boolean packetLogging;

    public void init() {
        Floyd.INSTANCE.getEventBus().register(this);

        this.add(new HostsFileCheck());
        this.add(new ProxyCheck());
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (stopWatch.finished(3000L)) {
            threadPool.execute(() -> {
                boolean detected = false;

                for (final Check check : this.checkList) {
                    if (check.run()) {
                        detected = true;
                        break;
                    }
                }

                this.packetLogging = detected;
            });
            stopWatch.reset();
        }
    };

    public void add(final Check check) {
        this.checkList.add(check);
    }
}