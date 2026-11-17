package cordoba.audio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

// Volúmenes de 0 a 10 (el general multiplica a los otros). Se guardan en opciones.txt.
public final class Volumen {

    public static final int MAXIMO = 10;
    private static final Path FICHERO = Path.of("opciones.txt");

    private static volatile int general = MAXIMO, musica = 8, efectos = MAXIMO;

    private Volumen() { }

    public static int getGeneral() { return general; }
    public static int getMusica()  { return musica; }
    public static int getEfectos() { return efectos; }

    public static void setGeneral(int v) { general = limitar(v); }
    public static void setMusica(int v)  { musica = limitar(v); }
    public static void setEfectos(int v) { efectos = limitar(v); }

    // de 0 a 1, ya con el general aplicado
    public static float factorMusica()  { return general * musica / (float) (MAXIMO * MAXIMO); }
    public static float factorEfectos() { return general * efectos / (float) (MAXIMO * MAXIMO); }

    private static int limitar(int v) {
        return Math.max(0, Math.min(MAXIMO, v));
    }

    // lee opciones.txt; si falla, se quedan los valores por defecto
    public static void cargar() {
        try {
            if (!Files.exists(FICHERO)) return;
            List<String> lineas = Files.readAllLines(FICHERO, StandardCharsets.UTF_8);
            for (String l : lineas) {
                String[] p = l.split("=", 2);
                if (p.length != 2) continue;
                int v;
                try { v = Integer.parseInt(p[1].trim()); } catch (NumberFormatException e) { continue; }
                switch (p[0].trim()) {
                    case "general": setGeneral(v); break;
                    case "musica":  setMusica(v);  break;
                    case "efectos": setEfectos(v); break;
                    default: break;
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudieron leer las opciones: " + e.getMessage());
        }
    }

    public static void guardar() {
        String txt = "general=" + general + "\nmusica=" + musica + "\nefectos=" + efectos + "\n";
        try {
            Files.writeString(FICHERO, txt, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("No se pudieron guardar las opciones: " + e.getMessage());
        }
    }
}
