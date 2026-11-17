package cordoba.niveles.elementos;

// Farola = checkpoint: se enciende al pasar y, si Coco muere, vuelve aquí.
public class Farola {

    public final float x;        // centro (px)
    public final float ySuelo;   // suelo donde se apoya (px)
    public boolean encendida;

    public Farola(float x, float ySuelo) {
        this.x = x;
        this.ySuelo = ySuelo;
    }
}
