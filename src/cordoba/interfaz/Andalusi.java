package cordoba.interfaz;

import java.awt.*;

/**
 * Motivos decorativos de inspiración andalusí en pixel art, para la interfaz:
 *
 *  - Estrella de ocho puntas (dos cuadrados superpuestos).
 *  - Dovelas rojas y blancas, como los arcos de la Mezquita.
 *  - Friso de arcos de herradura.
 *  - Cenefa de alicatado (azulejos geométricos).
 *  - Esquinas escalonadas, como las almenas de la Mezquita.
 *  - Botones con todo lo anterior.
 */
public final class Andalusi {

    private Andalusi() { }

    // Paleta
    public static final Color ORO        = new Color(0xD9A441);
    public static final Color ORO_CLARO  = new Color(0xF6D77E);
    public static final Color ORO_OSCURO = new Color(0x8C5A1C);
    public static final Color MARRON     = new Color(0x2A140A);
    public static final Color ROJO       = new Color(0xB33A2C);
    public static final Color ROJO_OSC   = new Color(0x7A2219);
    public static final Color CREMA      = new Color(0xF3E4C0);
    public static final Color AZUL       = new Color(0x1F5F8B);
    public static final Color TURQUESA   = new Color(0x2A9D8F);
    public static final Color VERDE      = new Color(0x1E6B47);

    // Colores de los botones (cuerpo)
    public static final Color BOTON_VERDE   = new Color(0x1F7A55);
    public static final Color BOTON_ALBERO  = new Color(0xB9832A);
    public static final Color BOTON_GRANATE = new Color(0x9A2E2A);
    public static final Color BOTON_AZUL    = new Color(0x2C5E86);

    // =====================================================================

    /** Estrella de ocho puntas centrada en (cx, cy). giro = 0..1 la hace rotar. */
    public static void estrella(Graphics2D g, int cx, int cy, int r, Color relleno, Color borde, double giro) {
        double a0 = giro * Math.PI / 2;
        for (int capa = 0; capa < 2; capa++) {
            Color c = capa == 0 ? borde : relleno;
            double rr = capa == 0 ? r + 1 : r;
            for (int sq = 0; sq < 2; sq++) {
                int[] xs = new int[4], ys = new int[4];
                for (int i = 0; i < 4; i++) {
                    double a = a0 + sq * Math.PI / 4 + i * Math.PI / 2 + Math.PI / 4;
                    xs[i] = cx + (int) Math.round(Math.cos(a) * rr);
                    ys[i] = cy + (int) Math.round(Math.sin(a) * rr);
                }
                g.setColor(c);
                g.fillPolygon(xs, ys, 4);
            }
        }
        g.setColor(borde);
        g.fillRect(cx, cy, 1, 1);   // punto central
    }

    /** Estrella de ocho puntas pequeña dibujada píxel a píxel (para botones). */
    private static final String[] ESTRELLA_PEQ = {
        "...#...",
        ".#####.",
        ".##.##.",
        "###.###",
        ".##.##.",
        ".#####.",
        "...#...",
    };

    public static void estrellaPequena(Graphics2D g, int cx, int cy, Color relleno, Color borde, Color centro) {
        int x0 = cx - 3, y0 = cy - 3;
        g.setColor(borde);
        for (int y = 0; y < 7; y++)
            for (int x = 0; x < 7; x++)
                if (ESTRELLA_PEQ[y].charAt(x) != '.') {
                    g.fillRect(x0 + x - 1, y0 + y, 1, 1);
                    g.fillRect(x0 + x + 1, y0 + y, 1, 1);
                    g.fillRect(x0 + x, y0 + y - 1, 1, 1);
                    g.fillRect(x0 + x, y0 + y + 1, 1, 1);
                }
        for (int y = 0; y < 7; y++)
            for (int x = 0; x < 7; x++) {
                char c = ESTRELLA_PEQ[y].charAt(x);
                if (c == '#') { g.setColor(relleno); g.fillRect(x0 + x, y0 + y, 1, 1); }
                else if (c == '.' && x >= 2 && x <= 4 && y >= 2 && y <= 4) { g.setColor(centro); g.fillRect(x0 + x, y0 + y, 1, 1); }
            }
    }

