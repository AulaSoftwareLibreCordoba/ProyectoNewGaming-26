package cordoba.ranking;

/**
 * Una partida terminada que entra en el ranking.
 *
 * El tiempo se guarda en FOTOGRAMAS (el juego va a 60 FPS fijos), así dos
 * partidas se comparan de forma exacta, sin errores de redondeo.
 */
public record Puntuacion(String nombre, int frames, int muertes, int compilaciones) {

    private static final String SEPARADOR = ";";

    public Puntuacion {
        nombre = limpiar(nombre);
    }

    /** Quita caracteres que romperían el fichero y recorta la longitud. */
    public static String limpiar(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (!Character.isISOControl(c) && c != ';') sb.append(c);
        }
        String limpio = sb.toString().strip();
        return limpio.length() > Ranking.MAX_NOMBRE ? limpio.substring(0, Ranking.MAX_NOMBRE).strip() : limpio;
    }

    /** Tiempo con centésimas: 1:23.45 */
    public String tiempo() {
        return formatearTiempo(frames);
    }

    public static String formatearTiempo(int frames) {
        long cs = frames * 100L / 60;
        return String.format("%d:%02d.%02d", cs / 6000, (cs / 100) % 60, cs % 100);
    }

    // ---- Lectura / escritura en el fichero: nombre;fotogramas;caidas;compilaciones ----

    String aLinea() {
        return nombre + SEPARADOR + frames + SEPARADOR + muertes + SEPARADOR + compilaciones;
    }

    /** Devuelve null si la línea está mal formada (así un fichero dañado no rompe el juego). */
    static Puntuacion desdeLinea(String linea) {
        String[] partes = linea.split(SEPARADOR, -1);
        if (partes.length != 4) return null;
        try {
            int f = Integer.parseInt(partes[1].strip());
            int m = Integer.parseInt(partes[2].strip());
            int c = Integer.parseInt(partes[3].strip());
            if (f < 0 || m < 0 || c < 0) return null;
            Puntuacion p = new Puntuacion(partes[0], f, m, c);
            return p.nombre().isEmpty() ? null : p;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
