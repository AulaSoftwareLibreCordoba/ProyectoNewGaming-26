package cordoba.interfaz;

import cordoba.audio.Sonido;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
 * Animación al abrir el juego: una terminal escribe "jugar", compila, se llena de código cada vez
 * más deprisa hasta volverse "salvaje" y, letra a letra, el código se convierte en los píxeles
 * del menú. Cualquier tecla o clic la salta.
 *
 *   0-45     cursor y se escribe el comando
 *   45-150   compilación
 *   150-205  avalancha de código
 *   185-235  el código se vuelve loco (letras que cambian y se ponen verdes)
 *   215-300  cada letra pasa a bloque de color y luego al menú
 */
public class Arranque {

    private static final int ESCRIBIR = 45, COMPILAR = 150, AVALANCHA = 205, LOCURA = 185, TRANSFORMAR = 215, FIN = 300;
    private static final int ALTO_LINEA = 12;
    private static final String PROMPT = "C:\\CordobaCodigoSalvaje> ";
    private static final String COMANDO = "jugar";

    private static final String[] COMPILACION = {
        "javac -encoding UTF-8 -d out -sourcepath src src\\cordoba\\Main.java",
        "  compilando cordoba/fisica/PhysicsConfig.java",
        "  compilando cordoba/entidades/Player.java",
        "  compilando cordoba/niveles/Level1.java   // Río Guadalquivir",
        "  compilando cordoba/niveles/Level2.java   // Calleja de las Flores",
        "  compilando cordoba/niveles/Level3.java   // Mezquita",
        "",
        "public class PhysicsConfig {",
        "    float gravedad    = 9.8f;",
        "    float velocidad   = 5.0f;",
        "    float fuerzaSalto = 12.0f;   // ¡Cámbiame!",
        "}",
        "",
        "Cargando sprites ............ OK",
        "Despertando a Coco .......... OK",
        "BUILD SUCCESSFUL",
        "",
        PROMPT + "java cordoba.Main",
    };

    private static final String[] CODIGO = {
        "if (coco.enSuelo && saltar) coco.vy = -fuerzaSalto;",
        "coco.vy += gravedad * dt;",
        "coco.x += velocidad * direccion;",
        "for (Farola f : nivel.getFarolas()) f.encender();",
        "if (colisiones.tocaPeligro(coco)) coco.morir();",
        "particulas.emitir(x, y, 40, ORO);",
        "Musica.reproducir(\"nivel1\");",
        "while (ejecutando) { actualizar(); dibujar(); }",
        "salmorejo.recogido = true; tiempo -= 3;",
        "if (meta.intersects(coco)) coco.celebrar();",
        "char[][] mapa = new char[filas][columnas];",
        "camX += (objetivoX - camX) * 0.10f;",
        "// TODO: que Coco no se coma el flamenquín de la consola",
        "float gravedad = 9.8f; float gravedad = 4.5f; float gravedad = 1.6f;",
        "nivel = Level.crear(2); // Calleja de las Flores",
        "maceta.caer(); maceta.romper(); maceta.caer();",
        "return new Cocodrilo(\"Coco\", SALVAJE);",
        "int columnas = 856; // columnas de la Mezquita",
        "if (fuerzaSalto > 20) System.out.println(\"¡¡VUELA!!\");",
        "private static final String NOMBRE = \"CÓDIGO SALVAJE\";",
        "puente.derrumbar(); plataforma.mover(); corriente.empujar();",
        "Ranking.guardar(nombre, tiempo, caidas, compilaciones);",
        "boolean salvaje = true;",
        "public void actualizar() { frame++; coco.correr(); }",
        "brisa.frenarCaida(coco); agua.salpicar(coco.x, coco.y);",
        "Sonido.victoria(); Sonido.victoria(); Sonido.victoria();",
    };
    private static final String RAROS = "{}[]();=+-*/<>#$%&|!?01";
    private static final String[] RESERVADAS = { "public", "private", "static", "final", "class", "float", "int",
        "char", "boolean", "void", "new", "if", "for", "while", "return", "true", "String" };

    private static final Color FONDO = new Color(0x0C0C0C), GRIS = new Color(0xCCCCCC), VERDE = new Color(0x7BE07B),
        AZUL = new Color(0x82AAFF), AMARILLO = new Color(0xF2D985), NARANJA = new Color(0xE8A05A),
        COMENTARIO = new Color(0x6A9955), BLANCO = new Color(0xF4F1E6);

