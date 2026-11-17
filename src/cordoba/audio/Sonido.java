package cordoba.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Efectos de sonido "chiptune" generados por código (onda cuadrada y ruido).
 * No necesita archivos de audio. Si el ordenador no tiene salida de sonido,
 * simplemente se desactiva sin provocar errores.
 *
 * Pulsa M durante la partida para silenciar / activar el sonido.
 *
 * ESTRATEGIA DE CONCURRENCIA
 * - Productores: cualquier hilo del juego (p. ej. el hilo de Swing/juego) llama a
 *   salto(), muerte()... Solo generan un byte[] y lo meten en la cola "nuevas".
 * - Consumidor único: el hilo "HiloSonido" es el ÚNICO que toca la línea de audio
 *   y la lista de voces activas. Por eso esa lista no necesita cerrojos.
 * - Único estado compartido: la cola "nuevas" (thread-safe) y los flags volatile.
 * - Los sonidos se MEZCLAN (se suman) en bloques pequeños, así que varios sonidos
 *   pueden sonar a la vez en lugar de esperar su turno uno detrás de otro.
 * - Errores: si falla el audio, "disponible" pasa a false y el juego sigue.
 */
public final class Sonido {

    private static final float FRECUENCIA_MUESTREO = 22050f;
    private static final int TAM_BLOQUE = 512;   // ~23 ms de audio por iteración
    private static final int MAX_VOCES = 8;      // sonidos simultáneos máximos

    /** Un sonido ya generado y su posición de lectura actual. */
    private static final class Voz {
        final byte[] datos;
        int pos = 0;
        Voz(byte[] datos) { this.datos = datos; }
    }

    private static final BlockingQueue<Voz> nuevas = new LinkedBlockingQueue<>();
    private static volatile boolean disponible = true;
    private static volatile boolean silenciado = false;
    private static Thread hilo;   // protegido por el cerrojo de la clase (synchronized)

    private Sonido() { }

