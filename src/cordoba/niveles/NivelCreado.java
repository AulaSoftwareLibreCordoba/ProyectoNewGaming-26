package cordoba.niveles;

import cordoba.niveles.elementos.Coleccionable;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.elementos.MacetaQueCae;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
 * Nivel leído de un archivo de texto (carpeta niveles_creados/). Formato:
 *
 *   nombre: El patio de Lucía
 *   tema: CALLEJA                (RIO, CALLEJA o MEZQUITA: estilo de los bloques)
 *   fondo: RIO                   (opcional: el paisaje de detrás; si no está, el mismo que el tema)
 *   mapa:
 *   SSSSSSSSSSSS
 *   S..I.....M.S
 *   S##########S
 *
 * Antes de "mapa:" las líneas que empiezan por // son comentarios. Después, cada línea es una fila.
 * Casillas:  . aire   # suelo   S pared   ^ pinchos   v pinchos en el techo   ~ agua   u corriente de aire
 *            p fondo de piedra (no choca)   b brisa   B brisa sobre fondo de piedra
 * Cosas (se apoyan en la casilla de debajo):  I Coco   M meta (2x3, esta es su esquina de abajo a la izquierda)
 *            F farola   m maceta   n naranjo   l palmera
 * Cosas que flotan:  C salmorejo   K flamenquín   G maceta que cae (hasta el primer suelo de debajo)
 */
public class NivelCreado extends Level {

    public static final int MAX_ANCHO = 400, MAX_ALTO = 200;
    // lo que ocupa la pantalla (640x360 en casillas de 16)
    private static final int MIN_ANCHO = 40, MIN_ALTO = 23;

    private final Path archivo;

    // lo que hay en el archivo, sin comprobar el mapa (el editor lo usa para abrir un nivel)
    public static final class Datos {
        public String nombre;
        public Tema tema;
        public Tema fondo;   // null = el mismo que el tema
        public List<String> filas;
        int primeraLinea;   // línea del archivo donde empieza el mapa (para los errores)
    }

    // lee y comprueba el archivo; si algo está mal lanza IllegalArgumentException con la línea del error
    public static NivelCreado cargar(Path archivo) throws IOException {
        Datos d = leer(archivo);
        return construir(archivo, d.nombre, d.tema, d.fondo, d.filas, d.primeraLinea, true);
    }

    // el nivel a partir de un mapa en memoria (el editor). Sin "estricto" no hace falta ni Coco ni meta
    // y las letras raras cuentan como aire, para poder pintar el nivel a medio hacer
    public static NivelCreado construir(Path archivo, String nombre, Tema tema, Tema fondo, List<String> filas,
                                        boolean estricto) {
        return construir(archivo, nombre, tema, fondo, filas, -1, estricto);
    }

    private static NivelCreado construir(Path archivo, String nombre, Tema tema, Tema fondo,
                                         List<String> filasOriginales, int lineaMapa, boolean estricto) {
        List<String> filas = new ArrayList<>(filasOriginales);
        if (filas.isEmpty()) throw new IllegalArgumentException("el mapa está vacío");
        int ancho = 0;
        for (String f : filas) ancho = Math.max(ancho, f.length());
        if (ancho > MAX_ANCHO || filas.size() > MAX_ALTO)
            throw new IllegalArgumentException("el mapa es demasiado grande (máximo " + MAX_ANCHO + " x " + MAX_ALTO + ")");
        // un mapa más pequeño que la pantalla se completa con cielo por arriba y aire a la derecha
        int faltan = Math.max(0, MIN_ALTO - filas.size());
        for (int i = 0; i < faltan; i++) filas.add(0, "");
        ancho = Math.max(ancho, MIN_ANCHO);
        if (nombre == null || nombre.isEmpty())
            nombre = archivo == null ? "Sin nombre" : archivo.getFileName().toString().replaceFirst("\\.txt$", "");
        int primera = lineaMapa < 0 ? -1 : lineaMapa - faltan;   // las filas añadidas no están en el archivo
        NivelCreado n = new NivelCreado(archivo, nombre, tema == null ? Tema.RIO : tema, filas, ancho, primera, estricto);
        n.fondo = fondo;
        return n;
    }

    // el archivo de texto de un nivel (lo que guarda el editor)
    public static String aTexto(String nombre, Tema tema, Tema fondo, List<String> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append("nombre: ").append(nombre.replace('\n', ' ').strip()).append('\n');
        sb.append("tema: ").append(tema.name()).append('\n');
        if (fondo != null && fondo != tema) sb.append("fondo: ").append(fondo.name()).append('\n');
        sb.append("// Hecho con el creador de niveles. Se puede editar en el juego (CREADOR > EDITAR).\n");
        sb.append("mapa:\n");
        for (String f : filas) sb.append(f).append('\n');
        return sb.toString();
    }

