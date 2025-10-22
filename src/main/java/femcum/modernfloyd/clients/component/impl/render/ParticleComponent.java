package femcum.modernfloyd.clients.component.impl.render;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.render.Render2DEvent;
import femcum.modernfloyd.clients.util.interfaces.ThreadAccess;
import femcum.modernfloyd.clients.util.render.particle.Particle;

import java.util.concurrent.ConcurrentLinkedQueue;

import static femcum.modernfloyd.clients.layer.Layers.BLOOM;
import static femcum.modernfloyd.clients.layer.Layers.REGULAR;

public class ParticleComponent extends Component implements ThreadAccess {

    public static ConcurrentLinkedQueue<Particle> particles = new ConcurrentLinkedQueue<>();
    public static int rendered, bloomed;

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<Render2DEvent> onRender2DEvent = event -> {
        if (particles.isEmpty() || true) {
            return;
        }

        getLayer(REGULAR).add(ParticleComponent::render);
        getLayer(BLOOM).add(ParticleComponent::bloom);
    };

    public static void bloom() {
        particles.forEach(Particle::bloom);

        bloomed = mc.ingameGUI.frame;
    }

    public static void render() {
        rendered = mc.ingameGUI.frame;

        particles.forEach(particle -> {
            particle.render();

            if (particle.time.getElapsedTime() > 50 * 3 * 20) {
                particles.remove(particle);
            }
        });

        if (particles.isEmpty()) return;

        threadPool.execute(() -> {
            particles.forEach(Particle::update);
        });
    }

    public static void add(final Particle particle) {
        particles.add(particle);
    }
}