    /** Arranca el hilo mezclador (solo la primera vez). */
    private static synchronized void iniciar() {
        if (hilo != null) return;
        hilo = new Thread(Sonido::bucleMezclador, "HiloSonido");
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Bucle del hilo de audio: suma todas las voces activas en bloques
     * pequeños y los escribe en la línea de audio.
     */
    private static void bucleMezclador() {
        SourceDataLine linea = null;
        List<Voz> activas = new ArrayList<>();
        int[] mezcla = new int[TAM_BLOQUE];
        byte[] salida = new byte[TAM_BLOQUE];
        try {
            AudioFormat formato = new AudioFormat(FRECUENCIA_MUESTREO, 8, 1, true, false);
            linea = AudioSystem.getSourceDataLine(formato);
            linea.open(formato, TAM_BLOQUE * 2);   // búfer pequeño = poca latencia
            linea.start();

            while (!Thread.currentThread().isInterrupted()) {
                // Sin sonidos activos NO dormimos: escribimos silencio. Así la línea
                // nunca se queda vacía (evita los "pops"). El ritmo lo marca
                // linea.write, que bloquea hasta que hay hueco, así que no gasta CPU.
                nuevas.drainTo(activas);
                while (activas.size() > MAX_VOCES) activas.remove(0);   // descarta los más antiguos

                Arrays.fill(mezcla, 0);
                for (Voz v : activas) {
                    int n = Math.min(TAM_BLOQUE, v.datos.length - v.pos);
                    for (int i = 0; i < n; i++) mezcla[i] += v.datos[v.pos + i];
                    v.pos += n;
                }
                activas.removeIf(v -> v.pos >= v.datos.length);

                for (int i = 0; i < TAM_BLOQUE; i++) {
                    salida[i] = (byte) Math.max(-128, Math.min(127, mezcla[i]));
                }
                linea.write(salida, 0, TAM_BLOQUE);   // bloquea hasta que hay hueco: marca el ritmo
            }
        } catch (Exception e) {
            disponible = false;                       // sin audio: el juego sigue funcionando
        } finally {
            if (linea != null) linea.close();
        }
    }

    /**
     * Genera un tono que va de frecuencia f1 a f2 durante "ms" milisegundos.
     * @param ruido      true = ruido blanco (explosiones, derrumbes)
     * @param retrasoMs  espera antes de que empiece a sonar (para encadenar notas)
     */
    private static void tono(float f1, float f2, int ms, float volumen, boolean ruido, int retrasoMs) {
        if (!disponible || silenciado) return;
        iniciar();
        int n = (int) (FRECUENCIA_MUESTREO * ms / 1000);
        int retraso = (int) (FRECUENCIA_MUESTREO * retrasoMs / 1000);
        byte[] b = new byte[retraso + n];   // los primeros "retraso" bytes quedan en silencio (0)
        double fase = 0;
        Random r = new Random(7);
        for (int i = 0; i < n; i++) {
            double t = (double) i / n;
            double f = f1 + (f2 - f1) * t;
            fase += f / FRECUENCIA_MUESTREO;
            double onda = ruido ? (r.nextDouble() * 2 - 1) : ((fase % 1.0) < 0.5 ? 1 : -1);
            double ataque = Math.min(1.0, i / 60.0);   // ~3 ms de subida: evita el clic inicial
            double envolvente = Math.min(1.0, (1 - t) * 1.6) * ataque;
            b[retraso + i] = (byte) (onda * envolvente * volumen * 90);
        }
        nuevas.offer(new Voz(b));
    }

    private static void tono(float f1, float f2, int ms, float volumen, boolean ruido) {
        tono(f1, f2, ms, volumen, ruido, 0);
    }

    // ---- Antirrebote: el mismo efecto no puede repetirse antes de un intervalo mínimo ----
    private static final int SALTO = 0, ATERRIZAR = 1, APLICAR = 2, ERROR = 3, CLIC = 4,
                             MUERTE = 5, CHECKPOINT = 6, CRUJIDO = 7, DERRUMBE = 8, VICTORIA = 9;
    private static final long[] ultimo = new long[10];          // nanoTime de la última vez
    private static final Object cerrojoAntirrebote = new Object(); // sección crítica mínima

    private static boolean permitir(int id, int minMs) {
        long ahora = System.nanoTime();
        synchronized (cerrojoAntirrebote) {
            if (ultimo[id] != 0 && ahora - ultimo[id] < minMs * 1_000_000L) return false;
            ultimo[id] = ahora;
            return true;
        }
    }

    public static void salto()       { if (permitir(SALTO, 120))      tono(320, 760, 90, 0.35f, false); }
    public static void aterrizar()   { if (permitir(ATERRIZAR, 150))  tono(160, 90, 40, 0.25f, true); }
    public static void aplicar()     {
        if (!permitir(APLICAR, 150)) return;
        tono(520, 520, 60, 0.35f, false);
        tono(780, 1040, 110, 0.35f, false, 60);
    }
    public static void error()       { if (permitir(ERROR, 200))      tono(220, 150, 160, 0.35f, false); }
    public static void clic()        { if (permitir(CLIC, 100))       tono(900, 900, 25, 0.25f, false); }
    public static void muerte()      { if (permitir(MUERTE, 500))     tono(600, 80, 380, 0.4f, false); }
    public static void checkpoint()  { if (permitir(CHECKPOINT, 300)) tono(660, 990, 140, 0.3f, false); }
    public static void crujido()     { if (permitir(CRUJIDO, 250))    tono(140, 110, 200, 0.35f, true); }
    public static void derrumbe()    { if (permitir(DERRUMBE, 300))   tono(200, 40, 350, 0.45f, true); }
    public static void victoria()    {
        if (!permitir(VICTORIA, 1000)) return;
        tono(523, 523, 120, 0.35f, false);
        tono(659, 659, 120, 0.35f, false, 120);
        tono(784, 1046, 300, 0.35f, false, 240);
    }

    public static void alternarSilencio() {
        silenciado = !silenciado;
        if (silenciado) nuevas.clear();   // al silenciar, se descartan los sonidos pendientes
    }
    public static boolean isSilenciado()  { return silenciado; }

    /** Opcional: llamar al cerrar el juego para parar el hilo de audio y liberar la línea. */
    public static synchronized void cerrar() {
        if (hilo == null) return;
        hilo.interrupt();
        try {
            hilo.join(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        hilo = null;
    }
}
