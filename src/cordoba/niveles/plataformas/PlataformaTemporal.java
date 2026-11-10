package cordoba.niveles.plataformas;

import cordoba.entidades.Player;
import cordoba.niveles.Level;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Plataforma de azulejos que aparece y desaparece en un ciclo fijo.
 * Parpadea poco antes de desaparecer para avisar al jugador.
 */
public class PlataformaTemporal extends PlataformaDinamica {

    private final int ciclo;      // duración total del ciclo (fotogramas)
    private final int visible;    // cuántos fotogramas del ciclo es sólida
    private final int desfase;    // para que no todas desaparezcan a la vez
    private int fase;

    public PlataformaTemporal(float x, float y, int anchoTiles, int ciclo, int visible, int desfase) {
        super(x, y, anchoTiles * Level.TILE, Level.TILE);
        this.ciclo = ciclo;
        this.visible = visible;
        this.desfase = desfase;
    }

    @Override
    public void actualizar(int frame, Player jugador) {
        fase = (frame + desfase) % ciclo;
    }

    @Override
    public boolean esSolida() {
        return fase < visible;
    }

    @Override
    public void dibujar(Graphics2D g, int camX, int camY, int frame) {
        int px = (int) x - camX, py = (int) y - camY;
        if (!esSolida()) {
            // contorno fantasma: indica dónde volverá a aparecer
            g.setColor(new Color(255, 255, 255, 70));
            g.setStroke(new BasicStroke(1));
            for (int i = 0; i < ancho; i += 4) {
                g.fillRect(px + i, py, 2, 1);
                g.fillRect(px + i, py + 15, 2, 1);
            }
            return;
        }
        int restante = visible - fase;
        if (restante < 40 && (frame / 3) % 2 == 0) return;   // parpadeo de aviso
        int tiles = (int) (ancho / Level.TILE);
        for (int i = 0; i < tiles; i++) {
            int tx = px + i * 16;
            // azulejo andaluz azul, blanco y amarillo
            g.setColor(new Color(0xF4F1E6));
            g.fillRect(tx, py, 16, 10);
            g.setColor(new Color(0x2457B8));
            g.fillRect(tx, py, 16, 1);
            g.fillRect(tx, py + 9, 16, 1);
            g.fillRect(tx, py, 1, 10);
            g.fillRect(tx + 7, py + 2, 2, 6);
            g.fillRect(tx + 5, py + 4, 6, 2);
            g.setColor(new Color(0xE8B32A));
            g.fillRect(tx + 7, py + 4, 2, 2);
            g.fillRect(tx + 2, py + 2, 1, 1);
            g.fillRect(tx + 13, py + 2, 1, 1);
            g.fillRect(tx + 2, py + 7, 1, 1);
            g.fillRect(tx + 13, py + 7, 1, 1);
            g.setColor(new Color(0x6B4A2A));
            g.fillRect(tx, py + 10, 16, 3);
        }
    }
}
