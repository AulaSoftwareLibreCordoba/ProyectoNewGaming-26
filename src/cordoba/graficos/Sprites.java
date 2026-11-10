package cordoba.graficos;

import cordoba.niveles.Level;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Todo el PIXEL ART del juego.
 *
 * El cocodrilo está "dibujado" con texto: cada carácter es un píxel de un color
 * de la PALETA. Así podéis editar el personaje directamente desde el código.
 *
 * Además, cada imagen se busca primero en la carpeta assets/. Si existe un PNG
 * con ese nombre, se usa el PNG (podéis retocarlo con Aseprite, Piskel, GIMP...).
 * Si no existe, se genera desde el código. Para crear los PNG ejecutad:
 *     java -cp out Main --exportar-assets
 */
public final class Sprites {

    private Sprites() { }

    // =====================================================================
    //  PALETA DEL COCODRILO (carácter -> color ARGB)
    // =====================================================================
    private static final Map<Character, Integer> PALETA = new HashMap<>();
    static {
        PALETA.put('o', 0xFF14301C); // contorno verde muy oscuro
        PALETA.put('G', 0xFF4FB848); // verde principal
        PALETA.put('D', 0xFF2F8A3A); // verde sombra / escamas
        PALETA.put('H', 0xFF9BE070); // brillo
        PALETA.put('Y', 0xFFF2E38A); // barriga
        PALETA.put('W', 0xFFFFFFFF); // blanco (ojo y dientes)
        PALETA.put('K', 0xFF101010); // pupila
        PALETA.put('R', 0xFFE0464E); // boca
        PALETA.put('P', 0xFFFF9AA8); // mejillas
    }

    // ---------------- Partes del cocodrilo (20 columnas cada fila) ----------------
    private static final String[] CABEZA_NORMAL = {
        "......ooooo.........",
        ".....oWWWWWo........",
        ".....oWWWKKo........",
        "....ooWWWKKoo.......",
        "...oGGoooooGGoooo...",
        "..oGHHGGGGGGGGGGGoo.",
        "..oGHGGGGGGGGGGGDGGo",
        "..oGGPGGGGGGGGGGGGGo",
        "..oGGGGGGoWoWoWoWoo.",
        "...oGGGGGGoooooooo..",
    };
    private static final String[] CABEZA_PARPADEO = {
        "......ooooo.........",
        ".....oGGGGGo........",
        ".....oooooGo........",
        "....ooGGGGGoo.......",
        "...oGGoooooGGoooo...",
        "..oGHHGGGGGGGGGGGoo.",
        "..oGHGGGGGGGGGGGDGGo",
        "..oGGPGGGGGGGGGGGGGo",
        "..oGGGGGGoWoWoWoWoo.",
        "...oGGGGGGoooooooo..",
    };
    private static final String[] CABEZA_BOCA_ABIERTA = {
        "......ooooo.........",
        ".....oWWWWWo........",
        ".....oWWWKKo........",
        "....ooWWWKKoo.......",
        "...oGGoooooGGoooo...",
        "..oGHHGGGGGGGGGGGoo.",
        "..oGHGGGGGGGGGGGDGGo",
        "..oGGPGGGGGoWoWoWoo.",
        "..oGGGGGGRRRRRRRo...",
        "...oGGGGGGoWoWoWo...",
    };
    private static final String[] CABEZA_FELIZ = {
        "......ooooo.........",
        ".....oWWWWWo........",
        ".....oWKKKWo........",
        "....ooKWWWKoo.......",
        "...oGGoooooGGoooo...",
        "..oGHHGGGGGGGGGGGoo.",
        "..oGHGGGGGGGGGGGDGGo",
        "..oGGPGGGGGGGGGGGGGo",
        "..oGGGGGGRRRRRRRRo..",
        "...oGGGGGGoooooooo..",
    };
    private static final String[] CUERPO_BRAZO_ABAJO = {
        "....oGGGGGGGo.......",
        "...oGGYYYYGGo.......",
        "o..oGYYYYYYGoo......",
        "oo.oGYYYYYYGGo......",
        "oGooGYYYYYYGo.......",
        ".oGGGGYYYYGGo.......",
        "..ooGGGGGGGGo.......",
    };
    private static final String[] CUERPO_BRAZOS_ARRIBA = {
        "....oGGGGGGGo.oo....",
        "...oGGYYYYGGooGo....",
        "o..oGYYYYYYGGo......",
        "oo.oGYYYYYYGo.......",
        "oGooGYYYYYYGo.......",
        ".oGGGGYYYYGGo.......",
        "..ooGGGGGGGGo.......",
    };
    private static final String[] PIERNAS_QUIETO = {
        "....oGGo..oGGo......",
        "....oGGo..oGGo......",
        "....ooooo.ooooo.....",
    };
    private static final String[] PIERNAS_PASO1 = {
        "...oGGo....oGGo.....",
        "..oGGo......oGGo....",
        "..oooo......ooooo...",
    };
    private static final String[] PIERNAS_PASO2 = {
        ".....oGGooGo........",
        ".....oGGGGo.........",
        ".....oooooo.........",
    };
    private static final String[] PIERNAS_SALTO = {
        "....oGGGGGGo........",
        "....oooooooo........",
        "....................",
    };
    private static final String[] PIERNAS_CAIDA = {
        "...oGGo...oGGo......",
        "...oGo.....oGo......",
        "...oo.......oo......",
    };

