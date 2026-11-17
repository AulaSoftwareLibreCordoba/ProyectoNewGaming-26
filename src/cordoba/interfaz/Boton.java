package cordoba.interfaz;

import cordoba.nucleo.Input;

import java.awt.Color;
import java.awt.Graphics2D;

// Botón en pixel art: se aclara con el ratón encima y se puede desactivar.
public class Boton {

    public final int x, y, w, h;
    public String texto;
    public Color color;
    public boolean activo = true;
    // estilo andalusí (menús); si es false, botón sencillo (consola)
    public boolean andalusi;
    // píxeles al doble de tamaño (pantalla de título)
    public boolean grande;

    public Boton(int x, int y, int w, int h, String texto, Color color) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.texto = texto;
        this.color = color;
    }

    // activa el estilo andalusí
    public Boton andalusi() {
        andalusi = true;
        return this;
    }

    // estilo andalusí con píxeles x2
    public Boton grande() {
        andalusi = true;
        grande = true;
        return this;
    }

    public boolean contiene(int mx, int my) {
        return activo && mx >= x && mx < x + w && my >= y && my < y + h;
    }

    // ¿clic encima en este fotograma?
    public boolean clicado(Input in) {
        return in.clic() && contiene(in.getRatonX(), in.getRatonY());
    }

    public void dibujar(Graphics2D g, Input in) {
        boolean encima = contiene(in.getRatonX(), in.getRatonY());
        dibujar(g, encima);
    }

    public void dibujar(Graphics2D g, boolean encima) {
        if (grande) {
            Andalusi.botonGrande(g, x, y, w, h, texto, color, encima, activo);
            return;
        }
        if (andalusi) {
            Andalusi.boton(g, x, y, w, h, texto, color, encima, activo);
            return;
        }
        Color base = activo ? color : new Color(0x55556A);
        Color cara = encima ? base.brighter() : base;
        int dy = encima && activo ? -1 : 0;
        // sombra
        g.setColor(new Color(0x101018));
        g.fillRect(x + 1, y + 2, w, h);
        // cuerpo
        g.setColor(cara.darker().darker());
        g.fillRect(x, y + dy, w, h);
        g.setColor(cara);
        g.fillRect(x + 1, y + 1 + dy, w - 2, h - 3);
        g.setColor(cara.brighter());
        g.fillRect(x + 1, y + 1 + dy, w - 2, 1);
        // texto
        g.setFont(h >= 18 ? Ui.FUENTE : Ui.FUENTE_PEQ);
        int tw = g.getFontMetrics().stringWidth(texto);
        int ty = Ui.baseCentrada(g, y + dy, h);
        Ui.texto(g, texto, x + (w - tw) / 2, ty, activo ? Ui.TEXTO : Ui.GRIS);
    }
}
