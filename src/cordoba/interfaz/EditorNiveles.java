package cordoba.interfaz;

import cordoba.audio.Sonido;
import cordoba.entidades.Player;
import cordoba.graficos.GameRenderer;
import cordoba.graficos.Sprites;
import cordoba.niveles.Level;
import cordoba.niveles.NivelCreado;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/*
 * Creador de niveles con el ratón: arriba la barra (nombre, fondo, tamaño, probar, guardar, salir),
 * en medio el nivel tal y como se verá jugando, y abajo la paleta con las piezas.
 *
 *   clic izquierdo (y arrastrar) = poner la pieza elegida · clic derecho = borrar
 *   rueda o A/D/←/→ = moverse a los lados · W/S/↑/↓ = arriba y abajo
 *   Q/E o 1..9 = elegir pieza · CTRL+Z = deshacer · G = cuadrícula · ESC = salir (guarda solo)
 *
 * Se guarda en niveles_creados/ con el mismo formato de texto que lee NivelCreado.
 */
public class EditorNiveles {

    public enum Accion { NINGUNA, PROBAR, SALIR }

    private static final int T = Level.TILE;
    private static final int BARRA = 24;                     // alto de la barra de arriba
    private static final int PALETA_Y = 322;                 // donde empieza la paleta
    private static final int VISTA_ALTO = PALETA_Y - BARRA;  // alto del trozo de nivel que se ve
    private static final int MIN_ANCHO = 40, MIN_ALTO = 23;
    private static final int PASO_TAMANO = 5;

    // piezas de la paleta: carácter del mapa y nombre
    private static final char[] PIEZAS = { '.', '#', 'S', '^', 'v', '~', 'u', 'p', 'b',
                                           'I', 'M', 'F', 'C', 'K', 'G', 'm', 'n', 'l' };
    private static final String[] NOMBRES = {
        "Goma (el clic derecho también borra)", "Suelo", "Pared", "Pinchos", "Pinchos en el techo", "Agua",
        "Corriente de aire (empuja hacia arriba)", "Fondo de piedra (no choca)", "Brisa (frena la caída)",
        "Coco: dónde empieza (solo uno)", "Meta (solo una)", "Farola: punto de control", "Salmorejo: resta 3 s",
        "Flamenquín: resta 3 s", "Maceta que cae", "Maceta", "Naranjo", "Palmera" };
    private static final int CASILLA_PALETA = 32, PALETA_X = (640 - PIEZAS.length * CASILLA_PALETA) / 2;

    private final GameRenderer renderer;

    // barra de arriba
    private final Boton bFondo    = boton(154, "FONDO", 62, Andalusi.BOTON_AZUL);
    private final Boton bAnchoMen = boton(238, "-", 14, Andalusi.BOTON_ALBERO);
    private final Boton bAnchoMas = boton(280, "+", 14, Andalusi.BOTON_ALBERO);
    private final Boton bAltoMen  = boton(314, "-", 14, Andalusi.BOTON_ALBERO);
    private final Boton bAltoMas  = boton(356, "+", 14, Andalusi.BOTON_ALBERO);
    private final Boton bDeshacer = boton(376, "DESHACER", 66, Andalusi.BOTON_ALBERO);
    private final Boton bProbar   = boton(446, "PROBAR", 56, Andalusi.BOTON_VERDE);
    private final Boton bGuardar  = boton(506, "GUARDAR", 62, Andalusi.BOTON_AZUL);
    private final Boton bSalir    = boton(572, "SALIR", 64, Andalusi.BOTON_GRANATE);
    private final Boton[] botones = { bFondo, bAnchoMen, bAnchoMas, bAltoMen, bAltoMas, bDeshacer, bProbar, bGuardar, bSalir };
    private static final Rectangle CAJA_NOMBRE = new Rectangle(4, 3, 146, 18);

    // ventana para elegir el fondo y el estilo de los bloques
    private static final int SEL_X = 70, SEL_Y = 30, SEL_W = 500, SEL_H = 270;
    private static final int MINI_W = 148, MINI_H = 83, MINI_Y = SEL_Y + 44, BLOQ_Y = SEL_Y + 172, BLOQ_H = 44;
    private static final String[] NOMBRES_TEMA = { "Río Guadalquivir", "Mezquita", "Calleja de las Flores" };   // orden de Level.Tema
    private final Boton bListo = new Boton(260, SEL_Y + SEL_H - 40, 120, 28, "LISTO", Andalusi.BOTON_VERDE).grande();
    private final BufferedImage[] miniaturas = new BufferedImage[Level.Tema.values().length];
    private boolean eligiendo;      // la ventana de fondo está abierta
    private boolean primeraEleccion;   // nivel nuevo: el fondo elegido también pone los bloques a juego
    private boolean eleccionNueva;     // la ventana se abrió al crear el nivel (cambia el título)

    // el nivel que se está haciendo
    private char[][] mapa;          // [fila][columna]
    private int ancho, alto;
    private String nombre;
    private Level.Tema tema;        // estilo de los bloques
    private Level.Tema fondo;       // paisaje de detrás
    private Path archivo;           // null hasta que se guarda la primera vez
    private boolean cambiado;       // hay cambios sin guardar

