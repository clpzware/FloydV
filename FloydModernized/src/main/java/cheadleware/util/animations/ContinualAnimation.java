package cheadleware.util.animations;

import cheadleware.util.animations.impl.SmoothStepAnimation;
import lombok.Getter;

public class ContinualAnimation {
    private float output;
    private float endpoint;
    @Getter
    private Animation animation = new SmoothStepAnimation(0, 0, Direction.FORWARDS);

    public void animate(float destination, int ms) {
        if (Math.abs(destination - endpoint) > 0.001f || animation.isDone()) {
            output = getOutput(); // current position
            endpoint = destination;
            // Use the 3-parameter constructor: (int ms, double endPoint, Direction direction)
            animation = new SmoothStepAnimation(ms, endpoint, Direction.FORWARDS);
        }
    }

    public boolean isDone() {
        return Math.abs(getOutput() - endpoint) < 0.001f || animation.isDone();
    }

    public void setOutput(float value) {
        output = value;
        endpoint = value;
        // Use the 3-parameter constructor
        animation = new SmoothStepAnimation(0, value, Direction.FORWARDS);
    }

    public float getOutput() {
        return animation.getOutput().floatValue();
    }
}