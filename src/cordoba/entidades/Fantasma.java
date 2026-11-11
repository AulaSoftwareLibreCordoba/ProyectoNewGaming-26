package cordoba.entidades;

import cordoba.graficos.Sprites;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * FANTASMA DEL RÉCORD: un Coco semitransparente que repite la partida más
 * rápida del nivel 2.
 *
 * No se graban las teclas, sino la POSICIÓN y el dibujo de Coco en cada
 * fotograma del nivel. Así el fantasma siempre hace exactamente lo mismo,
 * aunque el jugador del récord cambiara la física en mitad de la partida.
 *
 * El fotograma i del fantasma corresponde al fotograma i del nivel: si la
 * consola o la pausa detienen el tiempo, el fantasma también se detiene.
 *
 * Fichero binario: "COCO", versión, nombre, fotogramas del nivel y después
 * una muestra por fotograma (x, y como short + 1 byte con dibujo y dirección).
 */
public class Fantasma {

    private static final int MAGICO = 0x434F434F;   // "COCO"
    private static final int VERSION = 1;

    /** Dibujos posibles de Coco (el índice es lo que se guarda en el fichero). */
    private static final BufferedImage[] DIBUJOS = {
        Sprites.COCO_QUIETO, Sprites.COCO_PARPADEO, Sprites.COCO_PASO1, Sprites.COCO_PASO2,
        Sprites.COCO_SALTO, Sprites.COCO_CAIDA, Sprites.COCO_VICTORIA1, Sprites.COCO_VICTORIA2,
        Sprites.COCO_HERIDO
    };

    private final String nombre;
    private final int framesNivel;
    private final short[] xs, ys;
    private final byte[] info;   // bits 0-6: índice del dibujo · bit 7: mira a la derecha

    private Fantasma(String nombre, int framesNivel, short[] xs, short[] ys, byte[] info) {
        this.nombre = nombre;
        this.framesNivel = framesNivel;
        this.xs = xs;
        this.ys = ys;
        this.info = info;
    }

    public String getNombre()   { return nombre; }
    public int getFramesNivel() { return framesNivel; }

    // =====================================================================
    //  Reproducción
    // =====================================================================

    private int indice(int frameNivel) {
        return Math.max(0, Math.min(xs.length - 1, frameNivel - 1));
    }

    /** Caja de colisión (x, y) en el fotograma indicado del nivel. */
    public int x(int frameNivel) { return xs[indice(frameNivel)]; }
    public int y(int frameNivel) { return ys[indice(frameNivel)]; }

    public BufferedImage dibujo(int frameNivel) {
        int b = info[indice(frameNivel)];
        BufferedImage img = DIBUJOS[Math.min(DIBUJOS.length - 1, b & 0x7F)];
        return (b & 0x80) != 0 ? img : Sprites.volteada(img);
    }

    public boolean miraDerecha(int frameNivel) {
        return (info[indice(frameNivel)] & 0x80) != 0;
    }

    /** ¿Ya ha llegado a la meta? (entonces se queda celebrando) */
    public boolean terminado(int frameNivel) {
        return frameNivel > xs.length;
    }

    // =====================================================================
    //  Grabación
    // =====================================================================

    /** Va guardando la posición de Coco en cada fotograma del nivel. */
    public static class Grabadora {
        private short[] xs = new short[4096], ys = new short[4096];
        private byte[] info = new byte[4096];
        private int n;

        public void grabar(Player p) {
            if (n == xs.length) {
                xs = Arrays.copyOf(xs, n * 2);
                ys = Arrays.copyOf(ys, n * 2);
                info = Arrays.copyOf(info, n * 2);
            }
            xs[n] = aShort(p.x);
            ys[n] = aShort(p.y);
            int idx = 0;
            BufferedImage base = p.fotogramaBase();
            for (int i = 0; i < DIBUJOS.length; i++) if (DIBUJOS[i] == base) idx = i;
            info[n] = (byte) (idx | (p.mirandoDerecha ? 0x80 : 0));
            n++;
        }

        /** Crea el fantasma con lo grabado hasta ahora. */
        public Fantasma crear(String nombre, int framesNivel) {
            return new Fantasma(nombre, framesNivel,
                    Arrays.copyOf(xs, n), Arrays.copyOf(ys, n), Arrays.copyOf(info, n));
        }

        public boolean vacia() { return n == 0; }

        private static short aShort(float v) {
            return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(v)));
        }
    }

    /** Copia con otro nombre (el nombre se conoce al final de la partida). */
    public Fantasma conNombre(String nuevo) {
        return new Fantasma(nuevo, framesNivel, xs, ys, info);
    }

    // =====================================================================
    //  Fichero
    // =====================================================================

    public void guardar(Path fichero) throws IOException {
        Path tmp = fichero.resolveSibling(fichero.getFileName() + ".tmp");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(tmp)))) {
            out.writeInt(MAGICO);
            out.writeInt(VERSION);
            out.writeUTF(nombre);
            out.writeInt(framesNivel);
            out.writeInt(xs.length);
            for (int i = 0; i < xs.length; i++) {
                out.writeShort(xs[i]);
                out.writeShort(ys[i]);
                out.writeByte(info[i]);
            }
        }
        Files.move(tmp, fichero, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /** Carga el fantasma; devuelve null si no existe o está dañado. */
    public static Fantasma cargar(Path fichero) {
        if (!Files.exists(fichero)) return null;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(fichero)))) {
            if (in.readInt() != MAGICO || in.readInt() != VERSION) return null;
            String nombre = in.readUTF();
            int frames = in.readInt();
            int n = in.readInt();
            if (n <= 0 || n > 60 * 60 * 60) return null;   // máximo 1 hora de grabación
            short[] xs = new short[n], ys = new short[n];
            byte[] info = new byte[n];
            for (int i = 0; i < n; i++) {
                xs[i] = in.readShort();
                ys[i] = in.readShort();
                info[i] = in.readByte();
            }
            return new Fantasma(nombre, frames, xs, ys, info);
        } catch (IOException e) {
            System.err.println("No se pudo leer el fantasma: " + e.getMessage());
            return null;
        }
    }
}
