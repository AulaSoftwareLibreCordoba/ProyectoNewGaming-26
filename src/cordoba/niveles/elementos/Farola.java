package cordoba.niveles.elementos;

/**
 * Farola cordobesa que funciona como PUNTO DE CONTROL.
 * Cuando el cocodrilo pasa por delante se enciende y, si muere, reaparece aquí.
 */
public class Farola {

    public final float x;        // centro horizontal (píxeles)
    public final float ySuelo;   // altura del suelo donde se apoya (píxeles)
    public boolean encendida;

    public Farola(float x, float ySuelo) {
        this.x = x;
        this.ySuelo = ySuelo;
    }
}
