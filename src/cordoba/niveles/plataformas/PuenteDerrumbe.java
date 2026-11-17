package cordoba.niveles.plataformas;

import cordoba.audio.Sonido;
import cordoba.entidades.Player;
import cordoba.niveles.Level;

import java.awt.Color;
import java.awt.Graphics2D;

// Pasarela vieja: en cuanto Coco entra empieza una cuenta atrás y al acabar se hunde entera.
// El reto es subir la velocidad; saltar no ayuda, solo cuenta distancia / velocidad.
public class PuenteDerrumbe extends PlataformaDinamica {

    private enum Estado { INTACTO, CRUJIENDO, CAIDO }

    private final int framesLimite;     // fotogramas hasta hundirse (60 = 1 s)
    private Estado estado = Estado.INTACTO;
    private int contador;
    private float caida;                // lo que han caído las tablas

    // x, y en píxeles · anchoTiles en bloques de 16 px · framesLimite = tiempo antes de hundirse
    public PuenteDerrumbe(float x, float y, int anchoTiles, int framesLimite) {
        super(x, y, anchoTiles * Level.TILE, 6);
        this.framesLimite = framesLimite;
    }

    @Override
    public void actualizar(int frame, Player j) {
        switch (estado) {
            case INTACTO: {
                float centro = j.x + Player.ANCHO / 2f;
                float pies = j.y + Player.ALTO;
                boolean dentro = centro >= x && centro <= x + ancho;
                boolean cerca = pies <= y + 4 && pies >= y - 70;
                if (dentro && cerca && !j.muerto) {
                    estado = Estado.CRUJIENDO;
                    contador = 0;
                    Sonido.crujido();
                }
                break;
            }
            case CRUJIENDO:
                contador++;
                if (contador >= framesLimite) {
                    estado = Estado.CAIDO;
                    contador = 0;
                    caida = 0;
                    Sonido.derrumbe();
                }
                break;
            case CAIDO:
                contador++;
                caida += 0.5f + contador * 0.12f;
                // se rehace sola para poder reintentarlo
                if (contador > 150) reiniciar();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean esSolida() {
        return estado != Estado.CAIDO;
    }

    @Override
    public void reiniciar() {
        estado = Estado.INTACTO;
        contador = 0;
        caida = 0;
    }

    public boolean estaCrujiendo() {
        return estado == Estado.CRUJIENDO;
    }

    // segundos que quedan
    public float segundosRestantes() {
        return Math.max(0, (framesLimite - contador) / 60f);
    }

    public float getProgreso() {
        return estado == Estado.CRUJIENDO ? (float) contador / framesLimite : 0f;
    }

    @Override
    public void dibujar(Graphics2D g, int camX, int camY, int frame) {
        int tablas = (int) (ancho / 8);
        float progreso = getProgreso();
        for (int i = 0; i < tablas; i++) {
            int tx = (int) x + i * 8 - camX;
            int ty = (int) y - camY;
            if (estado == Estado.CRUJIENDO) {
                // tiembla cada vez más
                int amp = (int) (progreso * 3);
                if (amp > 0) ty += ((frame * 7 + i * 13) % (amp * 2 + 1)) - amp;
            } else if (estado == Estado.CAIDO) {
                ty += (int) (caida * (1 + (i % 3) * 0.35f));
                if (ty - ((int) y - camY) > 140) continue;
            }
            boolean aviso = progreso > 0.7f && (frame / 4) % 2 == 0;
            g.setColor(aviso ? new Color(0xC8643C) : new Color(0x9C6B3A));
            g.fillRect(tx, ty, 7, 5);
            g.setColor(new Color(0xC08A50));
            g.fillRect(tx, ty, 7, 1);
            g.setColor(new Color(0x5E3B1C));
            g.fillRect(tx + 7, ty, 1, 5);
            g.fillRect(tx, ty + 5, 8, 1);
            if (progreso > 0.4f && i % 3 == 1) {   // grietas
                g.setColor(new Color(0x3A2410));
                g.fillRect(tx + 3, ty + 1, 1, 3);
            }
        }
        if (estado != Estado.CAIDO) {
            // cuerdas
            g.setColor(new Color(0x7A5A30));
            int cy = (int) y - camY - 10;
            g.fillRect((int) x - camX, cy, (int) ancho, 1);
            for (int px = 0; px <= ancho; px += 32) g.fillRect((int) x + px - camX, cy, 1, 10);
        }
    }
}
