package cordoba.interfaz;

import java.awt.*;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Utilidades de dibujo: fuentes, textos con sombra, paneles y partir líneas.
public final class Ui {

    private Ui() { }

    // Pixel Operator va a 16 px; los títulos al doble, sin tamaños intermedios
    public static final int TAMANO_BASE = 16, TAMANO_TITULO = 32, INTERLINEADO = 15;
    private static final Font REGULAR = cargarFuente("PixelOperator.ttf");
    private static final Font NEGRITA = cargarFuente("PixelOperator-Bold.ttf");
    private static final Font MONO = cargarFuente("PixelOperatorMono.ttf");
    public static final Font FUENTE        = REGULAR.deriveFont((float) TAMANO_BASE);
    public static final Font FUENTE_PEQ    = FUENTE;
    public static final Font FUENTE_CODIGO = FUENTE;
    public static final Font FUENTE_MONO   = MONO.deriveFont((float) TAMANO_BASE);
    public static final Font FUENTE_HUD    = NEGRITA.deriveFont((float) TAMANO_BASE);
    public static final Font FUENTE_GRANDE = NEGRITA.deriveFont((float) TAMANO_TITULO);
    public static final Font FUENTE_TITULO = FUENTE_GRANDE;

    // con 6 posiciones de minutos cabe cualquier tiempo del juego
    public static final int CELDA_NUMERO = 8, CELDAS_MINUTOS = 6;
    public static final int CELDAS_TIEMPO = CELDAS_MINUTOS + 6;
    public static final int ANCHO_TIEMPO = CELDAS_TIEMPO * CELDA_NUMERO;
    public static final int ANCHO_CRONOMETRO = 170, X_TIEMPO_HUD = 68;

    private static Font cargarFuente(String nombre) {
        String recurso = "/assets/fonts/" + nombre;
        try (InputStream entrada = Ui.class.getResourceAsStream(recurso)) {
            if (entrada != null) return Font.createFont(Font.TRUETYPE_FONT, entrada);
            try (InputStream archivo = Files.newInputStream(Path.of("assets", "fonts", nombre))) {
                return Font.createFont(Font.TRUETYPE_FONT, archivo);
            }
        } catch (IOException | FontFormatException e) {
            throw new IllegalStateException("No se puede cargar " + recurso
                    + ". Ejecuta desde la carpeta del proyecto o incluye assets en el classpath.", e);
        }
    }

    // aplicar al lienzo antes de dibujar imágenes y letras
    public static void preparar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    // lo más grande que quepa en la ventana sin deformar (16:9)
    public static double escalaVentana(int ancho, int alto) {
        return Math.max(0.1, Math.min(ancho / 640.0, alto / 360.0));
    }

    // paleta
    public static final Color PANEL      = new Color(0x1E1E2E);
    public static final Color PANEL_2    = new Color(0x2A2A3D);
    public static final Color BORDE      = new Color(0xF2E38A);
    public static final Color TEXTO      = new Color(0xF4F1E6);
    public static final Color VERDE      = new Color(0x7BE07B);
    public static final Color AMARILLO   = new Color(0xF2D985);
    public static final Color ROJO       = new Color(0xFF6B6B);
    public static final Color GRIS       = new Color(0xBFC3D2);
    public static final Color SOMBRA     = new Color(0x14121C);

    // texto con sombra de 1 px (se lee sobre cualquier fondo)
    public static void texto(Graphics2D g, String s, int x, int y, Color color) {
        g.setColor(SOMBRA);
        dibujarTexto(g, s, x + 1, y + 1);
        g.setColor(color);
        dibujarTexto(g, s, x, y);
    }

    // contorno de 1 px que no se mete en los huecos de las letras
    public static void textoContorno(Graphics2D g, String s, int x, int y, Color color, Color contorno) {
        g.setColor(contorno);
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (dx != 0 || dy != 0) dibujarTexto(g, s, x + dx, y + dy);
        g.setColor(color);
        dibujarTexto(g, s, x, y);
    }

