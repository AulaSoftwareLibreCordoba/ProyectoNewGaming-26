package cordoba.ranking;

// Una partida del ranking. Los tiempos van en fotogramas (60 FPS fijos) para comparar exacto.
// framesN1 / framesN2 = tiempo de cada nivel (0 si no se jugó, p. ej. con el atajo "2").
public record Puntuacion(String nombre, int frames, int muertes, int compilaciones,
                         int framesN1, int framesN2) {

    private static final String SEPARADOR = ";";

    public Puntuacion {
        nombre = limpiar(nombre);
    }

    public int framesNivel(int nivel) {
        return nivel == 1 ? framesN1 : nivel == 2 ? framesN2 : 0;
    }

    // quita lo que rompería el fichero y recorta el largo
    public static String limpiar(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (!Character.isISOControl(c) && c != ';') sb.append(c);
        }
        String limpio = sb.toString().strip();
        return limpio.length() > Ranking.MAX_NOMBRE ? limpio.substring(0, Ranking.MAX_NOMBRE).strip() : limpio;
    }

    // tiempo total con centésimas: 1:23.45
    public String tiempo() {
        return formatearTiempo(frames);
    }

    public static String formatearTiempo(int frames) {
        long cs = frames * 100L / 60;
        return String.format("%d:%02d.%02d", cs / 6000, (cs / 100) % 60, cs % 100);
    }

    // ---- fichero: nombre;fotogramas;caidas;compilaciones;fotogramasN1;fotogramasN2 ----

    String aLinea() {
        return String.join(SEPARADOR, nombre, String.valueOf(frames), String.valueOf(muertes),
                String.valueOf(compilaciones), String.valueOf(framesN1), String.valueOf(framesN2));
    }

    // null si la línea está mal (así un fichero roto no tumba el juego); acepta el formato viejo de 4 campos
    static Puntuacion desdeLinea(String linea) {
        String[] partes = linea.split(SEPARADOR, -1);
        if (partes.length != 4 && partes.length != 6) return null;
        try {
            int[] n = new int[5];
            for (int i = 1; i < partes.length; i++) {
                n[i - 1] = Integer.parseInt(partes[i].strip());
                if (n[i - 1] < 0) return null;
            }
            Puntuacion p = new Puntuacion(partes[0], n[0], n[1], n[2], n[3], n[4]);
            return p.nombre().isEmpty() ? null : p;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