    public static Datos leer(Path archivo) throws IOException {
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        String nombre = null;
        Tema tema = null, fondo = null;
        int lineaMapa = -1;
        for (int i = 0; i < lineas.size() && lineaMapa < 0; i++) {
            String l = lineas.get(i).strip();
            if (l.isEmpty() || l.startsWith("//")) continue;
            int dosPuntos = l.indexOf(':');
            if (dosPuntos < 0) throw error(i, "se esperaba \"nombre:\", \"tema:\", \"fondo:\" o \"mapa:\"");
            String clave = l.substring(0, dosPuntos).strip().toLowerCase(), valor = l.substring(dosPuntos + 1).strip();
            switch (clave) {
                case "nombre": nombre = valor; break;
                case "fondo":
                    try { fondo = Tema.valueOf(valor.toUpperCase().replace('Í', 'I')); }
                    catch (IllegalArgumentException e) { throw error(i, "fondo \"" + valor + "\" no existe (usa RIO, CALLEJA o MEZQUITA)"); }
                    break;
                case "tema":
                    try { tema = Tema.valueOf(valor.toUpperCase().replace('Í', 'I')); }
                    catch (IllegalArgumentException e) { throw error(i, "tema \"" + valor + "\" no existe (usa RIO, CALLEJA o MEZQUITA)"); }
                    break;
                case "mapa": lineaMapa = i + 1; break;
                default: throw error(i, "no sé qué es \"" + clave + ":\"");
            }
        }
        if (lineaMapa < 0) throw new IllegalArgumentException("falta la línea \"mapa:\"");
        // filas del mapa (sin las vacías del final)
        List<String> filas = new ArrayList<>();
        for (int i = lineaMapa; i < lineas.size(); i++) filas.add(lineas.get(i).stripTrailing());
        while (!filas.isEmpty() && filas.get(filas.size() - 1).isEmpty()) filas.remove(filas.size() - 1);
        if (filas.isEmpty()) throw new IllegalArgumentException("el mapa está vacío");
        Datos d = new Datos();
        d.nombre = nombre == null || nombre.isEmpty() ? archivo.getFileName().toString().replaceFirst("\\.txt$", "") : nombre;
        d.tema = tema == null ? Tema.RIO : tema;
        d.fondo = fondo;
        d.filas = filas;
        d.primeraLinea = lineaMapa;
        return d;
    }

    // con línea del archivo si se sabe; si no (editor), con la fila del mapa
    private static IllegalArgumentException error(int linea, String que) {
        return new IllegalArgumentException("línea " + (linea + 1) + ": " + que);
    }

    // primeraLinea < 0: el mapa no viene de un archivo (editor)
    private static IllegalArgumentException errorMapa(int primeraLinea, int fila, String que) {
        return primeraLinea < 0 ? new IllegalArgumentException(que) : error(primeraLinea + fila, que);
    }

    private NivelCreado(Path archivo, String nombre, Tema tema, List<String> filas, int ancho, int primeraLinea,
                        boolean estricto) {
        super(0, nombre, tema, ancho, filas.size());
        this.archivo = archivo;
        boolean hayInicio = false, hayMeta = false;
        List<int[]> macetas = new ArrayList<>();   // se colocan al final, cuando el mapa está completo
        for (int f = 0; f < filas.size(); f++) {
            String fila = filas.get(f);
            for (int c = 0; c < ancho; c++) {
                char ch = c < fila.length() ? fila.charAt(c) : '.';
                if (ch == ' ') ch = '.';
                switch (ch) {
                    case '.': case '#': case 'S': case '^': case 'v': case '~': case 'u': case 'p':
                        mapa[f][c] = ch;
                        break;
                    case 'b':
                        brisa(c, f, c, f);
                        break;
                    case 'B':
                        mapa[f][c] = 'p';
                        brisa(c, f, c, f);
                        break;
                    case 'I':
                        if (hayInicio) {
                            if (!estricto) break;
                            throw errorMapa(primeraLinea, f, "hay dos 'I' (Coco solo puede empezar en un sitio)");
                        }
                        inicio(c, f + 1);
                        hayInicio = true;
                        break;
                    case 'M':
                        if (!hayMeta) meta(c, f + 1, 2, 3);
                        hayMeta = true;
                        break;
                    case 'F': farola(c, f + 1); break;
                    case 'm': decoracion(Decoracion.Tipo.MACETA, c, f + 1, (c + f) % 4); break;
                    case 'n': decoracion(Decoracion.Tipo.NARANJO, c, f + 1, 0); break;
                    case 'l': decoracion(Decoracion.Tipo.PALMERA, c, f + 1, 0); break;
                    case 'C': coleccionable(Coleccionable.Tipo.SALMOREJO, c, f); break;
                    case 'K': coleccionable(Coleccionable.Tipo.FLAMENQUIN, c, f); break;
                    case 'G': macetas.add(new int[] { c, f }); break;
                    default:
                        if (!estricto) break;
                        throw errorMapa(primeraLinea, f, "columna " + (c + 1) + ": no sé qué es '" + ch + "'");
                }
            }
        }
        if (estricto && !hayInicio) throw new IllegalArgumentException("falta Coco (la 'I'): pon dónde empieza");
        if (estricto && !hayMeta) throw new IllegalArgumentException("falta la meta (la 'M')");
        for (int[] m : macetas) {
            int suelo = m[1] + 1;
            while (suelo < filas.size() && !esSolido(m[0], suelo)) suelo++;
            obstaculos.add(new MacetaQueCae(m[0] * TILE + TILE / 2f, m[1] * TILE + 8, suelo * TILE - 6,
                    160, (m[0] * 37) % 160, m[0] % 4));
        }
    }

    public Path getArchivo() { return archivo; }
}