    /** Une cabeza + cuerpo + piernas en un único fotograma de 20x20. */
    private static String[] unir(String[] cabeza, String[] cuerpo, String[] piernas) {
        String[] r = new String[cabeza.length + cuerpo.length + piernas.length];
        System.arraycopy(cabeza, 0, r, 0, cabeza.length);
        System.arraycopy(cuerpo, 0, r, cabeza.length, cuerpo.length);
        System.arraycopy(piernas, 0, r, cabeza.length + cuerpo.length, piernas.length);
        return r;
    }

    // ---------------- Fotogramas del cocodrilo (mirando a la derecha) ----------------
    public static final BufferedImage COCO_QUIETO    = cargar("player/coco_quieto",    unir(CABEZA_NORMAL, CUERPO_BRAZO_ABAJO, PIERNAS_QUIETO));
    public static final BufferedImage COCO_PARPADEO  = cargar("player/coco_parpadeo",  unir(CABEZA_PARPADEO, CUERPO_BRAZO_ABAJO, PIERNAS_QUIETO));
    public static final BufferedImage COCO_PASO1     = cargar("player/coco_paso1",     unir(CABEZA_NORMAL, CUERPO_BRAZO_ABAJO, PIERNAS_PASO1));
    public static final BufferedImage COCO_PASO2     = cargar("player/coco_paso2",     unir(CABEZA_NORMAL, CUERPO_BRAZO_ABAJO, PIERNAS_PASO2));
    public static final BufferedImage COCO_SALTO     = cargar("player/coco_salto",     unir(CABEZA_BOCA_ABIERTA, CUERPO_BRAZOS_ARRIBA, PIERNAS_SALTO));
    public static final BufferedImage COCO_CAIDA     = cargar("player/coco_caida",     unir(CABEZA_NORMAL, CUERPO_BRAZOS_ARRIBA, PIERNAS_CAIDA));
    public static final BufferedImage COCO_VICTORIA1 = cargar("player/coco_victoria1", unir(CABEZA_FELIZ, CUERPO_BRAZOS_ARRIBA, PIERNAS_QUIETO));
    public static final BufferedImage COCO_VICTORIA2 = cargar("player/coco_victoria2", unir(CABEZA_FELIZ, CUERPO_BRAZOS_ARRIBA, PIERNAS_SALTO));
    public static final BufferedImage COCO_HERIDO    = cargar("player/coco_herido",    unir(CABEZA_PARPADEO, CUERPO_BRAZOS_ARRIBA, PIERNAS_CAIDA));