    // lo que se pinta (se rehace al cambiar algo)
    private NivelCreado vista;
    private Player coco;

    private float camX, camY;
    private int pieza = 1;
    private boolean cuadricula = true;
    private boolean escribiendoNombre;
    private boolean trazo;          // hay un trazo de ratón en marcha (para deshacer de golpe)
    private final Deque<char[][]> deshacer = new ArrayDeque<>();
    private String aviso = "";
    private Color colorAviso = Ui.TEXTO;
    private int timerAviso;
    private int frame;
    private NivelCreado prueba;

    public EditorNiveles(GameRenderer renderer) {
        this.renderer = renderer;
    }

    private static Boton boton(int x, String texto, int w, Color color) {
        return new Boton(x, 3, w, 18, texto, color).andalusi();
    }

    // ---- abrir ----

    // nivel nuevo: suelo abajo, Coco a la izquierda y la meta a la derecha
    public void nuevo() {
        ancho = 60;
        alto = MIN_ALTO;
        mapa = vacio(ancho, alto);
        for (int c = 0; c < ancho; c++) {
            mapa[alto - 2][c] = '#';
            mapa[alto - 1][c] = '#';
        }
        mapa[alto - 3][3] = 'I';
        mapa[alto - 3][ancho - 6] = 'M';
        nombre = "Mi nivel";
        tema = Level.Tema.CALLEJA;
        fondo = tema;
        archivo = null;
        empezar();
        cambiado = true;
        avisar("Elige una pieza abajo y pinta con el ratón · clic derecho borra · rueda o WASD para moverte", Ui.TEXTO);
        timerAviso = 600;
        // lo primero, elegir dónde ocurre el nivel
        eligiendo = true;
        primeraEleccion = true;
        eleccionNueva = true;
    }

    // abre un nivel guardado (aunque tenga errores: así se pueden arreglar)
    public void abrir(Path p) throws IOException {
        NivelCreado.Datos d = NivelCreado.leer(p);
        alto = Math.max(MIN_ALTO, d.filas.size());
        ancho = MIN_ANCHO;
        for (String f : d.filas) ancho = Math.max(ancho, f.length());
        ancho = Math.min(ancho, NivelCreado.MAX_ANCHO);
        alto = Math.min(alto, NivelCreado.MAX_ALTO);
        mapa = vacio(ancho, alto);
        int arriba = alto - Math.min(alto, d.filas.size());   // filas que faltan: cielo por arriba
        for (int f = 0; f < d.filas.size() && arriba + f < alto; f++) {
            String fila = d.filas.get(f);
            for (int c = 0; c < Math.min(ancho, fila.length()); c++)
                mapa[arriba + f][c] = fila.charAt(c) == ' ' ? '.' : fila.charAt(c);
        }
        nombre = d.nombre;
        tema = d.tema;
        fondo = d.fondo != null ? d.fondo : d.tema;
        archivo = p;
        eligiendo = false;
        empezar();
        cambiado = false;
        avisar("Editando " + p.getFileName(), Ui.TEXTO);
    }

    private void empezar() {
        deshacer.clear();
        escribiendoNombre = false;
        trazo = false;
        reconstruir();
        // la cámara empieza donde está Coco (o abajo a la izquierda)
        int[] i = buscar('I');
        camX = i == null ? 0 : i[0] * T - 200;
        camY = alto * T;
        limitarCamara();
    }

    private static char[][] vacio(int ancho, int alto) {
        char[][] m = new char[alto][ancho];
        for (char[] f : m) java.util.Arrays.fill(f, '.');
        return m;
    }

    public void avisar(String texto, Color color) {
        aviso = texto;
        colorAviso = color;
        timerAviso = 240;
    }

    // el nivel listo para jugar (después de PROBAR)
    public NivelCreado getPrueba() { return prueba; }

    public Level.Tema getTema() { return tema; }

    public Level.Tema getFondo() { return fondo; }

    public Path getArchivo() { return archivo; }

    // ---- actualizar ----

