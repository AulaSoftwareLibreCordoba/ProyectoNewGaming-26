package cordoba.graficos.luz;

import cordoba.niveles.Level;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Efectos que dan sensación de PROFUNDIDAD sin cambiar ningún sprite:
 *
 *  1. Neblina en las capas lejanas del fondo (perspectiva atmosférica).
 *  2. Sombra que proyecta el terreno sobre el fondo.
 *  3. Volumen: el interior de los bloques grandes se oscurece poco a poco.
 *  4. Sombras de contacto bajo los objetos.
 *
 * Los valores se ajustan en {@link EstiloVisual}.
 */
public class Profundidad {

    private static final int T = Level.TILE;
    private static final int ANCHO = 640, ALTO = 360;
    /** Hasta cuántos bloques se mira para calcular el volumen. */
    private static final int PROF_MAX = 4;

    private static final Map<BufferedImage, BufferedImage> NIEBLA = new IdentityHashMap<>();

    // ---------------------------------------------------------------------
    //  1. Neblina en el fondo
    // ---------------------------------------------------------------------

    /** Devuelve la capa del fondo con su neblina aplicada (se calcula una vez). */
    public static synchronized BufferedImage capaConNiebla(Level.Tema tema, int indice, BufferedImage capa) {
        EstiloVisual e = EstiloVisual.de(tema);
        float a = indice < e.nieblaCapa.length ? e.nieblaCapa[indice] : 0f;
        if (a <= 0f) return capa;
        return NIEBLA.computeIfAbsent(capa, c -> {
            BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = img.createGraphics();
            g.drawImage(c, 0, 0, null);
            // SRC_ATOP: la neblina solo tiñe lo que ya está pintado (no el hueco transparente)
            g.setComposite(AlphaComposite.SrcAtop);
            Color niebla = new Color(e.colorNiebla);
            // Más neblina abajo, junto al horizonte
            g.setPaint(new GradientPaint(0, 0, conAlfa(niebla, a * 0.55f),
                                         0, c.getHeight(), conAlfa(niebla, a)));
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
            g.dispose();
            return img;
        });
    }

    // ---------------------------------------------------------------------
    //  2. Sombra del terreno sobre el fondo
    // ---------------------------------------------------------------------

    /**
     * Llamar ANTES de dibujar los bloques: la sombra queda detrás de ellos.
     * (Todas las sombras se desplazan igual, así que no se solapan entre sí.)
     */
    public void sombraTerreno(Graphics2D g, Level nivel, int camX, int camY) {
        EstiloVisual e = EstiloVisual.de(nivel.getTema());
        if (e.alfaSombra <= 0) return;
        g.setColor(conAlfa(new Color(e.colorSombra), e.alfaSombra));
        int c1 = Math.max(0, camX / T - 1), c2 = Math.min(nivel.getColumnas() - 1, (camX + ANCHO) / T + 1);
        int f1 = Math.max(0, camY / T - 1), f2 = Math.min(nivel.getFilas() - 1, (camY + ALTO) / T + 1);
        for (int f = f1; f <= f2; f++) {
            int c = c1;
            while (c <= c2) {
                if (!sombraVisible(nivel, c, f)) { c++; continue; }
                int fin = c + 1;
                while (fin <= c2 && sombraVisible(nivel, fin, f)) fin++;
                g.fillRect(c * T - camX + e.sombraDx, f * T - camY + e.sombraDy, (fin - c) * T, T);
                c = fin;
            }
        }
    }

    /** ¿La sombra de este bloque cae sobre algún hueco? (si no, la tapa otro bloque) */
    private static boolean sombraVisible(Level n, int c, int f) {
        return n.esSolido(c, f) && (!n.esSolido(c - 1, f) || !n.esSolido(c, f + 1) || !n.esSolido(c - 1, f + 1)
                || !n.esSolido(c + 1, f) || !n.esSolido(c, f - 1) || !n.esSolido(c + 1, f + 1));
    }

    // ---------------------------------------------------------------------
    //  3. Volumen de los bloques
    // ---------------------------------------------------------------------

    /** Llamar DESPUÉS de dibujar los bloques. */
    public void volumen(Graphics2D g, Level nivel, int camX, int camY) {
        EstiloVisual e = EstiloVisual.de(nivel.getTema());
        if (e.volumenPaso <= 0) return;
        Color[] tonos = new Color[PROF_MAX + 1];
        for (int d = 2; d <= PROF_MAX; d++) {
            float a = Math.min(e.volumenMax, (d - 1) * e.volumenPaso);
            tonos[d] = new Color(0, 0, 0, Math.round(a * 255));
        }
        int c1 = Math.max(0, camX / T), c2 = Math.min(nivel.getColumnas() - 1, (camX + ANCHO) / T);
        int f1 = Math.max(0, camY / T), f2 = Math.min(nivel.getFilas() - 1, (camY + ALTO) / T);
        for (int f = f1; f <= f2; f++) {
            int c = c1;
            while (c <= c2) {
                int d = distanciaAlAire(nivel, c, f);
                // une los bloques seguidos con la misma profundidad en un solo rectángulo
                int fin = c + 1;
                while (fin <= c2 && distanciaAlAire(nivel, fin, f) == d) fin++;
                if (d >= 2) {
                    g.setColor(tonos[d]);
                    g.fillRect(c * T - camX, f * T - camY, (fin - c) * T, T);
                }
                c = fin;
            }
        }
    }

    /** Bloques que hay hasta el aire más cercano (arriba, abajo, izquierda o derecha). */
    private static int distanciaAlAire(Level nivel, int c, int f) {
        if (!nivel.esSolido(c, f)) return 0;
        for (int d = 1; d < PROF_MAX; d++) {
            if (!nivel.esSolido(c, f - d) || !nivel.esSolido(c, f + d)
                    || !nivel.esSolido(c - d, f) || !nivel.esSolido(c + d, f)) return d;
        }
        return PROF_MAX;
    }

    // ---------------------------------------------------------------------
    //  4. Sombras de contacto
    // ---------------------------------------------------------------------

    /** Óvalo oscuro en el suelo, centrado en cx (coordenadas de pantalla). */
    public static void sombraContacto(Graphics2D g, Level.Tema tema, int cx, int ySuelo, int ancho) {
        float a = EstiloVisual.de(tema).alfaSombraContacto;
        if (a <= 0) return;
        g.setColor(new Color(0, 0, 0, Math.round(a * 255)));
        g.fillRect(cx - ancho / 2 + 2, ySuelo - 1, ancho - 4, 2);
        g.fillRect(cx - ancho / 2, ySuelo, ancho, 1);
    }

    static Color conAlfa(Color c, float a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, Math.round(a * 255))));
    }
}
