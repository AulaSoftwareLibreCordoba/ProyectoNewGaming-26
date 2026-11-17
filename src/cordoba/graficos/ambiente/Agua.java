package cordoba.graficos.ambiente;

import cordoba.graficos.Fondos;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Agua animada del Guadalquivir:
 *
 *  - Bloques de agua del nivel: superficie con olas que avanzan, espuma,
 *    degradado más oscuro en lo hondo y reflejos de luz que se mueven (cáusticas).
 *  - Río del fondo (capa del Puente Romano): destellos que corren con la corriente.
 *
 * Los fotogramas de los bloques se calculan una sola vez y se guardan.
 */
public final class Agua {

    private static final int T = 16;
    /** Fotogramas de la animación y cuántos fotogramas del juego dura cada uno. */
    private static final int FRAMES = 32, PASO = 3;
    /** Profundidades distintas: 0 = superficie, 3 = lo más hondo. */
    private static final int PROFUNDIDADES = 4;

    // Paleta: de la superficie al fondo
    private static final int[] AGUA = { 0x4AA3D4, 0x3A8CC0, 0x2F78A8, 0x24628E, 0x1B4D73 };
    private static final int ESPUMA = 0xF2FBFF, CRESTA = 0xBDE8FF, BAJO_CRESTA = 0x7CC4E8;

    private static final BufferedImage[][][] CACHE = new BufferedImage[PROFUNDIDADES][2][];

    private Agua() { }

    /**
     * Dibuja un bloque de agua.
     * @param prof bloques de agua que hay encima (0 = este bloque es la superficie)
     */
    public static void dibujarBloque(Graphics2D g, int x, int y, int columna, int prof, int frame) {
        int p = Math.min(prof, PROFUNDIDADES - 1);
        int k = (frame / PASO) % FRAMES;
        g.drawImage(fotogramas(p, Math.floorMod(columna, 2))[k], x, y, null);
        // destellos sueltos en la superficie
        if (prof == 0) {
            int semilla = columna * 73 + (frame / 12) * 31;
            int h = (semilla * 1103515245 + 12345) >>> 16;
            if (h % 6 == 0) {
                int sx = x + 2 + h % 11, sy = y + 6 + (h >> 4) % 4;
                g.setColor(new Color(0xF2FBFF));
                g.fillRect(sx, sy, 3, 1);
                g.fillRect(sx + 1, sy - 1, 1, 3);
            }
        }
    }

    private static synchronized BufferedImage[] fotogramas(int prof, int variante) {
        if (CACHE[prof][variante] == null) {
            BufferedImage[] f = new BufferedImage[FRAMES];
            for (int k = 0; k < FRAMES; k++) f[k] = crear(prof, variante, k);
            CACHE[prof][variante] = f;
        }
        return CACHE[prof][variante];
    }

    private static BufferedImage crear(int prof, int variante, int k) {
        BufferedImage img = new BufferedImage(T, T, BufferedImage.TYPE_INT_ARGB);
        double t = 2 * Math.PI * k / FRAMES;
        for (int px = 0; px < T; px++) {
            int wx = variante * T + px;                    // el dibujo se repite cada 32 px
            double a = 2 * Math.PI * wx / 32.0;
            // Altura de la ola (dos ondas que avanzan a distinta velocidad)
            double ola = 1.6 * Math.sin(a - t) + 0.8 * Math.sin(2 * a + 2 * t);
            int sup = 4 + (int) Math.round(ola);
            boolean cresta = Math.sin(a - t) < -0.7;        // la parte alta de la ola
            for (int py = 0; py < T; py++) {
                int hondo = prof * T + py;                  // píxeles bajo la superficie
                int color;
                if (prof == 0 && py < sup) continue;        // aire: se ve el fondo
                if (prof == 0 && py == sup) color = cresta ? ESPUMA : CRESTA;
                else if (prof == 0 && py == sup + 1) color = BAJO_CRESTA;
                else {
                    // Degradado por bandas con tramado entre bandas
                    double banda = Math.min(AGUA.length - 1.001, 0.6 + hondo / 14.0);
                    int i = (int) banda;
                    if (banda - i > 0.5 && ((px + py) & 1) == 0) i++;
                    color = AGUA[Math.min(i, AGUA.length - 1)];
                    // Cáusticas: líneas de luz onduladas que se mueven
                    double c = Math.sin(a + 2 * Math.PI * hondo / 24.0 + t)
                             + Math.sin(2 * a - 2 * Math.PI * hondo / 16.0 + 2 * t);
                    if (hondo < 40 && c > 1.55) color = aclarar(color, hondo < 20 ? 34 : 18);
                }
                img.setRGB(px, py, 0xFF000000 | color);
            }
        }
        return img;
    }

    /**
     * Destellos del río del fondo (capa del Puente Romano). Se mueven con la
     * corriente y con el parallax de su capa.
     */
    static void brillosRio(Graphics2D g, int camX, int dy, double segundos) {
        int desp = (int) (camX * Fondos.PARALLAX[2]);
        Color claro = new Color(0xA8DAF0), blanco = new Color(0xE8F7FF);
        for (int i = 0; i < 46; i++) {
            int fila = 304 + (i * 7) % 54;                     // alto del río en la capa
            double velocidad = 6 + (i % 5) * 2.5 + (fila - 300) * 0.12;   // más rápido cerca
            int largo = 3 + (i * 5) % 8;
            int x = (int) Math.floorMod((long) (i * 137 + segundos * velocidad) - desp, Fondos.ANCHO + 20) - 10;
            // cada destello aparece y desaparece
            double brillo = Math.sin(segundos * 1.7 + i * 1.3);
            if (brillo < -0.2) continue;
            g.setColor(brillo > 0.75 ? blanco : claro);
            g.fillRect(x, fila + dy, largo, 1);
        }
        // ondas bajo los ojos del puente: líneas que suben y bajan
        g.setColor(new Color(0x6FB0D2));
        for (int i = 0; i < 10; i++) {
            int cx = Math.floorMod(i * 64 + 32 - desp, Fondos.ANCHO);
            int ox = (int) (Math.sin(segundos * 2 + i) * 3);
            g.fillRect(cx - 12 + ox, 296 + dy, 8, 1);
            g.fillRect(cx + 4 - ox, 299 + dy, 6, 1);
        }
    }

    private static int aclarar(int rgb, int n) {
        int r = Math.min(255, ((rgb >> 16) & 255) + n);
        int g = Math.min(255, ((rgb >> 8) & 255) + n);
        int b = Math.min(255, (rgb & 255) + n);
        return (r << 16) | (g << 8) | b;
    }
}