    public Accion actualizar(Input in) {
        frame++;
        if (timerAviso > 0) timerAviso--;
        int mx = in.getRatonX(), my = in.getRatonY();

        if (eligiendo) {
            actualizarEleccion(in, mx, my);
            return Accion.NINGUNA;
        }

        if (escribiendoNombre) {
            escribirNombre(in);
        } else {
            if (in.pulsada(KeyEvent.VK_ESCAPE)) return salir();
            teclas(in);
        }

        // barra de arriba
        if (in.clic()) {
            if (CAJA_NOMBRE.contains(mx, my)) {
                escribiendoNombre = true;
                Sonido.clic();
            } else {
                escribiendoNombre = false;
            }
            if (bFondo.clicado(in)) {
                eligiendo = true;
                primeraEleccion = false;
                eleccionNueva = false;
                Sonido.clic();
                return Accion.NINGUNA;
            }
            if (bAnchoMen.clicado(in)) cambiarTamano(-PASO_TAMANO, 0);
            if (bAnchoMas.clicado(in)) cambiarTamano(PASO_TAMANO, 0);
            if (bAltoMen.clicado(in)) cambiarTamano(0, -PASO_TAMANO);
            if (bAltoMas.clicado(in)) cambiarTamano(0, PASO_TAMANO);
            if (bDeshacer.clicado(in)) deshacer();
            if (bGuardar.clicado(in)) guardar(true);
            if (bProbar.clicado(in)) return probar();
            if (bSalir.clicado(in)) return salir();
            // paleta
            int p = piezaEn(mx, my);
            if (p >= 0) {
                pieza = p;
                Sonido.clic();
            }
        }

        // pintar y borrar en el nivel
        boolean enVista = my >= BARRA && my < PALETA_Y && mx >= 0 && mx < 640;
        boolean pinta = in.botonPulsado() && enVista, borra = in.derechoPulsado() && enVista;
        if (pinta || borra) {
            int c = (int) Math.floor((mx + camX) / T), f = (int) Math.floor((my - BARRA + camY) / T);
            if (c >= 0 && c < ancho && f >= 0 && f < alto) {
                if (!trazo) {
                    guardarDeshacer();
                    trazo = true;
                }
                poner(c, f, borra ? '.' : PIEZAS[pieza]);
            }
        } else if (!in.botonPulsado() && !in.derechoPulsado()) {
            trazo = false;
        }

        // rueda: a los lados (con MAYÚS, arriba y abajo)
        int r = in.getRueda();
        if (r != 0) {
            if (in.abajo(KeyEvent.VK_SHIFT)) camY += r * 3 * T;
            else camX += r * 3 * T;
        }
        limitarCamara();
        return Accion.NINGUNA;
    }

    private void teclas(Input in) {
        boolean ctrl = in.abajo(KeyEvent.VK_CONTROL);
        if (ctrl && in.pulsada(KeyEvent.VK_Z)) deshacer();
        if (ctrl && in.pulsada(KeyEvent.VK_S)) guardar(true);
        if (ctrl) return;
        float vel = in.abajo(KeyEvent.VK_SHIFT) ? 12 : 5;
        if (in.abajo(KeyEvent.VK_A) || in.abajo(KeyEvent.VK_LEFT)) camX -= vel;
        if (in.abajo(KeyEvent.VK_D) || in.abajo(KeyEvent.VK_RIGHT)) camX += vel;
        if (in.abajo(KeyEvent.VK_W) || in.abajo(KeyEvent.VK_UP)) camY -= vel;
        if (in.abajo(KeyEvent.VK_S) || in.abajo(KeyEvent.VK_DOWN)) camY += vel;
        if (in.pulsada(KeyEvent.VK_Q)) { pieza = Math.floorMod(pieza - 1, PIEZAS.length); Sonido.clic(); }
        if (in.pulsada(KeyEvent.VK_E)) { pieza = Math.floorMod(pieza + 1, PIEZAS.length); Sonido.clic(); }
        if (in.pulsada(KeyEvent.VK_G)) cuadricula = !cuadricula;
        for (int k = 0; k <= 9; k++)
            if (in.pulsada(KeyEvent.VK_0 + k)) { pieza = k; Sonido.clic(); }
    }

    private void escribirNombre(Input in) {
        String s = in.tomarEscrito();
        StringBuilder sb = new StringBuilder(nombre);
        for (char ch : s.toCharArray()) {
            if (ch == '\b') {
                if (sb.length() > 0) sb.setLength(sb.length() - 1);
            } else if (ch == '\n' || ch == '\r' || ch == 27) {
                escribiendoNombre = false;
            } else if (ch >= ' ' && sb.length() < 28) {
                sb.append(ch);
            }
        }
        if (in.pulsada(KeyEvent.VK_ESCAPE) || in.pulsada(KeyEvent.VK_ENTER)) escribiendoNombre = false;
        if (!sb.toString().equals(nombre)) {
            nombre = sb.toString();
            cambiado = true;
        }
    }

    private int piezaEn(int mx, int my) {
        if (my < PALETA_Y + 4 || my >= PALETA_Y + 4 + CASILLA_PALETA || mx < PALETA_X) return -1;
        int i = (mx - PALETA_X) / CASILLA_PALETA;
        return i < PIEZAS.length ? i : -1;
    }

    private void limitarCamara() {
        float maxX = Math.max(0, ancho * T - 640 + 32), maxY = Math.max(0, alto * T - VISTA_ALTO);
        camX = Math.max(-32, Math.min(camX, maxX));
        camY = Math.max(0, Math.min(camY, maxY));
    }

    // ---- cambiar el mapa ----

    private void poner(int c, int f, char ch) {
        if (mapa[f][c] == ch) return;
        // Coco y la meta solo pueden estar una vez: se quitan de donde estuvieran
        if (ch == 'I' || ch == 'M') {
            int[] antes = buscar(ch);
            if (antes != null) mapa[antes[1]][antes[0]] = '.';
        }
        mapa[f][c] = ch;
        cambiado = true;
        reconstruir();
    }

