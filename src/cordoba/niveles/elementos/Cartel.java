package cordoba.niveles.elementos;

// Cartel del tutorial: al pasar enseña su mensaje y, si tiene, ejecuta una acción (p. ej. desbloquear una variable).
public class Cartel {

    public final float x, ySuelo;
    public final String texto;
    public final Runnable accion;   // puede ser null
    public final String desbloquea; // variable que desbloquea (null si es un cartel normal)
    public boolean activado;

    public Cartel(float x, float ySuelo, String texto, Runnable accion, String desbloquea) {
        this.x = x;
        this.ySuelo = ySuelo;
        this.texto = texto;
        this.accion = accion;
        this.desbloquea = desbloquea;
    }
}
