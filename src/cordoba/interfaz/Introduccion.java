package cordoba.interfaz;

import cordoba.audio.Sonido;
import cordoba.graficos.GameRenderer;
import cordoba.graficos.Sprites;
import cordoba.niveles.Level;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

// Cinemática de entrada: Coco sale de la alcantarilla a buscar comida. Se salta con ENTER, ESPACIO o ESC.
public class Introduccion {

    private static final int ANCHO = 640, ALTO = 360;
    private static final int ESCALA = 3, TAM = 20 * ESCALA;   // Coco dibujado x3

    // fotograma en el que acaba la alcantarilla y empieza la calle
    private static final int CALLE = 540;
    // duración total
    private static final int FIN = CALLE + 250;

    // Alcantarilla
    private static final int SUELO = 282;          // borde del andén
    private static final int ESCALERA_X = 478;     // centro de la escalera
    private static final int ESPINA_X = 352;       // espina de pescado en el suelo

    // Calle
    private static final int SUELO_CALLE = 312;
    private static final int TAPA_X = 320;
    private static final int SALTO = 64;           // (fotograma de la calle) Coco sale de un brinco

    private final GameRenderer renderer;
    private int f;
    // frase del narrador de este fotograma (se pinta en la banda de abajo)
    private String narracion = "";

    public Introduccion(GameRenderer renderer) {
        this.renderer = renderer;
    }

    public void reiniciar() {
        f = 0;
    }

