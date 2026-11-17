package cordoba.interfaz;

import cordoba.audio.Sonido;
import cordoba.audio.Volumen;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

// Opciones de sonido (general, música y efectos). Se abre desde el título y desde la pausa.
public class PantallaOpciones {

    private static final int PX = 110, PY = 64, PW = 420, PH = 236;
    private static final int X_ETIQUETA = PX + 26, X_BARRA = PX + 156, Y_FILA0 = PY + 62, ALTO_FILA = 40;
    private static final int SEGMENTOS = Volumen.MAXIMO, ANCHO_SEG = 12, HUECO_SEG = 4, ALTO_SEG = 16;

    private static final String[] ETIQUETAS = { "GENERAL", "MÚSICA", "EFECTOS" };
    private static final IntSupplier[] LEER = { Volumen::getGeneral, Volumen::getMusica, Volumen::getEfectos };
    private static final IntConsumer[] PONER = { Volumen::setGeneral, Volumen::setMusica, Volumen::setEfectos };

    private final Boton bVolver = new Boton(240, PY + PH - 54, 160, 32, "VOLVER", Andalusi.BOTON_VERDE).grande();
    private int seleccion;    // 0..2 barras, 3 = VOLVER
    private int ratonX = Integer.MIN_VALUE, ratonY = Integer.MIN_VALUE;

    public void abrir(Input in) {
        seleccion = 0;
        ratonX = in.getRatonX();
        ratonY = in.getRatonY();
    }

    // true = cerrar (y volver a donde estaba)
    public boolean actualizar(Input in) {
        if (in.pulsada(KeyEvent.VK_ESCAPE)) return cerrar();
        boolean arriba = in.pulsada(KeyEvent.VK_W) || in.pulsada(KeyEvent.VK_UP);
        boolean abajo = in.pulsada(KeyEvent.VK_S) || in.pulsada(KeyEvent.VK_DOWN);
        if (arriba != abajo) {
            seleccion = Math.floorMod(seleccion + (arriba ? -1 : 1), ETIQUETAS.length + 1);
            Sonido.clic();
        }
        if (seleccion < ETIQUETAS.length) {
            if (in.pulsada(KeyEvent.VK_A) || in.pulsada(KeyEvent.VK_LEFT)) cambiar(seleccion, LEER[seleccion].getAsInt() - 1);
            if (in.pulsada(KeyEvent.VK_D) || in.pulsada(KeyEvent.VK_RIGHT)) cambiar(seleccion, LEER[seleccion].getAsInt() + 1);
        }

        // ratón
        int mx = in.getRatonX(), my = in.getRatonY();
        boolean seMovio = mx != ratonX || my != ratonY;
        ratonX = mx;
        ratonY = my;
        for (int i = 0; i < ETIQUETAS.length; i++) {
            int yf = Y_FILA0 + i * ALTO_FILA;
            boolean enFila = my >= yf - 6 && my < yf + ALTO_SEG + 6 && mx >= PX + 10 && mx < PX + PW - 10;
            if (enFila && seMovio) seleccion = i;
            int finBarra = X_BARRA + SEGMENTOS * (ANCHO_SEG + HUECO_SEG);
            if (enFila && in.botonPulsado() && mx >= X_BARRA - 12 && mx < finBarra + 4) {
                // a la izquierda del primer segmento = 0
                int valor = mx < X_BARRA ? 0 : Math.min(SEGMENTOS, (mx - X_BARRA) / (ANCHO_SEG + HUECO_SEG) + 1);
                seleccion = i;
                cambiar(i, valor);
            }
        }
        if (bVolver.contiene(mx, my) && seMovio) seleccion = ETIQUETAS.length;
        if (bVolver.clicado(in)) return cerrar();
        if (seleccion == ETIQUETAS.length && (in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_SPACE))) return cerrar();
        if (seleccion < ETIQUETAS.length && in.pulsada(KeyEvent.VK_ENTER)) return cerrar();
        return false;
    }

    private void cambiar(int fila, int valor) {
        int antes = LEER[fila].getAsInt();
        PONER[fila].accept(valor);
        if (LEER[fila].getAsInt() != antes) Sonido.clic();
    }

    private boolean cerrar() {
        Volumen.guardar();
        Sonido.clic();
        return true;
    }

    public void dibujar(Graphics2D g, int frame) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, 640, 360);
        Andalusi.marco(g, PX, PY, PW, PH, Ui.PANEL, true);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "OPCIONES", 320, PY + 40, Andalusi.ORO_CLARO);

        for (int i = 0; i < ETIQUETAS.length; i++) {
            int yf = Y_FILA0 + i * ALTO_FILA;
            boolean sel = i == seleccion;
            int valor = LEER[i].getAsInt();
            // fila elegida: franja y flechas que laten
            if (sel) {
                g.setColor(new Color(255, 220, 140, 28));
                g.fillRect(PX + 12, yf - 8, PW - 24, ALTO_SEG + 16);
                int bote = (frame / 15) % 2 * 2;
                flecha(g, X_ETIQUETA - 14 + bote, yf + ALTO_SEG / 2, Andalusi.ORO_CLARO);
            }
            textoAlineado(g, ETIQUETAS[i], X_ETIQUETA, yf, sel ? Andalusi.ORO_CLARO : Andalusi.CREMA);

            // barra de segmentos gordos (píxeles x2)
            for (int s = 0; s < SEGMENTOS; s++) {
                int sx = X_BARRA + s * (ANCHO_SEG + HUECO_SEG);
                boolean lleno = s < valor;
                g.setColor(Andalusi.MARRON);
                g.fillRect(sx - 2, yf - 2, ANCHO_SEG + 4, ALTO_SEG + 4);
                Color c = !lleno ? new Color(0x3A2A20)
                        : s < 6 ? Andalusi.VERDE.brighter() : s < 8 ? Andalusi.ORO : Andalusi.ROJO;
                g.setColor(c);
                g.fillRect(sx, yf, ANCHO_SEG, ALTO_SEG);
                if (lleno) {
                    g.setColor(Andalusi.mezclar(c, Color.WHITE, 0.35f));
                    g.fillRect(sx, yf, ANCHO_SEG, 2);
                }
            }
            String pct = valor * 10 + "%";
            int xp = X_BARRA + SEGMENTOS * (ANCHO_SEG + HUECO_SEG) + 6;
            textoAlineado(g, pct, xp, yf, valor == 0 ? Ui.GRIS : Andalusi.CREMA);
        }

        bVolver.dibujar(g, seleccion == ETIQUETAS.length);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "↑/↓ elegir · ←/→ cambiar · ENTER volver", 320, PY + PH - 10, Ui.GRIS);
    }

    // alineado por abajo para que las tildes no bajen la palabra
    private static void textoAlineado(Graphics2D g, String texto, int x, int yFila, Color c) {
        Andalusi.textoGrande(g, texto, x, yFila + ALTO_SEG - Andalusi.altoTextoGrande(texto), c);
    }

    // flechita gorda apuntando a la derecha
    private static void flecha(Graphics2D g, int x, int cy, Color c) {
        g.setColor(Andalusi.MARRON);
        g.fillRect(x - 2, cy - 8, 4, 16);
        g.fillRect(x + 2, cy - 6, 4, 12);
        g.fillRect(x + 6, cy - 4, 4, 8);
        g.setColor(c);
        g.fillRect(x, cy - 6, 2, 12);
        g.fillRect(x + 2, cy - 4, 2, 8);
        g.fillRect(x + 4, cy - 4, 2, 8);
        g.fillRect(x + 6, cy - 2, 2, 4);
    }
}
