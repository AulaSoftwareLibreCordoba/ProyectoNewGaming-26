package cordoba.graficos.ambiente;

import cordoba.niveles.Level;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// Nubes y agua animadas. Pura decoración; GameRenderer lo llama mientras pinta el fondo.
public final class Ambiente {

    private static final long INICIO = System.nanoTime();

    private Ambiente() { }

    // segundos desde que arrancó (el viento no para nunca)
    public static double segundos() {
        return (System.nanoTime() - INICIO) / 1e9;
    }

    // prepara una capa antes de pintarla (p. ej. le quita las nubes pintadas)
    public static BufferedImage prepararCapa(Level.Tema tema, int indice, BufferedImage capa) {
        return Nubes.sinNubesPintadas(tema, indice, capa);
    }

    // arena bajo las palmeras del río
    public static void orilla(Graphics2D g, int desp, int dy) {
        Orilla.dibujar(g, desp, dy, segundos());
    }

    // lo que va encima de cada capa del fondo
    public static void trasCapa(Graphics2D g, Level.Tema tema, int indice, int camX, int dy) {
        if (indice == 0) Nubes.dibujar(g, tema, camX, dy, segundos());
        if (indice == 2 && tema == Level.Tema.RIO) Agua.brillosRio(g, camX, dy, segundos());
    }
}
