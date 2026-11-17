package cordoba.niveles.plataformas;

import cordoba.entidades.Player;

import java.awt.Graphics2D;

// Base de las plataformas que no son tiles fijos (pasarelas que se hunden, temporales, móviles).
// Son de un solo sentido: las atraviesas desde abajo y solo te paran al caer encima.
public abstract class PlataformaDinamica {

    protected float x, y;          // esquina de arriba a la izquierda (px)
    protected float ancho, alto;   // tamaño (px)
    protected float dx;            // lo que se movió en horizontal el último fotograma

    protected PlataformaDinamica(float x, float y, float ancho, float alto) {
        this.x = x;
        this.y = y;
        this.ancho = ancho;
        this.alto = alto;
    }

    public abstract void actualizar(int frame, Player jugador);

    // ¿se puede pisar ahora?
    public abstract boolean esSolida();

    public abstract void dibujar(Graphics2D g, int camX, int camY, int frame);

    // vuelve al principio (cuando Coco reaparece)
    public void reiniciar() { }

    // Coco está encima
    public void alPisar(Player jugador) { }

    public float getX()     { return x; }
    public float getY()     { return y; }
    public float getAncho() { return ancho; }
    public float getDx()    { return dx; }
}
