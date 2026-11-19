package cordoba.niveles;

import cordoba.audio.Sonido;
import cordoba.entidades.Player;
import cordoba.fisica.PhysicsConfig;
import cordoba.graficos.Particulas;
import cordoba.niveles.elementos.Coleccionable;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.elementos.Farola;
import cordoba.niveles.elementos.ObstaculoMovil;
import cordoba.niveles.plataformas.PlataformaDinamica;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/*
 * Base de los niveles. Un nivel es un mapa de caracteres:
 *   '.' vacío   '#' suelo   'S' piedra   '^' pinchos   'v' pinchos de techo   '~' agua   'u' corriente de aire
 *   'p' fondo de piedra (no choca: el interior de un pozo o un aljibe)
 * más plataformas, obstáculos, farolas (checkpoints), decoración y la meta.
 * Level1 y Level2 lo montan en su constructor con suelo(), agua(), farola()...
 */
public abstract class Level {

    public static final int TILE = 16;

    // empuje de la corriente de aire (equivale a gravedad 12.5)
    public static final float FUERZA_CORRIENTE = 0.5f;

    // rozamiento dentro de la corriente (frena la velocidad vertical)
    public static final float ROZAMIENTO_CORRIENTE = 0.92f;

    // brisa del pozo: empuja un poco hacia arriba y frena la caída (no sube a Coco como la corriente).
    // Con la gravedad normal se sigue cayendo rápido; con poca gravedad se cae muy despacio
    public static final float FUERZA_BRISA = 0.08f;
    public static final float ROZAMIENTO_BRISA = 0.96f;

    public enum Tema { RIO, MEZQUITA, CALLEJA }

    // número de niveles de la partida
    public static final int TOTAL = 3;

    public static final String[] NOMBRES = {
        "", "Los primeros pasos por Córdoba", "La Calleja de las Flores", "El desafío de la Mezquita" };

    // el nivel con ese número (1: río, 2: Calleja de las Flores, 3: Mezquita)
    public static Level crear(int numero) {
        switch (numero) {
            case 1:  return new Level1();
            case 2:  return new Level2();
            default: return new Level3();
        }
    }

    protected final int numero;
    protected final String nombre;
    protected final Tema tema;
    protected final int columnas, filas;
    protected final char[][] mapa;

    protected final List<PlataformaDinamica> plataformas = new ArrayList<>();
    protected final List<ObstaculoMovil> obstaculos = new ArrayList<>();
    protected final List<Farola> farolas = new ArrayList<>();
    protected final List<Decoracion> decoraciones = new ArrayList<>();
    protected final List<Coleccionable> coleccionables = new ArrayList<>();
    protected final List<Rectangle> brisas = new ArrayList<>();          // en píxeles
    protected final List<Rectangle> paredesFloridas = new ArrayList<>(); // en píxeles: pared con macetas
    protected Rectangle meta = new Rectangle();

    private float inicioX, inicioY;
    private float reaparecerX, reaparecerY;
    private int indiceFarola = -1;
    private int frames;


    protected Level(int numero, String nombre, Tema tema, int columnas, int filas) {
        this.numero = numero;
        this.nombre = nombre;
        this.tema = tema;
        this.columnas = columnas;
        this.filas = filas;
        mapa = new char[filas][columnas];
        for (char[] fila : mapa) java.util.Arrays.fill(fila, '.');
    }

    // ---- para montar niveles ----

    // rellena columnas c1..c2 y filas f1..f2 (incluidas)
    protected void rellenar(int c1, int f1, int c2, int f2, char tipo) {
        for (int f = Math.max(0, f1); f <= Math.min(filas - 1, f2); f++)
            for (int c = Math.max(0, c1); c <= Math.min(columnas - 1, c2); c++)
                mapa[f][c] = tipo;
    }

    // suelo desde esa fila hasta abajo del todo
    protected void suelo(int c1, int c2, int fila) {
        rellenar(c1, fila, c2, filas - 1, '#');
    }

    // agua desde esa fila hasta abajo
    protected void agua(int c1, int c2, int fila) {
        rellenar(c1, fila, c2, filas - 1, '~');
    }

    protected void pinchos(int c1, int c2, int fila) {
        rellenar(c1, fila, c2, fila, '^');
    }

    protected void pinchosTecho(int c1, int c2, int fila) {
        rellenar(c1, fila, c2, fila, 'v');
    }

    protected void vaciar(int c1, int f1, int c2, int f2) {
        rellenar(c1, f1, c2, f2, '.');
    }

    // columna y fila del suelo donde aparece Coco
    protected void inicio(int col, int filaSuelo) {
        inicioX = col * TILE + (TILE - Player.ANCHO) / 2f;
        inicioY = filaSuelo * TILE - Player.ALTO;
        reaparecerX = inicioX;
        reaparecerY = inicioY;
    }

