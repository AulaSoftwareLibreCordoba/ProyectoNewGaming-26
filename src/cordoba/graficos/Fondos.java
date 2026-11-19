package cordoba.graficos;

import cordoba.niveles.Level;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

// Fondos de Córdoba en pixel art. Cada uno son varias capas de 640x360 que se mueven a distinta
// velocidad (parallax): las de lejos poco, las de cerca más.
public final class Fondos {

    public static final int ANCHO = 640, ALTO = 360;

    // cuánto se mueve cada capa con la cámara (0 = quieta, 1 = como el escenario)
    public static final float[] PARALLAX = { 0f, 0.1f, 0.3f, 0.55f };

    private static final Map<Level.Tema, BufferedImage[]> CACHE = new EnumMap<>(Level.Tema.class);

    private Fondos() { }

    // usa los PNG de assets/backgrounds si existen
    public static synchronized BufferedImage[] capas(Level.Tema tema) {
        return CACHE.computeIfAbsent(tema, t -> {
            BufferedImage[] generadas = null;
            BufferedImage[] r = new BufferedImage[4];
            for (int i = 0; i < 4; i++) {
                r[i] = Sprites.leerPng("backgrounds/" + t.name().toLowerCase() + "_capa" + i);
                if (r[i] == null) {
                    if (generadas == null) generadas = generarCapas(t);
                    r[i] = generadas[i];
                }
            }
            return r;
        });
    }

    public static BufferedImage[] generarCapas(Level.Tema tema) {
        switch (tema) {
            case RIO:
                return new BufferedImage[] { cieloDia(), ciudadLejana(), puenteRomano(), palmerasCercanas() };
            case CALLEJA:
                return new BufferedImage[] { cieloMediodia(), torreLejana(), fachadas(ALTO_FACHADAS_LEJOS, false),
                                             fachadas(ALTO_FACHADAS_CERCA, true) };
            default:
                return new BufferedImage[] { cieloAtardecer(), mezquitaLejana(), casasEncaladas(), salaColumnas() };
        }
    }

    // ---- Calleja de las Flores (nivel 2, vertical) ----
    // Las dos capas de fachadas son más altas que la pantalla: al subir por la calleja se desplazan hacia
    // abajo (GameRenderer reparte su altura entre lo que sube la cámara). Arriba asoman los tejados y la torre.

    public static final int ALTO_FACHADAS_LEJOS = 720, ALTO_FACHADAS_CERCA = 1040;

