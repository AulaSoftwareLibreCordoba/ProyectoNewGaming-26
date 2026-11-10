package cordoba.niveles.elementos;

/**
 * Cartel de tutorial. Al pasar por él se muestra su mensaje y, opcionalmente,
 * se ejecuta una acción (por ejemplo, desbloquear una variable).
 */
public class Cartel {

    public final float x, ySuelo;
    public final String texto;
    public final Runnable accion;   // puede ser null
    public boolean activado;

    public Cartel(float x, float ySuelo, String texto, Runnable accion) {
        this.x = x;
        this.ySuelo = ySuelo;
        this.texto = texto;
        this.accion = accion;
    }
}
