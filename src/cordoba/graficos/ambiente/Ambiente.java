package cordoba.graficos.ambiente;

import cordoba.niveles.Level;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Animaciones del AMBIENTE (nubes y agua): dan vida al escenario.
 * Solo son decoración: no cambian nada del juego.
 *
 * GameRenderer llama a estos métodos mientras dibuja el fondo.
 */
public final class Ambiente {

    private static final long INICIO = System.nanoTime();

    private Ambiente() { }

    /** Segundos desde que arrancó el juego (el viento y la corriente no se paran nunca). */
    public static double segundos() {
        return (System.nanoTime() - INICIO) / 1e9;
    }

    /** Prepara una capa del fondo antes de dibujarla (p. ej. quita las nubes pintadas). */
    public static BufferedImage prepararCapa(Level.Tema tema, int indice, BufferedImage capa) {
        return Nubes.sinNubesPintadas(tema, indice, capa);
    }

    /** Orilla de arena bajo las palmeras del río (desp = desplazamiento de su capa). */
    public static void orilla(Graphics2D g, int desp, int dy) {
        Orilla.dibujar(g, desp, dy, segundos());
    }

    /** Se llama después de dibujar cada capa del fondo: añade lo que va encima de ella. */
    public static void trasCapa(Graphics2D g, Level.Tema tema, int indice, int camX, int dy) {
        if (indice == 0) Nubes.dibujar(g, tema, camX, dy, segundos());
        if (indice == 2 && tema == Level.Tema.RIO) Agua.brillosRio(g, camX, dy, segundos());
    }
}
