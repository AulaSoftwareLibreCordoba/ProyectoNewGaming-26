package cordoba.graficos.luz;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

// Degradados circulares pixelados (por anillos) para las luces. Se generan una vez y se cachean.
final class SpritesLuz {

    // anillos del degradado: menos = más retro, más = más suave
    private static final int ANILLOS = 6;

    private static final Map<Long, BufferedImage> CACHE = new HashMap<>();

    private SpritesLuz() { }

    // máscara blanca: opaca en el centro y transparente en el borde
    static BufferedImage mascara(int radio) {
        return halo(radio, 0xFFFFFF);
    }

    // círculo de luz de ese color que se desvanece hacia fuera
    static synchronized BufferedImage halo(int radio, int rgb) {
        long clave = ((long) radio << 32) | (rgb & 0xFFFFFFL);
        return CACHE.computeIfAbsent(clave, k -> crear(radio, rgb));
    }

    private static BufferedImage crear(int radio, int rgb) {
        int d = radio * 2;
        BufferedImage img = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < d; y++) {
            for (int x = 0; x < d; x++) {
                double dist = Math.hypot(x + 0.5 - radio, y + 0.5 - radio) / radio;
                if (dist >= 1) continue;
                double luz = 1 - dist;
                luz = luz * luz * (3 - 2 * luz);              // suavizado
                // anillos + tramado 2x2 entre anillo y anillo
                double nivel = luz * ANILLOS;
                int anillo = (int) nivel;
                double resto = nivel - anillo;
                boolean tramado = ((x + y) & 1) == 0;
                if (resto > 0.5 && tramado) anillo++;
                double alfa = Math.min(1, anillo / (double) ANILLOS);
                img.setRGB(x, y, ((int) (alfa * 255) << 24) | (rgb & 0xFFFFFF));
            }
        }
        return img;
    }
}
