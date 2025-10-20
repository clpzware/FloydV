package fr.ambient.util.player.movecorrect;

public enum MoveCorrect {
    OFF, SPRINT, STRICT, SILENT;

    public static MoveCorrect getMoveCorrect(String mode) {
        switch (mode.toLowerCase()){
            case "strict" -> {
                return MoveCorrect.STRICT;
            }
            case "silent" -> {
                return MoveCorrect.SILENT;
            }
            case "sprint", "nosprint" -> {
                return MoveCorrect.SPRINT;
            }
            default -> {
                return MoveCorrect.OFF;
            }
        }
    }
}