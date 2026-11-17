package cordoba.niveles.elementos;

import java.awt.Rectangle;

// Comida que resta tiempo. Para llegar hay que cambiar la física.
public class Coleccionable {

    // 3 segundos
    public static final int BONUS_FRAMES = 3 * 60;

    // zona para cogerlo
    private static final int CAJA = 12;

    public enum Tipo { SALMOREJO, FLAMENQUIN }

    public final Tipo tipo;
    public final float x, y;   // centro (px)
    public boolean recogido;

    public Coleccionable(Tipo tipo, float x, float y) {
        this.tipo = tipo;
        this.x = x;
        this.y = y;
    }

    public Rectangle caja() {
        return new Rectangle(Math.round(x) - CAJA / 2, Math.round(y) - CAJA / 2, CAJA, CAJA);
    }
}
