package cordoba.interfaz;

import cordoba.graficos.GameRenderer;
import cordoba.graficos.Sprites;
import cordoba.niveles.Level;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Pantallas de menú: inicio, pausa y nivel completado.
 * (El final de la partida y el ranking están en PantallaRanking.)
 * Cada método actualizarX() devuelve la acción elegida por el jugador.
 */
public class GameMenu {

    public enum Accion { NINGUNA, JUGAR, SALIR, CONTINUAR, REINICIAR, MENU, NIVEL2, RANKING }

    private final GameRenderer renderer;

    // Pantalla de inicio
    private final Boton bJugar   = new Boton(245, 176, 150, 28, "JUGAR", Andalusi.BOTON_VERDE).andalusi();
    private final Boton bRanking = new Boton(245, 212, 150, 28, "RANKING", Andalusi.BOTON_ALBERO).andalusi();
    private final Boton bSalir   = new Boton(245, 248, 150, 28, "SALIR", Andalusi.BOTON_GRANATE).andalusi();
    private final Boton[] botonesInicio = { bJugar, bRanking, bSalir };
    private int seleccion;          // botón elegido con el teclado (↑/↓)

    // Pausa
    private final Boton bContinuar = new Boton(240, 150, 160, 22, "CONTINUAR", Andalusi.BOTON_VERDE).andalusi();
    private final Boton bReiniciar = new Boton(240, 180, 160, 22, "REINICIAR NIVEL", Andalusi.BOTON_AZUL).andalusi();
    private final Boton bMenuPausa = new Boton(240, 210, 160, 22, "MENÚ PRINCIPAL", Andalusi.BOTON_GRANATE).andalusi();

    // Victoria
    private final Boton bSiguiente = new Boton(180, 262, 130, 24, "CONTINUAR", Andalusi.BOTON_VERDE).andalusi();
    private final Boton bMenuVict  = new Boton(330, 262, 130, 24, "MENÚ", Andalusi.BOTON_AZUL).andalusi();

    /** Valores que va "escribiendo" el panel de código del menú (gravedad, velocidad, salto). */
    private static final String[][] DEMO_CODIGO = {
        { "9.8", "5.0", "12.0" }, { "4.5", "5.0", "12.0" }, { "4.5", "7.5", "12.0" }, { "9.8", "5.0", "15.0" },
    };

    public GameMenu(GameRenderer renderer) {
        this.renderer = renderer;
    }

    // =====================================================================
    //  PANTALLA DE INICIO
    // =====================================================================

