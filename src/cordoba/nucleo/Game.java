package cordoba.nucleo;

import cordoba.audio.Sonido;
import cordoba.entidades.Fantasma;
import cordoba.entidades.Player;
import cordoba.fisica.CollisionManager;
import cordoba.fisica.PhysicsConfig;
import cordoba.graficos.GameRenderer;
import cordoba.graficos.Particulas;
import cordoba.interfaz.CodeEditor;
import cordoba.interfaz.GameMenu;
import cordoba.interfaz.Introduccion;
import cordoba.interfaz.PantallaRanking;
import cordoba.interfaz.Ui;
import cordoba.niveles.Level1;
import cordoba.niveles.Level2;
import cordoba.niveles.Level;
import cordoba.ranking.Puntuacion;
import cordoba.ranking.Ranking;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

// El corazón del juego: bucle, estados y cámara. Todo se pinta en una imagen de 640x360 que luego
// se escala sin suavizar. Va a 60 actualizaciones fijas por segundo, así la física es igual en cualquier PC.
public class Game extends Canvas implements Runnable {

    public static final int ANCHO = 640, ALTO = 360;
    private static final int FPS = 60;

    private final BufferedImage lienzo = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
    private final Input input = new Input();
    private final PhysicsConfig fisica = new PhysicsConfig();
    private final GameRenderer renderer = new GameRenderer();
    private final GameMenu menu = new GameMenu(renderer);
    private final Introduccion intro = new Introduccion(renderer);
    private final CodeEditor editor = new CodeEditor();
    private final Particulas particulas = new Particulas();
    private final Ranking ranking = new Ranking(Path.of("ranking.txt"));
    private final PantallaRanking pantallaRanking = new PantallaRanking(renderer, ranking);

    // fantasma del récord del nivel 2
    private static final Path FICHERO_FANTASMA = Path.of("fantasma.dat");
    private Fantasma fantasma = Fantasma.cargar(FICHERO_FANTASMA);   // el guardado en disco
    private Fantasma fantasmaNivel;                // el que corre en este nivel (o null)
    private boolean fantasmaVisible = true;        // tecla G
    private Fantasma.Grabadora grabadora = new Fantasma.Grabadora();
    private Fantasma grabacionN2;                  // tu partida en el nivel 2

    private GameState estado = GameState.MENU;
    private Level nivel;
    private Player jugador;
    private CollisionManager colisiones;

    private float camX, camY;
    private int frame;                 // contador global para animaciones
    private int timerVictoria;         // celebración antes de la pantalla de victoria

    // estadísticas
    private int framesNivel, muertesNivel, compilacionesNivel;
    private int framesTotal, muertesTotal, compilacionesTotal;
    private final int[] framesPorNivel = new int[3];   // tiempo de cada nivel (se usan el 1 y el 2)

    // aviso temporal de abajo
    private String aviso;
    private Color colorAviso = Ui.VERDE;
    private int avisoTimer;

    private volatile boolean ejecutando;
    private Runnable alSalir = () -> System.exit(0);
    private Runnable alPantallaCompleta;   // F11 (lo pone Main)
    private volatile boolean rehacerBuffer;  // la ventana se ha recreado (F11) y el búfer viejo ya no vale

