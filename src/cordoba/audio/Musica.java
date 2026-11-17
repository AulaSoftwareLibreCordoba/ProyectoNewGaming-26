package cordoba.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Música de fondo. Los archivos van en assets/audio/musica/ (menu, intro, nivel1, nivel2, victoria .wav).
// Si falta alguno, esa pantalla va sin música.
public final class Musica {

    private static final String CARPETA = "assets/audio/musica/";
    private static final String[] EXTENSIONES = { ".wav", ".aiff", ".aif", ".au" };

    // en decibelios
    private static final float VOLUMEN_DB = -8f, ATENUACION_DB = -10f;

    private static final Map<String, Clip> CARGADAS = new HashMap<>();   // null = no existe
    private static String actual;      // pista que debería sonar
    private static Clip sonando;       // la que suena de verdad
    private static float aplicadoDb = Float.NaN;   // volumen puesto ahora mismo
    private static volatile boolean disponible = true;

    private Musica() { }

    // se llama cada fotograma; si la pista ya suena no hace nada
    public static synchronized void reproducir(String pista, boolean bajita) {
        if (!disponible) return;
        if (pista != null && !pista.equals(actual)) {
            parar();
            actual = pista;
            sonando = cargar(pista);
            if (sonando != null) {
                sonando.setFramePosition(0);
                aplicadoDb = Float.NaN;
            }
        }
        if (sonando == null) return;
        // volumen de opciones, más bajo en pausa
        float factor = Volumen.factorMusica();
        if (factor > 0f) {
            float db = VOLUMEN_DB + (bajita ? ATENUACION_DB : 0f) + (float) (20 * Math.log10(factor));
            if (Float.isNaN(aplicadoDb) || Math.abs(db - aplicadoDb) > 0.01f) {
                aplicadoDb = db;
                volumen(sonando, db);
            }
        }
        // silencio (M) o volumen a 0
        if (Sonido.isSilenciado() || factor <= 0f) {
            if (sonando.isRunning()) sonando.stop();
        } else if (!sonando.isRunning()) {
            sonando.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // sin música
    public static synchronized void parar() {
        if (sonando != null) sonando.stop();
        sonando = null;
        actual = null;
    }

    // carga assets/audio/musica/<pista>.wav
    private static Clip cargar(String pista) {
        if (CARGADAS.containsKey(pista)) return CARGADAS.get(pista);
        Clip clip = null;
        for (String ext : EXTENSIONES) {
            File f = new File(CARPETA + pista + ext);
            if (!f.exists()) continue;
            try (InputStream in = new BufferedInputStream(new java.io.FileInputStream(f));
                 AudioInputStream original = AudioSystem.getAudioInputStream(in)) {
                // a PCM de 16 bits
                AudioFormat o = original.getFormat();
                AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, o.getSampleRate(), 16,
                        o.getChannels(), o.getChannels() * 2, o.getSampleRate(), false);
                try (AudioInputStream convertido = AudioSystem.getAudioInputStream(pcm, original)) {
                    clip = AudioSystem.getClip();
                    clip.open(convertido);
                }
            } catch (javax.sound.sampled.LineUnavailableException e) {
                disponible = false;   // sin tarjeta de sonido
                System.err.println("Música desactivada: no hay salida de audio.");
            } catch (Exception e) {
                System.err.println("No se pudo cargar la música " + f + ": " + e.getMessage()
                        + " (¿es un .wav normal? Java no lee .mp3 ni .ogg)");
            }
            break;
        }
        CARGADAS.put(pista, clip);
        return clip;
    }

    private static void volumen(Clip clip, float db) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl c = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        c.setValue(Math.max(c.getMinimum(), Math.min(c.getMaximum(), db)));
    }
}
