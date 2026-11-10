package cordoba.niveles.plataformas;

import cordoba.entidades.Player;

import java.awt.Graphics2D;

/**
 * Clase base de todas las plataformas que no son tiles fijos del mapa:
 * pasarelas que se derrumban, plataformas temporales y plataformas móviles.
 *
 * Son plataformas de "un solo sentido": se puede saltar a través de ellas
 * desde abajo y solo frenan al jugador cuando cae encima.
 */
public abstract class PlataformaDinamica {

    protected float x, y;          // esquina superior izquierda (píxeles)
    protected float ancho, alto;   // tamaño (píxeles)
    protected float dx;            // desplazamiento horizontal del último fotograma

    protected PlataformaDinamica(float x, float y, float ancho, float alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    /** Se llama una vez por fotograma. */
    public abstract void actualizar(int frame, Player jugador);

    /** ¿Se puede pisar ahora mismo? */
    public abstract boolean esSolida();

    /** Dibuja la plataforma teniendo en cuenta la cámara. */
    public abstract void dibujar(Graphics2D g, int camX, int camY, int frame);

    /** Vuelve al estado inicial (cuando el jugador reaparece). */
    public void reiniciar() { }

    /** Aviso de que el jugador está encima. */
    public void alPisar(Player jugador) { }

    public float getX()     { return x; }
    public float getY()     { return y; }
    public float getAncho() { return ancho; }
    public float getDx()    { return dx; }
}