    public Accion actualizarInicio(Input in) {
        if (in.pulsada(KeyEvent.VK_UP) || in.pulsada(KeyEvent.VK_W)) seleccion = (seleccion + 2) % 3;
        if (in.pulsada(KeyEvent.VK_DOWN) || in.pulsada(KeyEvent.VK_S)) seleccion = (seleccion + 1) % 3;
        for (int i = 0; i < botonesInicio.length; i++)
            if (botonesInicio[i].contiene(in.getRatonX(), in.getRatonY())) seleccion = i;

        if (bJugar.clicado(in)) return Accion.JUGAR;
        if (bRanking.clicado(in)) return Accion.RANKING;
        if (bSalir.clicado(in)) return Accion.SALIR;
        if (in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_SPACE))
            return seleccion == 0 ? Accion.JUGAR : seleccion == 1 ? Accion.RANKING : Accion.SALIR;
        if (in.pulsada(KeyEvent.VK_2)) return Accion.NIVEL2;   // atajo para profesores
        return Accion.NINGUNA;
    }

    public void dibujarInicio(Graphics2D g, Input in, int frame) {
        double t = frame / 60.0;

        // ---- Fondo: el río con nubes y agua animadas, y rayos de sol ----
        renderer.dibujarFondo(g, Level.Tema.RIO, frame / 2, 0, 368, true);   // con orilla de arena
        dibujarRayosSol(g, 502, 70, t);
        for (int x = 0; x < 640; x += 16) {
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16) % 2, true, false), x, 312, null);
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16 + 1) % 2, false, false), x, 328, null);
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16) % 2, false, false), x, 344, null);
        }
        // penumbra cálida arriba para que el título se lea bien
        g.setPaint(new GradientPaint(0, 0, new Color(40, 16, 8, 120), 0, 180, new Color(40, 16, 8, 0)));
        g.fillRect(0, 0, 640, 180);

        // ---- Pétalos de azahar y motas de luz que flotan ----
        dibujarPetalos(g, t);

        // ---- Título en una placa con arquería, como la Mezquita ----
        dibujarPlacaTitulo(g, frame);

        // ---- Coco caminando (escala x4) ----
        BufferedImage coco = (frame / 40) % 4 == 3 ? Sprites.COCO_VICTORIA1
                : Sprites.COCO_ANDAR[(frame / 8) % Sprites.COCO_ANDAR.length];
        int bote = (frame / 8) % 2 == 0 ? 0 : -2;
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(116, 306, 72, 8);
        g.drawImage(coco, 110, 232 + bote, 80, 80, null);

        // ---- Panel de código que se "edita" solo ----
        dibujarCodigo(g, frame);

        // ---- Botones (ratón o ↑/↓ + ENTER) ----
        int mx = in.getRatonX(), my = in.getRatonY();
        boolean ratonEncima = false;
        for (Boton b : botonesInicio) ratonEncima |= b.contiene(mx, my);
        for (int i = 0; i < botonesInicio.length; i++) {
            boolean elegido = botonesInicio[i].contiene(mx, my) || (!ratonEncima && i == seleccion);
            botonesInicio[i].dibujar(g, elegido);
        }
        g.setFont(Ui.FUENTE_PEQ);
        String ayuda = "↑/↓ elegir · ENTER aceptar";
        int aw = g.getFontMetrics().stringWidth(ayuda) + 14;
        Andalusi.escalonado(g, 320 - aw / 2, 284, aw, 14, 1, new Color(26, 12, 6, 170));
        Ui.textoCentrado(g, ayuda, 320, 294, Andalusi.ORO_CLARO);

        // ---- Pie con cenefa de alicatado ----
        g.setColor(new Color(20, 10, 6, 190));
        g.fillRect(0, 326, 640, 34);
        Andalusi.cenefa(g, 0, 322, 640);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "A/D o flechas: mover  ·  ESPACIO: saltar  ·  E: consola de código  ·  ESC: pausa", 320, 341, Ui.TEXTO);
        Ui.textoCentrado(g, "Demo para 2.º DAM · Java + Swing", 320, 355, Andalusi.ORO_CLARO);

        // ---- Fundido de entrada al arrancar ----
        if (frame < 45) {
            g.setColor(new Color(0, 0, 0, Math.max(0, 255 - frame * 255 / 45)));
            g.fillRect(0, 0, 640, 360);
        }
    }

    /** Haces de luz que salen del sol y giran muy despacio. */
    private void dibujarRayosSol(Graphics2D g, int cx, int cy, double t) {
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        for (int i = 0; i < 9; i++) {
            double a = t * 0.05 + i * Math.PI * 2 / 9;
            double ancho = 0.07 + 0.03 * Math.sin(t * 0.7 + i);
            int largo = 520;
            int[] xs = { cx, cx + (int) (Math.cos(a - ancho) * largo), cx + (int) (Math.cos(a + ancho) * largo) };
            int[] ys = { cy, cy + (int) (Math.sin(a - ancho) * largo), cy + (int) (Math.sin(a + ancho) * largo) };
            int alfa = 14 + (int) (8 * Math.sin(t * 0.9 + i * 2));
            g.setColor(new Color(255, 236, 170, Math.max(0, alfa)));
            g.fillPolygon(xs, ys, 3);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
    }

    /** Pétalos blancos (azahar) que caen en diagonal y motas doradas que suben. */
    private void dibujarPetalos(Graphics2D g, double t) {
        for (int i = 0; i < 28; i++) {
            boolean petalo = i % 3 != 0;
            double vx = 10 + (i % 5) * 4, vy = petalo ? 12 + (i % 7) * 3 : -(6 + i % 4 * 2);
            int x = (int) Math.floorMod((long) (i * 113 + t * vx + Math.sin(t * 1.3 + i) * 10), 700) - 30;
            int y = (int) Math.floorMod((long) (i * 71 + t * vy), 380) - 10;
            if (petalo) {
                boolean giro = ((int) (t * 3 + i)) % 2 == 0;
                g.setColor(i % 4 == 0 ? new Color(0xFFD9E4) : new Color(0xFFF8EC));
                g.fillRect(x, y, giro ? 2 : 1, giro ? 1 : 2);
            } else {
                int alfa = (int) (120 + 100 * Math.sin(t * 2 + i));
                g.setColor(new Color(255, 220, 120, Math.max(0, Math.min(255, alfa))));
                g.fillRect(x, y, 1, 1);
            }
        }
    }

    /** Placa del título: marco dorado, arquería de dovelas arriba y cenefa abajo. */
    private void dibujarPlacaTitulo(Graphics2D g, int frame) {
        int px = 120, py = 18, pw = 400, ph = 134;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillRect(px + 4, py + 4, pw, ph);
        Andalusi.escalonado(g, px, py, pw, ph, 3, new Color(26, 12, 6, 200));
        // filetes dorados
        g.setColor(Andalusi.ORO);
        g.drawRect(px + 3, py + 3, pw - 7, ph - 7);
        g.setColor(Andalusi.ORO_OSCURO);
        g.drawRect(px + 5, py + 5, pw - 11, ph - 11);
        Andalusi.frisoArcos(g, px + 8, py + 10, pw - 16, 24, new Color(0x3A1E10));
        Andalusi.cenefa(g, px + 8, py + ph - 14, pw - 16);
        Andalusi.estrella(g, px + 3, py + 3, 5, Andalusi.ORO_CLARO, Andalusi.MARRON, 0);
        Andalusi.estrella(g, px + pw - 4, py + 3, 5, Andalusi.ORO_CLARO, Andalusi.MARRON, 0);
        Andalusi.estrella(g, px + 3, py + ph - 4, 5, Andalusi.ORO_CLARO, Andalusi.MARRON, 0);
        Andalusi.estrella(g, px + pw - 4, py + ph - 4, 5, Andalusi.ORO_CLARO, Andalusi.MARRON, 0);

        // Título: cada letra flota con su propio ritmo y un brillo la recorre
        g.setFont(Ui.FUENTE_TITULO);
        letrasOndulantes(g, "CÓRDOBA:", 82, frame, new Color(0xF2D27A), new Color(0x3A1E0E), 0);
        letrasOndulantes(g, "CÓDIGO SALVAJE", 116, frame, new Color(0x7BE07B), new Color(0x0E2A14), 9);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Un plataformas donde programas las leyes de la física", 320, 133, Andalusi.CREMA);
    }

    private void letrasOndulantes(Graphics2D g, String s, int y, int frame, Color color, Color contorno, int desfase) {
        FontMetrics fm = g.getFontMetrics();
        int x = 320 - fm.stringWidth(s) / 2;
        int brillo = (frame / 3) % (s.length() + 40) - 20 + desfase;   // letra iluminada
        for (int i = 0; i < s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            int dy = (int) Math.round(Math.sin(frame / 18.0 + (i + desfase) * 0.55) * 2.5);
            Color col = Math.abs(i - brillo) <= 1 ? Andalusi.mezclar(color, Color.WHITE, 0.6f) : color;
            Ui.textoContorno(g, c, x, y + dy, col, contorno);
            x += fm.stringWidth(c);
        }
    }

    /** Panel de código: cada pocos segundos cambia un valor y "compila". */
    private void dibujarCodigo(Graphics2D g, int frame) {
        int ciclo = 180;
        int paso = (frame / ciclo) % DEMO_CODIGO.length;
        int enCiclo = frame % ciclo;
        String[] ahora = DEMO_CODIGO[paso], antes = DEMO_CODIGO[(paso + DEMO_CODIGO.length - 1) % DEMO_CODIGO.length];
        boolean compilado = enCiclo > 100;

        int x = 432, y = 170, w = 190, h = 84;
        Andalusi.marco(g, x, y, w, h, Ui.PANEL, false);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.texto(g, "// ¡Cámbiame!", x + 10, y + 17, new Color(0x6A9955));
        String[] nombres = { "gravedad", "velocidad", "fuerzaSalto" };
        for (int i = 0; i < 3; i++) {
            boolean cambia = !ahora[i].equals(antes[i]);
            // el valor nuevo se escribe letra a letra
            String valor = ahora[i];
            if (cambia && !compilado) {
                int letras = Math.min(valor.length(), Math.max(0, (enCiclo - 20) / 10));
                valor = letras == 0 ? antes[i] : valor.substring(0, letras);
            }
            String linea = "float " + nombres[i] + " = " + valor + "f;";
            Color col = cambia && !compilado ? Ui.AMARILLO : new Color(0x82AAFF);
            Ui.texto(g, linea, x + 10, y + 31 + i * 13, col);
            if (cambia && !compilado && (frame / 15) % 2 == 0) {
                g.setColor(Ui.TEXTO);
                g.fillRect(x + 11 + g.getFontMetrics().stringWidth(linea), y + 23 + i * 13, 2, 9);
            }
        }
        if (compilado) Ui.texto(g, "BUILD SUCCESSFUL", x + 10, y + 72, Ui.VERDE);
        else Ui.texto(g, "editando...", x + 10, y + 72, Ui.GRIS);
    }

    // =====================================================================
    //  PAUSA
    // =====================================================================

    public Accion actualizarPausa(Input in) {
        if (bContinuar.clicado(in) || in.pulsada(KeyEvent.VK_ESCAPE)) return Accion.CONTINUAR;
        if (bReiniciar.clicado(in)) return Accion.REINICIAR;
        if (bMenuPausa.clicado(in)) return Accion.MENU;
        return Accion.NINGUNA;
    }

    public void dibujarPausa(Graphics2D g, Input in) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, 640, 360);
        Andalusi.marco(g, 220, 100, 200, 148, Ui.PANEL, true);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "PAUSA", 320, 132, Andalusi.ORO_CLARO);
        bContinuar.dibujar(g, in);
        bReiniciar.dibujar(g, in);
        bMenuPausa.dibujar(g, in);
    }

    // =====================================================================
    //  NIVEL COMPLETADO
    // =====================================================================

    public Accion actualizarVictoria(Input in) {
        if (bSiguiente.clicado(in) || in.pulsada(KeyEvent.VK_ENTER)) return Accion.CONTINUAR;
        if (bMenuVict.clicado(in) || in.pulsada(KeyEvent.VK_ESCAPE)) return Accion.MENU;
        return Accion.NINGUNA;
    }

    public void dibujarVictoria(Graphics2D g, Input in, Level nivel, int frames, int muertes,
                                int compilaciones, int frame) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, 640, 360);
        Andalusi.marco(g, 150, 60, 340, 240, Ui.PANEL, true);
        Andalusi.frisoArcos(g, 160, 66, 320, 20, new Color(0x141420));
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "¡NIVEL COMPLETADO!", 320, 104, Ui.VERDE);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Nivel " + nivel.getNumero() + ": " + nivel.getNombre(), 320, 120, Ui.TEXTO);

        BufferedImage coco = (frame / 15) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        int bote = (frame / 15) % 2 == 0 ? 0 : -6;
        g.drawImage(coco, 290, 128 + bote, 60, 60, null);

        dibujarEstadisticas(g, frames, muertes, compilaciones, 206);
        Ui.textoCentrado(g, "Siguiente: Nivel 2 — El desafío de la Mezquita", 320, 252, Ui.AMARILLO);
        bSiguiente.dibujar(g, in);
        bMenuVict.dibujar(g, in);
    }

    private void dibujarEstadisticas(Graphics2D g, int frames, int muertes, int compilaciones, int y) {
        int seg = frames / 60;
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, String.format("Tiempo: %d:%02d", seg / 60, seg % 60), 320, y, Ui.TEXTO);
        Ui.textoCentrado(g, "Caídas: " + muertes, 320, y + 14, Ui.TEXTO);
        Ui.textoCentrado(g, "Cambios de código compilados: " + compilaciones, 320, y + 28, new Color(0x82AAFF));
    }
}
