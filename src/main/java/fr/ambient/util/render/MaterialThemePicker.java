package fr.ambient.util.render;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExcludeFlow
@ExcludeConstant
public class MaterialThemePicker {

    public static class MaterialTheme {
        private final String name;
        private final List<Color> shades;

        MaterialTheme(String name, Color shade50, Color shade100, Color shade200, Color shade300,
                      Color shade400, Color shade500, Color shade600, Color shade700, Color shade800, Color shade900) {
            this.name = name;
            this.shades = Arrays.asList(shade50, shade100, shade200, shade300, shade400, shade500, shade600, shade700, shade800, shade900);
        }

        public String getName() {
            return name;
        }

        public List<Color> getShades() {
            return shades;
        }

        public Color getShade(int shade) {
            return switch (shade) {
                case 50 -> shades.get(0);
                case 100 -> shades.get(1);
                case 200 -> shades.get(2);
                case 300 -> shades.get(3);
                case 400 -> shades.get(4);
                case 500 -> shades.get(5);
                case 600 -> shades.get(6);
                case 700 -> shades.get(7);
                case 800 -> shades.get(8);
                case 900 -> shades.get(9);
                default -> throw new IllegalArgumentException("Invalid shade name");
            };
        }
    }

    private static final List<MaterialTheme> materialThemes = new ArrayList<>();