    private static BufferedImage cieloMediodia() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        bandas(g, new int[] { 0x3F94DA, 0x56A6E4, 0x6EB7EC, 0x8AC7F1, 0xA9D7F5 }, ALTO);
        g.dispose();
        return img;
    }

    // torre de la Mezquita-Catedral al fondo, con los tejados delante
    private static BufferedImage torreLejana() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        Color piedra = new Color(0xE4CDA0), sombra = new Color(0xC6A877), hueco = new Color(0x7E6248);
        int cx = 430;
        g.setColor(piedra);
        g.fillRect(cx - 24, 150, 48, 210);            // fuste
        g.fillRect(cx - 19, 104, 38, 46);             // cuerpo de campanas
        g.fillRect(cx - 14, 74, 28, 30);              // cuerpo del reloj
        g.fillRect(cx - 9, 54, 18, 20);               // linterna
        g.fillOval(cx - 10, 44, 20, 16);              // cúpula
        g.setColor(sombra);
        g.fillRect(cx + 14, 150, 10, 210);
        g.fillRect(cx + 11, 104, 8, 46);
        g.fillRect(cx + 8, 74, 6, 30);
        g.fillRect(cx - 26, 148, 52, 3);              // impostas
        g.fillRect(cx - 21, 102, 42, 3);
        g.fillRect(cx - 16, 72, 32, 3);
        g.setColor(hueco);
        for (int k = 0; k < 3; k++) {                 // arcos de las campanas
            int ax = cx - 15 + k * 11;
            g.fillRect(ax, 116, 7, 26);
            g.fillOval(ax, 112, 7, 8);
        }
        g.fillRect(cx - 4, 82, 8, 14);
        g.fillRect(cx - 3, 58, 6, 12);
        for (int y = 175; y < 340; y += 40) {         // ventanitas del fuste
            g.fillRect(cx - 12, y, 6, 12);
            g.fillRect(cx + 4, y, 6, 12);
        }
        g.setColor(new Color(0xB98A3C));              // San Rafael en lo alto
        g.fillRect(cx - 1, 30, 2, 15);
        g.fillRect(cx - 4, 33, 8, 2);
        // tejados de las casas de delante
        Random r = new Random(4);
        for (int x = 0; x < ANCHO; ) {
            int w = 50 + r.nextInt(60), top = 292 + r.nextInt(5) * 8;
            g.setColor(new Color(0xE8E1D4));
            g.fillRect(x, top + 6, w, ALTO - top);
            g.setColor(new Color(0xB86A50));
            g.fillRect(x - 2, top, w + 4, 7);
            g.setColor(new Color(0x9A5440));
            for (int tx = x; tx < x + w; tx += 3) g.fillRect(tx, top, 1, 7);
            x += w;
        }
        g.dispose();
        return img;
    }

    // fachadas encaladas de la calleja (cerca = más grandes, con más macetas y rejas)
    private static BufferedImage fachadas(int alto, boolean cerca) {
        BufferedImage img = new BufferedImage(ANCHO, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Random r = new Random(cerca ? 33 : 17);
        int tejado = cerca ? 230 : 150;               // por encima: cielo (transparente)
        int pasoV = cerca ? 72 : 52, pasoH = cerca ? 64 : 44;
        Color cal = cerca ? new Color(0xEFEAE0) : new Color(0xE4DED2);
        Color[] flores = { new Color(0xE8344A), new Color(0xFF6FA8), new Color(0xF2F2F2), new Color(0xC2185B) };
        for (int x = 0; x < ANCHO; ) {
            int w = (cerca ? 120 : 80) + r.nextInt(5) * 16;
            if (x + w > ANCHO - 40) w = ANCHO - x;
            int top = tejado + r.nextInt(4) * 10;
            g.setColor(cal);
            g.fillRect(x, top, w, alto - top);
            g.setColor(Fondos.oscurecer(cal, 0.92f));
            g.fillRect(x + w - 5, top, 5, alto - top);    // canto en sombra
            g.setColor(new Color(0xB5553C));               // alero de teja
            g.fillRect(x - 3, top - 6, w + 6, 7);
            g.setColor(new Color(0x8E3E2B));
            for (int tx = x - 3; tx < x + w + 3; tx += 4) g.fillRect(tx, top - 6, 1, 7);
            // ventanas con reja y macetas, repetidas hacia abajo (así la capa casa al desplazarse)
            int fase = r.nextInt(pasoV / 2);
            for (int wy = top + 26 + fase; wy < alto - 30; wy += pasoV) {
                for (int wx = x + 14; wx < x + w - 24; wx += pasoH) {
                    int vw = cerca ? 18 : 12, vh = cerca ? 26 : 18;
                    g.setColor(new Color(0x2E3446));
                    g.fillRect(wx, wy, vw, vh);
                    g.setColor(new Color(0x15161C));
                    for (int k = 0; k < vw; k += 4) g.fillRect(wx + k, wy, 1, vh);
                    g.fillRect(wx - 2, wy + vh, vw + 4, 2);
                    // macetas azules colgadas alrededor de la ventana
                    int n = cerca ? 4 : 2;
                    for (int k = 0; k < n; k++) {
                        int mx = wx - 8 + k * (vw + 10) / Math.max(1, n - 1);
                        int my = wy + vh + 6 + (k % 2) * 8;
                        maceta(g, mx, my, cerca, flores[r.nextInt(flores.length)]);
                    }
                    if (cerca && r.nextInt(3) == 0) maceta(g, wx + vw + 12, wy - 10, true, flores[r.nextInt(flores.length)]);
                }
            }
            x += w;
        }
        g.dispose();
        return img;
    }

    private static void maceta(Graphics2D g, int x, int y, boolean grande, Color flor) {
        int s = grande ? 2 : 1;
        g.setColor(new Color(0x2E7D32));
        g.fillRect(x - s, y - 3 * s, 7 * s, 3 * s);
        g.setColor(flor);
        g.fillRect(x, y - 4 * s, 2 * s, 2 * s);
        g.fillRect(x + 3 * s, y - 5 * s, 2 * s, 2 * s);
        g.setColor(new Color(0x1E4FA8));
        g.fillRect(x, y, 5 * s, 4 * s);
        g.setColor(new Color(0x4C7FE0));
        g.fillRect(x + s, y + s, s, 2 * s);
        g.setColor(new Color(0x4A3A2A));
        g.fillRect(x + 2 * s, y - s, s, s);           // el clavo de la pared
    }

    static Color oscurecer(Color c, float f) {
        return new Color(Math.round(c.getRed() * f), Math.round(c.getGreen() * f), Math.round(c.getBlue() * f));
    }

    private static BufferedImage nueva() {
        return new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB);
    }

    // degradado retro por bandas, con un poco de tramado entre ellas
    private static void bandas(Graphics2D g, int[] colores, int altoTotal) {
        int alto = altoTotal / colores.length;
        for (int i = 0; i < colores.length; i++) {
            g.setColor(new Color(colores[i]));
            g.fillRect(0, i * alto, ANCHO, alto + 1);
            if (i + 1 < colores.length) {
                g.setColor(new Color(colores[i + 1]));
                int y = i * alto + alto - 3;
                for (int x = 0; x < ANCHO; x += 2) {
                    g.fillRect(x + (y % 2), y, 1, 1);
                    g.fillRect(x, y + 2, 1, 1);
                    g.fillRect(x + 1, y + 2, 1, 1);
                }
            }
        }
        g.setColor(new Color(colores[colores.length - 1]));
        g.fillRect(0, colores.length * alto, ANCHO, ALTO);
    }

    private static void nube(Graphics2D g, int x, int y, int tam) {
        g.setColor(new Color(0xDCEFFA));
        g.fillOval(x, y + tam / 3, tam * 3, tam);
        g.setColor(Color.WHITE);
        g.fillOval(x + tam / 2, y, tam, tam);
        g.fillOval(x + tam, y - tam / 3, tam + 4, tam + 2);
        g.fillOval(x + tam * 2 - 4, y + 2, tam, tam - 2);
        g.fillRect(x + tam / 2, y + tam / 2, tam * 2, tam / 2);
    }

    // ---- río (nivel 1) ----

    private static BufferedImage cieloDia() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        bandas(g, new int[] { 0x4FA8E8, 0x62B6EE, 0x78C3F2, 0x92D0F4, 0xADDDF5, 0xC8E8F3, 0xE3EFE6, 0xFBE9CF }, 320);
        // sol
        g.setColor(new Color(0xFFF6CF));
        g.fillOval(478, 46, 48, 48);
        g.setColor(new Color(0xFFE98A));
        g.fillOval(484, 52, 36, 36);
        nube(g, 40, 50, 18);
        nube(g, 230, 90, 14);
        nube(g, 380, 35, 12);
        nube(g, 560, 120, 16);
        g.dispose();
        return img;
    }

    private static BufferedImage ciudadLejana() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        Random r = new Random(3);
        // Sierra Morena al fondo
        g.setColor(new Color(0x8DB49A));
        for (int x = 0; x < ANCHO; x++) {
            double a = 2 * Math.PI * x / ANCHO;
            int y = (int) (206 + 10 * Math.sin(a * 2) + 6 * Math.sin(a * 5 + 1));
            g.fillRect(x, y, 1, ALTO - y);
        }
        g.setColor(new Color(0x76A085));
        for (int x = 0; x < ANCHO; x++) {
            double a = 2 * Math.PI * x / ANCHO;
            int y = (int) (222 + 7 * Math.sin(a * 3 + 2) + 4 * Math.sin(a * 7));
            g.fillRect(x, y, 1, ALTO - y);
        }
        // casitas blancas
        for (int x = 0; x < ANCHO; ) {
            int w = 8 + r.nextInt(10), h = 8 + r.nextInt(14);
            if (x > 160 && x < 440) { x += w; continue; }
            g.setColor(new Color(0xEDE6D6));
            g.fillRect(x, 248 - h, w, h + 12);
            g.setColor(new Color(0xC77B54));
            g.fillRect(x, 248 - h, w, 2);
            g.setColor(new Color(0x9AA0A8));
            g.fillRect(x + 2, 252 - h, 2, 2);
            x += w;
        }
        // la Mezquita a lo lejos
        Color muro = new Color(0xD8B98A), sombra = new Color(0xB8976A);
        g.setColor(muro);
        g.fillRect(170, 226, 260, 34);
        for (int x = 170; x < 430; x += 6) {      // almenas
            g.fillRect(x, 223, 3, 3);
        }
        g.setColor(sombra);
        for (int x = 178; x < 425; x += 14) {      // arquitos de las puertas
            g.fillRect(x, 238, 5, 8);
            g.fillRect(x + 1, 237, 3, 1);
        }
        // nave de la catedral
        g.setColor(new Color(0xCFAE7E));
        g.fillRect(272, 204, 70, 22);
        g.fillPolygon(new int[] { 268, 307, 346 }, new int[] { 205, 192, 205 }, 3);
        // campanario
        g.setColor(new Color(0xE2C595));
        g.fillRect(212, 170, 16, 58);
        g.fillRect(215, 156, 10, 14);
        g.fillRect(218, 146, 4, 10);
        g.setColor(sombra);
        g.fillRect(217, 178, 6, 8);
        g.fillRect(217, 196, 6, 8);
        g.fillRect(218, 160, 4, 6);
        g.dispose();
        return img;
    }

    private static BufferedImage puenteRomano() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        // el Guadalquivir
        g.setColor(new Color(0x3E8DB5));
        g.fillRect(0, 286, ANCHO, ALTO - 286);
        g.setColor(new Color(0x5FA8CC));
        for (int y = 290; y < ALTO; y += 9) {
            for (int x = (y * 7) % 23; x < ANCHO; x += 31) g.fillRect(x, y, 10, 1);
        }
        // tablero del puente
        Color piedra = new Color(0xC9A46B), piedraOsc = new Color(0xA7834D), piedraClara = new Color(0xE2C08A);
        g.setColor(piedra);
        g.fillRect(0, 244, ANCHO, 52);
        g.setColor(piedraClara);
        g.fillRect(0, 244, ANCHO, 2);
        g.setColor(piedraOsc);
        g.fillRect(0, 250, ANCHO, 1);
        for (int x = 0; x < ANCHO; x += 8) g.fillRect(x, 246, 1, 4); // pretil
        // ojos del puente: 10, uno cada 64 px
        for (int i = 0; i < 10; i++) {
            int cx = i * 64 + 32;
            // por el hueco se ve el río y la otra orilla
            g.setColor(new Color(0x7FB3C9));
            g.fillOval(cx - 22, 262, 44, 44);
            g.fillRect(cx - 22, 284, 44, 20);
            g.setColor(new Color(0x5B95B3));
            g.fillRect(cx - 22, 292, 44, 10);
            // dovelas
            g.setColor(piedraOsc);
            g.drawArc(cx - 23, 261, 46, 46, 0, 180);
            g.drawArc(cx - 24, 260, 48, 48, 0, 180);
            // tajamar (la pila con punta)
            int px = i * 64;
            g.setColor(piedraOsc);
            g.fillPolygon(new int[] { px - 7, px, px + 7 }, new int[] { 296, 286, 296 }, 3);
            g.fillRect(px - 7, 296, 14, 4);
        }
        // reflejo bajo el puente
        g.setColor(new Color(0x3279A0));
        g.fillRect(0, 300, ANCHO, 4);
        g.dispose();
        return img;
    }

    private static BufferedImage palmerasCercanas() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        BufferedImage p = Sprites.palmera();
        g.drawImage(p, 70, 220, 40, 70, null);
        g.drawImage(p, 120, 232, 30, 58, null);
        g.drawImage(p, 410, 210, 48, 84, null);
        BufferedImage c = Sprites.canas();
        for (int x = 0; x < ANCHO; x += 90) g.drawImage(c, x + 30, 276, null);
        g.dispose();
        return img;
    }

    // ---- Mezquita (nivel 2) ----

    private static BufferedImage cieloAtardecer() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        bandas(g, new int[] { 0x2B1E4A, 0x4A2A63, 0x7A3470, 0xB24A6E, 0xE0686A, 0xF58E5E, 0xFDB46A, 0xFFD58A }, 320);
        g.setColor(new Color(0xFFE6A8));
        g.fillOval(390, 200, 64, 64);
        g.setColor(new Color(0xFFF3CF));
        g.fillOval(400, 210, 44, 44);
        Random r = new Random(11);
        g.setColor(new Color(0xFFF2D0));
        for (int i = 0; i < 40; i++) g.fillRect(r.nextInt(ANCHO), r.nextInt(80), 1, 1);
        g.dispose();
        return img;
    }

    private static BufferedImage mezquitaLejana() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        Color muro = new Color(0x9C5F52), muroOsc = new Color(0x7E4A42), muroCla = new Color(0xB6735F);
        // muro con contrafuertes y almenas
        g.setColor(muro);
        g.fillRect(0, 216, ANCHO, ALTO - 216);
        for (int x = 0; x < ANCHO; x += 8) {
            g.fillRect(x + 1, 212, 6, 4);
            g.fillRect(x + 2, 209, 4, 3);
            g.fillRect(x + 3, 207, 2, 2);
        }
        for (int x = 20; x < ANCHO; x += 40) {
            g.setColor(muroCla);
            g.fillRect(x, 216, 6, 60);
            g.setColor(muroOsc);
            // puertas de herradura
            g.fillRect(x + 14, 236, 10, 20);
            g.fillOval(x + 13, 230, 12, 12);
        }
        // crucero de la catedral
        g.setColor(new Color(0x8C5348));
        g.fillRect(290, 180, 110, 36);
        g.fillPolygon(new int[] { 284, 345, 406 }, new int[] { 182, 160, 182 }, 3);
        g.setColor(muroOsc);
        for (int x = 300; x < 395; x += 16) g.fillRect(x, 190, 5, 12);
        // campanario
        Color torre = new Color(0xB06B55);
        g.setColor(torre);
        g.fillRect(104, 118, 34, 100);
        g.fillRect(108, 92, 26, 26);
        g.fillRect(112, 74, 18, 18);
        g.fillRect(116, 62, 10, 12);
        g.fillRect(120, 52, 2, 10);
        g.fillRect(118, 54, 6, 2);
        g.setColor(muroOsc);
        g.fillRect(112, 130, 6, 12);
        g.fillRect(124, 130, 6, 12);
        g.fillRect(112, 160, 6, 12);
        g.fillRect(124, 160, 6, 12);
        g.fillRect(114, 98, 5, 12);
        g.fillRect(123, 98, 5, 12);
        g.fillRect(118, 78, 6, 9);
        g.setColor(new Color(0xD08A6A));
        g.fillRect(104, 118, 34, 2);
        g.fillRect(108, 92, 26, 2);
        // palmeras en silueta
        g.setColor(new Color(0x5E3A3F));
        for (int px : new int[] { 220, 470, 560 }) {
            g.fillRect(px, 170, 3, 50);
            for (int k = -12; k <= 12; k += 4) g.fillRect(px + k, 168 + Math.abs(k) / 3, 4, 2);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage casasEncaladas() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        Random r = new Random(21);
        int x = 0;
        while (x < ANCHO) {
            int w = 48 + r.nextInt(5) * 8;
            if (x + w > ANCHO - 30) w = ANCHO - x;  // la última casa cierra el mosaico
            int h = 70 + r.nextInt(6) * 10;
            int top = 300 - h;
            // fachada encalada
            g.setColor(new Color(0xF4EFE4));
            g.fillRect(x, top, w, h + 60);
            g.setColor(new Color(0xDAD2C2));
            g.fillRect(x + w - 4, top, 4, h + 60);
            // zócalo color albero
            g.setColor(new Color(0xE0B85C));
            g.fillRect(x, 288, w, 72);
            // tejado
            g.setColor(new Color(0xB5553C));
            g.fillRect(x - 2, top - 4, w + 4, 5);
            g.setColor(new Color(0x8E3E2B));
            for (int tx = x - 2; tx < x + w + 2; tx += 3) g.fillRect(tx, top - 4, 1, 5);
            // ventanas con reja
            for (int wy = top + 12; wy < 270; wy += 30) {
                for (int wx = x + 8; wx < x + w - 14; wx += 22) {
                    g.setColor(new Color(0x3A3346));
                    g.fillRect(wx, wy, 10, 14);
                    g.setColor(new Color(0x1E1E26));
                    for (int k = 0; k < 10; k += 3) g.fillRect(wx + k, wy, 1, 14);
                    g.fillRect(wx - 1, wy + 14, 12, 1);
                    // maceta azul colgando
                    g.setColor(new Color(0x1E4FA8));
                    g.fillRect(wx + 2, wy + 17, 6, 4);
                    g.setColor(new Color(0x2E7D32));
                    g.fillRect(wx + 1, wy + 15, 8, 2);
                    g.setColor(r.nextBoolean() ? new Color(0xE8344A) : new Color(0xFF6FA8));
                    g.fillRect(wx + 2, wy + 14, 2, 2);
                    g.fillRect(wx + 6, wy + 14, 2, 2);
                }
            }
            // puerta con arco
            if (w >= 56) {
                int dx = x + w / 2 - 7;
                g.setColor(new Color(0xD9A441));
                g.fillRect(dx - 2, 262, 18, 28);
                g.fillOval(dx - 2, 256, 18, 14);
                g.setColor(new Color(0x6B3E1F));
                g.fillRect(dx, 264, 14, 26);
                g.fillOval(dx, 258, 14, 12);
                g.setColor(new Color(0xE0C060));
                g.fillRect(dx + 10, 276, 2, 2);
            }
            x += w;
        }
        g.dispose();
        return img;
    }

    // arco de herradura con dovelas rojas y blancas
    private static void arcoDovelas(Graphics2D g, int cx, int cy, int radio, int grosor) {
        int segmentos = 14;
        double ini = Math.toRadians(-200), fin = Math.toRadians(20);
        for (int i = 0; i < segmentos; i++) {
            double a1 = ini + (fin - ini) * i / segmentos;
            double a2 = ini + (fin - ini) * (i + 1) / segmentos;
            int[] xs = {
                (int) (cx + Math.cos(a1) * radio), (int) (cx + Math.cos(a2) * radio),
                (int) (cx + Math.cos(a2) * (radio - grosor)), (int) (cx + Math.cos(a1) * (radio - grosor)) };
            int[] ys = {
                (int) (cy + Math.sin(a1) * radio), (int) (cy + Math.sin(a2) * radio),
                (int) (cy + Math.sin(a2) * (radio - grosor)), (int) (cy + Math.sin(a1) * (radio - grosor)) };
            g.setColor(i % 2 == 0 ? new Color(0x9E3326) : new Color(0xE6D3AA));
            g.fillPolygon(xs, ys, 4);
        }
    }

    private static BufferedImage salaColumnas() {
        BufferedImage img = nueva();
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x2A1A14));
        g.fillRect(0, 0, ANCHO, ALTO);
        // bosque de columnas con doble arco, como en la Mezquita
        for (int fila = 0; fila < 2; fila++) {
            int sep = fila == 0 ? 64 : 48;
            int base = fila == 0 ? 0 : 24;
            float osc = fila == 0 ? 0.55f : 1f;
            for (int cx = base; cx < ANCHO + sep; cx += sep) {
                int ax = cx + sep / 2;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, osc));
                arcoDovelas(g2, ax, 110 - fila * 10, sep / 2 - 2, 9);
                arcoDovelas(g2, ax, 50 - fila * 10, sep / 2 - 2, 8);
                g2.setColor(new Color(0xC9B48A));
                g2.fillRect(cx - 3, 104 - fila * 10, 6, ALTO);
                g2.setColor(new Color(0xE8D7AE));
                g2.fillRect(cx - 3, 104 - fila * 10, 2, ALTO);
                g2.setColor(new Color(0x8C7A57));
                g2.fillRect(cx - 5, 100 - fila * 10, 10, 4); // capitel
                g2.dispose();
            }
        }
        // lámparas
        g.setColor(new Color(0xFFCF6B));
        for (int x = 40; x < ANCHO; x += 128) {
            g.fillRect(x, 0, 1, 150);
            g.fillOval(x - 4, 150, 9, 7);
        }
        g.dispose();
        return img;
    }
}
