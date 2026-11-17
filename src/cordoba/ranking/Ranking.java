package cordoba.ranking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Ranking de la competición. Gana quien tarde menos; si empatan, menos caídas, luego menos compilaciones
// y luego quien lo hizo antes. Se guardan TODAS las partidas en ranking.txt pero solo se enseñan 10.
// También hay ranking por nivel (solo por tiempo).
public class Ranking {

    public static final int TOP = 10;
    public static final int MAX_NOMBRE = 12;

    private static final Comparator<Puntuacion> ORDEN = Comparator
            .comparingInt(Puntuacion::frames)
            .thenComparingInt(Puntuacion::muertes)
            .thenComparingInt(Puntuacion::compilaciones);

    private final Path fichero;
    private final List<Puntuacion> partidas = new ArrayList<>();   // siempre ordenada
    private boolean guardadoOk = true;

    public Ranking(Path fichero) {
        this.fichero = fichero;
        cargar();
    }

    private void cargar() {
        if (!Files.exists(fichero)) return;
        try {
            for (String linea : Files.readAllLines(fichero, StandardCharsets.UTF_8)) {
                Puntuacion p = Puntuacion.desdeLinea(linea);
                if (p != null) partidas.add(p);
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer el ranking: " + e.getMessage());
        }
        partidas.sort(ORDEN);   // sort es estable: en empate se respeta el orden del fichero
    }

    public List<Puntuacion> mejores() {
        return List.copyOf(partidas.subList(0, Math.min(TOP, partidas.size())));
    }

    // solo las partidas que jugaron ese nivel
    public List<Puntuacion> mejoresNivel(int nivel) {
        List<Puntuacion> lista = new ArrayList<>();
        for (Puntuacion p : partidas) if (p.framesNivel(nivel) > 0) lista.add(p);
        // estable: en empate va delante quien lo hizo antes
        lista.sort(Comparator.comparingInt(p -> p.framesNivel(nivel)));
        return List.copyOf(lista.subList(0, Math.min(TOP, lista.size())));
    }

    // null si nadie lo ha jugado aún
    public Puntuacion mejorNivel(int nivel) {
        List<Puntuacion> lista = mejoresNivel(nivel);
        return lista.isEmpty() ? null : lista.get(0);
    }

    // puesto (desde 1) que tendría una partida así
    public int posicionPara(int frames, int muertes, int compilaciones) {
        Puntuacion nueva = new Puntuacion("?", frames, muertes, compilaciones, 0, 0);
        int pos = 1;
        for (Puntuacion p : partidas) {
            if (ORDEN.compare(p, nueva) <= 0) pos++;   // en empate, la antigua va delante
        }
        return pos;
    }

    // ¿batiría el récord del nivel? (empatar no vale)
    public boolean esRecordNivel(int nivel, int framesNivel) {
        if (framesNivel <= 0) return false;
        Puntuacion mejor = mejorNivel(nivel);
        return mejor == null || framesNivel < mejor.framesNivel(nivel);
    }

    // la añade, guarda y devuelve el puesto
    public int registrar(Puntuacion p) {
        int pos = posicionPara(p.frames(), p.muertes(), p.compilaciones());
        partidas.add(pos - 1, p);
        guardar();
        return pos;
    }

    public boolean isGuardadoOk() {
        return guardadoOk;
    }

    private void guardar() {
        List<String> lineas = new ArrayList<>();
        for (Puntuacion p : partidas) lineas.add(p.aLinea());
        try {
            // primero a un temporal: si el juego se cierra a medias, no se pierde el ranking
            Path tmp = fichero.resolveSibling(fichero.getFileName() + ".tmp");
            Files.write(tmp, lineas, StandardCharsets.UTF_8);
            Files.move(tmp, fichero, StandardCopyOption.REPLACE_EXISTING);
            guardadoOk = true;
        } catch (IOException e) {
            System.err.println("No se pudo guardar el ranking: " + e.getMessage());
            guardadoOk = false;
        }
    }
}
