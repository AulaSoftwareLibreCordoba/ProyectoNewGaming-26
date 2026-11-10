package cordoba.fisica;

import cordoba.entidades.Player;
import cordoba.niveles.Level;
import cordoba.niveles.elementos.ObstaculoMovil;
import cordoba.niveles.plataformas.PlataformaDinamica;

/**
 * Detección y resolución de colisiones del jugador con el nivel.
 *
 * Técnica usada: "movimiento por ejes separados".
 *   1. Movemos en X y, si nos metemos en un bloque, retrocedemos hasta su borde.
 *   2. Movemos en Y y hacemos lo mismo (así sabemos si pisamos suelo o techo).
 * El movimiento se divide en pasos de 4 px como máximo para que el jugador
 * nunca atraviese un bloque de 16 px aunque vaya muy rápido.
 */
public class CollisionManager {

    private static final int T = Level.TILE;
    private static final float PASO_MAXIMO = 4f;

    private final Level nivel;

    public CollisionManager(Level nivel) {
        this.nivel = nivel;
    }

    /** Mueve al jugador según su velocidad (vx, vy) resolviendo colisiones. */
    public void moverJugador(Player p) {
        // Si estaba sobre una plataforma móvil, la plataforma lo arrastra
        if (p.plataformaBajo != null && p.plataformaBajo.esSolida() && p.plataformaBajo.getDx() != 0) {
            moverX(p, p.plataformaBajo.getDx(), false);
        }
        p.plataformaBajo = null;
        p.enSuelo = false;

        float dx = p.vx, dy = p.vy;
        int pasos = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / PASO_MAXIMO));
        float px = dx / pasos, py = dy / pasos;
        boolean chocaX = false, chocaY = false;
        for (int i = 0; i < pasos; i++) {
            if (!chocaX && px != 0) chocaX = moverX(p, px, true);
            if (!chocaY && py != 0) chocaY = moverY(p, py);
        }
    }

    /** Mueve en horizontal. Devuelve true si ha chocado con una pared. */
    private boolean moverX(Player p, float d, boolean frenar) {
        p.x += d;
        int f1 = fila(p.y);
        int f2 = fila(p.y + Player.ALTO - 0.01f);
        if (d > 0) {
            int c = columna(p.x + Player.ANCHO - 0.01f);
            for (int f = f1; f <= f2; f++) {
                if (nivel.esSolido(c, f)) {
                    p.x = c * T - Player.ANCHO;
                    if (frenar) p.vx = 0;
                    return true;
                }
            }
        } else if (d < 0) {
            int c = columna(p.x);
            for (int f = f1; f <= f2; f++) {
                if (nivel.esSolido(c, f)) {
                    p.x = (c + 1) * T;
                    if (frenar) p.vx = 0;
                    return true;
                }
            }
        }
        return false;
    }

    /** Mueve en vertical. Devuelve true si ha tocado suelo o techo. */
    private boolean moverY(Player p, float d) {
        float piesAntes = p.y + Player.ALTO;
        p.y += d;
        int c1 = columna(p.x);
        int c2 = columna(p.x + Player.ANCHO - 0.01f);
        if (d > 0) {
            // ¿Pisamos un bloque?
            int f = fila(p.y + Player.ALTO - 0.01f);
            for (int c = c1; c <= c2; c++) {
                if (nivel.esSolido(c, f)) {
                    p.y = f * T - Player.ALTO;
                    p.vy = 0;
                    p.enSuelo = true;
                    return true;
                }
            }
            // ¿Pisamos una plataforma dinámica? (solo desde arriba)
            for (PlataformaDinamica pl : nivel.getPlataformas()) {
                if (!pl.esSolida()) continue;
                boolean solapaX = p.x + Player.ANCHO > pl.getX() && p.x < pl.getX() + pl.getAncho();
                boolean veniaDeArriba = piesAntes <= pl.getY() + 1f;
                boolean llega = p.y + Player.ALTO >= pl.getY();
                if (solapaX && veniaDeArriba && llega) {
                    p.y = pl.getY() - Player.ALTO;
                    p.vy = 0;
                    p.enSuelo = true;
                    p.plataformaBajo = pl;
                    pl.alPisar(p);
                    return true;
                }
            }
        } else if (d < 0) {
            // ¿Nos damos con la cabeza?
            int f = fila(p.y);
            for (int c = c1; c <= c2; c++) {
                if (nivel.esSolido(c, f)) {
                    p.y = (f + 1) * T;
                    p.vy = 0;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ¿El jugador toca algo peligroso? (pinchos, agua, bolas de pinchos o caer
     * fuera del mapa). Las cajas de los peligros son algo más pequeñas que el
     * tile para que el juego sea "justo".
     */
    public boolean tocaPeligro(Player p) {
        float x1 = p.x + 2, x2 = p.x + Player.ANCHO - 2;
        float y1 = p.y + 2, y2 = p.y + Player.ALTO - 1;
        int c1 = columna(x1), c2 = columna(x2);
        int f1 = fila(y1), f2 = fila(y2);
        for (int f = f1; f <= f2; f++) {
            for (int c = c1; c <= c2; c++) {
                char t = nivel.tile(c, f);
                float tx = c * T, ty = f * T;
                switch (t) {
                    case '^':
                        if (solapa(x1, y1, x2, y2, tx + 2, ty + 8, tx + 14, ty + 16)) return true;
                        break;
                    case 'v':
                        if (solapa(x1, y1, x2, y2, tx + 2, ty, tx + 14, ty + 8)) return true;
                        break;
                    case '~':
                        if (solapa(x1, y1, x2, y2, tx, ty + 5, tx + 16, ty + 16)) return true;
                        break;
                    default:
                        break;
                }
            }
        }
        for (ObstaculoMovil o : nivel.getObstaculos()) {
            if (o.golpea(p)) return true;
        }
        return p.y > nivel.getAltoPx() + 24;
    }

    private static boolean solapa(float ax1, float ay1, float ax2, float ay2,
                                  float bx1, float by1, float bx2, float by2) {
        return ax1 < bx2 && ax2 > bx1 && ay1 < by2 && ay2 > by1;
    }

    private static int columna(float px) {
        return (int) Math.floor(px / T);
    }

    private static int fila(float py) {
        return (int) Math.floor(py / T);
    }
}
