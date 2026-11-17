package cordoba.fisica;

// Una variable de la consola (gravedad, velocidad...).
// Se guarda en décimas (98 = 9.8) para no acumular errores de float.
public class VariableFisica {

    private final String nombre;        // tal cual sale en el "código"
    private final String descripcion;
    private final int porDefecto;
    private final int minimo;
    private final int maximo;
    private final int paso;             // lo que cambia con cada clic

    private int valor;                  // el valor APLICADO
    private boolean bloqueada;

    public VariableFisica(String nombre, String descripcion,
                          int porDefecto, int minimo, int maximo, int paso) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.porDefecto = porDefecto;
        this.minimo = minimo;
        this.maximo = maximo;
        this.paso = paso;
        this.valor = porDefecto;
    }

    public float getValor() {
        return valor / 10f;
    }

    public int getDecimas() {
        return valor;
    }

    public void setDecimas(int decimas) {
        valor = limitar(decimas);
    }

    // que nunca se salga de [minimo, maximo]
    public int limitar(int decimas) {
        return Math.max(minimo, Math.min(maximo, decimas));
    }

    // siguiente valor encajando en el paso: 9.8 -> 10.0 -> 10.5 ...
    public int siguienteArriba(int decimas) {
        return limitar((decimas / paso + 1) * paso);
    }

    // lo mismo hacia abajo: 9.8 -> 9.5 -> 9.0 ...
    public int siguienteAbajo(int decimas) {
        return limitar(((decimas + paso - 1) / paso - 1) * paso);
    }

    public void restaurar() {
        valor = porDefecto;
    }

    // 98 -> "9.8"
    public static String formatear(int decimas) {
        return (decimas / 10) + "." + (decimas % 10);
    }

    public String getNombre()       { return nombre; }
    public String getDescripcion()  { return descripcion; }
    public int getMinimo()          { return minimo; }
    public int getMaximo()          { return maximo; }
    public int getPorDefecto()      { return porDefecto; }
    public boolean isBloqueada()    { return bloqueada; }
    public void setBloqueada(boolean b) { bloqueada = b; }
}
