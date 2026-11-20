package cordoba.interfaz;

import cordoba.audio.Sonido;
import cordoba.graficos.GameRenderer;
import cordoba.niveles.Level;
import cordoba.niveles.NivelCreado;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
 * Creador de niveles: la lista de niveles guardados en niveles_creados/.
 * JUGAR los juega (sin ranking ni fantasma), EDITAR y NUEVO abren el editor (EditorNiveles).
 */
public class PantallaNiveles {

    public static final Path CARPETA = Path.of("niveles_creados");

    public enum Accion { NINGUNA, JUGAR, EDITAR, NUEVO, VOLVER }

    private static final int PX = 60, PY = 14, PW = 520, PH = 332;
    private static final int LISTA_X = PX + 18, LISTA_Y = PY + 52, LISTA_W = PW - 36, FILA = 22, VISIBLES = 8;

    // lo que se sabe de cada archivo
    private static final class Entrada {
        final Path archivo;
        final long modificado;
        String nombre, tema, error;
        int columnas, filas;

        Entrada(Path archivo, long modificado) {
            this.archivo = archivo;
            this.modificado = modificado;
        }
    }

    private final GameRenderer renderer;
    private final Boton bJugar  = new Boton(PX + 16, PY + PH - 56, 116, 30, "JUGAR", Andalusi.BOTON_VERDE).grande();
    private final Boton bEditar = new Boton(PX + 140, PY + PH - 56, 116, 30, "EDITAR", Andalusi.BOTON_AZUL).grande();
    private final Boton bNuevo  = new Boton(PX + 264, PY + PH - 56, 116, 30, "NUEVO", Andalusi.BOTON_ALBERO).grande();
    private final Boton bVolver = new Boton(PX + 388, PY + PH - 56, 116, 30, "VOLVER", Andalusi.BOTON_GRANATE).grande();
    private final Boton[] botones = { bJugar, bEditar, bNuevo, bVolver };

    private final List<Entrada> entradas = new ArrayList<>();
    private int elegida;              // fila de la lista elegida (-1 si no hay)
    private int boton = 2;            // botón con el teclado (empieza en NUEVO si no hay niveles)
    private int scroll;
    private int ratonX = Integer.MIN_VALUE, ratonY = Integer.MIN_VALUE;
    private int frameLectura, ultimoClicFila = -1, frameUltimoClic, frame;
    private String aviso = "";        // mensaje de abajo (nivel completado, errores...)
    private Color colorAviso = Ui.GRIS;
    private NivelCreado elegido;

    public PantallaNiveles(GameRenderer renderer) {
        this.renderer = renderer;
    }

    public void abrir(Input in) {
        ratonX = in.getRatonX();
        ratonY = in.getRatonY();
        leer();
        boton = entradas.isEmpty() ? 2 : 0;
    }

    // mensaje tras jugar un nivel
    public void avisar(String texto, Color color) {
        aviso = texto;
        colorAviso = color;
    }

    // el nivel que se ha elegido con JUGAR
    public NivelCreado getElegido() { return elegido; }

    // el archivo elegido en la lista (para EDITAR)
    public Path getArchivoElegido() {
        return elegida >= 0 && elegida < entradas.size() ? entradas.get(elegida).archivo : null;
    }

    // vuelve a leer un nivel del disco (para REINICIAR); null si ahora tiene un error
    public NivelCreado recargar(Path archivo) {
        try {
            return NivelCreado.cargar(archivo);
        } catch (IOException | IllegalArgumentException e) {
            avisar(archivo.getFileName() + ": " + e.getMessage(), Ui.ROJO);
            return null;
        }
    }

    // que la lista se quede en este archivo (al volver del editor)
    public void elegir(Path archivo) {
        leer();
        for (int i = 0; i < entradas.size(); i++) if (entradas.get(i).archivo.equals(archivo)) elegida = i;
        ajustarScroll();
    }

    // ---- lectura de la carpeta ----