    protected void farola(int col, int filaSuelo) {
        farolas.add(new Farola(col * TILE + TILE / 2f, filaSuelo * TILE));
    }

    protected void decoracion(Decoracion.Tipo tipo, int col, int filaSuelo, int variante) {
        decoraciones.add(new Decoracion(tipo, col * TILE + TILE / 2f, filaSuelo * TILE, variante));
    }

    // comida flotando en el centro de esa casilla
    protected void coleccionable(Coleccionable.Tipo tipo, int col, int fila) {
        coleccionables.add(new Coleccionable(tipo, col * TILE + TILE / 2f, fila * TILE + TILE / 2f));
    }

    // rectángulo de anchoTiles x altoTiles apoyado en filaSuelo
    protected void meta(int col, int filaSuelo, int anchoTiles, int altoTiles) {
        meta = new Rectangle(col * TILE, (filaSuelo - altoTiles) * TILE, anchoTiles * TILE, altoTiles * TILE);
    }

    // ---- consultas del mapa ----

    // fuera del mapa por los lados y por arriba cuenta como pared
    public boolean esSolido(int c, int f) {
        if (c < 0 || c >= columnas) return true;
        if (f < 0) return true;
        if (f >= filas) return false;
        char t = mapa[f][c];
        return t == '#' || t == 'S';
    }

    public char tile(int c, int f) {
        if (c < 0 || c >= columnas || f < 0 || f >= filas) return '.';
        return mapa[f][c];
    }

    // zona de brisa de las columnas c1..c2 y filas f1..f2
    protected void brisa(int c1, int f1, int c2, int f2) {
        brisas.add(new Rectangle(c1 * TILE, f1 * TILE, (c2 - c1 + 1) * TILE, (f2 - f1 + 1) * TILE));
    }

    // pared de patio llena de macetas (solo se dibuja; las casillas tienen que ser pared 'S')
    protected void paredFlorida(int c1, int f1, int c2, int f2) {
        paredesFloridas.add(new Rectangle(c1 * TILE, f1 * TILE, (c2 - c1 + 1) * TILE, (f2 - f1 + 1) * TILE));
    }

    public boolean enBrisa(float px, float py) {
        for (Rectangle r : brisas) if (r.contains(px, py)) return true;
        return false;
    }

    public List<Rectangle> getBrisas()          { return brisas; }
    public List<Rectangle> getParedesFloridas() { return paredesFloridas; }

    public boolean esCorriente(float px, float py) {
        return tile((int) Math.floor(px / TILE), (int) Math.floor(py / TILE)) == 'u';
    }

    // ---- lógica ----

    // valores de la física al empezar el nivel (todo desbloqueado)
    public void configurarFisica(PhysicsConfig f) {
        f.restaurarTodo();
    }

    public void actualizar(Player p, Particulas particulas) {
        frames++;
        for (PlataformaDinamica pl : plataformas) pl.actualizar(frames, p);
        for (ObstaculoMovil o : obstaculos) o.actualizar(frames);

        float cx = p.centroX();
        float pies = p.y + Player.ALTO;

        // farolas = checkpoints
        for (int i = 0; i < farolas.size(); i++) {
            Farola fa = farolas.get(i);
            if (!fa.encendida && !p.muerto && cx >= fa.x - 4 && Math.abs(pies - fa.ySuelo) < 80) {
                fa.encendida = true;
                if (i > indiceFarola) {
                    indiceFarola = i;
                    reaparecerX = fa.x - Player.ANCHO / 2f;
                    reaparecerY = fa.ySuelo - Player.ALTO;
                }
                Sonido.checkpoint();
                particulas.emitir(fa.x, fa.ySuelo - 30, 14, new Color(0xFFE27A), 1.6f, 0.02f, 30);
            }
        }

    }

    // cuando Coco reaparece, las plataformas vuelven a como estaban
    public void reiniciarDinamicos() {
        frames = 0;
        for (PlataformaDinamica pl : plataformas) pl.reiniciar();
    }

    public int getNumero()           { return numero; }
    public String getNombre()        { return nombre; }
    public Tema getTema()            { return tema; }
    public int getColumnas()         { return columnas; }
    public int getFilas()            { return filas; }
    public int getAnchoPx()          { return columnas * TILE; }
    public int getAltoPx()           { return filas * TILE; }
    public int getFrames()           { return frames; }
    public float getInicioX()        { return inicioX; }
    public float getInicioY()        { return inicioY; }
    public float getReaparecerX()    { return reaparecerX; }
    public float getReaparecerY()    { return reaparecerY; }
    public Rectangle getMeta()       { return meta; }
    public List<PlataformaDinamica> getPlataformas() { return plataformas; }
    public List<ObstaculoMovil> getObstaculos()      { return obstaculos; }
    public List<Farola> getFarolas()                 { return farolas; }
    public List<Decoracion> getDecoraciones()        { return decoraciones; }
    public List<Coleccionable> getColeccionables()   { return coleccionables; }
}
