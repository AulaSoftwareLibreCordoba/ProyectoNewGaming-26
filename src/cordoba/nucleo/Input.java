package cordoba.nucleo;

import java.awt.event.*;

/**
 * Guarda el estado del teclado y del ratón.
 *
 * - abajo(tecla):   la tecla está pulsada ahora mismo.
 * - pulsada(tecla): la tecla se ha pulsado en ESTE fotograma (un solo disparo).
 *
 * Los eventos llegan desde el hilo de Swing y el juego los lee desde su
 * propio hilo, por eso los métodos son "synchronized".
 */
public class Input implements KeyListener, MouseListener, MouseMotionListener {

    private final boolean[] teclas = new boolean[1024];
    private final boolean[] pulsadas = new boolean[1024];

    // Ratón en coordenadas de PANTALLA (luego Game las convierte a 640x360)
    private int ratonX, ratonY;
    private boolean clic;            // clic en este fotograma
    private boolean botonPulsado;    // botón mantenido

    // Conversión pantalla -> resolución interna (la actualiza Game)
    private double escala = 1;
    private int offX, offY;

    // ------------------------- Teclado -------------------------

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

    @Override
    public void keyTyped(KeyEvent e) { }

    public synchronized boolean abajo(int codigo) {
        return teclas[codigo];
    }

    public synchronized boolean pulsada(int codigo) {
        return pulsadas[codigo];
    }

    /** Se llama al final de cada actualización para "olvidar" las pulsaciones. */
    public synchronized void finFrame() {
        java.util.Arrays.fill(pulsadas, false);
        clic = false;
    }

    /** Suelta todas las teclas (útil al perder el foco de la ventana). */
    public synchronized void soltarTodo() {
        java.util.Arrays.fill(teclas, false);
        botonPulsado = false;
    }

    // ------------------------- Ratón -------------------------

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

    /** X del ratón en la resolución interna (0..639). */
    public synchronized int getRatonX() {
        return (int) ((ratonX - offX) / escala);
    }

    /** Y del ratón en la resolución interna (0..359). */
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