    /** Animación de caminar: se recorre en bucle. */
    public static final BufferedImage[] COCO_ANDAR = { COCO_PASO1, COCO_PASO2, COCO_QUIETO, COCO_PASO2 };

    // =====================================================================
    //  Carga / generación de imágenes
    // =====================================================================

    /** Convierte un dibujo en texto a imagen usando la paleta del cocodrilo. */
    public static BufferedImage desdeTexto(String[] filas) {
        int alto = filas.length, ancho = filas[0].length();
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                Integer c = PALETA.get(filas[y].charAt(x));
                if (c != null) img.setRGB(x, y, c);
            }
        }
        return img;
    }

    /** Busca assets/<ruta>.png; si no existe genera la imagen desde el texto. */
    private static BufferedImage cargar(String ruta, String[] datos) {
        BufferedImage desdeArchivo = leerPng(ruta);
        return desdeArchivo != null ? desdeArchivo : desdeTexto(datos);
    }

    /** Intenta leer un PNG de la carpeta assets (devuelve null si no está). */
    public static BufferedImage leerPng(String ruta) {
        try {
            File f = new File("assets/" + ruta + ".png");
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception ignorada) {
            // si el PNG está corrupto usamos la versión generada
        }
        return null;
    }

    /** Devuelve una copia volteada horizontalmente (para mirar a la izquierda). */
    public static BufferedImage voltear(BufferedImage img) {
        BufferedImage r = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                r.setRGB(img.getWidth() - 1 - x, y, img.getRGB(x, y));
        return r;
    }

    private static final Map<BufferedImage, BufferedImage> VOLTEADAS = new HashMap<>();

    /** Versión volteada guardada en caché para no recalcularla cada fotograma. */
    public static synchronized BufferedImage volteada(BufferedImage img) {
        return VOLTEADAS.computeIfAbsent(img, Sprites::voltear);
    }

    // =====================================================================
    //  TILES (bloques de 16x16) generados por código
    // =====================================================================
    private static final Map<String, BufferedImage> CACHE_TILES = new HashMap<>();

    /**
     * Devuelve la textura de un tile.
     * @param tema         ambientación del nivel
     * @param tipo         carácter del mapa ('#', 'S', '^', 'v', '~')
     * @param variante     0..3, para que no todos los bloques sean iguales
     * @param arribaLibre  true si encima no hay bloque (borde superior)
     * @param abajoLibre   true si debajo no hay bloque (borde inferior)
     */
    public static synchronized BufferedImage tile(Level.Tema tema, char tipo, int variante,
                                                  boolean arribaLibre, boolean abajoLibre) {
        String clave = tema + "_" + tipo + "_" + variante + "_" + arribaLibre + "_" + abajoLibre;
        BufferedImage img = CACHE_TILES.get(clave);
        if (img == null) {
            String nombre = "tiles/" + nombreArchivo(clave);
            img = leerPng(nombre);
            if (img == null) img = generarTile(tema, tipo, variante, arribaLibre, abajoLibre);
            CACHE_TILES.put(clave, img);
        }
        return img;
    }

    private static String nombreArchivo(String clave) {
        return clave.toLowerCase().replace('#', 'T').replace('^', 'P').replace('~', 'A')
                .replace("true", "1").replace("false", "0");
    }

    private static BufferedImage generarTile(Level.Tema tema, char tipo, int var,
                                             boolean arriba, boolean abajo) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Random r = new Random(tipo * 31L + var * 7L + tema.ordinal() * 101L);

        switch (tipo) {
            case '#':
                if (tema == Level.Tema.RIO) tileTierra(g, r, arriba);
                else tileAdoquin(g, r, arriba);
                break;
            case 'S':
                if (tema == Level.Tema.RIO) tileSillar(g, r, var, arriba, abajo);
                else tileMezquita(g, r, var, arriba, abajo);
                break;
            case '^':
                tilePinchos(g, false);
                break;
            case 'v':
                tilePinchos(g, true);
                break;
            default:
                break;
        }
        g.dispose();
        return img;
    }

    /** Tierra de la ribera del Guadalquivir con césped arriba. */
    private static void tileTierra(Graphics2D g, Random r, boolean arriba) {
        g.setColor(new Color(0x8A5A2E));
        g.fillRect(0, 0, 16, 16);
        for (int i = 0; i < 14; i++) {
            g.setColor(r.nextBoolean() ? new Color(0x6E4421) : new Color(0xA8713C));
            g.fillRect(r.nextInt(16), r.nextInt(16), 1 + r.nextInt(2), 1);
        }
        g.setColor(new Color(0x9C968A));
        g.fillRect(r.nextInt(12) + 2, r.nextInt(10) + 5, 2, 2);
        if (arriba) {
            g.setColor(new Color(0x3F8F2A));
            g.fillRect(0, 0, 16, 6);
            for (int x = 0; x < 16; x++) {
                if (r.nextInt(3) == 0) g.fillRect(x, 6, 1, 1 + r.nextInt(2));
            }
            g.setColor(new Color(0x5DBB3A));
            g.fillRect(0, 0, 16, 4);
            g.setColor(new Color(0x92E05C));
            g.fillRect(0, 0, 16, 1);
            for (int x = 1; x < 16; x += 3 + r.nextInt(2)) g.fillRect(x, 1, 1, 1);
        }
    }

    /** Adoquines del casco histórico. */
    private static void tileAdoquin(Graphics2D g, Random r, boolean arriba) {
        g.setColor(new Color(0x7A6650));
        g.fillRect(0, 0, 16, 16);
        // ladrillo antiguo interior
        for (int fila = 0; fila < 4; fila++) {
            int desplaz = (fila % 2) * 4;
            for (int x = -desplaz; x < 16; x += 8) {
                g.setColor(new Color(0x9A6B4A + r.nextInt(3) * 0x050302));
                g.fillRect(x + 1, fila * 4 + 1, 6, 3);
            }
        }
        if (arriba) {
            g.setColor(new Color(0xD9B26A)); // albero
            g.fillRect(0, 0, 16, 5);
            g.setColor(new Color(0xBFA88A));
            for (int x = 0; x < 16; x += 4) {
                g.fillRect(x, 0, 3, 3);
            }
            g.setColor(new Color(0xE8D9BC));
            for (int x = 0; x < 16; x += 4) g.fillRect(x, 0, 2, 1);
            g.setColor(new Color(0x6B5A44));
            g.fillRect(0, 5, 16, 1);
        }
    }

    /** Sillares de piedra arenisca (Puente Romano, murallas). */
    private static void tileSillar(Graphics2D g, Random r, int var, boolean arriba, boolean abajo) {
        g.setColor(new Color(0xA8864F)); // mortero
        g.fillRect(0, 0, 16, 16);
        for (int fila = 0; fila < 2; fila++) {
            int desplaz = ((fila + var) % 2) * 8;
            for (int x = -desplaz; x < 16; x += 16) {
                int bx = x + 1, by = fila * 8 + 1;
                g.setColor(new Color(0xD8B27A));
                g.fillRect(bx, by, 14, 6);
                g.setColor(new Color(0xEDD09C));
                g.fillRect(bx, by, 14, 1);
                g.fillRect(bx, by, 1, 6);
                g.setColor(new Color(0xBC945E));
                g.fillRect(bx, by + 5, 14, 1);
                g.setColor(new Color(0xC9A26B));
                g.fillRect(bx + 3 + r.nextInt(8), by + 2 + r.nextInt(2), 2, 1);
            }
        }
        if (arriba) {
            g.setColor(new Color(0xF4DEAE));
            g.fillRect(0, 0, 16, 2);
        }
        if (abajo) {
            g.setColor(new Color(0x8C6B3B));
            g.fillRect(0, 13, 16, 3);
        }
    }

    /** Piedra dorada de la Mezquita; por debajo, las dovelas rojas y blancas. */
    private static void tileMezquita(Graphics2D g, Random r, int var, boolean arriba, boolean abajo) {
        g.setColor(new Color(0xB89660));
        g.fillRect(0, 0, 16, 16);
        for (int fila = 0; fila < 2; fila++) {
            int desplaz = ((fila + var) % 2) * 8;
            for (int x = -desplaz; x < 16; x += 16) {
                g.setColor(new Color(0xE0C590));
                g.fillRect(x + 1, fila * 8 + 1, 14, 6);
                g.setColor(new Color(0xEFDCB0));
                g.fillRect(x + 1, fila * 8 + 1, 14, 1);
                g.setColor(new Color(0xC9AA74));
                g.fillRect(x + 1, fila * 8 + 6, 14, 1);
            }
        }
        if (abajo) {
            // Dovelas bicolor del arco (rojo ladrillo y blanco crema)
            for (int i = 0; i < 4; i++) {
                boolean rojo = (i + var) % 2 == 0;
                g.setColor(rojo ? new Color(0xB33A2C) : new Color(0xF3E4C0));
                g.fillRect(i * 4, 6, 4, 10);
                g.setColor(rojo ? new Color(0x8C2A20) : new Color(0xD8C59C));
                g.fillRect(i * 4 + 3, 6, 1, 10);
            }
            g.setColor(new Color(0x5C3B24));
            g.fillRect(0, 5, 16, 1);
            g.fillRect(0, 15, 16, 1);
        }
        if (arriba) {
            g.setColor(new Color(0xF6E7C4));
            g.fillRect(0, 0, 16, 2);
            g.setColor(new Color(0x9C7A48));
            g.fillRect(0, 2, 16, 1);
        }
    }

    /** Pinchos metálicos (hacia arriba o colgando del techo). */
    private static void tilePinchos(Graphics2D g, boolean techo) {
        for (int i = 0; i < 3; i++) {
            int bx = 1 + i * 5;
            int[] xs = { bx, bx + 2, bx + 5 };
            int[] ys = techo ? new int[] { 0, 10, 0 } : new int[] { 16, 6, 16 };
            g.setColor(new Color(0x5E6873));
            g.fillPolygon(xs, ys, 3);
            int[] xs2 = { bx + 1, bx + 2, bx + 3 };
            int[] ys2 = techo ? new int[] { 0, 8, 0 } : new int[] { 16, 8, 16 };
            g.setColor(new Color(0xD5DCE3));
            g.fillPolygon(xs2, ys2, 3);
        }
        g.setColor(new Color(0x3B424A));
        if (techo) g.fillRect(0, 0, 16, 2); else g.fillRect(0, 14, 16, 2);
    }

    // =====================================================================
    //  OBJETOS DEL ESCENARIO
    // =====================================================================

    /** Farola cordobesa de hierro (encendida o apagada). 12x36 */
    public static BufferedImage farola(boolean encendida) {
        String nombre = encendida ? "farola_on" : "farola_off";
        return CACHE_OBJETOS.computeIfAbsent(nombre, k -> {
            BufferedImage desdePng = leerPng("tiles/" + k);
            if (desdePng != null) return desdePng;
            BufferedImage img = new BufferedImage(12, 36, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            Color hierro = new Color(0x22252B), brillo = new Color(0x4A505A);
            g.setColor(hierro);
            g.fillRect(5, 12, 2, 21);     // poste
            g.fillRect(3, 32, 6, 4);      // base
            g.fillRect(2, 2, 8, 1);       // tapa
            g.fillRect(4, 0, 4, 2);       // remate
            g.fillRect(1, 3, 1, 8);
            g.fillRect(10, 3, 1, 8);
            g.fillRect(2, 11, 8, 1);
            g.setColor(brillo);
            g.fillRect(5, 14, 1, 17);
            // cristal
            g.setColor(encendida ? new Color(0xFFE27A) : new Color(0x3C4A5C));
            g.fillRect(2, 3, 8, 8);
            g.setColor(encendida ? new Color(0xFFF6C8) : new Color(0x5A6B80));
            g.fillRect(3, 4, 2, 4);
            g.setColor(hierro);
            g.fillRect(5, 3, 2, 8);
            g.dispose();
            return img;
        });
    }

    /** Cartel de madera con un signo de exclamación. 14x20 */
    public static BufferedImage cartel() {
        return CACHE_OBJETOS.computeIfAbsent("cartel", k -> {
            BufferedImage desdePng = leerPng("interface/" + k);
            if (desdePng != null) return desdePng;
            BufferedImage img = new BufferedImage(14, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(0x5A3A1E));
            g.fillRect(6, 10, 2, 10);
            g.fillRect(0, 0, 14, 11);
            g.setColor(new Color(0xB07A42));
            g.fillRect(1, 1, 12, 9);
            g.setColor(new Color(0xC98F52));
            g.fillRect(1, 1, 12, 1);
            g.setColor(new Color(0x2B1A0C));
            g.fillRect(6, 2, 2, 5);
            g.fillRect(6, 8, 2, 1);
            g.dispose();
            return img;
        });
    }

    private static final Map<String, BufferedImage> CACHE_OBJETOS = new HashMap<>();

    /** Maceta azul con geranios (típica de los patios cordobeses). 10x12 */
    public static BufferedImage maceta(int variante) {
        return CACHE_OBJETOS.computeIfAbsent("maceta" + variante, k -> {
            BufferedImage img = new BufferedImage(12, 13, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            Color[] flores = { new Color(0xE8344A), new Color(0xFF6FA8), new Color(0xFFFFFF), new Color(0xE8344A) };
            Color flor = flores[variante % flores.length];
            g.setColor(new Color(0x2E7D32));
            g.fillRect(2, 3, 8, 4);
            g.fillRect(1, 5, 10, 2);
            g.setColor(flor);
            g.fillRect(2, 1, 3, 3);
            g.fillRect(7, 0, 3, 3);
            g.fillRect(5, 3, 2, 2);
            g.setColor(new Color(0xFFF59D));
            g.fillRect(3, 2, 1, 1);
            g.fillRect(8, 1, 1, 1);
            g.setColor(new Color(0x1E4FA8));
            g.fillRect(1, 7, 10, 2);
            g.fillRect(2, 9, 8, 4);
            g.setColor(new Color(0x4C7FE0));
            g.fillRect(3, 9, 2, 3);
            g.setColor(new Color(0xF2F2F2));
            g.fillRect(4, 10, 4, 1);
            g.dispose();
            return img;
        });
    }

    /** Palmera. 40x70 */
    public static BufferedImage palmera() {
        return CACHE_OBJETOS.computeIfAbsent("palmera", k -> {
            BufferedImage img = new BufferedImage(40, 70, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            // tronco curvado
            for (int y = 14; y < 70; y++) {
                int x = 19 + (int) (Math.sin(y / 18.0) * 3);
                g.setColor((y / 3) % 2 == 0 ? new Color(0x8B6B3D) : new Color(0x6E522B));
                g.fillRect(x, y, 4, 1);
            }
            // hojas
            g.setColor(new Color(0x2E7D32));
            int[][] hojas = { { -18, 6 }, { -12, -6 }, { 0, -12 }, { 12, -6 }, { 18, 6 }, { -8, 10 }, { 8, 10 } };
            for (int[] h : hojas) {
                for (int i = 0; i <= 10; i++) {
                    int x = 20 + h[0] * i / 10;
                    int y = 14 + h[1] * i / 10 + (i * i) / 14;
                    g.fillRect(x - 1, y, 3, 2);
                }
            }
            g.setColor(new Color(0x4CAF50));
            for (int[] h : hojas) {
                for (int i = 0; i <= 8; i++) {
                    int x = 20 + h[0] * i / 10;
                    int y = 14 + h[1] * i / 10 + (i * i) / 14;
                    g.fillRect(x, y, 1, 1);
                }
            }
            g.setColor(new Color(0x7B4A1E));
            g.fillRect(18, 14, 5, 3);
            g.dispose();
            return img;
        });
    }

    /** Naranjo (como los del Patio de los Naranjos). 28x34 */
    public static BufferedImage naranjo() {
        return CACHE_OBJETOS.computeIfAbsent("naranjo", k -> {
            BufferedImage img = new BufferedImage(28, 34, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(0x6E4A26));
            g.fillRect(12, 18, 4, 16);
            g.setColor(new Color(0x2F7A34));
            g.fillOval(1, 2, 26, 20);
            g.setColor(new Color(0x3F9A40));
            g.fillOval(4, 2, 18, 13);
            Random r = new Random(5);
            g.setColor(new Color(0xFF9800));
            for (int i = 0; i < 9; i++) g.fillRect(4 + r.nextInt(19), 4 + r.nextInt(14), 2, 2);
            g.setColor(new Color(0x1F5C25));
            g.fillRect(4, 18, 20, 2);
            // alcorque (círculo de tierra)
            g.setColor(new Color(0x8A5A2E));
            g.fillRect(8, 32, 12, 2);
            g.dispose();
            return img;
        });
    }

    /** Cañas y juncos de la orilla del río. 16x18 */
    public static BufferedImage canas() {
        return CACHE_OBJETOS.computeIfAbsent("canas", k -> {
            BufferedImage img = new BufferedImage(16, 18, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            int[] xs = { 2, 5, 8, 11, 13 };
            int[] hs = { 12, 17, 14, 16, 10 };
            for (int i = 0; i < xs.length; i++) {
                g.setColor(new Color(0x6B8E23));
                g.fillRect(xs[i], 18 - hs[i], 1, hs[i]);
                g.setColor(new Color(0x7A4E2A));
                g.fillRect(xs[i] - 0, 18 - hs[i], 2, 3);
            }
            g.dispose();
            return img;
        });
    }

    /** Guarda todas las imágenes generadas como PNG dentro de assets/. */
    public static void exportarAssets() throws Exception {
        String[][] jugador = {
            { "coco_quieto" }, { "coco_parpadeo" }, { "coco_paso1" }, { "coco_paso2" }, { "coco_salto" },
            { "coco_caida" }, { "coco_victoria1" }, { "coco_victoria2" }, { "coco_herido" } };
        BufferedImage[] imgs = { COCO_QUIETO, COCO_PARPADEO, COCO_PASO1, COCO_PASO2, COCO_SALTO,
            COCO_CAIDA, COCO_VICTORIA1, COCO_VICTORIA2, COCO_HERIDO };
        for (int i = 0; i < imgs.length; i++) guardar(imgs[i], "player/" + jugador[i][0]);

        char[] tipos = { '#', 'S', '^', 'v' };
        for (Level.Tema tema : Level.Tema.values())
            for (char t : tipos)
                for (int v = 0; v < 2; v++)
                    for (int a = 0; a < 2; a++)
                        for (int b = 0; b < 2; b++) {
                            String clave = tema + "_" + t + "_" + v + "_" + (a == 1) + "_" + (b == 1);
                            guardar(generarTile(tema, t, v, a == 1, b == 1), "tiles/" + nombreArchivo(clave));
                        }
        guardar(farola(true), "tiles/farola_on");
        guardar(farola(false), "tiles/farola_off");
        guardar(cartel(), "interface/cartel");
        guardar(maceta(0), "tiles/maceta");
        guardar(palmera(), "tiles/palmera");
        guardar(naranjo(), "tiles/naranjo");
        for (Level.Tema tema : Level.Tema.values()) {
            BufferedImage[] capas = Fondos.generarCapas(tema);
            for (int i = 0; i < capas.length; i++)
                guardar(capas[i], "backgrounds/" + tema.name().toLowerCase() + "_capa" + i);
        }
    }

    private static void guardar(BufferedImage img, String ruta) throws Exception {
        File f = new File("assets/" + ruta + ".png");
        f.getParentFile().mkdirs();
        ImageIO.write(img, "png", f);
    }
}