    static {
        materialThemes.add(new MaterialTheme(
                "Dark Red",
                new Color(255, 235, 238), // shade50
                new Color(244, 199, 195), // shade100
                new Color(239, 154, 154), // shade200
                new Color(210, 82, 82),   // shade300
                new Color(229, 115, 115), // shade400
                new Color(244, 67, 54),   // shade500
                new Color(229, 57, 53),   // shade600
                new Color(211, 47, 47),   // shade700
                new Color(198, 40, 40),   // shade800
                new Color(153, 0, 0)      // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Red",
                new Color(255, 235, 238),
                new Color(255, 205, 210),
                new Color(239, 154, 154),
                new Color(229, 115, 115),
                new Color(229, 115, 115),
                new Color(244, 67, 54),
                new Color(229, 57, 53),
                new Color(211, 47, 47),
                new Color(183, 28, 28),
                new Color(121, 14, 14)
        ));
        materialThemes.add(new MaterialTheme(
                "Pink",
                new Color(252, 228, 236),
                new Color(248, 187, 208),
                new Color(244, 143, 177),
                new Color(240, 98, 146),
                new Color(236, 64, 122),
                new Color(233, 30, 99),
                new Color(216, 27, 96),
                new Color(194, 24, 91),
                new Color(173, 20, 87),
                new Color(136, 14, 79)
        ));
        materialThemes.add(new MaterialTheme(
                "Purple",
                new Color(243, 229, 245),
                new Color(225, 190, 231),
                new Color(206, 147, 216),
                new Color(186, 104, 200),
                new Color(171, 71, 188),
                new Color(156, 39, 176),
                new Color(142, 36, 170),
                new Color(123, 31, 162),
                new Color(106, 27, 154),
                new Color(74, 20, 140)
        ));
        materialThemes.add(new MaterialTheme(
                "Deep Purple",
                new Color(237, 231, 246), // shade50
                new Color(209, 196, 233), // shade100
                new Color(179, 157, 219), // shade200
                new Color(149, 117, 205), // shade300
                new Color(126, 87, 194),  // shade400
                new Color(103, 58, 183),  // shade500
                new Color(94, 53, 177),   // shade600
                new Color(81, 45, 168),   // shade700
                new Color(69, 39, 160),   // shade800
                new Color(49, 27, 146)    // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Indigo",
                new Color(232, 234, 246), // shade50
                new Color(197, 202, 233), // shade100
                new Color(159, 168, 218), // shade200
                new Color(121, 134, 203), // shade300
                new Color(92, 107, 192),  // shade400
                new Color(63, 81, 181),   // shade500
                new Color(57, 73, 171),   // shade600
                new Color(48, 63, 159),   // shade700
                new Color(40, 53, 147),   // shade800
                new Color(26, 35, 126)    // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Blue",
                new Color(227, 242, 253), // shade50
                new Color(187, 222, 251), // shade100
                new Color(144, 202, 249), // shade200
                new Color(100, 181, 246), // shade300
                new Color(66, 165, 245),  // shade400
                new Color(33, 150, 243),  // shade500
                new Color(30, 136, 229),  // shade600
                new Color(25, 118, 210),  // shade700
                new Color(21, 101, 192),  // shade800
                new Color(13, 71, 161)    // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Light Blue",
                new Color(225, 245, 254), // shade50
                new Color(179, 229, 252), // shade100
                new Color(129, 212, 250), // shade200
                new Color(79, 195, 247),  // shade300
                new Color(41, 182, 246),  // shade400
                new Color(3, 169, 244),   // shade500
                new Color(3, 155, 229),   // shade600
                new Color(2, 136, 209),   // shade700
                new Color(2, 119, 189),   // shade800
                new Color(1, 87, 155)     // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Cyan",
                new Color(224, 247, 250), // shade50
                new Color(178, 235, 242), // shade100
                new Color(128, 222, 234), // shade200
                new Color(77, 208, 225),  // shade300
                new Color(38, 198, 218),  // shade400
                new Color(0, 188, 212),   // shade500
                new Color(0, 172, 193),   // shade600
                new Color(0, 151, 167),   // shade700
                new Color(0, 131, 143),   // shade800
                new Color(0, 96, 100)     // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Teal",
                new Color(224, 242, 241), // shade50
                new Color(178, 223, 219), // shade100
                new Color(128, 203, 196), // shade200
                new Color(77, 182, 172),  // shade300
                new Color(38, 166, 154),  // shade400
                new Color(0, 150, 136),   // shade500
                new Color(0, 137, 123),   // shade600
                new Color(0, 121, 107),   // shade700
                new Color(0, 105, 92),    // shade800
                new Color(0, 77, 64)      // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Green",
                new Color(232, 245, 233), // shade50
                new Color(200, 230, 201), // shade100
                new Color(165, 214, 167), // shade200
                new Color(129, 199, 132), // shade300
                new Color(102, 187, 106), // shade400
                new Color(76, 175, 80),   // shade500
                new Color(67, 160, 71),   // shade600
                new Color(56, 142, 60),   // shade700
                new Color(46, 125, 50),   // shade800
                new Color(27, 94, 32)     // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Light Green",
                new Color(241, 248, 233), // shade50
                new Color(220, 237, 200), // shade100
                new Color(197, 225, 165), // shade200
                new Color(174, 213, 129), // shade300
                new Color(156, 204, 101), // shade400
                new Color(139, 195, 74),  // shade500
                new Color(124, 179, 66),  // shade600
                new Color(104, 159, 56),  // shade700
                new Color(85, 139, 47),   // shade800
                new Color(51, 105, 30)    // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Lime",
                new Color(249, 251, 231), // shade50
                new Color(240, 244, 195), // shade100
                new Color(230, 238, 156), // shade200
                new Color(220, 231, 117), // shade300
                new Color(212, 225, 87),  // shade400
                new Color(205, 220, 57),  // shade500
                new Color(192, 202, 51),  // shade600
                new Color(175, 180, 43),  // shade700
                new Color(158, 157, 36),  // shade800
                new Color(130, 119, 23)   // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Yellow",
                new Color(255, 253, 231), // shade50
                new Color(255, 249, 196), // shade100
                new Color(255, 245, 157), // shade200
                new Color(255, 241, 118), // shade300
                new Color(255, 238, 88),  // shade400
                new Color(255, 235, 59),  // shade500
                new Color(253, 216, 53),  // shade600
                new Color(251, 192, 45),  // shade700
                new Color(249, 168, 37),  // shade800
                new Color(245, 127, 23)   // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Amber",
                new Color(255, 248, 225), // shade50
                new Color(255, 236, 179), // shade100
                new Color(255, 224, 130), // shade200
                new Color(255, 213, 79),  // shade300
                new Color(255, 202, 40),  // shade400
                new Color(255, 193, 7),   // shade500
                new Color(255, 179, 0),   // shade600
                new Color(255, 160, 0),   // shade700
                new Color(255, 143, 0),   // shade800
                new Color(255, 111, 0)    // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Orange",
                new Color(255, 243, 224), // shade50
                new Color(255, 224, 178), // shade100
                new Color(255, 204, 128), // shade200
                new Color(255, 183, 77),  // shade300
                new Color(255, 167, 38),  // shade400
                new Color(255, 152, 0),   // shade500
                new Color(251, 140, 0),   // shade600
                new Color(245, 124, 0),   // shade700
                new Color(239, 108, 0),   // shade800
                new Color(230, 81, 0)     // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Deep Orange",
                new Color(251, 233, 231), // shade50
                new Color(255, 204, 188), // shade100
                new Color(255, 171, 145), // shade200
                new Color(255, 138, 101), // shade300
                new Color(255, 112, 67),  // shade400
                new Color(255, 87, 34),   // shade500
                new Color(244, 81, 30),   // shade600
                new Color(230, 74, 25),   // shade700
                new Color(216, 67, 21),   // shade800
                new Color(191, 54, 12)    // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Gray",
                new Color(250, 250, 250), // shade50
                new Color(245, 245, 245), // shade100
                new Color(238, 238, 238), // shade200
                new Color(224, 224, 224), // shade300
                new Color(189, 189, 189), // shade400
                new Color(158, 158, 158), // shade500
                new Color(117, 117, 117), // shade600
                new Color(97, 97, 97),    // shade700
                new Color(66, 66, 66),    // shade800
                new Color(33, 33, 33)     // shade900
        ));
        materialThemes.add(new MaterialTheme(
                "Blue Gray",
                new Color(236, 239, 241), // shade50
                new Color(207, 216, 220), // shade100
                new Color(176, 190, 197), // shade200
                new Color(144, 164, 174), // shade300
                new Color(120, 144, 156), // shade400
                new Color(96, 125, 139),  // shade500
                new Color(84, 110, 122),  // shade600
                new Color(69, 90, 100),   // shade700
                new Color(55, 71, 79),    // shade800
                new Color(38, 50, 56)     // shade900
        ));
    }

