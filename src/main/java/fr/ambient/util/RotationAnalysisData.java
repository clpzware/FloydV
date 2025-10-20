package fr.ambient.util;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RotationAnalysisData {
    private float yawDelta;
    private float pitchDelta;

    private float lastYawDelta;
    private float lastPitchDelta;

    private float playerHitVecY;

}
