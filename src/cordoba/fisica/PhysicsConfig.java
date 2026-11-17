package cordoba.fisica;

/*
 * Todas las variables que se pueden tocar en la consola. Player no tiene números mágicos: siempre pregunta aquí.
 * El jugador ve "unidades del juego" (9.8, 5.0...) y los métodos ...Px() las pasan a píxeles por fotograma.
 *
 * Para añadir una variable (p. ej. friccion): ponle un índice, métela en el array y úsala en Player.
 * La consola la enseña sola.
 */
public class PhysicsConfig {

    // de unidades del juego a píxeles/fotograma
    public static final float FACTOR_GRAVEDAD  = 0.04f;  // 9.8  -> 0.392 px/fotograma²
    public static final float FACTOR_VELOCIDAD = 0.40f;  // 5.0  -> 2.0   px/fotograma
    public static final float FACTOR_SALTO     = 0.55f;  // 12.0 -> 6.6   px/fotograma

    // tope de caída para no atravesar el suelo
    public static final float CAIDA_MAXIMA = 9.0f;

    public static final int GRAVEDAD     = 0;
    public static final int VELOCIDAD    = 1;
    public static final int FUERZA_SALTO = 2;

    private final VariableFisica[] variables = {
        //                 nombre         descripción                                        defecto  min  max  paso  (en décimas)
        new VariableFisica("gravedad",    "Atracción hacia el suelo. Menos = flotas más",       98,   30, 200, 5),
        new VariableFisica("velocidad",   "Rapidez al andar. Más = corres más lejos",           50,   20, 100, 5),
        new VariableFisica("fuerzaSalto", "Impulso del salto. Más = saltas más alto",          120,   80, 160, 5),
    };

    // ---- en píxeles (Player los lee cada fotograma) ----

    public float gravedadPx()    { return getValor(GRAVEDAD) * FACTOR_GRAVEDAD; }
    public float velocidadPx()   { return getValor(VELOCIDAD) * FACTOR_VELOCIDAD; }
    public float fuerzaSaltoPx() { return getValor(FUERZA_SALTO) * FACTOR_SALTO; }

    public float getValor(int indice) {
        return variables[indice].getValor();
    }

    public VariableFisica getVariable(int indice) {
        return variables[indice];
    }

    public int getCantidad() {
        return variables.length;
    }

    // todo a los valores por defecto y desbloqueado
    public void restaurarTodo() {
        for (VariableFisica v : variables) {
            v.restaurar();
            v.setBloqueada(false);
        }
    }

    // para el HUD: "gravedad=9.8  velocidad=5.0 ..."
    public String resumen() {
        StringBuilder sb = new StringBuilder();
        for (VariableFisica v : variables) {
            if (sb.length() > 0) sb.append("  ");
            sb.append(v.getNombre()).append('=').append(VariableFisica.formatear(v.getDecimas()));
        }
        return sb.toString();
    }
}