    // flechas y ordinales que no trae Pixel Operator, dibujados a mano
    private static void dibujarTexto(Graphics2D g, String s, int x, int y) {
        int inicio = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean ordinal = c == 'º' || c == 'ª';
            if (!ordinal && (c < '←' || c > '↓')) continue;
            String tramo = s.substring(inicio, i);
            g.drawString(tramo, x, y);
            x += g.getFontMetrics().stringWidth(tramo);
            int paso = Math.max(1, Math.round(g.getFont().getSize2D() / TAMANO_BASE));
            int ancho = g.getFontMetrics().charWidth(c);
            if (ordinal) {
                int sx = x + paso, sy = y - 10 * paso;
                g.drawRect(sx, sy, 2 * paso, 3 * paso);
                if (c == 'ª') g.fillRect(sx, sy + 2 * paso, 3 * paso, paso);
                x += ancho;
                inicio = i + 1;
                continue;
            }
            int cx = x + ancho / 2, cy = y - 3 * paso;
            boolean horizontal = c == '←' || c == '→';
            int sentido = c == '←' || c == '↑' ? -1 : 1;
            for (int j = -2; j <= 2; j++) {
                g.fillRect(cx + (horizontal ? j * paso : 0), cy + (horizontal ? 0 : j * paso), paso, paso);
            }
            for (int j = 1; j <= 2; j++) {
                int punta = sentido * (2 - j) * paso;
                for (int lado : new int[]{-1, 1})
                    g.fillRect(cx + (horizontal ? punta : lado * j * paso),
                            cy + (horizontal ? lado * j * paso : punta), paso, paso);
            }
            x += ancho;
            inicio = i + 1;
        }
        g.drawString(s.substring(inicio), x, y);
    }

    // cada carácter en su celda, así ':' y '.' no bailan
    public static void tiempo(Graphics2D g, String tiempo, int x, int y, Color color) {
        int separador = tiempo.indexOf(':');
        if (separador < 1 || separador > CELDAS_MINUTOS
                || tiempo.length() - separador != 6 || tiempo.charAt(separador + 3) != '.')
            throw new IllegalArgumentException("Tiempo esperado: minutos:ss.cc: " + tiempo);
        Font anterior = g.getFont();
        g.setFont(FUENTE_MONO);
        int relleno = CELDAS_MINUTOS - separador;
        for (int i = 0; i < tiempo.length(); i++)
            texto(g, tiempo.substring(i, i + 1), x + (relleno + i) * CELDA_NUMERO, y, color);
        g.setFont(anterior);
    }

    public static int anchoDato(Graphics2D g, String etiqueta, String valor) {
        return g.getFontMetrics(FUENTE).stringWidth(etiqueta)
                + g.getFontMetrics(FUENTE_MONO).stringWidth(valor);
    }

    // etiqueta normal y valor monoespaciado
    public static void dato(Graphics2D g, String etiqueta, String valor, int x, int y, Color color) {
        Font anterior = g.getFont();
        g.setFont(FUENTE);
        texto(g, etiqueta, x, y, color);
        x += g.getFontMetrics().stringWidth(etiqueta);
        g.setFont(FUENTE_MONO);
        texto(g, valor, x, y, color);
        g.setFont(anterior);
    }

    public static void datoCentrado(Graphics2D g, String etiqueta, String valor, int cx, int y, Color color) {
        dato(g, etiqueta, valor, cx - anchoDato(g, etiqueta, valor) / 2, y, color);
    }

    public static void textoCentrado(Graphics2D g, String s, int cx, int y, Color color) {
        int w = g.getFontMetrics().stringWidth(s);
        texto(g, s, cx - w / 2, y, color);
    }

    // acorta por anchura sin cambiar el tamaño de letra
    public static String recortar(Graphics2D g, String s, int ancho) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= ancho) return s;
        String puntos = "...";
        if (fm.stringWidth(puntos) > ancho) return "";
        int fin = s.length();
        while (fin > 0 && fm.stringWidth(s.substring(0, fin) + puntos) > ancho) fin--;
        return s.substring(0, fin) + puntos;
    }

    public static int baseCentrada(Graphics2D g, int y, int alto) {
        FontMetrics fm = g.getFontMetrics();
        return y + (alto - fm.getAscent() - fm.getDescent()) / 2 + fm.getAscent();
    }

    // panel con doble borde retro
    public static void panel(Graphics2D g, int x, int y, int w, int h, Color fondo, Color borde) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(x + 3, y + 3, w, h);
        g.setColor(fondo);
        g.fillRect(x, y, w, h);
        g.setColor(borde);
        g.drawRect(x, y, w - 1, h - 1);
        g.setColor(borde.darker());
        g.drawRect(x + 2, y + 2, w - 5, h - 5);
    }

    // parte el texto en líneas que quepan en anchoMax
    public static List<String> ajustar(Graphics2D g, String texto, int anchoMax) {
        List<String> lineas = new ArrayList<>();
        FontMetrics fm = g.getFontMetrics();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            String prueba = actual.length() == 0 ? palabra : actual + " " + palabra;
            if (fm.stringWidth(prueba) > anchoMax && actual.length() > 0) {
                lineas.add(actual.toString());
                actual = new StringBuilder(palabra);
            } else {
                actual = new StringBuilder(prueba);
            }
        }
        if (actual.length() > 0) lineas.add(actual.toString());
        return lineas;
    }
}
