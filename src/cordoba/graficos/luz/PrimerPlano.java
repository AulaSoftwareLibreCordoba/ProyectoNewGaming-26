package cordoba.graficos.luz;

import cordoba.niveles.Level;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.HashMap;
import java.util.Map;

// Primer plano: cosas ENTRE Coco y la cámara (palmeras, matorrales, columnas, lámparas...).
// Se ven grandes, a contraluz y desenfocadas, y van más rápido que el escenario: dan profundidad.
// Solo es decoración (sin colisión). Si Coco pasa por detrás, se vuelven medio transparentes.
public final class PrimerPlano {

    private static final int ANCHO = 640, ALTO = 360;

    // cuánto más rápido que la cámara se mueve el primer plano (1 = igual que Coco)
    public static final float PARALLAX = 1.6f;
    // cada cuántos píxeles se repite la secuencia de objetos
    private static final int PERIODO = 1500;
    // radio del desenfoque (en píxeles de la resolución interna)
    private static final int DESENFOQUE = 3;
    // opacidad normal y opacidad cuando Coco está detrás
    private static final float ALFA = 0.93f, ALFA_TAPANDO = 0.38f;

    private enum Forma { PALMERA, MATORRAL, HOJAS_ARRIBA, COLUMNA, NARANJO, LAMPARA }

    // una pieza: forma, posición en la secuencia y si va en espejo. "dentro" = forma que la sustituye
    // dentro de la Mezquita (null = la misma)
    private static final class Pieza {
        final Forma forma, dentro; final int x; final boolean espejo;
        Pieza(Forma forma, int x, boolean espejo) { this(forma, x, espejo, forma); }
        Pieza(Forma forma, int x, boolean espejo, Forma dentro) {
            this.forma = forma; this.x = x; this.espejo = espejo; this.dentro = dentro;
        }
    }

    private static final Pieza[] RIO = {
        new Pieza(Forma.PALMERA, 40, false),
        new Pieza(Forma.MATORRAL, 520, false),
        new Pieza(Forma.HOJAS_ARRIBA, 860, false),
        new Pieza(Forma.MATORRAL, 1180, true),
    };
    private static final Pieza[] MEZQUITA = {
        new Pieza(Forma.COLUMNA, 60, false),
        new Pieza(Forma.NARANJO, 560, false, Forma.COLUMNA),   // dentro no hay naranjos: otra columna
        new Pieza(Forma.LAMPARA, 900, false),
        new Pieza(Forma.COLUMNA, 1230, true),
    };

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final Map<Level.Tema, BufferedImage> BOKEH = new HashMap<>();

    private PrimerPlano() { }

    // ---- dibujo ----

    // se llama DESPUÉS de Coco y antes del HUD. jugador = rectángulo de Coco en pantalla (o null) para no taparlo
    public static void dibujar(Graphics2D g, Level.Tema tema, int camX, Rectangle jugador, int frame) {
        dibujar(g, tema, camX, 0f, jugador, frame);
    }

    // interior: 0 fuera, 1 dentro de la Mezquita (las piezas de fuera se funden con las de dentro)
    public static void dibujar(Graphics2D g, Level.Tema tema, int camX, float interior, Rectangle jugador, int frame) {
        dibujarBokeh(g, tema, camX, frame);
        Pieza[] piezas = tema == Level.Tema.RIO ? RIO : MEZQUITA;
        Composite antes = g.getComposite();
        for (Pieza p : piezas) {
            if (p.dentro == p.forma) {
                dibujarPieza(g, tema, p, p.forma, camX, jugador, 1f);
            } else {
                if (interior < 1f) dibujarPieza(g, tema, p, p.forma, camX, jugador, 1f - interior);
                if (interior > 0f) dibujarPieza(g, tema, p, p.dentro, camX, jugador, interior);
            }
        }
        g.setComposite(antes);
    }