    private void leer() {
        Path antes = getArchivoElegido();
        List<Entrada> nuevas = new ArrayList<>();
        if (Files.isDirectory(CARPETA)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(CARPETA, "*.txt")) {
                for (Path p : ds) nuevas.add(new Entrada(p, Files.getLastModifiedTime(p).toMillis()));
            } catch (IOException e) {
                avisar("No se pudo leer la carpeta: " + e.getMessage(), Ui.ROJO);
            }
        }
        nuevas.sort((a, b) -> a.archivo.getFileName().toString().compareToIgnoreCase(b.archivo.getFileName().toString()));
        for (Entrada e : nuevas) {
            // si no ha cambiado, se aprovecha lo que ya se sabía
            Entrada vieja = buscar(e.archivo);
            if (vieja != null && vieja.modificado == e.modificado) {
                e.nombre = vieja.nombre; e.tema = vieja.tema; e.error = vieja.error;
                e.columnas = vieja.columnas; e.filas = vieja.filas;
                continue;
            }
            try {
                NivelCreado n = NivelCreado.cargar(e.archivo);
                e.nombre = n.getNombre();
                e.tema = n.getTema().name();
                e.columnas = n.getColumnas();
                e.filas = n.getFilas();
            } catch (IOException | IllegalArgumentException ex) {
                e.nombre = e.archivo.getFileName().toString();
                e.error = ex.getMessage();
            }
        }
        entradas.clear();
        entradas.addAll(nuevas);
        elegida = entradas.isEmpty() ? -1 : 0;
        for (int i = 0; i < entradas.size(); i++) if (entradas.get(i).archivo.equals(antes)) elegida = i;
        ajustarScroll();
        frameLectura = 0;
    }

    private Entrada buscar(Path p) {
        for (Entrada e : entradas) if (e.archivo.equals(p)) return e;
        return null;
    }

    private void ajustarScroll() {
        if (elegida >= 0) {
            if (elegida < scroll) scroll = elegida;
            if (elegida >= scroll + VISIBLES) scroll = elegida - VISIBLES + 1;
        }
        scroll = Math.max(0, Math.min(scroll, entradas.size() - VISIBLES));
    }

    // ---- actualizar ----

    public Accion actualizar(Input in) {
        frame++;
        if (++frameLectura >= 60) leer();   // por si ha cambiado algo en la carpeta
        if (in.pulsada(KeyEvent.VK_ESCAPE)) { Sonido.clic(); return Accion.VOLVER; }
        boolean hay = elegida >= 0;
        bJugar.activo = hay && entradas.get(elegida).error == null;
        bEditar.activo = hay;

        // ↑/↓ eligen nivel, ←/→ eligen botón
        boolean arriba = in.pulsada(KeyEvent.VK_UP) || in.pulsada(KeyEvent.VK_W);
        boolean abajo = in.pulsada(KeyEvent.VK_DOWN) || in.pulsada(KeyEvent.VK_S);
        boolean izq = in.pulsada(KeyEvent.VK_LEFT) || in.pulsada(KeyEvent.VK_A);
        boolean der = in.pulsada(KeyEvent.VK_RIGHT) || in.pulsada(KeyEvent.VK_D);
        if (arriba != abajo && hay) {
            elegida = Math.floorMod(elegida + (arriba ? -1 : 1), entradas.size());
            Sonido.clic();
        }
        if (izq != der) {
            boton = Math.floorMod(boton + (izq ? -1 : 1), botones.length);
            Sonido.clic();
        }
        int rueda = in.getRueda();
        if (rueda != 0 && hay) elegida = Math.max(0, Math.min(entradas.size() - 1, elegida + rueda));

        int mx = in.getRatonX(), my = in.getRatonY();
        boolean seMovio = mx != ratonX || my != ratonY;
        ratonX = mx;
        ratonY = my;
        if (seMovio) for (int i = 0; i < botones.length; i++) if (botones[i].contiene(mx, my)) boton = i;
        ajustarScroll();

        if (in.clic()) {
            int fila = filaEn(mx, my);
            if (fila >= 0) {
                // doble clic = jugar
                boolean doble = fila == ultimoClicFila && frame - frameUltimoClic < 25;
                elegida = fila;
                ultimoClicFila = fila;
                frameUltimoClic = frame;
                Sonido.clic();
                if (doble && entradas.get(fila).error == null) return activar(0);
            }
            for (int i = 0; i < botones.length; i++) if (botones[i].clicado(in)) return activar(i);
        }
        if (in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_SPACE)) return activar(boton);
        if (in.pulsada(KeyEvent.VK_E) && hay) return activar(1);
        if (in.pulsada(KeyEvent.VK_N)) return activar(2);
        return Accion.NINGUNA;
    }

    private int filaEn(int mx, int my) {
        if (mx < LISTA_X || mx >= LISTA_X + LISTA_W || my < LISTA_Y) return -1;
        int i = (my - LISTA_Y) / FILA;
        if (i >= VISIBLES || scroll + i >= entradas.size()) return -1;
        return scroll + i;
    }

    private Accion activar(int b) {
        boton = b;
        if (!botones[b].activo) return Accion.NINGUNA;
        Sonido.clic();
        switch (b) {
            case 0: {
                NivelCreado nivel = recargar(entradas.get(elegida).archivo);   // la versión que hay en el disco
                if (nivel == null) { leer(); return Accion.NINGUNA; }
                elegido = nivel;
                aviso = "";
                return Accion.JUGAR;
            }
            case 1:  aviso = ""; return Accion.EDITAR;
            case 2:  aviso = ""; return Accion.NUEVO;
            default: return Accion.VOLVER;
        }
    }

    // ---- dibujar ----

    public void dibujar(Graphics2D g, int frame) {
        renderer.dibujarFondo(g, Level.Tema.CALLEJA, frame / 2, 0, 368, false);
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, 640, 360);
        Andalusi.marco(g, PX, PY, PW, PH, Ui.PANEL, true);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "CREADOR DE NIVELES", 320, PY + 38, Andalusi.ORO_CLARO);

        g.setFont(Ui.FUENTE);
        int n = entradas.size();
        if (n == 0) {
            Ui.textoCentrado(g, "Todavía no has creado ningún nivel", 320, LISTA_Y + 50, Ui.GRIS);
            Ui.textoCentrado(g, "Pulsa NUEVO y constrúyelo con el ratón", 320, LISTA_Y + 70, Ui.TEXTO);
        }
        for (int i = 0; i < VISIBLES && scroll + i < n; i++) {
            Entrada e = entradas.get(scroll + i);
            int y = LISTA_Y + i * FILA;
            boolean sel = scroll + i == elegida;
            if (sel) {
                g.setColor(new Color(255, 220, 140, 34));
                g.fillRect(LISTA_X, y, LISTA_W, FILA - 2);
                g.setColor(Andalusi.ORO);
                g.fillRect(LISTA_X, y, 2, FILA - 2);
            }
            int base = Ui.baseCentrada(g, y, FILA - 2);
            if (e.error == null) {
                String derecha = e.tema;
                int wd = g.getFontMetrics().stringWidth(derecha);
                Ui.texto(g, derecha, LISTA_X + LISTA_W - 8 - wd, base, Ui.GRIS);
                Ui.texto(g, Ui.recortar(g, e.nombre, LISTA_W - 30 - wd), LISTA_X + 10, base, sel ? Andalusi.ORO_CLARO : Ui.TEXTO);
            } else {
                Ui.texto(g, Ui.recortar(g, e.nombre + "  —  sin terminar", LISTA_W - 18), LISTA_X + 10, base,
                        sel ? new Color(0xFF9A8A) : Ui.ROJO);
            }
        }
        // flechas si hay más arriba o abajo
        if (scroll > 0) Ui.textoCentrado(g, "▲", PX + PW - 12, LISTA_Y + 10, Andalusi.ORO);
        if (scroll + VISIBLES < n) Ui.textoCentrado(g, "▼", PX + PW - 12, LISTA_Y + VISIBLES * FILA - 4, Andalusi.ORO);

        // línea de aviso (o qué le falta al nivel elegido)
        String linea = aviso;
        Color c = colorAviso;
        if (elegida >= 0 && entradas.get(elegida).error != null) {
            linea = "Le falta algo: " + entradas.get(elegida).error + ". Pulsa EDITAR para arreglarlo.";
            c = Ui.ROJO;
        }
        if (!linea.isEmpty()) {
            List<String> lineas = Ui.ajustar(g, linea, PW - 40);
            for (int i = 0; i < Math.min(2, lineas.size()); i++)
                Ui.textoCentrado(g, lineas.get(i), 320, LISTA_Y + VISIBLES * FILA + 10 + i * 13, c);
        }

        for (int i = 0; i < botones.length; i++) botones[i].dibujar(g, i == boton);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "↑/↓ nivel · ←/→ botón · doble clic: jugar · E: editar · N: nuevo · ESC: volver", 320, PY + PH - 10, Ui.GRIS);
    }
}
