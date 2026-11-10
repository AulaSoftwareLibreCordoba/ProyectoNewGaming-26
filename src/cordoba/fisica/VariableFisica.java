package cordoba.fisica;

/**
 * Representa UNA variable física que el jugador puede modificar desde la
 * consola de código (por ejemplo "gravedad").
 *
 * Truco importante: el valor se guarda en DÉCIMAS usando un int
 * (98 décimas = 9.8). Así evitamos los errores de redondeo típicos de float
 * cuando sumamos y restamos 0.5 muchas veces.
 */
public class VariableFisica {

    private final String nombre;        // nombre tal y como aparece en el "código"
    private final String descripcion;   // explicación corta para el jugador
    private final int porDefecto;       // valor inicial (en décimas)
    private final int minimo;           // límite inferior (en décimas)
    private final int maximo;           // límite superior (en décimas)
    private final int paso;             // cuánto cambia con cada clic (en décimas)

    private int valor;                  // valor actual APLICADO (en décimas)
    private boolean bloqueada;          // si está bloqueada no se puede editar

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

    /** Devuelve el valor como número decimal (ej: 9.8). */
    public float getValor() {
        return valor / 10f;
    }

    public int getDecimas() {
        return valor;
    }

    public void setDecimas(int decimas) {
        valor = limitar(decimas);
    }

    /** Recorta un valor para que nunca se salga de [minimo, maximo]. */
    public int limitar(int decimas) {
        return Math.max(minimo, Math.min(maximo, decimas));
    }

    /**
     * Calcula el siguiente valor hacia arriba, "encajando" en múltiplos del paso.
     * Ejemplo con paso 0.5: 9.8 -> 10.0 -> 10.5 ...
     */
    public int siguienteArriba(int decimas) {
        return limitar((decimas / paso + 1) * paso);
    }

    /** Igual que siguienteArriba pero hacia abajo: 9.8 -> 9.5 -> 9.0 ... */
    public int siguienteAbajo(int decimas) {
        return limitar(((decimas + paso - 1) / paso - 1) * paso);
    }

    public void restaurar() {
        valor = porDefecto;
    }

    /** Convierte décimas a texto: 98 -> "9.8" */
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
