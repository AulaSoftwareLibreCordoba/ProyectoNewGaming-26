package cordoba.nucleo;

import cordoba.audio.Sonido;
import cordoba.entidades.Player;
import cordoba.fisica.CollisionManager;
import cordoba.fisica.PhysicsConfig;
import cordoba.graficos.GameRenderer;
import cordoba.graficos.Particulas;
import cordoba.interfaz.CodeEditor;
import cordoba.interfaz.GameMenu;
import cordoba.interfaz.PantallaRanking;
import cordoba.interfaz.Ui;
import cordoba.niveles.Level1;
import cordoba.niveles.Level2;
import cordoba.niveles.Level;
import cordoba.ranking.Ranking;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

/**
 * Núcleo del juego: bucle principal, máquina de estados y cámara.
 *
 * El juego se dibuja en una imagen de 640x360 (resolución interna) que después
 * se escala a la ventana con "vecino más próximo" para conservar los píxeles.
 *
 * El bucle funciona a 60 actualizaciones por segundo fijas: así las físicas
 * son siempre iguales en cualquier ordenador.
 */
public class Game extends Canvas implements Runnable {

    public static final int ANCHO = 640, ALTO = 360;
    private static final int FPS = 60;

    private final BufferedImage lienzo = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_RGB);
    private final Input input = new Input();
    private final PhysicsConfig fisica = new PhysicsConfig();
    private final GameRenderer renderer = new GameRenderer();
    private final GameMenu menu = new GameMenu(renderer);
    private final CodeEditor editor = new CodeEditor();
    private final Particulas particulas = new Particulas();
    private final PantallaRanking pantallaRanking =
            new PantallaRanking(renderer, new Ranking(Path.of("ranking.txt")));

    private GameState estado = GameState.MENU;
    private Level nivel;
    private Player jugador;
    private CollisionManager colisiones;

    private float camX, camY;
    private int frame;                 // contador global (animaciones de interfaz)
    private int timerVictoria;         // celebración antes de la pantalla de victoria

    // Estadísticas
    private int framesNivel, muertesNivel, compilacionesNivel;
    private int framesTotal, muertesTotal, compilacionesTotal;

    // Aviso temporal en la parte inferior
    private String aviso;
    private Color colorAviso = Ui.VERDE;
    private int avisoTimer;

    private volatile boolean ejecutando;
    private Runnable alSalir = () -> System.exit(0);

    public Game() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                input.soltarTodo();   // evita teclas "pegadas" al cambiar de ventana
            }
        });
        setFocusTraversalKeysEnabled(false);
    }

    public void setAlSalir(Runnable r) {
        alSalir = r;
    }

    public void iniciar() {
        ejecutando = true;
        Thread hilo = new Thread(this, "BucleJuego");
        hilo.start();
        requestFocus();
    }

    /** Bucle principal con paso de tiempo fijo. */
    @Override
    public void run() {
        final double nsPorFrame = 1_000_000_000.0 / FPS;
        long anterior = System.nanoTime();
        double acumulado = 0;
        while (ejecutando) {
            long ahora = System.nanoTime();
            acumulado += (ahora - anterior) / nsPorFrame;
            anterior = ahora;
            if (acumulado > 5) acumulado = 5;   // si el PC se atasca, no "corremos" para recuperar
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

    // =====================================================================
    //  ACTUALIZACIÓN
    // =====================================================================

    public void actualizar() {
        frame++;
        // M silencia el sonido (salvo mientras se escribe el nombre, que podría llevar una M)
        if (estado != GameState.JUEGO_COMPLETADO && input.pulsada(KeyEvent.VK_M)) Sonido.alternarSilencio();

        switch (estado) {
            case MENU:
                switch (menu.actualizarInicio(input)) {
                    case JUGAR:  empezarPartida(1); break;
                    case NIVEL2: empezarPartida(2); break;
                    case RANKING:
                        pantallaRanking.verTabla();
                        estado = GameState.RANKING;
                        break;
                    case SALIR:  salir(); break;
                    default: break;
                }
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
                if (pantallaRanking.actualizarNombre(input, framesTotal, muertesTotal, compilacionesTotal))
                    estado = GameState.RANKING;
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
            estado = GameState.PAUSA;
            return;
        }
        if (puedeControlar && input.pulsada(KeyEvent.VK_R)) {
            jugador.morir();
        }

        boolean izq = input.abajo(KeyEvent.VK_A) || input.abajo(KeyEvent.VK_LEFT);
        boolean der = input.abajo(KeyEvent.VK_D) || input.abajo(KeyEvent.VK_RIGHT);
        boolean saltar = input.pulsada(KeyEvent.VK_SPACE) || input.pulsada(KeyEvent.VK_W) || input.pulsada(KeyEvent.VK_UP);

        paso(izq, der, saltar);
    }

    /**
     * Un fotograma de juego con las entradas indicadas.
     * Es público para poder probar los niveles de forma automática.
     */
    public void paso(boolean izq, boolean der, boolean saltar) {
        framesNivel++;
        nivel.actualizar(jugador, particulas);
        jugador.actualizar(izq, der, saltar, fisica, nivel, colisiones, particulas);
        particulas.actualizar();

        // ¿Ha tocado algo peligroso?
        if (!jugador.muerto && !jugador.celebrando && colisiones.tocaPeligro(jugador)) {
            jugador.morir();
            particulas.emitir(jugador.centroX(), jugador.centroY(), 20, new Color(0x4FB848), 2.2f, 0.12f, 35);
        }
        // Reaparecer tras la animación de muerte
        if (jugador.muerto && jugador.timerMuerte > 55) {
            muertesNivel++;
            reaparecer();
        }
        // ¿Ha llegado a la meta?
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
        moverCamara(false);
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
        // Las partículas siguen animándose para que se vea el efecto
        particulas.actualizar();
    }

    private void mostrarAviso(String texto, Color color) {
        aviso = texto;
        colorAviso = color;
        avisoTimer = 180;
    }

    // =====================================================================
    //  NIVELES
    // =====================================================================

    private void empezarPartida(int numero) {
        framesTotal = muertesTotal = compilacionesTotal = 0;
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
        if (nivel.getNumero() >= 2) {
            pantallaRanking.empezarNombre();
            estado = GameState.JUEGO_COMPLETADO;
        } else {
            estado = GameState.NIVEL_COMPLETADO;
        }
    }

    /** La cámara sigue a Coco con suavidad y mira un poco hacia donde avanza. */
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

    // =====================================================================
    //  DIBUJO
    // =====================================================================

    /** Dibuja el estado actual sobre la imagen interna de 640x360. */
    public BufferedImage renderizar() {
        Graphics2D g = lienzo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        switch (estado) {
            case MENU:
                menu.dibujarInicio(g, input, frame);
                break;
            case JUEGO_COMPLETADO:
                pantallaRanking.dibujarNombre(g, input, framesTotal, muertesTotal, compilacionesTotal, frame);
                break;
            case RANKING:
                pantallaRanking.dibujarTabla(g, input, frame);
                break;
            default: {
                int cx = Math.round(camX), cy = Math.round(camY);
                renderer.dibujarEscenario(g, nivel, jugador, particulas, cx, cy, nivel.getFrames());
                if (estado == GameState.EDITOR) {
                    editor.dibujar(g, input, frame);
                    String msg = nivel.getMensaje();
                    if (msg != null) renderer.dibujarMensaje(g, msg, Math.max(31, nivel.getMensajeTimer()));
                } else {
                    renderer.dibujarHUD(g, nivel, jugador, fisica, frame, muertesNivel, aviso, colorAviso, avisoTimer,
                            framesNivel < 600 || estado == GameState.PAUSA);
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

    /** Escala la imagen interna a la ventana manteniendo la proporción 16:9. */
    private void presentar() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(2);
            return;
        }
        renderizar();
        int w = getWidth(), h = getHeight();
        double escala = Math.min(w / (double) ANCHO, h / (double) ALTO);
        // Escala entera si queda bien (píxeles perfectos); si no, fraccionaria
        if (escala >= 1 && Math.floor(escala) / escala >= 0.85) escala = Math.floor(escala);
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

    // ---- Accesos para pruebas automáticas ----
    public Player getJugador()        { return jugador; }
    public Level getNivel()           { return nivel; }
    public PhysicsConfig getFisica()  { return fisica; }
    public GameState getEstado()      { return estado; }
    public Input getInput()           { return input; }
    public void setEstado(GameState e) { estado = e; }
    public void abrirEditor()         { estado = GameState.EDITOR; editor.abrir(fisica, nivel); }
}
