package cordoba.niveles.elementos;

import cordoba.entidades.Player;
import cordoba.graficos.Sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// Maceta colgada de un clavo: tiembla, cae, se rompe contra el suelo y vuelve a salir. Solo hace daño cayendo.
public class MacetaQueCae extends ObstaculoMovil {

    private static final float GRAVEDAD = 0.3f;
    private static final int AVISO = 45;   // fotogramas temblando antes de caer
    private static final int ROTA = 30;    // fotogramas con los trozos en el suelo

    private final float x, yArriba, yAbajo;
    private final int periodo, desfase, variante, frameCaida;
    private boolean cayendo, rota;

    // x = centro · yArriba = donde cuelga · yAbajo = suelo donde se rompe · periodo = fotogramas del ciclo
    public MacetaQueCae(float x, float yArriba, float yAbajo, int periodo, int desfase, int variante) {
        super(x, yArriba, x, yAbajo, periodo, desfase);
        this.x = x;
        this.yArriba = yArriba;
        this.yAbajo = yAbajo;
        this.periodo = periodo;
        this.desfase = desfase;
        this.variante = variante;
        int duracionCaida = (int) Math.ceil(Math.sqrt(2 * (yAbajo - yArriba) / GRAVEDAD));
        frameCaida = periodo - ROTA - duracionCaida;   // el ciclo termina justo al acabar de estar rota
        radio = 6;
    }

    @Override
    public void actualizar(int frame) {
        int t = Math.floorMod(frame + desfase, periodo);
        cx = x;
        cayendo = false;
        rota = false;
        if (t < frameCaida) {
            cy = yArriba;
            if (t >= frameCaida - AVISO && (t / 3) % 2 == 0) cx = x + ((t / 6) % 2 == 0 ? 1 : -1);   // tiembla
        } else {
            int tt = t - frameCaida;
            cy = yArriba + GRAVEDAD * tt * tt / 2;
            if (cy >= yAbajo) {
                cy = yAbajo;
                rota = true;
            } else {
                cayendo = true;
            }
        }
    }

    @Override
    public boolean golpea(Player j) {
        return cayendo && super.golpea(j);
    }

    @Override
    public void dibujar(Graphics2D g, int camX, int camY, int frame) {
        int px = Math.round(x) - camX, top = Math.round(yArriba) - camY;
        // clavo y alambre
        g.setColor(new Color(0x4A3A2A));
        g.fillRect(px - 1, top - 10, 3, 2);
        if (!cayendo && !rota) {
            g.setColor(new Color(0x6A6A6A));
            g.fillRect(px, top - 8, 1, 3);
        }
        if (rota) {
            // trozos de barro azul y tierra
            int y = Math.round(yAbajo) - camY;
            g.setColor(new Color(0x1E4FA8));
            g.fillRect(px - 7, y - 2, 4, 2);
            g.fillRect(px + 2, y - 3, 3, 3);
            g.fillRect(px + 6, y - 1, 2, 1);
            g.setColor(new Color(0x6B4A2A));
            g.fillRect(px - 3, y - 2, 5, 2);
            g.setColor(new Color(0xE8344A));
            g.fillRect(px - 1, y - 4, 2, 2);
            return;
        }
        BufferedImage img = Sprites.maceta(variante);
        g.drawImage(img, Math.round(cx) - camX - img.getWidth() / 2, Math.round(cy) - camY - img.getHeight() / 2, null);
    }
}
