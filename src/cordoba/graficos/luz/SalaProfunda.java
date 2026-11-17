package cordoba.graficos.luz;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Fondo del INTERIOR de la Mezquita con profundidad: un "bosque" de columnas
 * y arcos dobles repartido en varios PLANOS.
 *
 *  - Telón: luz cálida al fondo y una arquería lejanísima en silueta.
 *  - Planos 0..3: del más lejano al más cercano. Cuanto más lejos, más pequeños
 *    (perspectiva hacia el horizonte), más oscuros (niebla) y más despacio se
 *    mueven con la cámara (parallax).
 *  - Lámparas colgantes encendidas en los planos cercanos.
 *
 * Cada plano se prepara una sola vez: la franja de arcos es una imagen y las
 * columnas se pintan con rectángulos (así casi no cuesta tiempo).
 * La niebla de cada plano se ajusta en {@link EstiloVisual#nieblaSala}.
 */
public final class SalaProfunda {

    private static final int ANCHO = 640, ALTO = 360;
    /** Altura del horizonte (coordenadas de la capa): ahí convergen todos los planos. */
    private static final int HORIZONTE = 112;

    /** Un plano: separación entre columnas (su tamaño), parallax y desfase de las columnas. */
    private record Plano(int separacion, float parallax, int desfase, boolean lamparas) {
        float escala() { return separacion / 80f; }
    }

    private static final Plano[] PLANOS = {
        new Plano(32, 0.16f, 0, false),     // lejos: columnas pequeñas y muchas
        new Plano(64, 0.36f, 21, true),     // medio
        new Plano(128, 0.66f, 90, true),    // cerca: columnas grandes y pocas
    };

    // Colores sin niebla
    private static final int ROJO = 0x9E3326, CREMA = 0xE6D3AA, PIEDRA = 0xC9B48A, PIEDRA_LUZ = 0xE8D7AE,
                             PIEDRA_SOMBRA = 0x9C8862, CAPITEL = 0x8C7A57, MADERA = 0x4A2C1A, VIGA = 0x2E1A10,
                             ORO = 0xC8963E, SUELO = 0x5A3A26, SUELO_LINEA = 0x6E4A30, LAMPARA = 0xFFCF6B;

    /** Todo lo que se precalcula de cada plano. */
    private static final class Hecho {
        BufferedImage banda;
        Color fuste, fusteLuz, fusteSombra, capitel, suelo, sueloLinea;
        int yFuste, ySuelo, anchoFuste;
        float niebla;
    }

    private static Hecho[] hechos;
    private static BufferedImage telon;

    private SalaProfunda() { }

    /**
     * Dibuja la sala completa. dy = altura de pantalla donde empieza la capa,
     * alfa = transparencia (aparece poco a poco al bajar al interior).
     */
    public static void dibujar(Graphics2D g, int camX, int dy, float alfa) {
        preparar();
        EstiloVisual e = EstiloVisual.de(cordoba.niveles.Level.Tema.MEZQUITA);
        Composite antes = g.getComposite();
        Composite base = alfa < 1f ? AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa) : AlphaComposite.SrcOver;
        g.setComposite(base);
        g.drawImage(telon, 0, dy, null);

        for (int i = 0; i < PLANOS.length; i++) {
            Plano p = PLANOS[i];
            Hecho h = hechos[i];
            int sep = p.separacion;
            int desp = Math.floorMod((int) (camX * p.parallax), ANCHO);

            // suelo del plano (con juntas horizontales)
            g.setColor(h.suelo);
            g.fillRect(0, dy + h.ySuelo, ANCHO, ALTO - h.ySuelo);
            g.setColor(h.sueloLinea);
            for (int y = h.ySuelo + 2, k = 2; y < ALTO; y += k, k += 2) g.fillRect(0, dy + y, ANCHO, 1);

            // fustes de las columnas
            int x0 = Math.floorMod(p.desfase - desp, sep) - sep;
            int fw = h.anchoFuste;
            for (int x = x0; x < ANCHO + sep; x += sep) {
                int fx = x - fw / 2;
                g.setColor(h.fuste);
                g.fillRect(fx, dy + h.yFuste, fw, h.ySuelo - h.yFuste);
                g.setColor(h.fusteLuz);
                g.fillRect(fx, dy + h.yFuste, Math.max(1, fw / 4), h.ySuelo - h.yFuste);
                g.setColor(h.fusteSombra);
                g.fillRect(fx + fw - Math.max(1, fw / 4), dy + h.yFuste, Math.max(1, fw / 4), h.ySuelo - h.yFuste);
                g.setColor(h.capitel);                                     // basa
                g.fillRect(fx - 1, dy + h.ySuelo - Math.max(1, fw / 3), fw + 2, Math.max(1, fw / 3));
            }

            // franja de arcos (imagen precalculada que se repite)
            g.drawImage(h.banda, -desp, dy, null);
            g.drawImage(h.banda, -desp + ANCHO, dy, null);

            // lámparas colgantes
            if (p.lamparas && e.radioLampara > 0) {
                float s = p.escala();
                int yTecho = y(6, s), yLamp = y(150, s);
                int radio = Math.max(8, Math.round(e.radioLampara * s));
                float brillo = e.brilloLampara * (1 - h.niebla * 0.5f) * alfa;
                int lx0 = Math.floorMod(p.desfase + sep / 2 - desp, sep * 2) - sep * 2;
                for (int x = lx0; x < ANCHO + sep * 2; x += sep * 2) {
                    g.setComposite(base);
                    g.setColor(niebla(VIGA, h.niebla));
                    g.fillRect(x, dy + yTecho, 1, yLamp - yTecho);
                    g.setColor(new Color(LAMPARA));
                    int lw = Math.max(3, Math.round(7 * s)), lh = Math.max(2, Math.round(5 * s));
                    g.fillRect(x - lw / 2, dy + yLamp, lw, lh);
                    g.setColor(new Color(0xFFF2C0));
                    g.fillRect(x - lw / 2 + 1, dy + yLamp + 1, Math.max(1, lw - 2), 1);
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, brillo)));
                    g.drawImage(SpritesLuz.halo(radio, e.colorLampara), x - radio, dy + yLamp + lh / 2 - radio, null);
                }
                g.setComposite(base);
            }
        }
        g.setComposite(antes);
    }

    /** Altura en pantalla de un punto a altura "yMundo" de un plano con esa escala (perspectiva). */
    private static int y(int yMundo, float escala) {
        return Math.round(HORIZONTE + (yMundo - HORIZONTE) * escala);
    }

    // =====================================================================
    //  Preparación (una sola vez)
    // =====================================================================

    private static synchronized void preparar() {
        if (hechos != null) return;
        EstiloVisual e = EstiloVisual.de(cordoba.niveles.Level.Tema.MEZQUITA);
        telon = crearTelon(e);
        Hecho[] r = new Hecho[PLANOS.length];
        for (int i = 0; i < PLANOS.length; i++) r[i] = crearPlano(PLANOS[i], e.nieblaSala[i]);
        hechos = r;
    }

    private static Hecho crearPlano(Plano p, float niebla) {
        float s = p.escala();
        int sep = p.separacion;
        Hecho h = new Hecho();
        h.niebla = niebla;
        h.yFuste = y(110, s);
        h.ySuelo = Math.min(ALTO, y(400, s));
        h.anchoFuste = Math.max(2, Math.round(8 * s));
        h.fuste = niebla(PIEDRA, niebla);
        h.fusteLuz = niebla(PIEDRA_LUZ, niebla);
        h.fusteSombra = niebla(PIEDRA_SOMBRA, niebla);
        h.capitel = niebla(CAPITEL, niebla);
        h.suelo = niebla(SUELO, niebla);
        h.sueloLinea = niebla(SUELO_LINEA, niebla);

        int alto = h.yFuste + 1;
        BufferedImage img = new BufferedImage(ANCHO, alto, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = img.createGraphics();
        int ySup = y(46, s), yInf = y(104, s);
        int grosor = Math.max(2, Math.round(8 * s));
        int pilastra = Math.max(2, Math.round(10 * s));

        // techo de madera con vigas
        int t0 = Math.max(0, y(-30, s)), t1 = y(6, s);
        g.setColor(new Color(MADERA));
        g.fillRect(0, t0, ANCHO, t1 - t0);
        g.setColor(new Color(VIGA));
        for (int x = 0; x < ANCHO; x += Math.max(4, sep / 4)) g.fillRect(x, t0, Math.max(1, sep / 16), t1 - t0);
        g.setColor(new Color(ORO));
        g.fillRect(0, t1 - 1, ANCHO, 1);

        for (int k = -1; k <= ANCHO / sep + 1; k++) {
            int cx = p.desfase + k * sep;            // columna
            int ax = cx + sep / 2;                   // centro del arco
            // pilastra entre el arco de abajo y el de arriba
            g.setColor(new Color(PIEDRA));
            g.fillRect(cx - pilastra / 2, t1, pilastra, h.yFuste - t1);
            g.setColor(new Color(PIEDRA_LUZ));
            g.fillRect(cx - pilastra / 2, t1, Math.max(1, pilastra / 4), h.yFuste - t1);
            // arco de arriba (medio punto) y de abajo (herradura), con dovelas
            dovelas(g, ax, ySup, sep / 2 - 1, grosor, 10, -180, 0);
            dovelas(g, ax, yInf, sep / 2 - pilastra / 2 - 1, grosor, 12, -200, 20);
            // capitel
            g.setColor(new Color(CAPITEL));
            g.fillRect(cx - pilastra / 2 - 1, h.yFuste - Math.max(2, Math.round(5 * s)), pilastra + 2, Math.max(2, Math.round(5 * s)));
        }
        // la niebla oscurece el plano entero (solo donde hay dibujo)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, niebla));
        g.setColor(new Color(colorNiebla()));
        g.fillRect(0, 0, ANCHO, alto);
        g.dispose();
        h.banda = img;
        return h;
    }

    private static void dovelas(Graphics2D g, int cx, int cy, int radio, int grosor, int segmentos, int ini, int fin) {
        double a0 = Math.toRadians(ini), a1 = Math.toRadians(fin);
        for (int i = 0; i < segmentos; i++) {
            double b1 = a0 + (a1 - a0) * i / segmentos, b2 = a0 + (a1 - a0) * (i + 1) / segmentos;
            int r2 = radio - grosor;
            int[] xs = { cx + (int) Math.round(Math.cos(b1) * radio), cx + (int) Math.round(Math.cos(b2) * radio),
                         cx + (int) Math.round(Math.cos(b2) * r2), cx + (int) Math.round(Math.cos(b1) * r2) };
            int[] ys = { cy + (int) Math.round(Math.sin(b1) * radio), cy + (int) Math.round(Math.sin(b2) * radio),
                         cy + (int) Math.round(Math.sin(b2) * r2), cy + (int) Math.round(Math.sin(b1) * r2) };
            g.setColor(new Color(i % 2 == 0 ? ROJO : CREMA));
            g.fillPolygon(xs, ys, 4);
        }
    }

    /** Telón del fondo: oscuridad arriba, luz cálida en el horizonte y arquería lejanísima. */
    private static BufferedImage crearTelon(EstiloVisual e) {
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
        int arriba = 0x0C0605, luz = 0x5A3420, abajo = 0x140A07;
        for (int y = 0; y < ALTO; y++) {
            for (int x = 0; x < ANCHO; x++) {
                double t;
                int c;
                if (y < HORIZONTE) { t = Math.pow(y / (double) HORIZONTE, 2.2); c = mezclar(arriba, luz, t, x, y); }
                else { t = Math.min(1, (y - HORIZONTE) / 70.0); c = mezclar(luz, abajo, Math.sqrt(t), x, y); }
                img.setRGB(x, y, c);
            }
        }
        // arquería lejanísima en silueta (sin parallax: está "infinitamente" lejos)
        Graphics2D g = img.createGraphics();
        float s = 0.22f;
        int sep = 16;
        Color silueta = niebla(PIEDRA_SOMBRA, e.nieblaSala[0] + (1 - e.nieblaSala[0]) * 0.6f);
        g.setColor(silueta);
        int yTop = y(0, s), yFuste = y(110, s), ySuelo = y(400, s);
        g.fillRect(0, yTop, ANCHO, 2);
        for (int x = 0; x < ANCHO; x += sep) {
            g.fillRect(x, yTop, 2, ySuelo - yTop);
            g.drawArc(x + 1, y(46, s) - sep / 2 + 1, sep - 2, sep - 2, 0, 180);
            g.drawArc(x + 1, y(104, s) - sep / 2 + 2, sep - 2, sep - 2, -20, 220);
        }
        g.dispose();
        return img;
    }

    // ---------------------------------------------------------------------

    private static int colorNiebla() {
        return EstiloVisual.de(cordoba.niveles.Level.Tema.MEZQUITA).colorNieblaSala;
    }

    private static Color niebla(int rgb, float t) {
        return new Color(mezclar(rgb, colorNiebla(), t, 0, 0));
    }

    /** Mezcla dos colores; con x,y hace un tramado 2x2 para que el degradado sea "pixel art". */
    private static int mezclar(int a, int b, double t, int x, int y) {
        double umbral = (((x & 1) * 2 + (y & 1) * 3) % 4) / 4.0 - 0.375;
        t = Math.max(0, Math.min(1, Math.round((t + umbral * 0.08) * 12) / 12.0));
        int r = (int) Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int gg = (int) Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bb = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (gg << 8) | bb;
    }
}
