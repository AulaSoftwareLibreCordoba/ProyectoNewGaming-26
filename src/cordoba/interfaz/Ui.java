package cordoba.interfaz;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades de dibujo de interfaz con estética pixel art:
 * fuentes, textos con sombra, paneles con borde y ajuste de líneas.
 */
public final class Ui {

    private Ui() { }

    public static final Font FUENTE        = new Font(Font.MONOSPACED, Font.BOLD, 11);
    public static final Font FUENTE_PEQ    = new Font(Font.MONOSPACED, Font.BOLD, 10);
    public static final Font FUENTE_CODIGO = new Font(Font.MONOSPACED, Font.BOLD, 11);
    public static final Font FUENTE_GRANDE = new Font(Font.MONOSPACED, Font.BOLD, 20);
    public static final Font FUENTE_TITULO = new Font(Font.MONOSPACED, Font.BOLD, 30);

    // Paleta de la interfaz
    public static final Color PANEL      = new Color(0x1E1E2E);
    public static final Color PANEL_2    = new Color(0x2A2A3D);
    public static final Color BORDE      = new Color(0xF2E38A);
    public static final Color TEXTO      = new Color(0xF4F1E6);
    public static final Color VERDE      = new Color(0x7BE07B);
    public static final Color AMARILLO   = new Color(0xFFD54F);
    public static final Color ROJO       = new Color(0xFF6B6B);
    public static final Color GRIS       = new Color(0x8A8AA0);

    /** Texto con sombra de 1 píxel (se lee bien sobre cualquier fondo). */
    public static void texto(Graphics2D g, String s, int x, int y, Color color) {
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(s, x + 1, y + 1);
        g.setColor(color);
        g.drawString(s, x, y);
    }

    /** Texto con contorno grueso, para títulos. */
    public static void textoContorno(Graphics2D g, String s, int x, int y, Color color, Color contorno) {
        g.setColor(contorno);
        for (int dx = -2; dx <= 2; dx++)
            for (int dy = -2; dy <= 2; dy++)
                if (dx != 0 || dy != 0) g.drawString(s, x + dx, y + dy);
        g.setColor(color);
        g.drawString(s, x, y);
    }

    public static void textoCentrado(Graphics2D g, String s, int cx, int y, Color color) {
        int w = g.getFontMetrics().stringWidth(s);
        texto(g, s, cx - w / 2, y, color);
    }

    /** Panel rectangular con doble borde "retro". */
    public static void panel(Graphics2D g, int x, int y, int w, int h, Color fondo, Color borde) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(x + 3, y + 3, w, h);
        g.setColor(fondo);
        g.fillRect(x, y, w, h);
        g.setColor(borde);
        g.drawRect(x, y, w - 1, h - 1);
        g.setColor(borde.darker());
        g.drawRect(x + 2, y + 2, w - 5, h - 5);
    }

    /** Parte un texto en líneas que quepan en "anchoMax" píxeles. */
    public static List<String> ajustar(Graphics2D g, String texto, int anchoMax) {
        List<String> lineas = new ArrayList<>();
        FontMetrics fm = g.getFontMetrics();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) > anchoMax && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) lineas.add(actual.toString());
        return lineas;
    }
}