    private int[] buscar(char ch) {
        for (int f = 0; f < alto; f++)
            for (int c = 0; c < ancho; c++)
                if (mapa[f][c] == ch) return new int[] { c, f };
        return null;
    }

    private void guardarDeshacer() {
        char[][] copia = new char[alto][];
        for (int f = 0; f < alto; f++) copia[f] = mapa[f].clone();
        deshacer.push(copia);
        while (deshacer.size() > 60) deshacer.removeLast();
    }

    private void deshacer() {
        if (deshacer.isEmpty()) {
            avisar("No hay nada que deshacer", Ui.GRIS);
            return;
        }
        mapa = deshacer.pop();
        alto = mapa.length;
        ancho = mapa[0].length;
        cambiado = true;
        Sonido.clic();
        reconstruir();
    }

    // ---- ventana de fondo ----

    private Rectangle cajaMiniatura(int i) {
        return new Rectangle(SEL_X + 16 + i * (MINI_W + 10), MINI_Y, MINI_W, MINI_H);
    }

    private Rectangle cajaBloques(int i) {
        return new Rectangle(SEL_X + 16 + i * (MINI_W + 10), BLOQ_Y, MINI_W, BLOQ_H);
    }

    private void actualizarEleccion(Input in, int mx, int my) {
        Level.Tema[] temas = Level.Tema.values();
        if (in.pulsada(KeyEvent.VK_ESCAPE) || in.pulsada(KeyEvent.VK_ENTER) || bListo.clicado(in)) {
            eligiendo = false;
            Sonido.clic();
            return;
        }
        // ←/→ cambian el fondo
        boolean izq = in.pulsada(KeyEvent.VK_LEFT) || in.pulsada(KeyEvent.VK_A);
        boolean der = in.pulsada(KeyEvent.VK_RIGHT) || in.pulsada(KeyEvent.VK_D);
        if (izq != der) elegirFondo(temas[Math.floorMod(fondo.ordinal() + (izq ? -1 : 1), temas.length)]);
        if (!in.clic()) return;
        for (int i = 0; i < temas.length; i++) {
            if (cajaMiniatura(i).contains(mx, my)) elegirFondo(temas[i]);
            if (cajaBloques(i).contains(mx, my) && tema != temas[i]) {
                tema = temas[i];
                primeraEleccion = false;   // ya ha elegido bloques a mano
                cambiado = true;
                Sonido.clic();
                reconstruir();
            }
        }
    }

    private void elegirFondo(Level.Tema t) {
        if (fondo == t && !(primeraEleccion && tema != t)) return;
        fondo = t;
        if (primeraEleccion) tema = t;   // en un nivel nuevo, bloques a juego con el fondo
        cambiado = true;
        Sonido.clic();
        reconstruir();
    }

