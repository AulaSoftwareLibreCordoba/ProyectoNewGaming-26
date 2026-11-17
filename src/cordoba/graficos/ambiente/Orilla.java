package cordoba.graficos.ambiente;

import cordoba.graficos.Fondos;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Orilla de arena del Guadalquivir donde se apoyan las palmeras y las cañas
 * de la capa cercana del fondo (si no, parecen flotar sobre el río).
 *
 * La arena se mueve con su capa (parallax) y el agua lame el borde.
 */
public final class Orilla {

    /** Altura (en coordenadas de la capa) donde empieza la arena. */
    private static final int ARRIBA = 284, ALTO = 40;

    // Arena mojada → seca, guijarros y sombras
    private static final int MOJADA = 0xB08A58, ARENA = 0xE0C08A, ARENA_OSC = 0xC9A66B,
                             ARENA_CLARA = 0xF0D9A8, GUIJARRO = 0x9C8466;

    /** Pie de cada palmera y mata de cañas de la capa cercana (x centro, ancho de la sombra). */
    private static final int[][] PIES = {
        { 90, 18 }, { 135, 14 }, { 434, 22 },
        { 38, 12 }, { 128, 12 }, { 218, 12 }, { 308, 12 }, { 398, 12 }, { 488, 12 }, { 578, 12 },
    };

    private static BufferedImage textura;

    private Orilla() { }

    /** Dibuja la orilla (llamar justo antes de la capa cercana del fondo). */
    public static void dibujar(Graphics2D g, int desp, int dy, double segundos) {
        BufferedImage img = textura();
        g.drawImage(img, -desp, ARRIBA + dy, null);
        g.drawImage(img, -desp + Fondos.ANCHO, ARRIBA + dy, null);

        // El agua lame el borde: una lengua de espuma que sube y baja
        Color agua = new Color(0x5FA8CC), espuma = new Color(0xE8F7FF);
        for (int x = 0; x < Fondos.ANCHO; x += 2) {
            int lx = x + desp;   // posición en la capa: la ola viaja con la orilla
            double ola = Math.sin(lx * 0.07 + segundos * 1.8) + 0.5 * Math.sin(lx * 0.19 - segundos * 2.6);
            int alto = (int) Math.round(1.5 + ola * 1.3);    // 0..4 px sobre la arena
            if (alto <= 0) continue;
            g.setColor(agua);
            g.fillRect(x, ARRIBA + dy, 2, alto);
            g.setColor(espuma);
            g.fillRect(x, ARRIBA + dy + alto, 2, 1);
        }
    }

    private static synchronized BufferedImage textura() {
        if (textura != null) return textura;
        BufferedImage img = new BufferedImage(Fondos.ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        java.util.Random r = new java.util.Random(5);
        for (int y = 0; y < ALTO; y++) {
            for (int x = 0; x < Fondos.ANCHO; x++) {
                int c;
                if (y < 4) c = MOJADA;                                  // arena mojada junto al agua
                else if (y < 6) c = ((x + y) & 1) == 0 ? MOJADA : ARENA_OSC;
                else c = ARENA;
                // ondulaciones de la arena (dunas pequeñas)
                if (y >= 6 && Math.sin(x * 0.11 + y * 0.9) > 0.85) c = ARENA_CLARA;
                if (y >= 6 && Math.sin(x * 0.11 + y * 0.9 + 2.2) > 0.9) c = ARENA_OSC;
                img.setRGB(x, y, c);
            }
        }
        Graphics2D g = img.createGraphics();
        // guijarros
        for (int i = 0; i < 60; i++) {
            int x = r.nextInt(Fondos.ANCHO), y = 7 + r.nextInt(ALTO - 9);
            g.setColor(new Color(GUIJARRO));
            g.fillRect(x, y, 2, 1);
            g.setColor(new Color(ARENA_CLARA));
            g.fillRect(x, y - 1, 1, 1);
        }
        // sombra bajo cada palmera y mata de cañas
        g.setColor(new Color(0x9E7C4C));
        for (int[] p : PIES) {
            int y = 8;
            g.fillRect(p[0] - p[1] / 2 + 2, y - 1, p[1] - 4, 1);
            g.fillRect(p[0] - p[1] / 2, y, p[1], 2);
            g.fillRect(p[0] - p[1] / 2 + 2, y + 2, p[1] - 4, 1);
        }
        g.dispose();
        return textura = img;
    }
}