    public Game() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                input.soltarTodo();   // que no se queden teclas pegadas al cambiar de ventana
            }
        });
        setFocusTraversalKeysEnabled(false);
    }

    public void setAlSalir(Runnable r) {
        alSalir = r;
    }

    public void setAlPantallaCompleta(Runnable r) {
        alPantallaCompleta = r;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        rehacerBuffer = true;
    }

    public void iniciar() {
        ejecutando = true;
        Thread hilo = new Thread(this, "BucleJuego");
        hilo.start();
        requestFocus();
    }

    // bucle con paso de tiempo fijo
    @Override
    public void run() {
        final double nsPorFrame = 1_000_000_000.0 / FPS;
        long anterior = System.nanoTime();
        double acumulado = 0;
        while (ejecutando) {
            long ahora = System.nanoTime();
            acumulado += (ahora - anterior) / nsPorFrame;
            anterior = ahora;
            if (acumulado > 5) acumulado = 5;   // si el PC se atasca, no corremos para recuperar
            boolean dibujar = false;
            while (acumulado >= 1) {
                actualizar();
                acumulado -= 1;
                dibujar = true;
            }
            if (dibujar) presentar();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ---- actualizar ----

    public void actualizar() {
        frame++;
        // M silencia (menos al escribir el nombre, que puede llevar M)
        if (estado != GameState.JUEGO_COMPLETADO && input.pulsada(KeyEvent.VK_M)) Sonido.alternarSilencio();
        if (input.pulsada(KeyEvent.VK_F11) && alPantallaCompleta != null) EventQueue.invokeLater(alPantallaCompleta);

        switch (estado) {
            case MENU:
                switch (menu.actualizarInicio(input)) {
                    case JUGAR:
                        intro.reiniciar();
                        estado = GameState.INTRO;
                        break;
                    case NIVEL2: empezarPartida(2); break;
                    case RANKING:
                        pantallaRanking.verTabla();
                        estado = GameState.RANKING;
                        break;
                    case SALIR:  salir(); break;
                    default: break;
                }
                break;
            case INTRO:
                if (intro.actualizar(input)) empezarPartida(1);
                break;
            case JUGANDO:
                actualizarPartida();
                break;
            case EDITOR:
                actualizarEditor();
                break;
            case PAUSA:
                switch (menu.actualizarPausa(input)) {
                    case CONTINUAR: estado = GameState.JUGANDO; break;
                    case REINICIAR: cargarNivel(nivel.getNumero()); break;
                    case MENU:      estado = GameState.MENU; break;
                    default: break;
                }
                break;
            case NIVEL_COMPLETADO:
                switch (menu.actualizarVictoria(input)) {
                    case CONTINUAR: cargarNivel(nivel.getNumero() + 1); break;
                    case MENU:      estado = GameState.MENU; break;
                    default: break;
                }
                break;
            case JUEGO_COMPLETADO:
                Puntuacion guardada = pantallaRanking.actualizarNombre(input, framesTotal, muertesTotal,
                        compilacionesTotal, framesPorNivel[1], framesPorNivel[2]);
                if (guardada != null) {
                    if (grabacionN2 != null && ranking.mejorNivel(2) == guardada) guardarFantasma(guardada);
                    estado = GameState.RANKING;
                }
                break;
            case RANKING:
                if (pantallaRanking.actualizarTabla(input)) estado = GameState.MENU;
                break;
            default:
                break;
        }
        if (avisoTimer > 0) avisoTimer--;
        input.finFrame();
    }

    private void actualizarPartida() {
        boolean puedeControlar = !jugador.muerto && !jugador.celebrando;

        if (puedeControlar && input.pulsada(KeyEvent.VK_E)) {
            estado = GameState.EDITOR;
            editor.abrir(fisica, nivel);
            Sonido.clic();
            return;
        }
        if (input.pulsada(KeyEvent.VK_ESCAPE)) {
            menu.abrirPausa(input);
            estado = GameState.PAUSA;
            return;
        }
        if (puedeControlar && input.pulsada(KeyEvent.VK_R)) {
            jugador.morir();
        }
        alternarFantasma();

        boolean izq = input.abajo(KeyEvent.VK_A) || input.abajo(KeyEvent.VK_LEFT);
        boolean der = input.abajo(KeyEvent.VK_D) || input.abajo(KeyEvent.VK_RIGHT);
        boolean saltar = input.pulsada(KeyEvent.VK_SPACE) || input.pulsada(KeyEvent.VK_W) || input.pulsada(KeyEvent.VK_UP);

        paso(izq, der, saltar);
    }

    // un fotograma de juego; es público para poder probar los niveles automáticamente
    public void paso(boolean izq, boolean der, boolean saltar) {
        framesNivel++;
        nivel.actualizar(jugador, particulas);
        jugador.actualizar(izq, der, saltar, fisica, nivel, colisiones, particulas);
        particulas.actualizar();

        // ¿ha tocado algo que mata?
        if (!jugador.muerto && !jugador.celebrando && colisiones.tocaPeligro(jugador)) {
            jugador.morir();
            particulas.emitir(jugador.centroX(), jugador.centroY(), 20, new Color(0x4FB848), 2.2f, 0.12f, 35);
        }
        // reaparece tras la animación de muerte
        if (jugador.muerto && jugador.timerMuerte > 55) {
            muertesNivel++;
            reaparecer();
        }
        // ¿ha llegado a la meta?
        if (!jugador.muerto && !jugador.celebrando && nivel.getMeta().intersects(
                jugador.x, jugador.y, Player.ANCHO, Player.ALTO)) {
            jugador.celebrar();
            timerVictoria = 0;
            Sonido.victoria();
            particulas.emitir(jugador.centroX(), jugador.y, 40, new Color(0xFFE27A), 2.5f, 0.05f, 60);
        }
        if (jugador.celebrando) {
            timerVictoria++;
            if (timerVictoria % 12 == 0)
                particulas.emitir(jugador.centroX(), jugador.y - 10, 8, timerVictoria % 24 == 0 ? new Color(0x7BE07B) : Color.WHITE, 2f, 0.04f, 40);
            if (timerVictoria > 110) completarNivel();
        }
        grabadora.grabar(jugador);
        moverCamara(false);
    }

    // G enseña/oculta el fantasma
    private void alternarFantasma() {
        if (fantasmaNivel != null && input.pulsada(KeyEvent.VK_G)) {
            fantasmaVisible = !fantasmaVisible;
            Sonido.clic();
        }
    }

    private void actualizarEditor() {
        switch (editor.actualizar(input)) {
            case APLICADO:
                compilacionesNivel++;
                mostrarAviso("Cambios aplicados: " + editor.getUltimoResumen(), Ui.VERDE);
                particulas.emitir(jugador.centroX(), jugador.centroY(), 24, new Color(0x82AAFF), 2f, 0f, 40);
                break;
            case CERRAR:
                if (editor.hayCambiosPendientes()) {
                    mostrarAviso("Cerraste sin aplicar: ¡los cambios no se han compilado!", Ui.AMARILLO);
                }
                estado = GameState.JUGANDO;
                Sonido.clic();
                break;
            default:
                break;
        }
        alternarFantasma();
        // Coco y el nivel se quedan quietos, pero el cronómetro sigue (y el fantasma también)
        if (estado == GameState.EDITOR) {
            framesNivel++;
            grabadora.grabar(jugador);
        }
    }

    private void mostrarAviso(String texto, Color color) {
        aviso = texto;
        colorAviso = color;
        avisoTimer = 180;
    }

    // ---- niveles ----

    private void empezarPartida(int numero) {
        framesTotal = muertesTotal = compilacionesTotal = 0;
        java.util.Arrays.fill(framesPorNivel, 0);
        grabacionN2 = null;
        cargarNivel(numero);
    }

    public void cargarNivel(int numero) {
        nivel = numero == 1 ? new Level1() : new Level2();
        nivel.configurarFisica(fisica);
        colisiones = new CollisionManager(nivel);
        jugador = new Player(nivel.getInicioX(), nivel.getInicioY());
        particulas.limpiar();
        framesNivel = muertesNivel = compilacionesNivel = 0;
        avisoTimer = 0;
        grabadora = new Fantasma.Grabadora();
        fantasmaNivel = numero == 2 ? fantasmaDelRecord() : null;
        estado = GameState.JUGANDO;
        moverCamara(true);
    }

    private void reaparecer() {
        nivel.reiniciarDinamicos();
        jugador.colocar(nivel.getReaparecerX(), nivel.getReaparecerY());
        particulas.limpiar();
        moverCamara(true);
    }

    private void completarNivel() {
        framesTotal += framesNivel;
        muertesTotal += muertesNivel;
        compilacionesTotal += compilacionesNivel;
        framesPorNivel[Math.min(nivel.getNumero(), framesPorNivel.length - 1)] = framesNivel;
        if (nivel.getNumero() == 2) grabacionN2 = grabadora.crear("", framesNivel);
        if (nivel.getNumero() >= 2) {
            pantallaRanking.empezarNombre();
            estado = GameState.JUEGO_COMPLETADO;
        } else {
            menu.abrirVictoria(input);
            estado = GameState.NIVEL_COMPLETADO;
        }
    }

    // el fantasma solo vale si coincide con el récord actual (si borráis ranking.txt, el viejo deja de salir)
    private Fantasma fantasmaDelRecord() {
        Puntuacion record = ranking.mejorNivel(2);
        if (fantasma == null || record == null) return null;
        boolean coincide = record.framesN2() == fantasma.getFramesNivel()
                && record.nombre().equals(fantasma.getNombre());
        return coincide ? fantasma : null;
    }

    private void guardarFantasma(Puntuacion record) {
        fantasma = grabacionN2.conNombre(record.nombre());
        try {
            fantasma.guardar(FICHERO_FANTASMA);
        } catch (IOException e) {
            System.err.println("No se pudo guardar el fantasma: " + e.getMessage());
        }
    }

    // la cámara sigue a Coco suave y mira un poco hacia donde va
    private void moverCamara(boolean instantaneo) {
        float objetivoX = jugador.centroX() - ANCHO / 2f + (jugador.mirandoDerecha ? 40 : -40);
        float objetivoY = jugador.centroY() - ALTO / 2f - 10;
        if (instantaneo) {
            camX = objetivoX;
            camY = objetivoY;
        } else {
            camX += (objetivoX - camX) * 0.10f;
            camY += (objetivoY - camY) * 0.12f;
        }
        camX = Math.max(0, Math.min(camX, nivel.getAnchoPx() - ANCHO));
        camY = Math.max(0, Math.min(camY, nivel.getAltoPx() - ALTO));
    }

    private void salir() {
        ejecutando = false;
        alSalir.run();
    }

    // ---- dibujo ----

    public BufferedImage renderizar() {
        Graphics2D g = lienzo.createGraphics();
        Ui.preparar(g);
        switch (estado) {
            case MENU:
                menu.dibujarInicio(g, input, frame);
                break;
            case INTRO:
                intro.dibujar(g);
                break;
            case JUEGO_COMPLETADO:
                pantallaRanking.dibujarNombre(g, input, framesTotal, muertesTotal, compilacionesTotal,
                        framesPorNivel[1], framesPorNivel[2], frame);
                break;
            case RANKING:
                pantallaRanking.dibujarTabla(g, input, frame);
                break;
            default: {
                int cx = Math.round(camX), cy = Math.round(camY);
                Fantasma f = fantasmaVisible ? fantasmaNivel : null;
                renderer.dibujarEscenario(g, nivel, jugador, particulas, f, framesNivel, cx, cy, nivel.getFrames());
                if (estado == GameState.EDITOR) {
                    // consola arriba, por encima de Coco
                    editor.dibujar(g, input, frame, Puntuacion.formatearTiempo(framesTotal + framesNivel));
                    renderer.dibujarAvisosEspeciales(g, nivel, jugador, fisica, frame, editor.getAbajo() + 16);
                    String msg = nivel.getMensaje();
                    if (msg != null) renderer.dibujarMensaje(g, msg, nivel.getDesbloqueo(), Math.max(31, nivel.getMensajeTimer()), -1);
                } else {
                    renderer.dibujarHUD(g, nivel, jugador, fisica, frame, muertesNivel, aviso, colorAviso, avisoTimer,
                            framesNivel < 600 || estado == GameState.PAUSA,
                            framesTotal + framesNivel, framesNivel, fantasmaNivel, fantasmaVisible);
                }
                if (estado == GameState.PAUSA) menu.dibujarPausa(g, input);
                if (estado == GameState.NIVEL_COMPLETADO)
                    menu.dibujarVictoria(g, input, nivel, framesNivel, muertesNivel, compilacionesNivel, frame);
                break;
            }
        }
        g.dispose();
        return lienzo;
    }

    // escala la imagen a la ventana manteniendo 16:9
    private void presentar() {
        if (!isDisplayable()) return;   // la ventana se está rehaciendo (F11)
        BufferStrategy bs = getBufferStrategy();
        if (bs == null || rehacerBuffer) {
            rehacerBuffer = false;
            try {
                createBufferStrategy(2);
            } catch (IllegalStateException e) {
                rehacerBuffer = true;
            }
            return;
        }
        renderizar();
        try {
            pintar(bs);
        } catch (IllegalStateException e) {
            rehacerBuffer = true;   // se ha cambiado la ventana a medio dibujar
        }
    }

    private void pintar(BufferStrategy bs) {
        int w = getWidth(), h = getHeight();
        double escala = Ui.escalaVentana(w, h);
        int dw = (int) (ANCHO * escala), dh = (int) (ALTO * escala);
        int ox = (w - dw) / 2, oy = (h - dh) / 2;
        input.setTransformacion(escala, ox, oy);
        do {
            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(lienzo, ox, oy, dw, dh, null);
            g.dispose();
            bs.show();
        } while (bs.contentsLost());
        Toolkit.getDefaultToolkit().sync();
    }

    // para las pruebas automáticas
    public Player getJugador()        { return jugador; }
    public Level getNivel()           { return nivel; }
    public PhysicsConfig getFisica()  { return fisica; }
    public GameState getEstado()      { return estado; }
    public Input getInput()           { return input; }
    public void setEstado(GameState e) { estado = e; }
    public int getFramesNivel()       { return framesNivel; }
    public void abrirEditor()         { estado = GameState.EDITOR; editor.abrir(fisica, nivel); }
}