    // el fondo en pequeño (se pinta una vez y se guarda)
    private BufferedImage miniatura(Level.Tema t) {
        BufferedImage m = miniaturas[t.ordinal()];
        if (m != null) return m;
        BufferedImage grande = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = grande.createGraphics();
        renderer.dibujarFondo(g, t, 0, 0, 368);
        g.dispose();
        m = new BufferedImage(MINI_W, MINI_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D gm = m.createGraphics();
        gm.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gm.drawImage(grande, 0, 0, MINI_W, MINI_H, null);
        gm.dispose();
        miniaturas[t.ordinal()] = m;
        return m;
    }

    private void dibujarEleccion(Graphics2D g, int mx, int my) {
        Level.Tema[] temas = Level.Tema.values();
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, 640, 360);
        Andalusi.marco(g, SEL_X, SEL_Y, SEL_W, SEL_H, Ui.PANEL, true);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, eleccionNueva ? "¿DÓNDE OCURRE TU NIVEL?" : "FONDO", 320, SEL_Y + 34, Andalusi.ORO_CLARO);
        g.setFont(Ui.FUENTE);
        for (int i = 0; i < temas.length; i++) {
            Rectangle r = cajaMiniatura(i);
            boolean sel = fondo == temas[i], encima = r.contains(mx, my);
            g.drawImage(miniatura(temas[i]), r.x, r.y, null);
            marcoEleccion(g, r, sel, encima);
            Ui.textoCentrado(g, NOMBRES_TEMA[i], r.x + r.width / 2, r.y + r.height + 15, sel ? Andalusi.ORO_CLARO : Andalusi.CREMA);
        }
        Ui.textoCentrado(g, "Bloques", 320, BLOQ_Y - 8, Ui.GRIS);
        for (int i = 0; i < temas.length; i++) {
            Rectangle r = cajaBloques(i);
            boolean sel = tema == temas[i], encima = r.contains(mx, my);
            g.setColor(sel ? new Color(0x4A3020) : encima ? new Color(0x3A2618) : new Color(0x2A1C12));
            g.fillRect(r.x, r.y, r.width, r.height);
            // una muestra: suelo, pared y pinchos de ese estilo
            int tx = r.x + 8, ty = r.y + 6;
            g.drawImage(Sprites.tile(temas[i], '#', 0, true, false), tx, ty, 32, 32, null);
            g.drawImage(Sprites.tile(temas[i], 'S', 0, true, false), tx + 32, ty, 32, 32, null);
            Ui.texto(g, NOMBRES_TEMA[i].split(" ")[0], tx + 72, r.y + r.height / 2 + 5, sel ? Andalusi.ORO_CLARO : Andalusi.CREMA);
            marcoEleccion(g, r, sel, encima);
        }
        bListo.dibujar(g, bListo.contiene(mx, my));
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "clic para elegir · ←/→ fondo · ENTER listo", 320, SEL_Y + SEL_H - 4, Ui.GRIS);
    }

    private static void marcoEleccion(Graphics2D g, Rectangle r, boolean sel, boolean encima) {
        g.setColor(sel ? Andalusi.ORO_CLARO : encima ? Andalusi.ORO : Andalusi.ORO_OSCURO);
        g.drawRect(r.x - 1, r.y - 1, r.width + 1, r.height + 1);
        if (sel) {
            g.drawRect(r.x - 2, r.y - 2, r.width + 3, r.height + 3);
            g.drawRect(r.x - 3, r.y - 3, r.width + 5, r.height + 5);
        }
    }

    // más ancho = columnas a la derecha; más alto = filas por arriba (el suelo se queda donde está)
    private void cambiarTamano(int dAncho, int dAlto) {
        int nAncho = Math.max(MIN_ANCHO, Math.min(NivelCreado.MAX_ANCHO, ancho + dAncho));
        int nAlto = Math.max(MIN_ALTO, Math.min(NivelCreado.MAX_ALTO, alto + dAlto));
        if (nAncho == ancho && nAlto == alto) {
            avisar("Ese es el tamaño " + (dAncho + dAlto < 0 ? "mínimo" : "máximo"), Ui.GRIS);
            return;
        }
        guardarDeshacer();
        char[][] nuevo = vacio(nAncho, nAlto);
        for (int f = 0; f < nAlto; f++) {
            int fv = f - (nAlto - alto);
            if (fv < 0 || fv >= alto) continue;
            System.arraycopy(mapa[fv], 0, nuevo[f], 0, Math.min(ancho, nAncho));
        }
        if (nAlto > alto) camY += (nAlto - alto) * T;   // que la vista no salte
        mapa = nuevo;
        ancho = nAncho;
        alto = nAlto;
        cambiado = true;
        Sonido.clic();
        reconstruir();
    }

    private List<String> filas() {
        List<String> l = new ArrayList<>(alto);
        for (char[] f : mapa) l.add(new String(f));
        return l;
    }

    private void reconstruir() {
        vista = NivelCreado.construir(archivo, nombre, tema, fondo, filas(), false);
        int[] i = buscar('I');
        if (i == null) {
            coco = null;
        } else {
            coco = new Player(vista.getInicioX(), vista.getInicioY());
            coco.enSuelo = true;
        }
    }

    // ---- guardar, probar y salir ----

    private boolean guardar(boolean avisarlo) {
        try {
            Files.createDirectories(PantallaNiveles.CARPETA);
            if (archivo == null) archivo = archivoLibre(nombre);
            Files.writeString(archivo, NivelCreado.aTexto(nombre.isBlank() ? "Sin nombre" : nombre, tema, fondo, filas()),
                    StandardCharsets.UTF_8);
            cambiado = false;
            if (avisarlo) {
                avisar("Guardado en " + PantallaNiveles.CARPETA.resolve(archivo.getFileName()), Ui.VERDE);
                Sonido.clic();
            }
            return true;
        } catch (IOException e) {
            avisar("No se pudo guardar: " + e.getMessage(), Ui.ROJO);
            return false;
        }
    }

    // nombre de archivo a partir del nombre del nivel: "El patio de Lucía" -> el_patio_de_lucia.txt
    private static Path archivoLibre(String nombre) {
        String base = Normalizer.normalize(nombre.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "mi_nivel";
        Path p = PantallaNiveles.CARPETA.resolve(base + ".txt");
        for (int i = 2; Files.exists(p); i++) p = PantallaNiveles.CARPETA.resolve(base + "_" + i + ".txt");
        return p;
    }

    private Accion probar() {
        Sonido.clic();
        try {
            prueba = NivelCreado.construir(archivo, nombre, tema, fondo, filas(), true);
        } catch (IllegalArgumentException e) {
            avisar("No se puede probar: " + e.getMessage(), Ui.ROJO);
            return Accion.NINGUNA;
        }
        if (cambiado) guardar(false);
        return Accion.PROBAR;
    }

    private Accion salir() {
        Sonido.clic();
        if (cambiado && !guardar(false)) return Accion.NINGUNA;   // si no se puede guardar, no se pierde nada
        return Accion.SALIR;
    }

    // ---- dibujar ----

    public void dibujar(Graphics2D g, Input in, int frameGlobal) {
        int mx = in.getRatonX(), my = in.getRatonY();
        int cx = Math.round(camX), cy = Math.round(camY);

        // el nivel, entre la barra y la paleta
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 640, 360);
        Shape clip = g.getClip();
        g.clipRect(0, BARRA, 640, VISTA_ALTO);
        g.translate(0, BARRA);
        renderer.dibujarVistaEditor(g, vista, coco, cx, cy, frameGlobal);
        dibujarExtras(g, cx, cy);
        if (cuadricula) dibujarCuadricula(g, cx, cy);
        dibujarCursor(g, in, mx, my - BARRA, cx, cy);
        g.translate(0, -BARRA);
        g.setClip(clip);

        dibujarBarra(g, in, mx, my);
        dibujarPaleta(g, mx, my);
        if (eligiendo) {
            dibujarEleccion(g, mx, my);
            return;
        }

        // aviso encima de la paleta
        String texto = timerAviso > 0 ? aviso : "";
        int hover = piezaEn(mx, my);
        String ayuda = ayudaBarra(mx, my);
        if (hover >= 0) texto = NOMBRES[hover] + (hover <= 9 ? "  [" + hover + "]" : "");
        else if (ayuda != null) texto = ayuda;
        boolean tooltip = hover >= 0 || ayuda != null;
        if (!texto.isEmpty()) {
            g.setFont(Ui.FUENTE);
            String t = Ui.recortar(g, texto, 600);
            int w = g.getFontMetrics().stringWidth(t) + 16;
            g.setColor(new Color(20, 12, 8, 210));
            g.fillRect(320 - w / 2, PALETA_Y - 20, w, 17);
            Ui.textoCentrado(g, t, 320, PALETA_Y - 7, tooltip ? Andalusi.ORO_CLARO : colorAviso);
        }
    }

    // lo que la vista no pinta: macetas que caen y letras que el juego no conoce
    private void dibujarExtras(Graphics2D g, int cx, int cy) {
        int c1 = Math.max(0, cx / T), c2 = Math.min(ancho - 1, (cx + 640) / T);
        int f1 = Math.max(0, cy / T), f2 = Math.min(alto - 1, (cy + VISTA_ALTO) / T);
        for (int f = f1; f <= f2; f++) {
            for (int c = c1; c <= c2; c++) {
                char ch = mapa[f][c];
                int x = c * T - cx, y = f * T - cy;
                if (ch == 'G') {
                    icono(g, 'G', x, y, T, tema);
                    // por dónde caerá
                    g.setColor(new Color(255, 90, 70, 70));
                    int suelo = f + 1;
                    while (suelo < alto && !vista.esSolido(c, suelo)) suelo++;
                    g.fillRect(x + 7, y + T, 2, (suelo - f - 1) * T);
                } else if ("I.#S^v~upbBMFmnlCK".indexOf(ch) < 0) {
                    g.setColor(new Color(255, 60, 60, 120));
                    g.fillRect(x, y, T, T);
                    g.setFont(Ui.FUENTE);
                    Ui.textoCentrado(g, String.valueOf(ch), x + 8, y + 12, Color.WHITE);
                }
            }
        }
    }

    private void dibujarCuadricula(Graphics2D g, int cx, int cy) {
        g.setColor(new Color(255, 255, 255, 22));
        for (int c = Math.max(0, cx / T); c <= Math.min(ancho, (cx + 640) / T + 1); c++)
            g.fillRect(c * T - cx, Math.max(0, -cy), 1, Math.min(alto * T, VISTA_ALTO));
        for (int f = Math.max(0, cy / T); f <= Math.min(alto, (cy + VISTA_ALTO) / T + 1); f++)
            g.fillRect(Math.max(0, -cx), f * T - cy, Math.min(ancho * T - cx, 640), 1);
        // borde del nivel
        g.setColor(new Color(255, 220, 120, 160));
        g.drawRect(-cx, -cy, ancho * T, alto * T);
        // a la izquierda del nivel: oscuro
        if (cx < 0) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, -cx, VISTA_ALTO);
        }
        if (ancho * T - cx < 640) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(ancho * T - cx, 0, 640, VISTA_ALTO);
        }
    }

    // la pieza en transparencia donde se va a poner
    private void dibujarCursor(Graphics2D g, Input in, int mx, int my, int cx, int cy) {
        if (my < 0 || my >= VISTA_ALTO || escribiendoNombre) return;
        int c = (int) Math.floor((mx + cx) / (float) T), f = (int) Math.floor((my + cy) / (float) T);
        if (c < 0 || c >= ancho || f < 0 || f >= alto) return;
        int x = c * T - cx, y = f * T - cy;
        char ch = in.derechoPulsado() ? '.' : PIEZAS[pieza];
        if (ch == 'M') {
            // la meta ocupa 2x3 por encima de su casilla
            g.setColor(new Color(255, 230, 140, 50));
            g.fillRect(x, y - 2 * T, 2 * T, 3 * T);
            g.setColor(new Color(255, 230, 140, 180));
            g.drawRect(x, y - 2 * T, 2 * T - 1, 3 * T - 1);
        }
        Composite antes = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        if (ch != '.') icono(g, ch, x, y, T, tema);
        g.setComposite(antes);
        g.setColor(ch == '.' ? new Color(255, 90, 80) : Color.WHITE);
        g.drawRect(x, y, T - 1, T - 1);
        if (ch == '.') {
            g.drawLine(x + 3, y + 3, x + T - 4, y + T - 4);
            g.drawLine(x + T - 4, y + 3, x + 3, y + T - 4);
        }
    }

    private void dibujarBarra(Graphics2D g, Input in, int mx, int my) {
        g.setColor(new Color(0x24140C));
        g.fillRect(0, 0, 640, BARRA);
        g.setColor(Andalusi.ORO_OSCURO);
        g.fillRect(0, BARRA - 1, 640, 1);
        g.setFont(Ui.FUENTE);

        // nombre (clic para escribir)
        g.setColor(escribiendoNombre ? new Color(0x3A2A20) : new Color(0x2E1E14));
        g.fillRect(CAJA_NOMBRE.x, CAJA_NOMBRE.y, CAJA_NOMBRE.width, CAJA_NOMBRE.height);
        g.setColor(escribiendoNombre ? Andalusi.ORO_CLARO : CAJA_NOMBRE.contains(mx, my) ? Andalusi.ORO : Andalusi.ORO_OSCURO);
        g.drawRect(CAJA_NOMBRE.x, CAJA_NOMBRE.y, CAJA_NOMBRE.width - 1, CAJA_NOMBRE.height - 1);
        String n = nombre.isEmpty() && !escribiendoNombre ? "(ponle nombre)" : nombre;
        String visible = Ui.recortar(g, n + (cambiado ? "*" : ""), CAJA_NOMBRE.width - 12);
        int base = Ui.baseCentrada(g, CAJA_NOMBRE.y, CAJA_NOMBRE.height);
        Ui.texto(g, visible, CAJA_NOMBRE.x + 5, base, nombre.isEmpty() ? Ui.GRIS : Andalusi.CREMA);
        if (escribiendoNombre && (frame / 20) % 2 == 0) {
            g.setColor(Andalusi.ORO_CLARO);
            g.fillRect(CAJA_NOMBRE.x + 6 + g.getFontMetrics().stringWidth(Ui.recortar(g, nombre, CAJA_NOMBRE.width - 12)),
                    CAJA_NOMBRE.y + 3, 2, CAJA_NOMBRE.height - 6);
        }

        // tamaño: flechas dibujadas (la fuente no tiene ↔ ni ↕)
        g.setColor(Ui.GRIS);
        g.fillRect(222, 12, 11, 1);
        g.fillRect(223, 11, 1, 3);
        g.fillRect(231, 11, 1, 3);
        g.fillRect(306, 7, 1, 11);
        g.fillRect(305, 8, 3, 1);
        g.fillRect(305, 16, 3, 1);
        Ui.textoCentrado(g, String.valueOf(ancho), 266, base, Andalusi.CREMA);
        Ui.textoCentrado(g, String.valueOf(alto), 342, base, Andalusi.CREMA);
        for (Boton b : botones) b.dibujar(g, in);
    }

    private void dibujarPaleta(Graphics2D g, int mx, int my) {
        g.setColor(new Color(0x24140C));
        g.fillRect(0, PALETA_Y, 640, 360 - PALETA_Y);
        g.setColor(Andalusi.ORO_OSCURO);
        g.fillRect(0, PALETA_Y, 640, 1);
        for (int i = 0; i < PIEZAS.length; i++) {
            int x = PALETA_X + i * CASILLA_PALETA + 1, y = PALETA_Y + 4, s = CASILLA_PALETA - 2;
            boolean sel = i == pieza, encima = piezaEn(mx, my) == i;
            g.setColor(sel ? new Color(0x5A3A1C) : encima ? new Color(0x3E2818) : new Color(0x2E1E14));
            g.fillRect(x, y, s, s);
            icono(g, PIEZAS[i], x + 3, y + 3, s - 6, tema);
            g.setColor(sel ? Andalusi.ORO_CLARO : encima ? Andalusi.ORO : Andalusi.ORO_OSCURO);
            g.drawRect(x, y, s - 1, s - 1);
            if (sel) g.drawRect(x - 1, y - 1, s + 1, s + 1);
        }
    }

    // qué hace lo que hay debajo del ratón en la barra de arriba
    private String ayudaBarra(int mx, int my) {
        if (CAJA_NOMBRE.contains(mx, my)) return "Clic para cambiar el nombre del nivel";
        if (bFondo.contiene(mx, my)) return "Elegir el fondo y el estilo de los bloques";
        if (bAnchoMen.contiene(mx, my) || bAnchoMas.contiene(mx, my)) return "Ancho del nivel (crece por la derecha)";
        if (bAltoMen.contiene(mx, my) || bAltoMas.contiene(mx, my)) return "Alto del nivel (crece por arriba)";
        if (bDeshacer.contiene(mx, my)) return "Deshacer lo último (CTRL+Z)";
        if (bProbar.contiene(mx, my)) return "Jugar el nivel ahora (ESC para volver aquí)";
        if (bGuardar.contiene(mx, my)) return "Guardar (CTRL+S). Al salir y al probar se guarda solo";
        if (bSalir.contiene(mx, my)) return "Volver a la lista de niveles (se guarda solo)";
        return null;
    }

    // dibujo de cada pieza en un cuadrado de lado s
    static void icono(Graphics2D g, char ch, int x, int y, int s, Level.Tema tema) {
        switch (ch) {
            case '.': {
                g.setColor(new Color(0xE8A0A8));
                g.fillRect(x + s / 5, y + s / 3, s * 3 / 5, s / 3);
                g.setColor(new Color(0x5A7FD0));
                g.fillRect(x + s / 5, y + s / 3, s / 4, s / 3);
                g.setColor(Andalusi.MARRON);
                g.drawRect(x + s / 5, y + s / 3, s * 3 / 5, s / 3);
                break;
            }
            case '#': case 'S': case '^': case 'v': case 'p':
                g.drawImage(Sprites.tile(tema, ch, 0, ch == '#', ch == 'S'), x, y, s, s, null);
                break;
            case '~':
                g.setColor(new Color(0x2E7FB8));
                g.fillRect(x, y, s, s);
                g.setColor(new Color(0x8FD3F4));
                g.fillRect(x, y + s / 6, s, Math.max(1, s / 10));
                break;
            case 'u':
                g.setColor(new Color(150, 210, 255, 90));
                g.fillRect(x, y, s, s);
                g.setColor(new Color(0xD8F0FF));
                flechaArriba(g, x + s / 3, y + s / 6, s / 4);
                flechaArriba(g, x + s * 2 / 3, y + s / 2, s / 4);
                break;
            case 'b': case 'B':
                if (ch == 'B') g.drawImage(Sprites.tile(tema, 'p', 0, false, false), x, y, s, s, null);
                g.setColor(new Color(220, 235, 255, 70));
                g.fillRect(x, y, s, s);
                g.setColor(new Color(240, 248, 255, 200));
                for (int k = 0; k < 3; k++) g.fillRect(x + s / 5 + k * s / 3, y + s / 5 + (k % 2) * s / 4, 1, s / 3);
                break;
            case 'I':
                ajustar(g, Sprites.COCO_QUIETO, x, y, s);
                break;
            case 'M': {
                int u = Math.max(1, s / 8);
                g.setColor(new Color(0xF3E4C0));
                g.fillRect(x + u, y + 2 * u, s - 2 * u, s - 2 * u);
                g.setColor(new Color(0x7A4A22));
                g.fillRoundRect(x + 2 * u + u / 2, y + 3 * u, s - 5 * u, s - 3 * u, s / 2, s / 2);
                g.setColor(new Color(0x1E8E3E));
                g.fillRect(x + s / 2, y, s / 3, u);
                g.setColor(Color.WHITE);
                g.fillRect(x + s / 2, y + u, s / 3, u);
                g.setColor(new Color(0x3A2A1A));
                g.fillRect(x + s / 2 - 1, y, 1, 3 * u);
                break;
            }
            case 'F':
                ajustar(g, Sprites.farola(true), x, y, s);
                break;
            case 'C':
                ajustar(g, Sprites.salmorejo(), x, y, s);
                break;
            case 'K':
                ajustar(g, Sprites.flamenquin(), x, y, s);
                break;
            case 'G':
                ajustar(g, Sprites.maceta(1), x + s / 8, y, s * 3 / 4);
                g.setColor(new Color(0xFF6B5A));
                g.fillRect(x + s - 3, y + s / 4, 2, s / 2);
                g.fillRect(x + s - 4, y + s * 3 / 4 - 2, 4, 1);
                break;
            case 'm':
                ajustar(g, Sprites.maceta(0), x, y, s);
                break;
            case 'n':
                ajustar(g, Sprites.naranjo(), x, y, s);
                break;
            case 'l':
                ajustar(g, Sprites.palmera(), x, y, s);
                break;
            default:
                break;
        }
    }

    private static void flechaArriba(Graphics2D g, int cx, int y, int t) {
        g.fillRect(cx, y, 1, t * 2);
        g.drawLine(cx - t / 2, y + t / 2, cx, y);
        g.drawLine(cx + t / 2, y + t / 2, cx, y);
    }

    // la imagen entera dentro del cuadrado, sin deformarla, apoyada abajo
    private static void ajustar(Graphics2D g, BufferedImage img, int x, int y, int s) {
        double e = Math.min((double) s / img.getWidth(), (double) s / img.getHeight());
        int w = Math.max(1, (int) Math.round(img.getWidth() * e)), h = Math.max(1, (int) Math.round(img.getHeight() * e));
        g.drawImage(img, x + (s - w) / 2, y + s - h, w, h, null);
    }
}
