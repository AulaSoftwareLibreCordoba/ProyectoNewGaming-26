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

// Efectos chiptune generados por código. Se piden desde cualquier hilo (van a la cola "nuevas")
// y solo el hilo HiloSonido los mezcla y los manda a la tarjeta.
public final class Sonido {

    private static final float FRECUENCIA_MUESTREO = 22050f;
    private static final int TAM_BLOQUE = 256;   // ~12 ms de audio por vuelta (poco retraso)
    private static final int MAX_VOCES = 8;      // sonidos a la vez como mucho

    // un sonido ya generado y por dónde va
    private static final class Voz {
        final byte[] datos;
        int pos = 0;
        Voz(byte[] datos) { this.datos = datos; }
    }

    private static final BlockingQueue<Voz> nuevas = new LinkedBlockingQueue<>();
    private static volatile boolean disponible = true;
    private static volatile boolean silenciado = false;
    private static Thread hilo;   // protegido por synchronized
    // la tarjeta de sonido tarda ~1 s en despertar la primera vez (las primeras escrituras se quedan
    // esperando); hasta entonces lo que suene llega tarde
    private static volatile boolean listo = false;

    private Sonido() { }

    // abrir el audio cuanto antes (al arrancar el programa), para que el primer sonido no llegue tarde
    public static void precalentar() {
        iniciar();
    }

    // ¿ya suena sin retraso? (también true si no hay audio, para no esperar en balde)
    public static boolean listo() {
        return listo || !disponible;
    }

    // arranca el hilo mezclador (solo la primera vez)
    private static synchronized void iniciar() {
        if (hilo != null) return;
        hilo = new Thread(Sonido::bucleMezclador, "HiloSonido");
        hilo.setDaemon(true);
        hilo.start();
    }

    // bucle del hilo de audio: suma las voces activas y lo manda a la tarjeta de sonido
    private static void bucleMezclador() {
        SourceDataLine linea = null;
        List<Voz> activas = new ArrayList<>();
        int[] mezcla = new int[TAM_BLOQUE];
        byte[] salida = new byte[TAM_BLOQUE];
        try {
            AudioFormat formato = new AudioFormat(FRECUENCIA_MUESTREO, 8, 1, true, false);
            linea = AudioSystem.getSourceDataLine(formato);
            linea.open(formato, TAM_BLOQUE * 3);   // búfer pequeño (~35 ms) = poco retraso
            linea.start();
            int bloques = 0;

            while (!Thread.currentThread().isInterrupted()) {
                if (!listo && ++bloques > 16) {
                    listo = true;
                    nuevas.clear();   // lo pedido mientras despertaba ya llegaría tarde
                }
                // Sin sonidos escribimos silencio en vez de dormir: así no hay "pops".
                // linea.write espera a que haya hueco, así que no se come la CPU.
                nuevas.drainTo(activas);
                while (activas.size() > MAX_VOCES) activas.remove(0);   // fuera los más viejos

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
                linea.write(salida, 0, TAM_BLOQUE);
            }
        } catch (Exception e) {
            disponible = false;                       // sin audio el juego sigue igual
        } finally {
            if (linea != null) linea.close();
        }
    }

    // tono de f1 a f2 en ms milisegundos (ruido para golpes, retraso para encadenar notas)
    private static void tono(float f1, float f2, int ms, float volumen, boolean ruido, int retrasoMs) {
        if (!disponible || silenciado) return;
        volumen *= Volumen.factorEfectos();   // volumen de opciones
        if (volumen <= 0f) return;
        iniciar();
        int n = (int) (FRECUENCIA_MUESTREO * ms / 1000);
        int retraso = (int) (FRECUENCIA_MUESTREO * retrasoMs / 1000);
        byte[] b = new byte[retraso + n];   // lo del principio se queda en silencio (el retraso)
        double fase = 0;
        Random r = new Random(7);
        for (int i = 0; i < n; i++) {
            double t = (double) i / n;
            double f = f1 + (f2 - f1) * t;
            fase += f / FRECUENCIA_MUESTREO;
            double onda = ruido ? (r.nextDouble() * 2 - 1) : ((fase % 1.0) < 0.5 ? 1 : -1);
            double ataque = Math.min(1.0, i / 60.0);   // subida suave para que no haga clic
            double envolvente = Math.min(1.0, (1 - t) * 1.6) * ataque;
            b[retraso + i] = (byte) (onda * envolvente * volumen * 90);
        }
        nuevas.offer(new Voz(b));
    }

    private static void tono(float f1, float f2, int ms, float volumen, boolean ruido) {
        tono(f1, f2, ms, volumen, ruido, 0);
    }

