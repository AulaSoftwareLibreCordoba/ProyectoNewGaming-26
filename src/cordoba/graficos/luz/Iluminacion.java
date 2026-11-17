package cordoba.graficos.luz;

import cordoba.entidades.Player;
import cordoba.niveles.Level;
import cordoba.niveles.elementos.Farola;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/*
 * La luz de la escena, por pasos:
 *  0. fondo: resplandor del sol y, dentro de la Mezquita, penumbra con lámparas
 *  1. penumbra: capa oscura con "agujeros" donde hay luces (farolas, meta, Coco)
 *  2. halos de color alrededor de cada luz
 *  3. viñeta en las esquinas
 * Solo lee el estado del juego. Los valores están en EstiloVisual.
 */
public class Iluminacion {

    private static final int ANCHO = 640, ALTO = 360;

    private final BufferedImage penumbra = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB_PRE);
    // penumbra + viñeta ya mezcladas (solo se rehace si cambian)
    private final BufferedImage base = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB_PRE);
    private long claveBase = -1;
    // true si la viñeta ya va dentro de la penumbra este fotograma
    private boolean vinetaIncluida;

    private BufferedImage vinetaImagen;
    private List<Trozo> vinetaTrozos;
    private float vinetaFuerza = -1;

    // luz del fondo; llamar justo después de pintar sus capas
    public void fondo(Graphics2D g, Level.Tema tema, int camX, int camY) {
        EstiloVisual e = EstiloVisual.de(tema);
        float interior = EstiloVisual.factorInterior(tema, camY);
        Composite antes = g.getComposite();
        if (e.brilloSol > 0 && interior < 1) {
            int sy = e.solSigueCamara ? e.solY - camY / 4 : e.solY;
            halo(g, e.solX, sy, e.solRadio, e.colorSol, e.brilloSol * (1 - interior));
        }
        g.setComposite(antes);
    }

    // capa del fondo con su neblina, calculada una vez
    public static synchronized BufferedImage prepararCapa(Level.Tema tema, int indice, BufferedImage capa) {
        BufferedImage conNiebla = Profundidad.capaConNiebla(tema, indice, capa);
        EstiloVisual e = EstiloVisual.de(tema);
        return RAPIDA.computeIfAbsent(conNiebla, Iluminacion::sinTransparenciaSiEsOpaca);
    }

    // una capa sin transparencias (el cielo) se copia mucho más rápido como RGB
    private static BufferedImage sinTransparenciaSiEsOpaca(BufferedImage c) {
        int w = c.getWidth(), h = c.getHeight();
        int[] px = c.getRGB(0, 0, w, h, null, 0, w);
        for (int v : px) if ((v >>> 24) != 255) return c;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, w, h, px, 0, w);
        return img;
    }

    private static final Map<BufferedImage, BufferedImage> RAPIDA = new IdentityHashMap<>();

    // penumbra + halos; después del escenario y antes del HUD
    public void aplicar(Graphics2D g, Level nivel, Player p, int camX, int camY, int frame) {
        Level.Tema tema = nivel.getTema();
        EstiloVisual e = EstiloVisual.de(tema);
        float interior = EstiloVisual.factorInterior(tema, camY);

        // 1. penumbra
        float oscuridad = e.oscuridadExterior + (e.oscuridadInterior - e.oscuridadExterior) * interior;
        vinetaIncluida = oscuridad > 0.01f;
        if (vinetaIncluida) {
            int color = mezclar(e.colorAmbiente, e.colorInterior, interior);
            int alfa = Math.round(oscuridad * 255);
            long clave = ((long) color << 16) | ((long) alfa << 8) | Math.round(e.vineta * 255);
            if (clave != claveBase) {
                Graphics2D b = base.createGraphics();
                b.setComposite(AlphaComposite.Src);
                b.setColor(new Color((color & 0xFFFFFF) | (alfa << 24), true));
                b.fillRect(0, 0, ANCHO, ALTO);
                b.setComposite(AlphaComposite.SrcOver);
                if (e.vineta > 0) b.drawImage(vinetaImagen(e.vineta), 0, 0, null);
                b.dispose();
                claveBase = clave;
            }
            Graphics2D m = penumbra.createGraphics();
            m.setComposite(AlphaComposite.Src);
            m.drawImage(base, 0, 0, null);
            // cada luz borra un trozo de penumbra
            m.setComposite(AlphaComposite.DstOut);
            for (Farola f : nivel.getFarolas()) {
                if (f.encendida) recortar(m, (int) f.x - camX, (int) f.ySuelo - 29 - camY, parpadeo(e.radioFarola + 14, frame, (int) f.x), 1f);
            }
            Rectangle meta = nivel.getMeta();
            recortar(m, meta.x + meta.width / 2 - camX, meta.y + meta.height / 2 - camY, e.radioMeta + 10, 1f);
            if (!p.muerto) recortar(m, Math.round(p.centroX()) - camX, Math.round(p.centroY()) - camY, e.radioJugador, 0.85f);
            m.dispose();
            g.drawImage(penumbra, 0, 0, null);
        }

        // 2. halos
        Composite antes = g.getComposite();
        for (Farola f : nivel.getFarolas()) {
            if (f.encendida) halo(g, (int) f.x - camX, (int) f.ySuelo - 29 - camY,
                                  parpadeo(e.radioFarola, frame, (int) f.x), e.colorFarola, e.brilloFarola);
        }
        Rectangle meta = nivel.getMeta();
        float pulso = 0.85f + 0.15f * (float) Math.sin(frame * 0.05);
        halo(g, meta.x + meta.width / 2 - camX, meta.y + meta.height / 2 - camY, e.radioMeta, e.colorMeta, e.brilloMeta * pulso);
        g.setComposite(antes);
    }

    // bordes más oscuros; al final del escenario
    public void vineta(Graphics2D g, Level.Tema tema) {
        float fuerza = EstiloVisual.de(tema).vineta;
        if (fuerza <= 0 || vinetaIncluida) return;   // si hay penumbra, ya va dentro
        vinetaImagen(fuerza);
        for (Trozo t : vinetaTrozos) g.drawImage(t.img, t.x, t.y, null);
    }

    private BufferedImage vinetaImagen(float fuerza) {
        if (vinetaImagen == null || vinetaFuerza != fuerza) {
            vinetaImagen = crearVineta(fuerza);
            vinetaTrozos = trozos(vinetaImagen);
            vinetaFuerza = fuerza;
        }
        return vinetaImagen;
    }

    private static void recortar(Graphics2D m, int cx, int cy, int radio, float fuerza) {
        if (radio <= 0 || fuera(cx, cy, radio)) return;
        m.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, Math.min(1f, fuerza)));
        m.drawImage(SpritesLuz.mascara(radio), cx - radio, cy - radio, null);
    }

    private static void halo(Graphics2D g, int cx, int cy, int radio, int color, float alfa) {
        if (radio <= 0 || alfa <= 0.01f || fuera(cx, cy, radio)) return;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alfa)));
        g.drawImage(SpritesLuz.halo(radio, color), cx - radio, cy - radio, null);
    }

    private static boolean fuera(int cx, int cy, int r) {
        return cx + r < 0 || cx - r > ANCHO || cy + r < 0 || cy - r > ALTO;
    }

    // las llamas tiemblan: el radio cambia 2 px de vez en cuando
    private static int parpadeo(int radio, int frame, int semilla) {
        int t = (frame + semilla * 7) / 9;
        return radio + (((t * 1103515245 + 12345) >>> 16) % 3 == 0 ? 2 : 0);
    }

    private static int mezclar(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int gg = Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bb = Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (gg << 8) | bb;
    }

    // trozo de viñeta (el centro es transparente y no se pinta: ahorra tiempo)
    private record Trozo(BufferedImage img, int x, int y) { }

    private static List<Trozo> trozos(BufferedImage img) {
        List<Trozo> r = new ArrayList<>();
        int tw = 32, th = 24;
        for (int y = 0; y < ALTO; y += th) {
            for (int x = 0; x < ANCHO; x += tw) {
                boolean vacio = true;
                for (int yy = y; yy < y + th && vacio; yy++)
                    for (int xx = x; xx < x + tw; xx++)
                        if ((img.getRGB(xx, yy) >>> 24) != 0) { vacio = false; break; }
                if (!vacio) r.add(new Trozo(img.getSubimage(x, y, tw, th), x, y));
            }
        }
        return r;
    }

    private static BufferedImage crearVineta(float fuerza) {
        BufferedImage img = new BufferedImage(ANCHO, ALTO, BufferedImage.TYPE_INT_ARGB_PRE);
        for (int y = 0; y < ALTO; y++) {
            for (int x = 0; x < ANCHO; x++) {
                double dx = (x - ANCHO / 2.0) / (ANCHO / 2.0), dy = (y - ALTO / 2.0) / (ALTO / 2.0);
                double d = Math.sqrt(dx * dx * 0.8 + dy * dy * 0.8);
                double a = Math.max(0, d - 0.55) / 0.6;
                a = Math.min(1, a * a) * fuerza;
                // escalones de 1/24 con tramado para que no se noten bandas
                int paso = (int) Math.round(a * 24 + (((x ^ y) & 1) == 0 ? 0.25 : -0.25));
                int alfa = Math.max(0, Math.min(255, paso * 255 / 24));
                img.setRGB(x, y, alfa << 24);
            }
        }
        return img;
    }
}