    private static void dibujarPieza(Graphics2D g, Level.Tema tema, Pieza p, Forma forma, int camX,
                                     Rectangle jugador, float opacidad) {
        BufferedImage img = imagen(tema, forma, p.espejo);
        int margen = img.getWidth();
        int x = Math.floorMod(Math.round(p.x - camX * PARALLAX) + margen, PERIODO) - margen;
        if (x > ANCHO || x + img.getWidth() < 0) return;
        int y = arriba(forma) ? 0 : ALTO - img.getHeight() + hundido(forma);
        float alfa = ALFA;
        if (jugador != null) {
            // Cuanto más cerca está Coco del objeto, más transparente se vuelve
            Rectangle r = nucleo(forma, x, y, img);
            if (p.espejo) r.x = 2 * x + img.getWidth() - r.x - r.width;
            double dx = Math.max(0, Math.max(r.x - jugador.getMaxX(), jugador.x - r.getMaxX()));
            double dy = Math.max(0, Math.max(r.y - jugador.getMaxY(), jugador.y - r.getMaxY()));
            double d = Math.hypot(dx, dy);
            alfa = (float) (ALFA_TAPANDO + (ALFA - ALFA_TAPANDO) * Math.min(1, d / 40.0));
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alfa * opacidad * transparencia(forma)))));
        g.drawImage(img, x, y, null);
    }

    // los setos anchos (matorral y naranjo) distraen si se ven enteros: se hunden bajo la pantalla
    // para que solo asome la parte de arriba, y van más transparentes
    private static int hundido(Forma f) {
        switch (f) {
            case MATORRAL: return 55;
            case NARANJO:  return 60;
            default:       return 0;
        }
    }

    // opacidad propia de cada forma (1 = normal)
    private static float transparencia(Forma f) {
        return f == Forma.MATORRAL || f == Forma.NARANJO ? 0.55f : 1f;
    }

    private static boolean arriba(Forma f) {
        return f == Forma.HOJAS_ARRIBA || f == Forma.LAMPARA;
    }

    // parte "maciza" del objeto, la que de verdad tapa a Coco
    private static Rectangle nucleo(Forma f, int x, int y, BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        switch (f) {
            case PALMERA:  return new Rectangle(x + w / 6, y, w * 2 / 3, h);
            case COLUMNA:  return new Rectangle(x + 10, y + 60, 64, h - 60);   // solo el fuste
            case LAMPARA:  return new Rectangle(x, y + h / 2, w, h / 2);
            default:       return new Rectangle(x, y + h / 4, w, h * 3 / 4);
        }
    }

    // destellos redondos desenfocados (como los de una cámara de cine) que flotan despacio
    private static void dibujarBokeh(Graphics2D g, Level.Tema tema, int camX, int frame) {
        BufferedImage bokeh = BOKEH.computeIfAbsent(tema,
                t -> crearBokeh(t == Level.Tema.RIO ? 0xFFECB0 : 0xFFBE6E));
        Composite antes = g.getComposite();
        for (int i = 0; i < 7; i++) {
            float par = 1.3f + (i % 3) * 0.25f;
            int x = Math.floorMod((int) (i * 173 - camX * par + Math.sin(frame / 90.0 + i) * 12), ANCHO + 80) - 40;
            int y = Math.floorMod((int) (i * 97 + 40 - frame * (0.08 + (i % 4) * 0.04)), ALTO + 60) - 30;
            float a = (float) (0.10 + 0.07 * Math.sin(frame / 50.0 + i * 1.7));
            int r = 6 + (i * 5) % 9;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, a)));
            g.drawImage(bokeh, x - r, y - r, r * 2, r * 2, null);
        }
        g.setComposite(antes);
    }

    private static BufferedImage crearBokeh(int rgb) {
        int n = 32;
        BufferedImage img = new BufferedImage(n, n, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                double d = Math.hypot(x - n / 2.0 + 0.5, y - n / 2.0 + 0.5) / (n / 2.0);
                if (d > 1) continue;
                // disco casi plano con el borde un poco más brillante y suave
                double a = d > 0.85 ? (1 - d) / 0.15 : 0.7 + 0.3 * d;
                img.setRGB(x, y, ((int) (a * 255) << 24) | rgb);
            }
        }
        return img;
    }

    // ---- generación de las imágenes (una vez, luego se guardan en caché) ----

    private static synchronized BufferedImage imagen(Level.Tema tema, Forma f, boolean espejo) {
        return CACHE.computeIfAbsent(tema + "_" + f + "_" + espejo, k -> {
            BufferedImage nitida = generar(f);
            tenir(nitida, tema);
            if (f == Forma.LAMPARA) luzLampara(nitida);
            if (espejo) nitida = espejar(nitida);
            return desenfocar(nitida, DESENFOQUE);
        });
    }

    private static BufferedImage generar(Forma f) {
        switch (f) {
            case PALMERA:      return palmera();
            case MATORRAL:     return matorral();
            case HOJAS_ARRIBA: return hojasArriba();
            case COLUMNA:      return columna();
            case NARANJO:      return naranjo();
            default:           return lampara();
        }
    }

    private static Graphics2D lapiz(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g;
    }

    // palmera enorme: el tronco sale por abajo y la copa queda arriba
    private static BufferedImage palmera() {
        BufferedImage img = new BufferedImage(300, ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lapiz(img);
        // tronco curvado con anillos
        int copaX = 168, copaY = 52;
        for (int y = ALTO; y > copaY; y -= 2) {
            double t = (ALTO - y) / (double) (ALTO - copaY);
            double x = 120 + (copaX - 120) * Math.pow(t, 1.6);
            double ancho = 30 - 12 * t;
            g.setColor(((y / 8) % 2 == 0) ? new Color(0x6B4E2E) : new Color(0x573E24));
            g.fill(new Rectangle.Double(x - ancho / 2, y - 2, ancho, 3));
        }
        // frondas
        double[][] frondas = { { 200, 150 }, { 215, 120 }, { 240, 135 }, { 265, 120 }, { 290, 140 },
                               { 330, 130 }, { 355, 150 }, { 160, 120 }, { 20, 130 } };
        for (double[] fr : frondas)
            fronda(g, copaX, copaY, Math.toRadians(fr[0]), fr[1], fr[1] * 0.55, new Color(0x2F6B2C), new Color(0x3F8A36));
        // dátiles
        g.setColor(new Color(0xB5651D));
        for (int i = 0; i < 7; i++) g.fillOval(copaX - 14 + i * 4, copaY + 6 + (i % 2) * 4, 7, 7);
        g.dispose();
        return img;
    }

    // hojas de palmera que cuelgan desde arriba de la pantalla
    private static BufferedImage hojasArriba() {
        BufferedImage img = new BufferedImage(300, 170, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lapiz(img);
        double[][] frondas = { { 10, 220 }, { 30, 200 }, { 55, 170 }, { 80, 140 }, { 5, 250 } };
        for (double[] fr : frondas)
            fronda(g, 120, -30, Math.toRadians(fr[0]), fr[1], fr[1] * 0.45, new Color(0x2B6128), new Color(0x3C8234));
        for (double[] fr : frondas)
            fronda(g, 120, -30, Math.toRadians(180 - fr[0] * 0.6), fr[1] * 0.6, fr[1] * 0.4, new Color(0x2B6128), new Color(0x3C8234));
        g.dispose();
        return img;
    }

    // matorral de la orilla con cañas que asoman
    private static BufferedImage matorral() {
        BufferedImage img = new BufferedImage(280, 130, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lapiz(img);
        // cañas
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 9; i++) {
            int x = 30 + i * 26;
            int alto = 70 + (i * 37) % 50;
            g.setColor(new Color(0x5E7A26));
            g.drawLine(x, 130, x + (i % 3 - 1) * 10, 130 - alto);
            g.setColor(new Color(0x6B4428));
            g.fillRoundRect(x + (i % 3 - 1) * 10 - 3, 130 - alto - 4, 7, 16, 6, 6);
        }
        // arbusto
        int[][] bolas = { { 20, 70, 90 }, { 80, 50, 100 }, { 150, 62, 90 }, { 200, 78, 80 }, { 110, 85, 120 } };
        for (int[] b : bolas) {
            g.setColor(new Color(0x2A5A26));
            g.fillOval(b[0], b[1], b[2], b[2]);
            g.setColor(new Color(0x386F2E));
            g.fillOval(b[0] + b[2] / 5, b[1] + 4, b[2] / 2, b[2] / 3);
        }
        g.dispose();
        return img;
    }

    // columna de mármol con el arranque de un arco de dovelas rojas y blancas
    private static BufferedImage columna() {
        BufferedImage img = new BufferedImage(250, ALTO, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lapiz(img);
        int cx = 42, fuste = 34, capitel = 92;
        // arco (dovelas alternas) que sale de la columna hacia la derecha
        double ra = 150, re = 110, ccx = cx + ra, ccy = capitel + 20;
        for (int i = 0; i < 11; i++) {
            double a1 = Math.toRadians(180 + i * 9), a2 = Math.toRadians(180 + (i + 1) * 9);
            Path2D p = new Path2D.Double();
            p.moveTo(ccx + Math.cos(a1) * ra, ccy + Math.sin(a1) * ra);
            p.lineTo(ccx + Math.cos(a2) * ra, ccy + Math.sin(a2) * ra);
            p.lineTo(ccx + Math.cos(a2) * re, ccy + Math.sin(a2) * re);
            p.lineTo(ccx + Math.cos(a1) * re, ccy + Math.sin(a1) * re);
            p.closePath();
            g.setColor(i % 2 == 0 ? new Color(0x9E3326) : new Color(0xD8C49A));
            g.fill(p);
        }
        // el muro encima del arco
        g.setColor(new Color(0x8E6E4C));
        g.fillRect(0, 0, 250, (int) (ccy - ra) + 2);
        // fuste con brillo lateral
        g.setColor(new Color(0x7E6650));
        g.fillRect(cx - fuste / 2, capitel, fuste, ALTO - capitel);
        g.setColor(new Color(0xA48A6E));
        g.fillRect(cx - fuste / 2 + 6, capitel, 6, ALTO - capitel);
        g.setColor(new Color(0x5E4A38));
        g.fillRect(cx + fuste / 2 - 7, capitel, 7, ALTO - capitel);
        // capitel y cimacio
        g.setColor(new Color(0xB89660));
        g.fillRect(cx - fuste / 2 - 6, capitel - 16, fuste + 12, 16);
        g.setColor(new Color(0x8C6B3B));
        g.fillRect(cx - fuste / 2 - 12, capitel - 26, fuste + 24, 10);
        g.setColor(new Color(0x6E5232));
        for (int i = 0; i < 4; i++) g.fillRect(cx - fuste / 2 - 2 + i * 10, capitel - 13, 4, 10);
        g.dispose();
        return img;
    }

    // copa de un naranjo muy cerca de la cámara
    private static BufferedImage naranjo() {
        BufferedImage img = new BufferedImage(270, 140, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lapiz(img);
        int[][] bolas = { { 10, 50, 110 }, { 70, 20, 130 }, { 150, 40, 115 }, { 60, 70, 120 } };
        for (int[] b : bolas) {
            g.setColor(new Color(0x24502A));
            g.fillOval(b[0], b[1], b[2], b[2]);
            g.setColor(new Color(0x2F6634));
            g.fillOval(b[0] + b[2] / 6, b[1] + 6, b[2] / 2, b[2] / 3);
        }
        g.setColor(new Color(0xE07A10));
        int[][] naranjas = { { 50, 70 }, { 110, 45 }, { 170, 75 }, { 205, 62 }, { 90, 95 }, { 140, 100 }, { 30, 98 } };
        for (int[] n : naranjas) g.fillOval(n[0], n[1], 13, 13);
        g.dispose();
        return img;
    }

    // lámpara de bronce colgada de una cadena (como las de la Mezquita)
    private static BufferedImage lampara() {
        BufferedImage img = new BufferedImage(90, 190, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lapiz(img);
        int cx = 45;
        g.setColor(new Color(0x4A3420));
        for (int y = 0; y < 120; y += 8) g.drawOval(cx - 3, y, 6, 9);
        g.setColor(new Color(0x8C6420));
        g.fillOval(cx - 34, 118, 68, 24);
        g.setColor(new Color(0xB08030));
        g.fillRect(cx - 30, 128, 60, 26);
        g.fillOval(cx - 30, 144, 60, 22);
        g.setColor(new Color(0x5E4214));
        for (int i = 0; i < 5; i++) g.fillOval(cx - 24 + i * 11, 134, 6, 12);   // celosía
        g.setColor(new Color(0x8C6420));
        g.fillOval(cx - 6, 162, 12, 14);
        g.dispose();
        return img;
    }

    // fronda de palmera: un nervio curvado con hojitas a los lados
    private static void fronda(Graphics2D g, double x0, double y0, double ang, double largo, double caida,
                               Color oscuro, Color claro) {
        int pasos = 22;
        double px = x0, py = y0;
        for (int i = 1; i <= pasos; i++) {
            double t = i / (double) pasos;
            double x = x0 + Math.cos(ang) * largo * t;
            double y = y0 + Math.sin(ang) * largo * t + caida * t * t;
            double dirx = x - px, diry = y - py, n = Math.hypot(dirx, diry);
            dirx /= n; diry /= n;
            // hojitas perpendiculares, caídas hacia abajo
            double hoja = 6 + 22 * Math.sin(Math.PI * Math.min(1, t * 1.15)) * (1 - t * 0.4);
            g.setStroke(new BasicStroke((float) (3.2 - 1.6 * t), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(i % 2 == 0 ? oscuro : claro);
            for (int lado = -1; lado <= 1; lado += 2) {
                double hx = -diry * lado, hy = dirx * lado;
                g.draw(new java.awt.geom.Line2D.Double(x, y, x + (hx + dirx * 0.4) * hoja,
                        y + (hy + dirx * 0.1 + 0.55) * hoja));
            }
            g.setColor(new Color(0x3A4A1E));
            g.setStroke(new BasicStroke((float) (4.5 - 3 * t), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new java.awt.geom.Line2D.Double(px, py, x, y));
            px = x; py = y;
        }
    }

    // ---- efectos: contraluz, espejo y desenfoque ----

    // oscurece la imagen hacia el color de sombra del tema (a contraluz)
    private static void tenir(BufferedImage img, Level.Tema tema) {
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.SrcAtop);
        Color sombra = tema == Level.Tema.RIO ? new Color(14, 24, 18, 185) : new Color(24, 12, 6, 175);
        g.setColor(sombra);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        // borde iluminado por arriba (luz que se cuela)
        Color luz = tema == Level.Tema.RIO ? new Color(255, 214, 140, 40) : new Color(255, 170, 90, 36);
        g.setPaint(new GradientPaint(0, 0, luz, 0, img.getHeight() * 0.6f, new Color(0, 0, 0, 0)));
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.dispose();
    }

    // resplandor cálido de la lámpara (después de oscurecerla, para que brille)
    private static void luzLampara(BufferedImage img) {
        Graphics2D g = lapiz(img);
        int cx = img.getWidth() / 2, cy = 146;
        g.setPaint(new RadialGradientPaint(cx, cy, 40, new float[] { 0f, 0.35f, 1f },
                new Color[] { new Color(255, 230, 150, 230), new Color(255, 170, 70, 110), new Color(255, 140, 40, 0) }));
        g.fillOval(cx - 40, cy - 40, 80, 80);
        g.dispose();
    }

    private static BufferedImage espejar(BufferedImage img) {
        BufferedImage r = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = r.createGraphics();
        g.drawImage(img, img.getWidth(), 0, -img.getWidth(), img.getHeight(), null);
        g.dispose();
        return r;
    }

    // desenfoque casi gaussiano: tres pasadas de media de vecinos en horizontal y vertical.
    // Con alfa premultiplicado para que los bordes no se pongan negros
    static BufferedImage desenfocar(BufferedImage origen, int radio) {
        int w = origen.getWidth(), h = origen.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = img.createGraphics();
        g.drawImage(origen, 0, 0, null);
        g.dispose();
        int[] px = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        int[] tmp = new int[px.length];
        for (int pasada = 0; pasada < 3; pasada++) {
            caja(px, tmp, w, h, radio, true);
            caja(tmp, px, w, h, radio, false);
        }
        return img;
    }

    private static void caja(int[] src, int[] dst, int w, int h, int r, boolean horizontal) {
        int lineas = horizontal ? h : w, largo = horizontal ? w : h;
        int div = r * 2 + 1;
        for (int l = 0; l < lineas; l++) {
            int a = 0, rr = 0, gg = 0, bb = 0;
            for (int k = -r; k <= r; k++) {
                int c = src[idx(l, Math.max(0, Math.min(largo - 1, k)), w, horizontal)];
                a += c >>> 24; rr += (c >> 16) & 255; gg += (c >> 8) & 255; bb += c & 255;
            }
            for (int i = 0; i < largo; i++) {
                dst[idx(l, i, w, horizontal)] = ((a / div) << 24) | ((rr / div) << 16) | ((gg / div) << 8) | (bb / div);
                int sale = src[idx(l, Math.max(0, i - r), w, horizontal)];
                int entra = src[idx(l, Math.min(largo - 1, i + r + 1), w, horizontal)];
                a += (entra >>> 24) - (sale >>> 24);
                rr += ((entra >> 16) & 255) - ((sale >> 16) & 255);
                gg += ((entra >> 8) & 255) - ((sale >> 8) & 255);
                bb += (entra & 255) - (sale & 255);
            }
        }
    }

    private static int idx(int linea, int i, int w, boolean horizontal) {
        return horizontal ? linea * w + i : i * w + linea;
    }
}
