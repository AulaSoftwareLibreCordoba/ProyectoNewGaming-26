package cordoba.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Efectos de sonido "chiptune" generados por código (onda cuadrada y ruido).
 * No necesita archivos de audio. Si el ordenador no tiene salida de sonido,
 * simplemente se desactiva sin provocar errores.
 *
 * Pulsa M durante la partida para silenciar / activar el sonido.
 */
public final class Sonido {

    private static final float FRECUENCIA_MUESTREO = 22050f;
    private static final BlockingQueue<byte[]> cola = new LinkedBlockingQueue<>();
    private static volatile boolean disponible = true;
    private static volatile boolean silenciado = false;
    private static boolean iniciado = false;

    private Sonido() { }

    /** Abre la línea de audio en un hilo aparte (solo la primera vez). */
    private static synchronized void iniciar() {
        if (iniciado) return;
        iniciado = true;
        Thread hilo = new Thread(() -> {
            try {
                AudioFormat formato = new AudioFormat(FRECUENCIA_MUESTREO, 8, 1, true, false);
                SourceDataLine linea = AudioSystem.getSourceDataLine(formato);
                linea.open(formato, 2048);
                linea.start();
                while (true) {
                    byte[] datos = cola.take();
                    linea.write(datos, 0, datos.length);
                }
            } catch (Exception e) {
                disponible = false;   // sin audio: el juego sigue funcionando
            }
        }, "HiloSonido");
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Genera un tono que va de frecuencia f1 a f2 durante "ms" milisegundos.
     * @param ruido true = ruido blanco (explosiones, derrumbes)
     */
    private static void tono(float f1, float f2, int ms, float volumen, boolean ruido) {
        if (!disponible || silenciado) return;
        iniciar();
        if (cola.size() > 3) return;  // evitamos que se acumulen sonidos
        int n = (int) (FRECUENCIA_MUESTREO * ms / 1000);
        byte[] b = new byte[n];
        double fase = 0;
        Random r = new Random(7);
        for (int i = 0; i < n; i++) {
            double t = (double) i / n;
            double f = f1 + (f2 - f1) * t;
            fase += f / FRECUENCIA_MUESTREO;
            double onda = ruido ? (r.nextDouble() * 2 - 1) : ((fase % 1.0) < 0.5 ? 1 : -1);
            double envolvente = Math.min(1.0, (1 - t) * 1.6);
            b[i] = (byte) (onda * envolvente * volumen * 90);
        }
        cola.offer(b);
    }

    public static void salto()       { tono(320, 760, 90, 0.35f, false); }
    public static void aterrizar()   { tono(160, 90, 40, 0.25f, true); }
    public static void aplicar()     { tono(520, 520, 60, 0.35f, false); tono(780, 1040, 110, 0.35f, false); }
    public static void error()       { tono(220, 150, 160, 0.35f, false); }
    public static void clic()        { tono(900, 900, 25, 0.25f, false); }
    public static void muerte()      { tono(600, 80, 380, 0.4f, false); }
    public static void checkpoint()  { tono(660, 990, 140, 0.3f, false); }
    public static void crujido()     { tono(140, 110, 200, 0.35f, true); }
    public static void derrumbe()    { tono(200, 40, 350, 0.45f, true); }
    public static void victoria()    { tono(523, 523, 120, 0.35f, false); tono(659, 659, 120, 0.35f, false); tono(784, 1046, 300, 0.35f, false); }

    public static void alternarSilencio() { silenciado = !silenciado; }
    public static boolean isSilenciado()  { return silenciado; }
}
