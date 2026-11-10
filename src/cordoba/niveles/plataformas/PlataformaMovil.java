package cordoba.niveles.plataformas;

import cordoba.entidades.Player;
import cordoba.niveles.Level;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Plataforma que va y viene horizontalmente entre xMin y xMax.
 * Si el jugador está encima, la plataforma lo arrastra (ver CollisionManager).
 */
public class PlataformaMovil extends PlataformaDinamica {

    private final float xMin, xMax, velocidad;
    private final int desfase;

    /**
     * @param velocidad píxeles por fotograma
     * @param desfase   fotogramas de adelanto (para sincronizarla con otras plataformas)
     */
    public PlataformaMovil(float xMin, float xMax, float y, int anchoTiles, float velocidad, int desfase) {
        super(xMin, y, anchoTiles * Level.TILE, 8);
        this.xMin = xMin;
        this.xMax = xMax;
        this.velocidad = velocidad;
        this.desfase = desfase;
    }

    @Override
    public void actualizar(int frame, Player jugador) {
        float recorrido = xMax - xMin;
        float t = ((frame + desfase) * velocidad) % (2 * recorrido);   // movimiento de ida y vuelta
        float nuevaX = t < recorrido ? xMin + t : xMin + 2 * recorrido - t;
        dx = nuevaX - x;
        x = nuevaX;
    }

    @Override
    public boolean esSolida() {
        return true;
    }

    @Override
    public void reiniciar() {
        x = xMin;
        dx = 0;
    }

    @Override
    public void dibujar(Graphics2D g, int camX, int camY, int frame) {
        int px = (int) x - camX, py = (int) y - camY;
        g.setColor(new Color(0x5C3B24));
        g.fillRect(px, py, (int) ancho, 8);
        g.setColor(new Color(0xD9A441));
        g.fillRect(px + 1, py + 1, (int) ancho - 2, 3);
        g.setColor(new Color(0xF2D27A));
        g.fillRect(px + 1, py + 1, (int) ancho - 2, 1);
        g.setColor(new Color(0x8C2A20));
        for (int i = 4; i < ancho - 2; i += 8) g.fillRect(px + i, py + 5, 4, 2);
    }
}
