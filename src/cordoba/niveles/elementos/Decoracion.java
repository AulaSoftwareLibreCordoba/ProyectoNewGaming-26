package cordoba.niveles.elementos;

import cordoba.graficos.Sprites;

import java.awt.image.BufferedImage;

/**
 * Elemento decorativo sin colisión (macetas, palmeras, naranjos, cañas...).
 */
public class Decoracion {

    public enum Tipo { MACETA, PALMERA, NARANJO, CANAS }

    public final Tipo tipo;
    public final float x, ySuelo;   // centro horizontal y suelo
    public final int variante;

    public Decoracion(Tipo tipo, float x, float ySuelo, int variante) {
        this.tipo = tipo;
        this.x = x;
        this.ySuelo = ySuelo;
        this.variante = variante;
    }

    public BufferedImage imagen() {
        switch (tipo) {
            case MACETA:  return Sprites.maceta(variante);
            case PALMERA: return Sprites.palmera();
            case NARANJO: return Sprites.naranjo();
            default:      return Sprites.canas();
        }
    }
}
