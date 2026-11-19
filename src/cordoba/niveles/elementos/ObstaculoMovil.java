package cordoba.niveles.elementos;

import cordoba.entidades.Player;

import java.awt.Color;
import java.awt.Graphics2D;

// Bola de pinchos que va y viene. Si toca a Coco, vuelta a la última farola.
public class ObstaculoMovil {

    private final float x1, y1, x2, y2;
    private final int periodo, desfase;
    protected float radio = 7;
    protected float cx, cy;

    // periodo = fotogramas en ir y volver · desfase = para que no vayan todas a la vez
    public ObstaculoMovil(float x1, float y1, float x2, float y2, int periodo, int desfase) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.periodo = periodo;
        this.desfase = desfase;
        cx = x1;
        cy = y1;
    }

    public void actualizar(int frame) {
        double t = ((frame + desfase) % periodo) / (double) periodo;
        double s = (1 - Math.cos(t * Math.PI * 2)) / 2;   // va de 0 a 1 y vuelve
        cx = (float) (x1 + (x2 - x1) * s);
        cy = (float) (y1 + (y2 - y1) * s);
    }

    // choque círculo-rectángulo con la caja de Coco (un poco reducida)
    public boolean golpea(Player j) {
        float rx1 = j.x + 2, ry1 = j.y + 2, rx2 = j.x + Player.ANCHO - 2, ry2 = j.y + Player.ALTO - 2;
        float px = Math.max(rx1, Math.min(cx, rx2));
        float py = Math.max(ry1, Math.min(cy, ry2));
        float ddx = cx - px, ddy = cy - py;
        return ddx * ddx + ddy * ddy < (radio - 1) * (radio - 1);
    }

    public void dibujar(Graphics2D g, int camX, int camY, int frame) {
        int px = (int) cx - camX, py = (int) cy - camY;
        // cadena del recorrido
        g.setColor(new Color(0, 0, 0, 60));
        g.drawLine((int) x1 - camX, (int) y1 - camY, (int) x2 - camX, (int) y2 - camY);
        // pinchos girando
        double giro = frame * 0.12;
        g.setColor(new Color(0x9AA4AE));
        for (int i = 0; i < 8; i++) {
            double a = giro + i * Math.PI / 4;
            int sx = px + (int) Math.round(Math.cos(a) * 10);
            int sy = py + (int) Math.round(Math.sin(a) * 10);
            g.fillRect(sx - 1, sy - 1, 3, 3);
        }
        g.setColor(new Color(0x3B424A));
        g.fillOval(px - 7, py - 7, 14, 14);
        g.setColor(new Color(0x6E7884));
        g.fillOval(px - 6, py - 6, 11, 11);
        g.setColor(new Color(0xC9D2DA));
        g.fillRect(px - 3, py - 4, 3, 2);
        g.setColor(new Color(0xE04848));
        g.fillRect(px - 1, py, 2, 2);
    }
}
