package cordoba.nucleo;

import java.awt.event.*;

// Teclado y ratón. abajo() = la tecla está pulsada ahora · pulsada() = se pulsó en ESTE fotograma.
// Los eventos llegan del hilo de Swing y el juego lee desde el suyo, por eso todo es synchronized.
public class Input implements KeyListener, MouseListener, MouseMotionListener {

    private final boolean[] teclas = new boolean[1024];
    private final boolean[] pulsadas = new boolean[1024];

    // ratón en coordenadas de pantalla (luego se pasan a 640x360)
    private int ratonX, ratonY;
    private boolean clic;            // clic en este fotograma
    private boolean botonPulsado;    // botón mantenido

    // pantalla -> resolución interna (la pone Game)
    private double escala = 1;
    private int offX, offY;

    // ---- teclado ----

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        if (c >= 0 && c < teclas.length) {
            if (!teclas[c]) pulsadas[c] = true;
            teclas[c] = true;
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent e) {
        int c = e.getKeyCode();
        if (c >= 0 && c < teclas.length) teclas[c] = false;
    }

    // lo escrito en este fotograma (para el nombre del ranking)
    private final StringBuilder escrito = new StringBuilder();

    @Override
    public synchronized void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c != KeyEvent.CHAR_UNDEFINED) escrito.append(c);
    }

    // lo escrito desde el último fotograma ('\b' = borrar, '\n' = ENTER) y lo vacía
    public synchronized String tomarEscrito() {
        String s = escrito.toString();
        escrito.setLength(0);
        return s;
    }

    public synchronized boolean abajo(int codigo) {
        return teclas[codigo];
    }

    public synchronized boolean pulsada(int codigo) {
        return pulsadas[codigo];
    }

    // al final de cada fotograma se olvidan las pulsaciones
    public synchronized void finFrame() {
        java.util.Arrays.fill(pulsadas, false);
        escrito.setLength(0);
        clic = false;
    }

    // suelta todo (al perder el foco de la ventana)
    public synchronized void soltarTodo() {
        java.util.Arrays.fill(teclas, false);
        botonPulsado = false;
    }

    // ---- ratón ----

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        ratonX = e.getX();
        ratonY = e.getY();
        clic = true;
        botonPulsado = true;
    }

    @Override
    public synchronized void mouseReleased(MouseEvent e) {
        botonPulsado = false;
    }

    @Override
    public synchronized void mouseMoved(MouseEvent e) {
        ratonX = e.getX();
        ratonY = e.getY();
    }

    @Override
    public synchronized void mouseDragged(MouseEvent e) {
        ratonX = e.getX();
        ratonY = e.getY();
    }

    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    public synchronized void setTransformacion(double escala, int offX, int offY) {
        this.escala = escala;
        this.offX = offX;
        this.offY = offY;
    }

    // x del ratón en 640x360
    public synchronized int getRatonX() {
        return (int) ((ratonX - offX) / escala);
    }

    // y del ratón en 640x360
    public synchronized int getRatonY() {
        return (int) ((ratonY - offY) / escala);
    }

    public synchronized boolean clic() {
        return clic;
    }

    public synchronized boolean botonPulsado() {
        return botonPulsado;
    }
}