    /** Arco de herradura de dovelas alternas (rojo / crema). */
    public static void dovelas(Graphics2D g, int cx, int cy, int radio, int grosor, int segmentos, Color a, Color b) {
        double ini = Math.toRadians(-200), fin = Math.toRadians(20);
        for (int i = 0; i < segmentos; i++) {
            double a1 = ini + (fin - ini) * i / segmentos, a2 = ini + (fin - ini) * (i + 1) / segmentos;
            int[] xs = { cx + (int) Math.round(Math.cos(a1) * radio), cx + (int) Math.round(Math.cos(a2) * radio),
                         cx + (int) Math.round(Math.cos(a2) * (radio - grosor)), cx + (int) Math.round(Math.cos(a1) * (radio - grosor)) };
            int[] ys = { cy + (int) Math.round(Math.sin(a1) * radio), cy + (int) Math.round(Math.sin(a2) * radio),
                         cy + (int) Math.round(Math.sin(a2) * (radio - grosor)), cy + (int) Math.round(Math.sin(a1) * (radio - grosor)) };
            g.setColor(i % 2 == 0 ? a : b);
            g.fillPolygon(xs, ys, 4);
        }
    }

    /** Fila de arcos de herradura pequeños (arquería ciega) entre x y x+w. */
    public static void frisoArcos(Graphics2D g, int x, int y, int w, int separacion, Color hueco) {
        int n = Math.max(1, w / separacion);
        int sobra = (w - n * separacion) / 2;
        int radio = separacion / 2 - 1;
        for (int i = 0; i < n; i++) {
            int cx = x + sobra + i * separacion + separacion / 2;
            g.setColor(hueco);
            g.fillOval(cx - radio + 2, y + 2, radio * 2 - 4, radio * 2 - 2);
            g.fillRect(cx - radio + 2, y + radio, radio * 2 - 4, radio + 2);
            dovelas(g, cx, y + radio, radio, 3, 8, ROJO, CREMA);
            // columnita entre arcos
            g.setColor(ORO);
            g.fillRect(cx + radio - 1, y + radio, 2, radio + 3);
        }
    }

    /** Cenefa de alicatado de alto 6: rombos y estrellitas en azul, turquesa y crema. */
    public static void cenefa(Graphics2D g, int x, int y, int w) {
        g.setColor(AZUL);
        g.fillRect(x, y, w, 6);
        for (int i = 0; i < w; i += 8) {
            g.setColor(CREMA);
            g.fillRect(x + i + 3, y + 1, 2, 4);     // rombo (cruz)
            g.fillRect(x + i + 2, y + 2, 4, 2);
            g.setColor(TURQUESA);
            g.fillRect(x + i + 3, y + 2, 2, 2);
            g.setColor(ORO);
            if (i + 7 < w) g.fillRect(x + i + 7, y + 2, 1, 2);
        }
        g.setColor(ORO_OSCURO);
        g.fillRect(x, y, w, 1);
        g.fillRect(x, y + 5, w, 1);
    }

    /** Rectángulo con las esquinas escalonadas (como una almena). */
    public static void escalonado(Graphics2D g, int x, int y, int w, int h, int paso, Color c) {
        g.setColor(c);
        g.fillRect(x + paso * 2, y, w - paso * 4, h);
        g.fillRect(x + paso, y + paso, w - paso * 2, h - paso * 2);
        g.fillRect(x, y + paso * 2, w, h - paso * 4);
    }

    /** Marco: borde oscuro, filete de oro y esquinas escalonadas, con estrellas en las esquinas. */
    public static void marco(Graphics2D g, int x, int y, int w, int h, Color fondo, boolean estrellas) {
        g.setColor(new Color(0, 0, 0, 110));
        g.fillRect(x + 4, y + 4, w, h);
        escalonado(g, x, y, w, h, 2, MARRON);
        escalonado(g, x + 1, y + 1, w - 2, h - 2, 2, ORO);
        escalonado(g, x + 3, y + 3, w - 6, h - 6, 2, MARRON);
        escalonado(g, x + 4, y + 4, w - 8, h - 8, 2, fondo);
        if (estrellas) {
            estrella(g, x + 4, y + 4, 4, ORO_CLARO, MARRON, 0);
            estrella(g, x + w - 5, y + 4, 4, ORO_CLARO, MARRON, 0);
            estrella(g, x + 4, y + h - 5, 4, ORO_CLARO, MARRON, 0);
            estrella(g, x + w - 5, y + h - 5, 4, ORO_CLARO, MARRON, 0);
        }
    }