    public static MaterialTheme findClosestTheme(Color inputColor) {
        MaterialTheme closestTheme = null;
        double minDistance = Double.MAX_VALUE;

        for (MaterialTheme theme : materialThemes) {
            for (Color shade : theme.getShades()) {
                double distance = labColorDistance(inputColor, shade);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestTheme = theme;
                }
            }
        }

        return closestTheme;
    }

    private static double labColorDistance(Color c1, Color c2) {
        double[] lab1 = rgbToLab(c1);
        double[] lab2 = rgbToLab(c2);
        double lDiff = lab1[0] - lab2[0];
        double aDiff = lab1[1] - lab2[1];
        double bDiff = lab1[2] - lab2[2];
        return Math.sqrt(lDiff * lDiff + aDiff * aDiff + bDiff * bDiff);
    }

    private static double[] rgbToLab(Color color) {
        double[] xyz = rgbToXyz(color);
        return xyzToLab(xyz);
    }

    private static double[] rgbToXyz(Color color) {
        double r = pivotRgb(color.getRed() / 255.0);
        double g = pivotRgb(color.getGreen() / 255.0);
        double b = pivotRgb(color.getBlue() / 255.0);

        double x = r * 0.4124564 + g * 0.3575761 + b * 0.1804375;
        double y = r * 0.2126729 + g * 0.7151522 + b * 0.0721750;
        double z = r * 0.0193339 + g * 0.1191920 + b * 0.9503041;
        return new double[]{x, y, z};
    }

    private static double[] xyzToLab(double[] xyz) {
        double x = pivotXyz(xyz[0] / 0.95047);
        double y = pivotXyz(xyz[1]);
        double z = pivotXyz(xyz[2] / 1.08883);
        double l = 116 * y - 16;
        double a = 500 * (x - y);
        double b = 200 * (y - z);
        return new double[]{l, a, b};
    }

    private static double pivotRgb(double n) {
        return (n > 0.04045) ? Math.pow((n + 0.055) / 1.055, 2.4) : n / 12.92;
    }

    private static double pivotXyz(double n) {
        return (n > 0.008856) ? Math.pow(n, 1.0 / 3.0) : (7.787 * n) + 16.0 / 116.0;
    }
}