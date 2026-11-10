package cordoba.fisica;

/**
 * CONFIGURACIÓN FÍSICA CENTRALIZADA.
 *
 * Todas las variables que el jugador puede modificar viven aquí. La clase
 * Player NO tiene números "mágicos" de física: siempre pregunta a esta clase.
 *
 * Los valores que ve el jugador (9.8, 5.0, 12.0) son "unidades del juego".
 * Los métodos ...Px() los convierten a píxeles por fotograma (el juego va a 60 FPS).
 *
 * Si queréis añadir una variable nueva (por ejemplo "friccion"):
 *   1. Añadid una constante de índice (FRICCION = 3).
 *   2. Añadid un new VariableFisica(...) al array.
 *   3. Usadla desde Player con getValor(FRICCION).
 * ¡La consola de código la mostrará automáticamente!
 */
public class PhysicsConfig {

    // --- Factores de conversión "unidades del juego" -> píxeles/fotograma ---
    public static final float FACTOR_GRAVEDAD  = 0.04f;  // 9.8  -> 0.392 px/fotograma²
    public static final float FACTOR_VELOCIDAD = 0.40f;  // 5.0  -> 2.0   px/fotograma
    public static final float FACTOR_SALTO     = 0.55f;  // 12.0 -> 6.6   px/fotograma

    /** Velocidad máxima de caída (px/fotograma). Evita atravesar el suelo. */
    public static final float CAIDA_MAXIMA = 9.0f;

    // --- Índices de cada variable dentro del array ---
    public static final int GRAVEDAD     = 0;
    public static final int VELOCIDAD    = 1;
    public static final int FUERZA_SALTO = 2;

    private final VariableFisica[] variables = {
        //                 nombre         descripción                                        defecto  min  max  paso  (en décimas)
        new VariableFisica("gravedad",    "Atracción hacia el suelo. Menos = flotas más",       98,   30, 200, 5),
        new VariableFisica("velocidad",   "Rapidez al andar. Más = corres más lejos",           50,   20, 100, 5),
        new VariableFisica("fuerzaSalto", "Impulso del salto. Más = saltas más alto",          120,   80, 160, 5),
    };

    // ------------------------------------------------------------------
    //  Valores convertidos a píxeles (los usa Player en cada fotograma)
    // ------------------------------------------------------------------

    public float gravedadPx()    { return getValor(GRAVEDAD) * FACTOR_GRAVEDAD; }
    public float velocidadPx()   { return getValor(VELOCIDAD) * FACTOR_VELOCIDAD; }
    public float fuerzaSaltoPx() { return getValor(FUERZA_SALTO) * FACTOR_SALTO; }

    // ------------------------------------------------------------------
    //  Acceso genérico
    // ------------------------------------------------------------------

    public float getValor(int indice) {
        return variables[indice].getValor();
    }

    public VariableFisica getVariable(int indice) {
        return variables[indice];
    }

    public int getCantidad() {
        return variables.length;
    }

    /** Vuelve a los valores por defecto y desbloquea todo. */
    public void restaurarTodo() {
        for (VariableFisica v : variables) {
            v.restaurar();
            v.setBloqueada(false);
        }
    }

    /** Texto corto para el HUD: "gravedad=9.8  velocidad=5.0 ..." */
    public String resumen() {
        StringBuilder sb = new StringBuilder();
        for (VariableFisica v : variables) {
            if (sb.length() > 0) sb.append("  ");
            sb.append(v.getNombre()).append('=').append(VariableFisica.formatear(v.getDecimas()));
        }
        return sb.toString();
    }
}