    // =====================================================================
    //  BOTÓN
    // =====================================================================

    /**
     * Botón andalusí: esquinas escalonadas, filete de oro, banda de dovelas
     * arriba, cenefa abajo y estrellas a los lados del texto.
     */
    public static void boton(Graphics2D g, int x, int y, int w, int h, String texto,
                             Color base, boolean encima, boolean activo) {
        if (!activo) base = new Color(0x55556A);
        double t = System.nanoTime() / 1e9;
        int dy = encima && activo ? -1 : 0;
        y += dy;
        Color cuerpo = encima ? mezclar(base, Color.WHITE, 0.18f) : base;

        // sombra
        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(x + 2, y + 3 - dy, w, h);
        // contorno, filete de oro y cuerpo
        escalonado(g, x, y, w, h, 1, MARRON);
        escalonado(g, x + 1, y + 1, w - 2, h - 2, 1, encima ? ORO_CLARO : ORO);
        escalonado(g, x + 2, y + 2, w - 4, h - 4, 1, MARRON);
        g.setColor(cuerpo);
        g.fillRect(x + 3, y + 3, w - 6, h - 6);
        // luz arriba y sombra abajo (volumen)
        g.setColor(mezclar(cuerpo, Color.WHITE, 0.22f));
        g.fillRect(x + 3, y + 3, w - 6, 1);
        g.setColor(mezclar(cuerpo, Color.BLACK, 0.30f));
        g.fillRect(x + 3, y + h - 5, w - 6, 2);

        boolean grande = h >= 20;
        if (grande) {
            // banda de dovelas (rojo y crema) bajo el borde superior
            for (int i = x + 4, k = 0; i < x + w - 4; i += 5, k++) {
                g.setColor(k % 2 == 0 ? ROJO : CREMA);
                g.fillRect(i, y + 4, Math.min(4, x + w - 4 - i), 2);
            }
            // cenefa fina abajo
            for (int i = x + 4; i < x + w - 4; i += 4) {
                g.setColor(TURQUESA);
                g.fillRect(i, y + h - 5, 2, 1);
                g.setColor(CREMA);
                g.fillRect(i + 2, y + h - 5, 1, 1);
            }
        }

        // texto
        g.setFont(h >= 18 ? Ui.FUENTE : Ui.FUENTE_PEQ);
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(texto);
        int cy = y + h / 2 + (grande ? 1 : 0);
        int ty = cy + fm.getAscent() / 2 - 2;
        Ui.texto(g, texto, x + (w - tw) / 2, ty, activo ? (encima ? ORO_CLARO : CREMA) : new Color(0xA0A0B0));

        // estrellas a los lados (laten cuando el ratón está encima)
        if (grande && w >= 80) {
            int sx = Math.max(x + 10, x + (w - tw) / 2 - 14);
            boolean destello = encima && ((int) (t * 4)) % 2 == 0;
            Color relleno = destello ? Color.WHITE : encima ? ORO_CLARO : ORO;
            Color centro = encima ? ROJO : cuerpo;
            estrellaPequena(g, sx, cy - 1, relleno, MARRON, centro);
            estrellaPequena(g, x + w - (sx - x) - 1, cy - 1, relleno, MARRON, centro);
        }
        // brillo que recorre el botón al pasar el ratón
        if (encima && activo) {
            int bx = x + 3 + (int) ((t * 90) % (w + 20)) - 10;
            g.setColor(new Color(255, 255, 255, 60));
            for (int k = 0; k < 4; k++) {
                int px = bx + k;
                if (px > x + 3 && px < x + w - 4) g.fillRect(px, y + 3, 1, h - 6);
            }
        }
    }

    static Color mezclar(Color a, Color b, float t) {
        return new Color(Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                         Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                         Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
