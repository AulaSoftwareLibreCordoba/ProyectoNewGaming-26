package cordoba.graficos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Sistema de partículas sencillo: polvo al aterrizar, chispas al aplicar
 * cambios, salpicaduras... Solo es decoración visual.
 */
public class Particulas {

    private static class Particula {
        float x, y, vx, vy, gravedad;
        int vida, vidaMax;
        Color color;
    }

    private final List<Particula> lista = new ArrayList<>();
    private final Random azar = new Random();

    public void emitir(float x, float y, int cantidad, Color color, float fuerza, float gravedad, int vida) {
        for (int i = 0; i < cantidad; i++) {
            Particula p = new Particula();
            p.x = x;
            p.y = y;
            double ang = azar.nextDouble() * Math.PI * 2;
            float v = fuerza * (0.3f + azar.nextFloat() * 0.7f);
            p.vx = (float) Math.cos(ang) * v;
            p.vy = (float) Math.sin(ang) * v - fuerza * 0.3f;
            p.gravedad = gravedad;
            p.vida = p.vidaMax = vida / 2 + azar.nextInt(vida / 2 + 1);
            p.color = color;
            lista.add(p);
        }
    }

    public void actualizar() {
        Iterator<Particula> it = lista.iterator();
        while (it.hasNext()) {
            Particula p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravedad;
            p.vx *= 0.96f;
            if (--p.vida <= 0) it.remove();
        }
    }

    public void dibujar(Graphics2D g, int camX, int camY) {
        for (Particula p : lista) {
            int alfa = (int) (255f * p.vida / p.vidaMax);
            g.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), Math.max(0, Math.min(255, alfa))));
            int tam = p.vida > p.vidaMax / 2 ? 2 : 1;
            g.fillRect((int) p.x - camX, (int) p.y - camY, tam, tam);
        }
    }

    public void limpiar() {
        lista.clear();
    }
}
