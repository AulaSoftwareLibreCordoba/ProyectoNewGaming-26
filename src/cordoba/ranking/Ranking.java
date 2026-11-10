package cordoba.ranking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * RANKING de la competición.
 *
 * Criterio: gana quien tarde MENOS. Si empatan, quien tenga menos caídas;
 * después, menos compilaciones; y si aun así empatan, quien lo logró antes.
 *
 * Se guardan TODAS las partidas en un fichero de texto (UTF-8, una por línea):
 *     nombre;fotogramas;caidas;compilaciones
 * pero en pantalla solo se muestran las {@link #TOP} mejores.
 */
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

    /** Las 10 mejores partidas, de mejor a peor. */
    public List<Puntuacion> mejores() {
        return List.copyOf(partidas.subList(0, Math.min(TOP, partidas.size())));
    }

    /** Puesto (empezando en 1) que tendría una partida con estos datos. */
    public int posicionPara(int frames, int muertes, int compilaciones) {
        Puntuacion nueva = new Puntuacion("?", frames, muertes, compilaciones);
        int pos = 1;
        for (Puntuacion p : partidas) {
            if (ORDEN.compare(p, nueva) <= 0) pos++;   // en empate, la antigua va delante
        }
        return pos;
    }

    /** Añade la partida, guarda el fichero y devuelve el puesto obtenido. */
    public int registrar(Puntuacion p) {
        int pos = posicionPara(p.frames(), p.muertes(), p.compilaciones());
        partidas.add(pos - 1, p);
        guardar();
        return pos;
    }

    /** ¿Se pudo escribir el fichero la última vez? */
    public boolean isGuardadoOk() {
        return guardadoOk;
    }

    private void guardar() {
        List<String> lineas = new ArrayList<>();
        for (Puntuacion p : partidas) lineas.add(p.aLinea());
        try {
            // Se escribe primero en un temporal: si el juego se cierra a medias, no se pierde el ranking
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
