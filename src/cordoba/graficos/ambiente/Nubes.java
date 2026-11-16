package cordoba.graficos.ambiente;

import cordoba.niveles.Level;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Nubes que se mueven con el viento, en TRES capas de profundidad:
 * las lejanas son pequeñas, pálidas y lentas; las cercanas, grandes y rápidas.
 * Además se desplazan un poco con la cámara (parallax).
 *
 * Las nubes que venían pintadas en el cielo del río se borran al cargar
 * (ver {@link #sinNubesPintadas}) para que no haya nubes "congeladas".
 */
public final class Nubes {

    /** Una capa de nubes: cuántas, tamaño, altura, viento y parallax. */
    private record Capa(int cantidad, int anchoMin, int anchoMax, int yMin, int yMax,
                        float viento, float parallax, float alfa) { }

    // Ajustes por tema --------------------------------------------------
    private static final Capa[] CAPAS_RIO = {
        new Capa(6, 26, 44, 18, 120, 3.0f, 0.03f, 0.55f),   // lejos
        new Capa(4, 46, 70, 30, 130, 7.0f, 0.06f, 0.80f),   // medio
        new Capa(3, 78, 110, 40, 150, 13.0f, 0.10f, 1.00f), // cerca
    };
    private static final Capa[] CAPAS_MEZQUITA = {
        new Capa(5, 40, 70, 20, 90, 2.0f, 0.03f, 0.45f),
        new Capa(4, 70, 110, 50, 140, 4.5f, 0.06f, 0.70f),
        new Capa(2, 110, 150, 90, 175, 8.0f, 0.10f, 0.85f),
    };
    // Colores: luz, medio, sombra
    private static final int[] TONOS_RIO      = { 0xFFFFFF, 0xEEF6FB, 0xC6DDEC };
    private static final int[] TONOS_MEZQUITA = { 0xFFE0BE, 0xF4A88E, 0xA9607E };

    private static final int ANCHO = 640, MARGEN = 160;

    private record Nube(BufferedImage img, float x0, int y, Capa capa) { }

    private static final Map<Level.Tema, List<Nube>> NUBES = new java.util.EnumMap<>(Level.Tema.class);
    private static final Map<BufferedImage, BufferedImage> LIMPIAS = new IdentityHashMap<>();

    private Nubes() { }

    /** Dibuja todas las nubes (llamar justo después del cielo). */
    static void dibujar(Graphics2D g, Level.Tema tema, int camX, int dy, double segundos) {
        Composite antes = g.getComposite();
        float alfaActual = -1;
        for (Nube n : nubes(tema)) {
            Capa c = n.capa;
            int w = n.img.getWidth();
            double pos = n.x0 + segundos * c.viento - camX * c.parallax;
            int x = (int) Math.floorMod((long) Math.floor(pos), ANCHO + MARGEN) - w / 2 - MARGEN / 4;
            if (x > ANCHO || x + w < 0) continue;
            if (c.alfa != alfaActual) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, c.alfa));
                alfaActual = c.alfa;
            }
            g.drawImage(n.img, x, n.y + dy, null);
        }
        g.setComposite(antes);
    }

    private static synchronized List<Nube> nubes(Level.Tema tema) {
        return NUBES.computeIfAbsent(tema, t -> {
            boolean rio = t == Level.Tema.RIO;
            Capa[] capas = rio ? CAPAS_RIO : CAPAS_MEZQUITA;
            int[] tonos = rio ? TONOS_RIO : TONOS_MEZQUITA;
            Random r = new Random(rio ? 7 : 19);
            List<Nube> lista = new ArrayList<>();
            for (Capa c : capas) {               // primero las lejanas: quedan detrás
                for (int i = 0; i < c.cantidad; i++) {
                    int w = c.anchoMin + r.nextInt(c.anchoMax - c.anchoMin + 1);
                    int h = rio ? w / 2 : w / 5 + 4;          // al atardecer, nubes alargadas
                    float x0 = (ANCHO + MARGEN) * (i + r.nextFloat() * 0.6f) / c.cantidad;
                    int y = c.yMin + r.nextInt(c.yMax - c.yMin + 1);
                    lista.add(new Nube(crear(r, w, h, tonos), x0, y, c));
                }
            }
            return lista;
        });
    }

    /** Genera una nube en pixel art: varios "bultos" redondos con la base plana. */
    private static BufferedImage crear(Random r, int w, int h, int[] tonos) {
        boolean[][] m = new boolean[h][w];
        int bultos = 4 + r.nextInt(3);
        for (int b = 0; b < bultos; b++) {
            double u = (b + 0.5) / bultos;                       // 0 = izquierda, 1 = derecha
            double cx = w * (0.12 + 0.76 * u) + r.nextInt(5) - 2;
            // más altos en el centro (silueta de cúmulo) y algo de azar
            double rad = h * (0.20 + 0.30 * Math.sin(Math.PI * u) + r.nextDouble() * 0.12);
            double cy = h - 1 - rad * 0.55;                       // la base queda plana (se corta abajo)
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    if (Math.hypot((x + 0.5 - cx) * 0.9, y + 0.5 - cy) < rad) m[y][x] = true;
        }

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!m[y][x]) continue;
                boolean bordeLuz = y < 2 || !m[y - 2][x] || (x > 0 && y > 0 && !m[y - 1][x - 1]);
                double abajo = y / (double) h;
                int tono;
                if (bordeLuz) tono = tonos[0];
                else if (abajo > 0.82 || (abajo > 0.72 && ((x + y) & 1) == 0)) tono = tonos[2];
                else tono = tonos[1];
                img.setRGB(x, y, 0xFF000000 | tono);
            }
        }
        return img;
    }

    /**
     * El cielo del río trae nubes pintadas. Se borran copiando, de la misma fila,
     * el color del cielo más cercano (respetando el tramado de las bandas).
     */
    static synchronized BufferedImage sinNubesPintadas(Level.Tema tema, int indice, BufferedImage capa) {
        if (tema != Level.Tema.RIO || indice != 0) return capa;
        return LIMPIAS.computeIfAbsent(capa, c -> {
            int w = c.getWidth(), h = c.getHeight();
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                int[] fila = c.getRGB(0, y, w, 1, null, 0, w);
                int[] nueva = fila.clone();
                for (int x = 0; x < w; x++) {
                    if (!esNube(fila[x])) continue;
                    for (int d = 2; d < w; d += 2) {          // misma paridad = mismo tramado
                        if (x - d >= 0 && !esNube(fila[x - d])) { nueva[x] = fila[x - d]; break; }
                        if (x + d < w && !esNube(fila[x + d])) { nueva[x] = fila[x + d]; break; }
                    }
                }
                img.setRGB(0, y, w, 1, nueva, 0, w);
            }
            return img;
        });
    }

    private static boolean esNube(int argb) {
        int rgb = argb & 0xFFFFFF;
        return rgb == 0xFFFFFF || rgb == 0xDCEFFA;
    }
}