    // avanza un fotograma. Devuelve true cuando la cinemática ha terminado
    public boolean actualizar(Input in) {
        f++;
        if (in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_SPACE) || in.pulsada(KeyEvent.VK_ESCAPE)) return true;
        // efectos de sonido en momentos concretos
        if (f == 120 || f == 150) Sonido.crujido();                    // tripa que ruge
        int c = f - CALLE;
        if (c == 22 || c == 42) Sonido.aterrizar();                     // la tapa traquetea
        if (c == SALTO) Sonido.salto();
        if (c == SALTO + 34) Sonido.aterrizar();                       // cae la tapa
        if (c == SALTO + 28) Sonido.checkpoint();                      // Coco aterriza
        return f >= FIN;
    }

    public void dibujar(Graphics2D g) {
        narracion = "";
        if (f < CALLE) dibujarAlcantarilla(g);
        else dibujarCalle(g, f - CALLE);
        dibujarBandas(g);
    }

    // ---- escena 1: la alcantarilla ----

    private void dibujarAlcantarilla(Graphics2D g) {
        dibujarLadrillos(g);
        dibujarTunel(g, 150, 0.0f);
        dibujarRejillaYLuz(g);
        dibujarEscalera(g);
        dibujarAnden(g);
        dibujarAguaSucia(g);
        dibujarGotas(g);
        dibujarEspina(g);
        if (f >= 320) dibujarOlor(g);

        // ---- Coco ----
        double x;
        int y = SUELO - TAM;
        BufferedImage img;
        boolean derecha = true;
        if (f < 110) {                                    // entra caminando
            x = -60 + (f / 110.0) * 260;
            img = andar(f);
        } else if (f < 190) {                             // le ruge la tripa
            x = 200 + ((f / 3) % 2 == 0 && f < 170 ? 1 : 0);
            img = (f / 20) % 2 == 0 ? Sprites.COCO_HERIDO : Sprites.COCO_QUIETO;
        } else if (f < 240) {                             // va hacia la espina
            x = 200 + (f - 190) / 50.0 * 90;
            img = andar(f);
        } else if (f < 390) {                             // no hay nada... pero huele a comida
            x = 290;
            boolean triste = f < 320;
            img = triste ? ((f / 30) % 2 == 0 ? Sprites.COCO_PARPADEO : Sprites.COCO_HERIDO)
                         : ((f / 12) % 2 == 0 ? Sprites.COCO_SALTO : Sprites.COCO_QUIETO);
            if (!triste) y -= (f / 12) % 2 == 0 ? 4 : 0;   // da saltitos de emoción
        } else if (f < 450) {                             // camina hasta la escalera
            x = 290 + (f - 390) / 60.0 * (ESCALERA_X - TAM / 2.0 - 290);
            img = andar(f);
        } else {                                          // sube la escalera
            x = ESCALERA_X - TAM / 2.0;
            y = SUELO - TAM - (int) ((f - 450) * 3.6);
            img = (f / 8) % 2 == 0 ? Sprites.COCO_SALTO : Sprites.COCO_CAIDA;
            derecha = (f / 8) % 2 == 0;
        }
        dibujarCoco(g, (int) x, y, img, derecha);

        // ---- Bocadillos ----
        int cx = (int) x + TAM / 2;
        if (f >= 115 && f < 190) {
            if (f < 165) {
                // "GRRR" tembloroso junto a la tripa
                g.setFont(Ui.FUENTE_GRANDE);
                int temblor = (f / 2) % 2 == 0 ? -1 : 1;
                Ui.textoContorno(g, "GRRR...", cx + 30 + temblor, y + 44, Ui.AMARILLO, new Color(0x2A140A));
            }
            if (f >= 135 && f < 168) bocadillo(g, cx, y - 6, "¡Qué hambre tengo!");
        }
        if (f >= 168 && f < 250) pensarEnSalmorejo(g, cx, y, f - 168);
        if (f >= 250 && f < 320) bocadillo(g, cx, y - 6, f < 285 ? "¿Nada? ¿Ni una raspa?" : "Aquí abajo no hay nada...");
        if (f >= 335 && f < 395) bocadillo(g, cx, y - 6, "¡Mmm! ¿Eso es... salmorejo?");

        // ---- Texto de la narración ----
        if (f < 168) narrar(g, "Bajo las calles de Córdoba, en las alcantarillas, vive Coco...", f, 0);
        else if (f < 250) narrar(g, "Solo puede pensar en una cosa: un buen plato de salmorejo.", f, 168);
        else if (f < 320) narrar(g, "Pero hace días que no come nada.", f, 250);
        else narrar(g, "Entonces, por la rejilla, se cuela un olor delicioso...", f, 320);

        // ---- Fundidos: negro al empezar, blanco al salir a la luz ----
        fundido(g, Color.BLACK, 1f - f / 40f);
        fundido(g, new Color(0xFFF6DC), (f - (CALLE - 40)) / 40f);
    }

    private BufferedImage andar(int fr) {
        return Sprites.COCO_ANDAR[(fr / 6) % Sprites.COCO_ANDAR.length];
    }

    private void dibujarLadrillos(Graphics2D g) {
        g.setColor(new Color(0x231C18));
        g.fillRect(0, 0, ANCHO, ALTO);
        for (int fila = 0; fila * 14 < SUELO + 20; fila++) {
            int desfase = fila % 2 == 0 ? 0 : 14;
            for (int x = -desfase; x < ANCHO; x += 28) {
                int tono = (x * 7 + fila * 13) % 3;
                g.setColor(tono == 0 ? new Color(0x3A2E26) : tono == 1 ? new Color(0x34291F) : new Color(0x2F2620));
                g.fillRect(x + 1, fila * 14 + 1, 26, 12);
            }
        }
        // humedad: manchas verdosas que bajan del techo
        g.setPaint(new GradientPaint(0, 0, new Color(40, 70, 40, 90), 0, 90, new Color(40, 70, 40, 0)));
        g.fillRect(0, 0, ANCHO, 90);
    }

    // túnel oscuro al fondo que se pierde en la distancia
    private void dibujarTunel(Graphics2D g, int cx, float abierto) {
        int[] radios = { 120, 92, 66, 42 };
        for (int i = 0; i < radios.length; i++) {
            int r = radios[i];
            int base = SUELO - 8;
            g.setColor(new Color(0x1A1411));
            g.fillRect(cx - r - 6, base - r, r * 2 + 12, r);
            g.fillOval(cx - r - 6, base - r * 2 + 10, r * 2 + 12, r * 2);
            int oscuro = 18 - i * 4;
            g.setColor(new Color(Math.max(4, oscuro), Math.max(3, oscuro - 3), Math.max(3, oscuro - 4)));
            g.fillRect(cx - r, base - r + 6, r * 2, r);
            g.fillOval(cx - r, base - r * 2 + 16, r * 2, r * 2);
        }
    }

    private void dibujarRejillaYLuz(Graphics2D g) {
        int x0 = ESCALERA_X - 34, w = 68;
        // haz de luz que baja de la rejilla
        int[] xs = { x0 + 4, x0 + w - 4, x0 + w + 40, x0 - 40 };
        int[] ys = { 14, 14, SUELO, SUELO };
        float pulso = (float) (0.85 + 0.15 * Math.sin(f / 25.0));
        g.setPaint(new GradientPaint(0, 14, new Color(255, 236, 170, (int) (110 * pulso)),
                                     0, SUELO, new Color(255, 236, 170, 10)));
        g.fillPolygon(xs, ys, 4);
        // motas de polvo dentro del haz
        g.setColor(new Color(255, 240, 200, 150));
        for (int i = 0; i < 14; i++) {
            int px = x0 - 20 + (i * 37 + (int) (Math.sin(f / 40.0 + i) * 10)) % (w + 40);
            int py = 20 + Math.floorMod(i * 53 + f / 3, SUELO - 30);
            g.fillRect(px, py, 1, 1);
        }
        // la rejilla en el techo
        g.setColor(new Color(0xFFF2C8));
        g.fillRect(x0, 0, w, 14);
        g.setColor(new Color(0x2A2A2E));
        g.fillRect(x0 - 4, 12, w + 8, 4);
        for (int x = x0; x <= x0 + w; x += 8) g.fillRect(x, 0, 3, 14);
    }

    private void dibujarEscalera(Graphics2D g) {
        int x0 = ESCALERA_X - 18;
        g.setColor(new Color(0x4A4A52));
        g.fillRect(x0, 16, 4, SUELO - 16);
        g.fillRect(x0 + 32, 16, 4, SUELO - 16);
        for (int y = 26; y < SUELO; y += 18) {
            g.setColor(new Color(0x5E5E68));
            g.fillRect(x0, y, 36, 3);
            g.setColor(new Color(0x7A4A2A));   // óxido
            g.fillRect(x0 + 6 + (y % 3) * 7, y + 3, 3, 2);
        }
    }

    private void dibujarAnden(Graphics2D g) {
        g.setColor(new Color(0x5A5048));
        g.fillRect(0, SUELO, ANCHO, 18);
        g.setColor(new Color(0x7A6E62));
        g.fillRect(0, SUELO, ANCHO, 3);
        g.setColor(new Color(0x40382F));
        for (int x = 0; x < ANCHO; x += 32) g.fillRect(x, SUELO + 3, 1, 15);
    }

    private void dibujarAguaSucia(Graphics2D g) {
        int y0 = SUELO + 18;
        g.setPaint(new GradientPaint(0, y0, new Color(0x3E5A2E), 0, ALTO, new Color(0x1A2A14)));
        g.fillRect(0, y0, ANCHO, ALTO - y0);
        g.setColor(new Color(120, 160, 90, 120));
        for (int i = 0; i < 18; i++) {
            int x = Math.floorMod(i * 71 - f, ANCHO + 40) - 20;
            int y = y0 + 6 + (i * 13) % 40;
            g.fillRect(x, y, 10 + i % 4 * 3, 1);
        }
        // reflejo de la luz de la rejilla
        g.setColor(new Color(255, 236, 170, 50));
        g.fillRect(ESCALERA_X - 40 + (int) (Math.sin(f / 15.0) * 3), y0 + 4, 80, 2);
    }

    private void dibujarGotas(Graphics2D g) {
        int[] xs = { 60, 250, 410, 590 };
        for (int i = 0; i < xs.length; i++) {
            int ciclo = Math.floorMod(f + i * 37, 90);
            if (ciclo < 70) {
                int y = 4 + ciclo * ciclo / 20;
                if (y > SUELO) continue;
                g.setColor(new Color(150, 200, 170, 200));
                g.fillRect(xs[i], y, 1, 3);
            } else {
                g.setColor(new Color(150, 200, 170, 120));   // salpicadura
                int r = (ciclo - 70) / 3;
                g.drawOval(xs[i] - r, SUELO - 1, r * 2, 2);
            }
        }
    }

    // raspa de pescado sin nada de carne
    private void dibujarEspina(Graphics2D g) {
        int x = ESPINA_X, y = SUELO - 6;
        g.setColor(new Color(0xD8D2C0));
        g.fillRect(x, y + 2, 22, 2);
        for (int i = 0; i < 5; i++) {
            g.fillRect(x + 4 + i * 3, y - 1, 1, 3);
            g.fillRect(x + 4 + i * 3, y + 4, 1, 3);
        }
        g.fillRect(x + 22, y, 5, 6);                 // cabeza
        g.fillRect(x - 4, y, 2, 6);                  // cola
        g.fillRect(x - 2, y + 2, 2, 2);
        g.setColor(new Color(0x231C18));
        g.fillRect(x + 24, y + 2, 1, 1);             // ojo
    }

    // líneas onduladas de olor que bajan por la rejilla
    private void dibujarOlor(Graphics2D g) {
        float alfa = Math.min(1f, (f - 320) / 30f);
        for (int k = 0; k < 3; k++) {
            int base = ESCALERA_X - 20 + k * 20;
            for (int y = 16; y < 200; y += 2) {
                int dy = Math.floorMod(y - (f - 320) * 2, 200);
                if (dy > 150) continue;
                int x = base + (int) (Math.sin((y + f * 2) / 12.0 + k) * 6);
                g.setColor(new Color(255, 180, 90, (int) (160 * alfa * (1 - dy / 150f))));
                g.fillRect(x, y, 2, 2);
            }
        }
    }

    // ---- escena 2: la calle ----

    private void dibujarCalle(Graphics2D g, int c) {
        renderer.dibujarFondo(g, Level.Tema.RIO, 200 + c / 4, 0, 368, true);
        for (int x = 0; x < ANCHO; x += 16) {
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16) % 2, true, false), x, SUELO_CALLE, null);
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16 + 1) % 2, false, false), x, SUELO_CALLE + 16, null);
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16) % 2, false, false), x, SUELO_CALLE + 32, null);
        }
        // el agujero de la alcantarilla
        g.setColor(new Color(0x1A120C));
        g.fillOval(TAPA_X - 26, SUELO_CALLE - 4, 52, 10);

        // ---- La tapa: traquetea, salta por los aires y cae a un lado ----
        int tx = TAPA_X, ty = SUELO_CALLE - 3;
        boolean deLado = false;
        if (c >= 20 && c < SALTO) {
            boolean golpe = (c >= 20 && c < 28) || (c >= 40 && c < 48) || c >= 56;
            if (golpe) ty -= (c / 2) % 2 == 0 ? 3 : 0;
        } else if (c >= SALTO) {
            double t = Math.min(c - SALTO, 34);
            tx = (int) (TAPA_X - t * 2.6);
            ty = (int) (SUELO_CALLE - 3 - 13 * t + 0.4 * t * t);
            deLado = c - SALTO < 34;
            if (c - SALTO >= 34) ty = SUELO_CALLE - 3;
        }
        dibujarTapa(g, tx, ty, deLado, c);

        // ---- Coco sale de un brinco ----
        if (c >= SALTO) {
            double t = c - SALTO;
            double vuelo = 27.7;   // fotogramas hasta volver al suelo
            double pie;
            double x;
            BufferedImage img;
            if (t < vuelo) {
                pie = SUELO_CALLE + 60 - 16 * t + 0.5 * t * t;
                x = TAPA_X + t * 2.0;
                img = t < 16 ? Sprites.COCO_SALTO : Sprites.COCO_CAIDA;
            } else {
                pie = SUELO_CALLE;
                x = TAPA_X + vuelo * 2.0;
                img = (c / 15) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
                if (img == Sprites.COCO_VICTORIA2) pie -= 5;
            }
            int px = (int) x - TAM / 2, py = (int) pie - TAM;
            Shape antes = g.getClip();
            if (t < 10) g.clipRect(0, 0, ANCHO, SUELO_CALLE + 2);   // aún medio dentro del agujero
            if (t >= vuelo) {
                g.setColor(new Color(0, 0, 0, 70));
                g.fillOval((int) x - 24, SUELO_CALLE - 4, 48, 6);
            }
            dibujarCoco(g, px, py, img, true);
            g.setClip(antes);
            if (t >= vuelo + 6 && c < FIN - CALLE - 40) bocadillo(g, (int) x, py - 6, "¡A buscar comida por Córdoba!");
        } else if (c >= 20) {
            // ojos que asoman bajo la tapa
            if ((c / 10) % 3 != 0) {
                g.setColor(new Color(0xFFF59D));
                g.fillRect(TAPA_X - 8, SUELO_CALLE - 1, 3, 2);
                g.fillRect(TAPA_X + 5, SUELO_CALLE - 1, 3, 2);
            }
        }

        // ---- Cartel del nivel ----
        if (c >= 130) cartelNivel(g, c - 130);

        if (c < SALTO) narrar(g, "Coco empuja la tapa de la alcantarilla...", c, 0);

        // Fundidos: de blanco al entrar y a negro al terminar
        fundido(g, new Color(0xFFF6DC), 1f - c / 30f);
        fundido(g, Color.BLACK, (c - (FIN - CALLE - 40)) / 40f);
    }

    private void dibujarTapa(Graphics2D g, int cx, int y, boolean volando, int c) {
        int w = 50, h = 8;
        if (volando) {
            // gira en el aire: se ve más estrecha a ratos
            double giro = Math.abs(Math.cos(c * 0.35));
            w = Math.max(8, (int) (50 * giro));
            h = 8 + (int) ((1 - giro) * 6);
        }
        g.setColor(new Color(0x3C3C44));
        g.fillOval(cx - w / 2, y - h / 2, w, h);
        g.setColor(new Color(0x5A5A66));
        g.fillOval(cx - w / 2 + 2, y - h / 2, w - 4, h - 3);
        if (!volando) {
            g.setColor(new Color(0x3C3C44));
            for (int i = -16; i <= 16; i += 8) g.fillRect(cx + i, y - 2, 4, 1);   // relieve
        }
    }

    // placa con el nombre del nivel que aparece al final
    private void cartelNivel(Graphics2D g, int t) {
        float a = Math.min(1f, t / 20f);
        Composite antes = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        int w = 330, h = 62, x = ANCHO / 2 - w / 2, y = 60 - (int) ((1 - a) * 12);
        Andalusi.marco(g, x, y, w, h, Ui.PANEL, true);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "NIVEL 1", ANCHO / 2, y + 28, Andalusi.ORO_CLARO);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Los primeros pasos por Córdoba", ANCHO / 2, y + 47, Ui.TEXTO);
        g.setComposite(antes);
    }

    // ---- ayudas de dibujo ----

    private void dibujarCoco(Graphics2D g, int x, int y, BufferedImage img, boolean derecha) {
        BufferedImage i = derecha ? img : Sprites.volteada(img);
        g.drawImage(i, x, y, TAM, TAM, null);
    }

    // plato de salmorejo (assets/interface/salmorejo.png; si falta, se dibuja uno sencillo)
    private static BufferedImage salmorejo;

    private static BufferedImage salmorejo() {
        if (salmorejo == null) {
            salmorejo = Sprites.leerPng("interface/salmorejo");
            if (salmorejo == null) {
                salmorejo = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = salmorejo.createGraphics();
                g.setColor(new Color(0xEEEEEE));
                g.fillArc(6, 8, 36, 30, 180, 180);
                g.setColor(new Color(0xE8762C));
                g.fillOval(8, 18, 32, 10);
                g.setColor(new Color(0xFFF59D));
                g.fillRect(20, 20, 4, 3);
                g.dispose();
            }
        }
        return salmorejo;
    }

    // bocadillo con el salmorejo (t = fotogramas desde que aparece)
    private void pensarEnSalmorejo(Graphics2D g, int cx, int yCabeza, int t) {
        Color borde = new Color(0x2A140A), relleno = new Color(0xFFF8EC);
        float aparecer = Math.min(1f, t / 12f);
        Composite antes = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, aparecer));

        // burbujitas que suben desde la cabeza de Coco
        int[][] burbujas = { { 6, -4, 5 }, { 14, -16, 8 }, { 24, -32, 11 } };
        for (int i = 0; i < burbujas.length; i++) {
            if (t < i * 4) continue;
            int[] b = burbujas[i];
            int bx = cx + b[0], by = yCabeza + b[1], r = b[2];
            g.setColor(borde);
            g.fillOval(bx - r - 1, by - r - 1, r * 2 + 2, r * 2 + 2);
            g.setColor(relleno);
            g.fillOval(bx - r, by - r, r * 2, r * 2);
        }
        if (t >= 12) {
            // la nube: varios círculos que se solapan
            int nx = Math.max(70, Math.min(570, cx + 40)), ny = yCabeza - 92;
            int[][] nube = { { -46, 0, 30 }, { -20, -22, 34 }, { 16, -24, 34 }, { 46, -2, 30 },
                             { 22, 22, 32 }, { -22, 22, 32 }, { 0, 0, 44 } };
            g.setColor(borde);
            for (int[] c : nube) g.fillOval(nx + c[0] - c[2] - 2, ny + c[1] - c[2] - 2, c[2] * 2 + 4, c[2] * 2 + 4);
            g.setColor(relleno);
            for (int[] c : nube) g.fillOval(nx + c[0] - c[2], ny + c[1] - c[2], c[2] * 2, c[2] * 2);

            // el plato, a doble tamaño, flotando
            int flota = (int) Math.round(Math.sin(t / 10.0) * 2);
            int lado = 96;
            g.drawImage(salmorejo(), nx - lado / 2, ny - lado / 2 + 4 + flota, lado, lado, null);

            // destellos de frescor (el salmorejo se toma fresquito, sin vapor)
            int[][] destellos = { { -30, -16 }, { 28, -20 }, { 34, 14 } };
            for (int k = 0; k < destellos.length; k++) {
                if (((t / 8) + k) % 3 == 0) continue;
                int dx = nx + destellos[k][0], dy = ny + destellos[k][1] + flota;
                g.setColor(new Color(0x9AD4FF));
                g.fillRect(dx - 3, dy, 7, 1);
                g.fillRect(dx, dy - 3, 1, 7);
                g.setColor(Color.WHITE);
                g.fillRect(dx, dy, 1, 1);
            }
            // corazoncitos de ganas
            if ((t / 15) % 2 == 0) {
                g.setColor(new Color(0xE8344A));
                corazon(g, nx + 44, ny - 30);
                corazon(g, nx - 50, ny - 22);
            }
        }
        g.setComposite(antes);

        // babas de Coco
        if (t >= 20) {
            int largo = Math.min(10, (t - 20) / 4);
            g.setColor(new Color(170, 220, 255, 200));
            g.fillRect(cx + 22, yCabeza + 30, 2, 3 + largo);
            g.fillRect(cx + 21, yCabeza + 32 + largo, 4, 3);
        }
    }

    private void corazon(Graphics2D g, int x, int y) {
        g.fillRect(x - 3, y - 2, 3, 3);
        g.fillRect(x + 1, y - 2, 3, 3);
        g.fillRect(x - 3, y, 7, 2);
        g.fillRect(x - 2, y + 2, 5, 1);
        g.fillRect(x - 1, y + 3, 3, 1);
        g.fillRect(x, y + 4, 1, 1);
    }

    // bocadillo de cómic con su "colita" apuntando a quien habla (cx, yBase)
    private void bocadillo(Graphics2D g, int cx, int yBase, String texto) {
        g.setFont(Ui.FUENTE);
        int w = g.getFontMetrics().stringWidth(texto) + 16, h = 20;
        int x = Math.max(6, Math.min(ANCHO - w - 6, cx - w / 2)), y = Math.max(34, yBase - h - 8);
        g.setColor(new Color(0x2A140A));
        g.fillRoundRect(x - 1, y - 1, w + 2, h + 2, 10, 10);
        g.fillPolygon(new int[] { cx - 6, cx + 6, cx }, new int[] { y + h, y + h, y + h + 9 }, 3);
        g.setColor(new Color(0xFFF8EC));
        g.fillRoundRect(x, y, w, h, 9, 9);
        g.fillPolygon(new int[] { cx - 4, cx + 4, cx }, new int[] { y + h - 1, y + h - 1, y + h + 6 }, 3);
        Ui.texto(g, texto, x + 8, y + 14, new Color(0x2A140A));
    }

    // texto del narrador (en la banda negra de abajo), que se escribe letra a letra
    private void narrar(Graphics2D g, String texto, int ahora, int inicio) {
        int letras = Math.min(texto.length(), Math.max(0, ahora - inicio));
        narracion = texto.substring(0, letras);
    }

    // bandas negras de cine arriba y abajo, con la ayuda para saltar
    private void dibujarBandas(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, ANCHO, 26);
        g.fillRect(0, ALTO - 30, ANCHO, 30);
        g.setFont(Ui.FUENTE_PEQ);
        String s = "ENTER / ESC: saltar";
        Ui.texto(g, s, ANCHO - g.getFontMetrics().stringWidth(s) - 8, 17, Ui.GRIS);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, narracion, ANCHO / 2, ALTO - 12, Andalusi.CREMA);
    }

    private void fundido(Graphics2D g, Color c, float a) {
        if (a <= 0f) return;
        a = Math.min(1f, a);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (a * 255)));
        g.fillRect(0, 0, ANCHO, ALTO);
    }
}
