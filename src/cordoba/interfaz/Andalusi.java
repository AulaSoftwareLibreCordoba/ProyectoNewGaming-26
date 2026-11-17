package cordoba.interfaz;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

// Adornos andalusíes para la interfaz: estrellas, dovelas, arcos, alicatado y botones.
public final class Andalusi {

    private Andalusi() { }

    // paleta
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

    // colores de los botones
    public static final Color BOTON_VERDE   = new Color(0x1F7A55);
    public static final Color BOTON_ALBERO  = new Color(0xB9832A);
    public static final Color BOTON_GRANATE = new Color(0x9A2E2A);
    public static final Color BOTON_AZUL    = new Color(0x2C5E86);

    // estrella de ocho puntas en (cx, cy); giro de 0 a 1 la hace rotar
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

    // estrellita para los botones, píxel a píxel
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

    // arco de herradura con dovelas alternas
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

    // fila de arquitos de herradura entre x y x+w
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

    // cenefa de alicatado de 6 px: rombos y estrellitas
    public static void cenefa(Graphics2D g, int x, int y, int w) {
        g.setColor(AZUL);
        g.fillRect(x, y, w, 6);
        for (int i = 0; i < w; i += 8) {
            g.setColor(CREMA);
            g.fillRect(x + i + 3, y + 1, 2, 4);     // rombo
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

    // rectángulo con las esquinas escalonadas, como una almena
    public static void escalonado(Graphics2D g, int x, int y, int w, int h, int paso, Color c) {
        g.setColor(c);
        g.fillRect(x + paso * 2, y, w - paso * 4, h);
        g.fillRect(x + paso, y + paso, w - paso * 2, h - paso * 2);
        g.fillRect(x, y + paso * 2, w, h - paso * 4);
    }

    // marco: borde oscuro, filete de oro, esquinas escalonadas y estrellas
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

    // ---- botón ----

    // botón andalusí
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
        // luz arriba y sombra abajo
        g.setColor(mezclar(cuerpo, Color.WHITE, 0.22f));
        g.fillRect(x + 3, y + 3, w - 6, 1);
        g.setColor(mezclar(cuerpo, Color.BLACK, 0.30f));
        g.fillRect(x + 3, y + h - 5, w - 6, 2);

        boolean grande = h >= 20;
        if (grande) {
            // banda de dovelas bajo el borde
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
        int ty = Ui.baseCentrada(g, y, h);
        Ui.texto(g, texto, x + (w - tw) / 2, ty, activo ? (encima ? ORO_CLARO : CREMA) : new Color(0xA0A0B0));

        // estrellas a los lados (laten con el ratón encima)
        if (grande && w >= 80) {
            int sx = Math.max(x + 10, x + (w - tw) / 2 - 14);
            boolean destello = encima && ((int) (t * 4)) % 2 == 0;
            Color relleno = destello ? Color.WHITE : encima ? ORO_CLARO : ORO;
            Color centro = encima ? ROJO : cuerpo;
            estrellaPequena(g, sx, cy - 1, relleno, MARRON, centro);
            estrellaPequena(g, x + w - (sx - x) - 1, cy - 1, relleno, MARRON, centro);
        }
        // brillo que recorre el botón con el ratón encima
        if (encima && activo) {
            int bx = x + 3 + (int) ((t * 90) % (w + 20)) - 10;
            g.setColor(new Color(255, 255, 255, 60));
            for (int k = 0; k < 4; k++) {
                int px = bx + k;
                if (px > x + 3 && px < x + w - 4) g.fillRect(px, y + 3, 1, h - 6);
            }
        }
    }

    // Texto un poco más bajo: se dibuja a 16 (a 14 la fuente se deforma) y se quita la 3.ª fila de las
    // mayúsculas. Devuelve una máscara blanca recortada.
    private static final Map<String, java.awt.image.BufferedImage> CACHE_TEXTO = new HashMap<>();

    private static synchronized java.awt.image.BufferedImage textoBajo(String texto) {
        return CACHE_TEXTO.computeIfAbsent(texto, t -> {
            Graphics2D m = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
            m.setFont(Ui.FUENTE);
            FontMetrics fm = m.getFontMetrics();
            int w = fm.stringWidth(t) + 2, base = fm.getAscent(), h = base + fm.getDescent();
            m.dispose();
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            Ui.preparar(g);
            g.setFont(Ui.FUENTE);
            g.setColor(Color.WHITE);
            g.drawString(t, 0, base);
            g.dispose();
            int quitar = base - 7;   // 3.ª fila de las mayúsculas
            // recorta lo vacío y se salta esa fila
            int arriba = h, abajo = -1;
            for (int y = 0; y < h; y++)
                for (int x = 0; x < w; x++)
                    if ((img.getRGB(x, y) >>> 24) > 0) { arriba = Math.min(arriba, y); abajo = Math.max(abajo, y); }
            if (abajo < 0) return img;
            int alto = abajo - arriba + 1 - (quitar >= arriba && quitar <= abajo ? 1 : 0);
            java.awt.image.BufferedImage r = new java.awt.image.BufferedImage(w, alto, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            for (int y = arriba, yr = 0; y <= abajo; y++) {
                if (y == quitar) continue;
                for (int x = 0; x < w; x++) r.setRGB(x, yr, img.getRGB(x, y));
                yr++;
            }
            return r;
        });
    }

    // la máscara blanca pintada de un color
    private static java.awt.image.BufferedImage tenido(java.awt.image.BufferedImage mascara, Color c) {
        java.awt.image.BufferedImage r = new java.awt.image.BufferedImage(mascara.getWidth(), mascara.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        int rgb = c.getRGB() & 0xFFFFFF;
        for (int y = 0; y < r.getHeight(); y++)
            for (int x = 0; x < r.getWidth(); x++) {
                int a = mascara.getRGB(x, y) >>> 24;
                if (a > 0) r.setRGB(x, y, (a << 24) | rgb);
            }
        return r;
    }

    // botón de los menús con píxeles x2 (se pinta a media resolución y se escala). w y h pares
    public static void botonGrande(Graphics2D g, int x, int y, int w, int h, String texto,
                                   Color base, boolean encima, boolean activo) {
        if (!activo) base = new Color(0x55556A);
        int bw = w / 2, bh = h / 2;
        double t = System.nanoTime() / 1e9;
        Color cuerpo = encima ? mezclar(base, Color.WHITE, 0.15f) : base;
        Color filete = encima ? ORO_CLARO : ORO;

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(bw, bh, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D b = img.createGraphics();
        Ui.preparar(b);
        // contorno con esquinas recortadas, filete de oro y cuerpo
        b.setColor(MARRON);
        b.fillRect(1, 0, bw - 2, bh);
        b.fillRect(0, 1, bw, bh - 2);
        b.setColor(filete);
        b.fillRect(2, 1, bw - 4, bh - 2);
        b.fillRect(1, 2, bw - 2, bh - 4);
        b.setColor(cuerpo);
        b.fillRect(2, 2, bw - 4, bh - 4);
        // luz arriba, sombra abajo
        b.setColor(mezclar(cuerpo, Color.WHITE, 0.25f));
        b.fillRect(2, 2, bw - 4, 1);
        b.setColor(mezclar(cuerpo, Color.BLACK, 0.30f));
        b.fillRect(2, bh - 4, bw - 4, 2);
        // brillo que recorre el botón con el ratón encima
        if (encima && activo) {
            int sx = 2 + (int) ((t * 45) % (bw + 10)) - 5;
            b.setColor(new Color(255, 255, 255, 70));
            for (int k = 0; k < 3; k++)
                if (sx + k > 2 && sx + k < bw - 3) b.fillRect(sx + k, 3, 1, bh - 7);
        }

        // texto con sombra
        java.awt.image.BufferedImage letras = textoBajo(texto);
        int tx = (bw - letras.getWidth()) / 2;
        int ty = (bh - 3 - letras.getHeight()) / 2 + 1;
        b.drawImage(tenido(letras, MARRON), tx, ty + 1, null);
        b.drawImage(tenido(letras, activo ? (encima ? ORO_CLARO : CREMA) : new Color(0xA0A0B0)), tx, ty, null);

        // estrellitas a los lados (destellan con el ratón encima)
        boolean destello = encima && ((int) (t * 4)) % 2 == 0;
        Color relleno = destello ? Color.WHITE : filete;
        Color centro = encima ? ROJO : cuerpo;
        int cy = (bh - 3) / 2 + 1;
        estrellaPequena(b, 6, cy, relleno, MARRON, centro);
        estrellaPequena(b, bw - 7, cy, relleno, MARRON, centro);
        b.dispose();

        int dy = encima && activo ? -2 : 0;
        // sombra en el suelo (también a 2x)
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(x + 2, y + 4, w - 2, h - 2);
        g.drawImage(img, x, y + dy, w, h, null);
    }

    // texto con píxeles x2, como el de los botones
    public static void textoGrande(Graphics2D g, String texto, int x, int y, Color color) {
        java.awt.image.BufferedImage letras = textoBajo(texto);
        int w = letras.getWidth() * 2, h = letras.getHeight() * 2;
        g.drawImage(tenido(letras, MARRON), x, y + 2, w, h, null);
        g.drawImage(tenido(letras, color), x, y, w, h, null);
    }

    public static int anchoTextoGrande(String texto) {
        return textoBajo(texto).getWidth() * 2;
    }

    public static int altoTextoGrande(String texto) {
        return textoBajo(texto).getHeight() * 2;
    }

    static Color mezclar(Color a, Color b, float t) {
        return new Color(Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                         Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                         Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
}