    private final List<String> lineas = new ArrayList<>();
    private final Random azar = new Random(7);
    private int t;
    private int espera;   // fotogramas esperando al audio (solo para que el cursor parpadee)
    private int siguienteCompilacion;
    private double avalanchaAcum;
    private int[] pixeles;

    public Arranque() {
        reiniciar();
    }

    public void reiniciar() {
        t = 0;
        espera = 0;
        lineas.clear();
        lineas.add(PROMPT);
        siguienteCompilacion = 0;
        avalanchaAcum = 0;
    }

    // ¿ya está convirtiéndose en el menú? (entonces hay que pintar el menú debajo)
    public boolean transformando() {
        return t >= TRANSFORMAR;
    }

    // true = terminada (o saltada)
    public boolean actualizar(Input in) {
        if (in.clic() || in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_SPACE) || in.pulsada(KeyEvent.VK_ESCAPE))
            return true;
        // con el cursor parpadeando se espera a que el audio despierte (máx. 2,5 s), si no
        // las primeras teclas sonarían tarde
        if (t == 0 && !Sonido.listo() && ++espera < 150) return false;
        t++;
        // escribir el comando letra a letra (una tecla por letra y ENTER al final)
        if (t > 12 && t <= ESCRIBIR && t % 5 == 0) {
            int letras = Math.min(COMANDO.length(), (t - 12) / 5);
            if (!lineas.get(0).equals(PROMPT + COMANDO.substring(0, letras))) Sonido.tecla();
            lineas.set(0, PROMPT + COMANDO.substring(0, letras));
        }
        if (t == ESCRIBIR - 3) Sonido.teclaEnter();
        // compilación: una línea cada pocos fotogramas (ya sin sonido: solo suena el comando)
        if (t > ESCRIBIR && t < COMPILAR && siguienteCompilacion < COMPILACION.length && (t - ESCRIBIR) % 5 == 0)
            lineas.add(COMPILACION[siguienteCompilacion++]);
        // avalancha: cada vez más líneas por fotograma
        if (t >= COMPILAR && t < AVALANCHA) {
            avalanchaAcum += 0.6 + (t - COMPILAR) * 0.12;
            while (avalanchaAcum >= 1) {
                avalanchaAcum--;
                String l = CODIGO[azar.nextInt(CODIGO.length)];
                int sangria = azar.nextInt(4) * 4 + (t - COMPILAR) / 6;
                lineas.add(" ".repeat(Math.min(40, sangria)) + l);
            }
        }
        while (lineas.size() > 200) lineas.remove(0);
        return t >= FIN;
    }

    // menu = el menú ya pintado (solo hace falta al transformarse)
    public void dibujar(Graphics2D g, BufferedImage menu) {
        g.setColor(FONDO);
        g.fillRect(0, 0, 640, 360);
        g.setFont(Ui.FUENTE_MONO);
        FontMetrics fm = g.getFontMetrics();
        int ancho = Math.max(6, fm.charWidth('M'));
        int columnas = 640 / ancho + 1, visibles = 360 / ALTO_LINEA;

        // las últimas líneas que caben (la terminal se desplaza hacia arriba)
        int desde = Math.max(0, lineas.size() - visibles);
        float locura = t < LOCURA ? 0f : Math.min(1f, (t - LOCURA) / 45f);
        Random r = new Random(t / 3);
        for (int i = desde; i < lineas.size(); i++) {
            int y = (i - desde + 1) * ALTO_LINEA - 2;
            String l = lineas.get(i);
            if (locura > 0) l = enloquecer(l, locura, r, columnas);
            dibujarLinea(g, l, 2, y, ancho, locura);
            // cursor parpadeando al final de la última línea
            if (i == lineas.size() - 1 && t < COMPILAR && ((t + espera) / 15) % 2 == 0) {
                g.setColor(GRIS);
                g.fillRect(2 + l.length() * ancho, y - 9, ancho - 1, 11);
            }
        }

        // pista para saltar
        if (t > 20 && t < TRANSFORMAR) {
            String s = "ENTER: saltar";
            g.setColor(FONDO);
            g.fillRect(634 - fm.stringWidth(s), 343, fm.stringWidth(s) + 6, 15);
            g.setColor(new Color(0x777777));
            g.drawString(s, 636 - fm.stringWidth(s), 354);
        }

        if (t >= TRANSFORMAR && menu != null) transformar(g, menu, ancho);
    }

    // las letras se cambian por símbolos sueltos; con más locura, más cambios y la línea se llena de basura
    private String enloquecer(String l, float locura, Random r, int columnas) {
        StringBuilder sb = new StringBuilder(l);
        if (locura > 0.5f)
            while (sb.length() < columnas && r.nextFloat() < locura) sb.append(RAROS.charAt(r.nextInt(RAROS.length())));
        for (int k = 0; k < sb.length(); k++)
            if (sb.charAt(k) != ' ' && r.nextFloat() < locura * 0.45f) sb.setCharAt(k, RAROS.charAt(r.nextInt(RAROS.length())));
        return sb.toString();
    }

    // colores como en un editor: reservadas en azul, números en amarillo, textos en naranja, comentarios en verde
    private void dibujarLinea(Graphics2D g, String l, int x, int y, int ancho, float locura) {
        Color defecto = l.startsWith(PROMPT) ? BLANCO : GRIS;
        if (l.contains(" OK") || l.startsWith("BUILD")) defecto = VERDE;
        int comentario = l.indexOf("//");
        int i = 0;
        while (i < l.length()) {
            char c = l.charAt(i);
            int j = i + 1;
            Color col = defecto;
            if (comentario >= 0 && i >= comentario) {
                j = l.length();
                col = COMENTARIO;
            } else if (c == '"') {
                j = l.indexOf('"', i + 1);
                j = j < 0 ? l.length() : j + 1;
                col = NARANJA;
            } else if (Character.isDigit(c)) {
                while (j < l.length() && (Character.isDigit(l.charAt(j)) || l.charAt(j) == '.' || l.charAt(j) == 'f')) j++;
                col = AMARILLO;
            } else if (Character.isLetter(c)) {
                while (j < l.length() && Character.isLetterOrDigit(l.charAt(j))) j++;
                String palabra = l.substring(i, j);
                for (String p : RESERVADAS) if (p.equals(palabra)) col = AZUL;
            }
            if (locura > 0) col = mezclar(col, VERDE, locura * 1.4f);
            g.setColor(col);
            g.drawString(l.substring(i, j), x + i * ancho, y);
            i = j;
        }
    }

    // cada letra se vuelve un bloque del color del menú y después el menú de verdad;
    // empieza abajo a la izquierda (donde está Coco) y se extiende en diagonal
    private void transformar(Graphics2D g, BufferedImage menu, int ancho) {
        int w = menu.getWidth(), h = menu.getHeight();
        if (pixeles == null || pixeles.length != w * h) pixeles = new int[w * h];
        menu.getRGB(0, 0, w, h, pixeles, 0, w);
        int columnas = (w + ancho - 1) / ancho, filas = (h + ALTO_LINEA - 1) / ALTO_LINEA;
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                double dist = ((double) c / columnas + (double) (filas - 1 - f) / filas) / 2;   // 0 abajo-izda, 1 arriba-dcha
                int ruido = ((c * 73 + f * 151) % 17) - 8;
                int s = t - (TRANSFORMAR + (int) (dist * 55) + ruido);
                if (s < 0) continue;
                int x = c * ancho, y = f * ALTO_LINEA;
                int cw = Math.min(ancho, w - x), ch = Math.min(ALTO_LINEA, h - y);
                if (s < 10) {
                    // bloque con el color medio de ese trozo del menú
                    long rr = 0, gg = 0, bb = 0;
                    for (int yy = y; yy < y + ch; yy++)
                        for (int xx = x; xx < x + cw; xx++) {
                            int p = pixeles[yy * w + xx];
                            rr += (p >> 16) & 255; gg += (p >> 8) & 255; bb += p & 255;
                        }
                    int n = cw * ch;
                    Color medio = new Color((int) (rr / n), (int) (gg / n), (int) (bb / n));
                    g.setColor(s < 3 ? mezclar(medio, VERDE, 0.7f) : medio);   // destello verde al cambiar
                    g.fillRect(x, y, cw, ch);
                } else {
                    g.drawImage(menu, x, y, x + cw, y + ch, x, y, x + cw, y + ch, null);
                }
            }
        }
    }

    private static Color mezclar(Color a, Color b, float k) {
        k = Math.max(0, Math.min(1, k));
        return new Color(Math.round(a.getRed() + (b.getRed() - a.getRed()) * k),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * k),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * k));
    }
}