    // antirrebote: el mismo sonido no se puede repetir demasiado seguido
    private static final int SALTO = 0, ATERRIZAR = 1, APLICAR = 2, ERROR = 3, CLIC = 4,
                             MUERTE = 5, CHECKPOINT = 6, CRUJIDO = 7, DERRUMBE = 8, VICTORIA = 9,
                             RECOGER = 10;
    private static final long[] ultimo = new long[11];         // última vez que sonó cada uno
    private static final Object cerrojoAntirrebote = new Object();

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
    // coger comida: dos notas que suben
    public static void recoger()     {
        if (!permitir(RECOGER, 150)) return;
        tono(880, 880, 60, 0.3f, false);
        tono(1320, 1760, 120, 0.3f, false, 60);
    }
    public static void victoria()    {
        if (!permitir(VICTORIA, 1000)) return;
        tono(523, 523, 120, 0.35f, false);
        tono(659, 659, 120, 0.35f, false, 120);
        tono(784, 1046, 300, 0.35f, false, 240);
    }

    // ---- teclado (animación de arranque) ----

    private static final Random azarTeclas = new Random();
    private static long ultimaTecla;

    // ruido pasado por un filtro de banda (biquad): suena a golpe de plástico, no a pitido
    private static final class Banda {
        private final double b0, b2, a1, a2;
        private double x1, x2, y1, y2;

        Banda(double centro, double q) {
            double w = 2 * Math.PI * centro / FRECUENCIA_MUESTREO, alfa = Math.sin(w) / (2 * q), a0 = 1 + alfa;
            b0 = alfa / a0; b2 = -alfa / a0; a1 = -2 * Math.cos(w) / a0; a2 = (1 - alfa) / a0;
        }

        double paso(double x) {
            double y = b0 * x + b2 * x2 - a1 * y1 - a2 * y2;
            x2 = x1; x1 = x; y2 = y1; y1 = y;
            return y;
        }
    }

    // pulsación de una tecla de teclado de verdad:
    //   "tac" = ruido filtrado a 2-3 kHz que se apaga en unos 10 ms (el plástico de la tecla)
    //   golpe = un poco de ruido más grave al llegar al fondo (el cuerpo del teclado)
    //   soltar (solo ENTER) = otro "tac" flojito
    // Cada tecla cambia un poco de tono y fuerza, como al escribir de verdad
    private static void golpeTecla(float volumen, int retrasoMs, boolean grande) {
        if (!disponible || silenciado) return;
        volumen *= Volumen.factorEfectos();
        if (volumen <= 0f) return;
        iniciar();
        int ms = grande ? 140 : 45;
        int n = (int) (FRECUENCIA_MUESTREO * ms / 1000);
        int retraso = (int) (FRECUENCIA_MUESTREO * retrasoMs / 1000);
        byte[] b = new byte[retraso + n];
        Random r;
        Banda tac, cuerpo, suelta;
        double fondo;
        synchronized (azarTeclas) {
            r = new Random(azarTeclas.nextLong());
            tac = new Banda((grande ? 1500 : 2200) + azarTeclas.nextInt(900), 1.4);
            cuerpo = new Banda((grande ? 260 : 380) + azarTeclas.nextInt(120), 2.0);
            suelta = new Banda(2600 + azarTeclas.nextInt(600), 1.6);
            fondo = (grande ? 0.006 : 0.004) + azarTeclas.nextInt(3) / 1000.0;
        }
        for (int i = 0; i < n; i++) {
            double s = i / FRECUENCIA_MUESTREO;
            double ruido = r.nextDouble() * 2 - 1;
            double ataque = Math.min(1.0, i / 6.0);
            double golpeTac = tac.paso(ruido) * Math.exp(-s * (grande ? 160 : 240)) * 2.6;
            double ds = s - fondo;
            double golpeCuerpo = cuerpo.paso(ds < 0 ? 0 : ruido) * (ds < 0 ? 0 : Math.exp(-ds * 120)) * 3.2;
            double soltar = 0;
            if (grande && s > 0.095) soltar = suelta.paso(ruido) * Math.exp(-(s - 0.095) * 300) * 1.2;
            double v = (golpeTac + golpeCuerpo + soltar) * ataque;
            b[retraso + i] = (byte) Math.max(-127, Math.min(127, v * volumen * 100));
        }
        nuevas.offer(new Voz(b));
    }

    // una tecla (con un mínimo de separación para no saturar)
    public static void tecla() {
        long ahora = System.nanoTime();
        synchronized (azarTeclas) {
            if (ahora - ultimaTecla < 70_000_000L) return;
            ultimaTecla = ahora;
        }
        golpeTecla(0.30f + azarTeclas.nextFloat() * 0.08f, 0, false);
    }

    // varias teclas seguidas, a ritmo de alguien escribiendo (unas 8 por segundo)
    public static void rafagaTeclas(int teclas) {
        int t = 0;
        for (int i = 0; i < teclas; i++) {
            golpeTecla(0.26f + azarTeclas.nextFloat() * 0.08f, t, false);
            t += 95 + azarTeclas.nextInt(60);
        }
    }

    public static void teclaEnter() {
        golpeTecla(0.40f, 0, true);
    }

    public static void alternarSilencio() {
        silenciado = !silenciado;
        if (silenciado) nuevas.clear();   // al silenciar tiramos lo pendiente
    }
    public static boolean isSilenciado()  { return silenciado; }

    // para el hilo de audio al cerrar el juego
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
