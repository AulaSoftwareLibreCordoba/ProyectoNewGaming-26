package cordoba.graficos;

import cordoba.audio.Sonido;
import cordoba.entidades.Player;
import cordoba.fisica.PhysicsConfig;
import cordoba.fisica.VariableFisica;
import cordoba.interfaz.Ui;
import cordoba.niveles.Level;
import cordoba.niveles.elementos.Cartel;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.elementos.Farola;
import cordoba.niveles.elementos.ObstaculoMovil;
import cordoba.niveles.plataformas.PlataformaDinamica;
import cordoba.niveles.plataformas.PuenteDerrumbe;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Dibuja la partida: fondo con parallax, tiles, objetos, jugador y HUD.
 * No modifica nada del juego: solo "lee" el estado y lo pinta.
 */
public class GameRenderer {

    private static final int T = Level.TILE;

    // =====================================================================
    //  ESCENARIO
    // =====================================================================

    public void dibujarEscenario(Graphics2D g, Level nivel, Player p, Particulas part,
                                 int camX, int camY, int frame) {
        dibujarFondo(g, nivel.getTema(), camX, camY, nivel.getAltoPx());
        dibujarCorrientes(g, nivel, camX, camY, frame);
        dibujarDecoracion(g, nivel.getDecoraciones(), camX, camY);
        dibujarMeta(g, nivel, camX, camY, frame);
        dibujarTiles(g, nivel, camX, camY, frame);
        for (Cartel c : nivel.getCarteles()) {
            g.drawImage(Sprites.cartel(), (int) c.x - 7 - camX, (int) c.ySuelo - 20 - camY, null);
        }
        for (Farola f : nivel.getFarolas()) dibujarFarola(g, f, camX, camY, frame);
        for (PlataformaDinamica pl : nivel.getPlataformas()) pl.dibujar(g, camX, camY, frame);
        for (ObstaculoMovil o : nivel.getObstaculos()) o.dibujar(g, camX, camY, frame);
        dibujarJugador(g, p, camX, camY, frame);
        part.dibujar(g, camX, camY);
    }

    /** Fondo por capas. Las más lejanas se desplazan menos (parallax). */
    public void dibujarFondo(Graphics2D g, Level.Tema tema, int camX, int camY, int altoNivel) {
        BufferedImage[] capas = Fondos.capas(tema);
        for (int i = 0; i < capas.length; i++) {
            float alfa = 1f;
            int dy = 0;
            if (tema == Level.Tema.RIO) dy = -camY / 4;
            if (tema == Level.Tema.MEZQUITA && i == 3) {
                // La sala de columnas aparece al bajar al interior de la Mezquita
                alfa = Math.max(0f, Math.min(1f, (camY - 110) / 60f));
                if (alfa <= 0) continue;
                dy = 90;
            }
            int desp = (int) (camX * Fondos.PARALLAX[i]) % Fondos.ANCHO;
            Composite antes = g.getComposite();
            if (alfa < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
            if (dy > 0) {
                g.setColor(new Color(0x2A1A14));
                g.fillRect(0, 0, Fondos.ANCHO, dy);
            }
            g.drawImage(capas[i], -desp, dy, null);
            g.drawImage(capas[i], -desp + Fondos.ANCHO, dy, null);
            g.setComposite(antes);
        }
    }

    private void dibujarTiles(Graphics2D g, Level nivel, int camX, int camY, int frame) {
        int c1 = Math.max(0, camX / T), c2 = Math.min(nivel.getColumnas() - 1, (camX + 640) / T);
        int f1 = Math.max(0, camY / T), f2 = Math.min(nivel.getFilas() - 1, (camY + 360) / T);
        Level.Tema tema = nivel.getTema();
        for (int f = f1; f <= f2; f++) {
            for (int c = c1; c <= c2; c++) {
                char t = nivel.tile(c, f);
                int x = c * T - camX, y = f * T - camY;
                switch (t) {
                    case '#':
                    case 'S': {
                        boolean arriba = !nivel.esSolido(c, f - 1) && f > 0;
                        boolean abajo = !nivel.esSolido(c, f + 1) && f + 1 < nivel.getFilas();
                        int variante = (c * 7 + f * 3) % 2;
                        if (t == 'S' && tema == Level.Tema.MEZQUITA && abajo) variante = c % 2;
                        g.drawImage(Sprites.tile(tema, t, variante, arriba, abajo), x, y, null);
                        break;
                    }
                    case '^':
                    case 'v':
                        g.drawImage(Sprites.tile(tema, t, 0, false, false), x, y, null);
                        break;
                    case '~':
                        dibujarAgua(g, nivel.tile(c, f - 1) != '~', x, y, c, frame);
                        break;
                    default:
                        break;
                }
            }
        }
        dibujarArcosCiegos(g, nivel, camX, camY, c1, c2, f1, f2);
    }

    /**
     * Decora los muros grandes con arcos ciegos (como los de las fachadas de
     * la Mezquita o los nichos del Puente Romano). Solo es decoración.
     */
    private void dibujarArcosCiegos(Graphics2D g, Level nivel, int camX, int camY,
                                    int c1, int c2, int f1, int f2) {
        for (int f = f1 - 3; f <= f2; f++) {
            for (int c = c1 - 2; c <= c2; c++) {
                if (Math.floorMod(c, 6) != 2 || Math.floorMod(f, 5) != 1) continue;
                boolean macizo = true;
                for (int ff = f - 1; ff <= f + 3 && macizo; ff++)
                    for (int cc = c - 1; cc <= c + 2; cc++)
                        if (nivel.tile(cc, ff) != 'S') { macizo = false; break; }
                if (!macizo) continue;
                int x = c * T - camX, y = f * T - camY;
                if (nivel.getTema() == Level.Tema.MEZQUITA) {
                    g.setColor(new Color(0xB89660));
                    g.drawRect(x + 1, y + 1, 29, 45);
                    g.setColor(new Color(0x5A3A22));
                    g.fillRect(x + 7, y + 18, 18, 28);
                    g.fillOval(x + 7, y + 9, 18, 18);
                    dovelas(g, x + 16, y + 18, 13, 6, 10, new Color(0xB33A2C), new Color(0xF3E4C0));
                    g.setColor(new Color(0x3A2416));
                    for (int k = 0; k < 3; k++) g.fillRect(x + 10 + k * 5, y + 24, 2, 20);   // celosía
                } else {
                    g.setColor(new Color(0xA8864F));
                    g.fillRect(x + 8, y + 18, 16, 26);
                    g.fillOval(x + 8, y + 10, 16, 16);
                    dovelas(g, x + 16, y + 18, 11, 4, 8, new Color(0xC9A46B), new Color(0xE2C08A));
                    g.setColor(new Color(0x8C6B3B));
                    g.fillRect(x + 6, y + 44, 20, 2);
                }
            }
        }
    }

    /** Arco de herradura formado por dovelas de dos colores alternos. */
    private void dovelas(Graphics2D g, int cx, int cy, int radio, int grosor, int segmentos, Color a, Color b) {
        double ini = Math.toRadians(-200), fin = Math.toRadians(20);
        for (int i = 0; i < segmentos; i++) {
            double a1 = ini + (fin - ini) * i / segmentos, a2 = ini + (fin - ini) * (i + 1) / segmentos;
            int[] xs = { cx + (int) Math.round(Math.cos(a1) * radio), cx + (int) Math.round(Math.cos(a2) * radio),
                         cx + (int) Math.round(Math.cos(a2) * (radio - grosor)), cx + (int) Math.round(Math.cos(a1) * (radio - grosor)) };
            int[] ys = { cy + (int) Math.round(Math.sin(a1) * radio), cy + (int) Math.round(Math.sin(a2) * radio),
                         cy + (int) Math.round(Math.sin(a2) * (radio - grosor)), cy + (int) Math.round(Math.sin(a1) * (radio - grosor)) };
            g.setColor(i % 2 == 0 ? a : b);
            g.fillPolygon(xs, ys, 4);
        }
    }

    /** Agua animada del Guadalquivir. */
    private void dibujarAgua(Graphics2D g, boolean superficie, int x, int y, int c, int frame) {
        g.setColor(new Color(0x2F78A8));
        g.fillRect(x, y, T, T);
        g.setColor(new Color(0x3D8DC0));
        int ola = (frame / 8 + c * 3) % 16;
        g.fillRect(x + ola, y + 9, 5, 1);
        g.fillRect(x + (ola + 8) % 16, y + 13, 4, 1);
        if (superficie) {
            g.setColor(new Color(0x56A8D8));
            g.fillRect(x, y + 3, T, 3);
            g.setColor(new Color(0xBDE8FF));
            for (int i = 0; i < T; i++) {
                int fase = (i + c * T + frame / 6) % 8;
                if (fase < 3) g.fillRect(x + i, y + 3, 1, 1);
            }
        }
    }

    /** Corriente de aire: líneas blancas que suben. */
    private void dibujarCorrientes(Graphics2D g, Level nivel, int camX, int camY, int frame) {
        int c1 = Math.max(0, camX / T), c2 = Math.min(nivel.getColumnas() - 1, (camX + 640) / T);
        int f1 = Math.max(0, camY / T), f2 = Math.min(nivel.getFilas() - 1, (camY + 360) / T);
        for (int f = f1; f <= f2; f++) {
            for (int c = c1; c <= c2; c++) {
                if (nivel.tile(c, f) != 'u') continue;
                int x = c * T - camX, y = f * T - camY;
                g.setColor(new Color(255, 200, 140, 28));
                g.fillRect(x, y, T, T);
                g.setColor(new Color(255, 255, 255, 110));
                for (int k = 0; k < 2; k++) {
                    int lx = (c * 5 + k * 8) % 14 + 1;
                    int ly = 15 - ((frame * 2 + k * 9 + c * 13 + f * 16) % 16);
                    g.fillRect(x + lx, y + ly, 1, 4);
                }
            }
        }
    }

    private void dibujarDecoracion(Graphics2D g, List<Decoracion> lista, int camX, int camY) {
        for (Decoracion d : lista) {
            BufferedImage img = d.imagen();
            int x = (int) d.x - img.getWidth() / 2 - camX;
            int y = (int) d.ySuelo - img.getHeight() - camY;
            if (x > 640 || x + img.getWidth() < 0) continue;
            g.drawImage(img, x, y, null);
        }
    }

    private void dibujarFarola(Graphics2D g, Farola f, int camX, int camY, int frame) {
        int x = (int) f.x - 6 - camX, y = (int) f.ySuelo - 36 - camY;
        if (f.encendida) {
            // halo de luz
            int r = 16 + (frame / 10) % 2;
            g.setColor(new Color(255, 220, 120, 45));
            g.fillOval(x + 6 - r, y + 7 - r, r * 2, r * 2);
            g.setColor(new Color(255, 230, 150, 60));
            g.fillOval(x + 6 - 9, y + 7 - 9, 18, 18);
        }
        g.drawImage(Sprites.farola(f.encendida), x, y, null);
    }

    /** Meta: Torre de la Calahorra (nivel 1) o puerta de la Mezquita (nivel 2). */
    private void dibujarMeta(Graphics2D g, Level nivel, int camX, int camY, int frame) {
        Rectangle m = nivel.getMeta();
        int x = m.x - camX, y = m.y - camY;
        int suelo = m.y + m.height - camY;
        if (nivel.getTema() == Level.Tema.RIO) {
            // Torre de la Calahorra: dos torres unidas por un arco
            Color piedra = new Color(0xD9B47C), sombra = new Color(0xB08A55), clara = new Color(0xF0D4A0);
            int bx = x - 24, w = m.width + 48;
            g.setColor(piedra);
            g.fillRect(bx, suelo - 104, 24, 104);
            g.fillRect(bx + w - 24, suelo - 104, 24, 104);
            g.fillRect(bx + 20, suelo - 80, w - 40, 32);
            g.setColor(clara);
            g.fillRect(bx, suelo - 104, 24, 2);
            g.fillRect(bx + w - 24, suelo - 104, 24, 2);
            // almenas
            g.setColor(piedra);
            for (int i = 0; i < 24; i += 6) {
                g.fillRect(bx + i, suelo - 109, 4, 5);
                g.fillRect(bx + w - 24 + i, suelo - 109, 4, 5);
            }
            for (int i = 20; i < w - 20; i += 6) g.fillRect(bx + i, suelo - 84, 4, 4);
            g.setColor(sombra);
            for (int yy = suelo - 96; yy < suelo; yy += 8) {
                g.fillRect(bx, yy, 24, 1);
                g.fillRect(bx + w - 24, yy, 24, 1);
            }
            g.fillRect(bx + 8, suelo - 92, 6, 10);
            g.fillRect(bx + w - 16, suelo - 92, 6, 10);
            // bandera de Córdoba (verde y blanca)
            int fx = bx + 10;
            g.setColor(new Color(0x3A2A1A));
            g.fillRect(fx, suelo - 128, 1, 20);
            int ondula = (frame / 10) % 2;
            g.setColor(new Color(0x1E8E3E));
            g.fillRect(fx + 1, suelo - 128 + ondula, 12, 3);
            g.setColor(Color.WHITE);
            g.fillRect(fx + 1, suelo - 125 + ondula, 12, 3);
            g.setColor(new Color(0x1E8E3E));
            g.fillRect(fx + 1, suelo - 122 + ondula, 12, 3);
            // puerta (meta)
            g.setColor(new Color(0x3A2416));
            g.fillRect(x, y, m.width, m.height);
            g.fillOval(x, y - m.width / 2, m.width, m.width);
        } else {
            // Puerta de la Mezquita con arco de herradura bicolor
            int cx = x + m.width / 2;
            g.setColor(new Color(0xE0C590));
            g.fillRect(x - 18, suelo - 76, m.width + 36, 76);
            g.setColor(new Color(0xF6E7C4));
            g.fillRect(x - 18, suelo - 76, m.width + 36, 2);
            for (int i = 0; i < m.width + 36; i += 6) g.fillRect(x - 18 + i, suelo - 80, 4, 4);
            // alfiz
            g.setColor(new Color(0xB89660));
            g.drawRect(x - 12, suelo - 70, m.width + 23, 70);
            // dovelas
            int radio = m.width / 2 + 9;
            for (int i = 0; i < 12; i++) {
                double a1 = Math.toRadians(-200 + i * 220.0 / 12), a2 = Math.toRadians(-200 + (i + 1) * 220.0 / 12);
                int cy = y + 6;
                int[] xs = { cx + (int) (Math.cos(a1) * radio), cx + (int) (Math.cos(a2) * radio),
                             cx + (int) (Math.cos(a2) * (radio - 8)), cx + (int) (Math.cos(a1) * (radio - 8)) };
                int[] ys = { cy + (int) (Math.sin(a1) * radio), cy + (int) (Math.sin(a2) * radio),
                             cy + (int) (Math.sin(a2) * (radio - 8)), cy + (int) (Math.sin(a1) * (radio - 8)) };
                g.setColor(i % 2 == 0 ? new Color(0xB33A2C) : new Color(0xF3E4C0));
                g.fillPolygon(xs, ys, 4);
            }
            // hoja de la puerta con clavos dorados
            g.setColor(new Color(0x5A3418));
            g.fillRect(x, y + 4, m.width, m.height - 4);
            g.fillOval(x, y - 4, m.width, 16);
            g.setColor(new Color(0xE0B040));
            for (int yy = y + 8; yy < suelo - 2; yy += 6)
                for (int xx = x + 3; xx < x + m.width - 2; xx += 6) g.fillRect(xx, yy, 1, 1);
        }
        // destello que indica "aquí está la salida"
        if ((frame / 20) % 2 == 0) {
            g.setColor(new Color(255, 240, 160, 140));
            g.fillRect(x + m.width / 2 - 1, y - 14, 2, 6);
            g.fillRect(x + m.width / 2 - 3, y - 12, 6, 2);
        }
    }

    private void dibujarJugador(Graphics2D g, Player p, int camX, int camY, int frame) {
        BufferedImage img = p.fotogramaActual();
        int x = p.xDibujo() - camX, y = p.yDibujo() - camY;
        if (p.muerto && (frame / 3) % 2 == 0) return;   // parpadeo al morir
        // sombra en el suelo
        if (p.enSuelo) {
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRect(Math.round(p.x) - camX, Math.round(p.y) + Player.ALTO - 1 - camY, Player.ANCHO, 2);
        }
        g.drawImage(img, x, y, null);
    }

    // =====================================================================
    //  HUD (información en pantalla)
    // =====================================================================

    public void dibujarHUD(Graphics2D g, Level nivel, Player p, PhysicsConfig fisica,
                           int frame, int muertes, String aviso, Color colorAviso, int avisoTimer,
                           boolean ayudaCompleta) {
        // --- Nivel actual ---
        g.setFont(Ui.FUENTE);
        String titulo = "NIVEL " + nivel.getNumero() + " · " + nivel.getNombre();
        int tw = g.getFontMetrics().stringWidth(titulo);
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(4, 4, tw + 12, 16);
        Ui.texto(g, titulo, 10, 16, Ui.BORDE);

        // --- Variables actuales (mini consola) ---
        g.setFont(Ui.FUENTE_PEQ);
        int x = 636;
        for (int i = fisica.getCantidad() - 1; i >= 0; i--) {
            VariableFisica v = fisica.getVariable(i);
            String s = v.getNombre() + "=" + VariableFisica.formatear(v.getDecimas());
            int w = g.getFontMetrics().stringWidth(s) + 8;
            x -= w + 2;
            g.setColor(new Color(0x1E1E2E));
            g.fillRect(x, 4, w, 14);
            g.setColor(v.isBloqueada() ? new Color(0x44445A) : new Color(0x82AAFF));
            g.drawRect(x, 4, w - 1, 13);
            Ui.texto(g, s, x + 4, 14, v.isBloqueada() ? Ui.GRIS : Ui.TEXTO);
        }

        // --- Ayuda de controles (completa al empezar; luego solo la consola) ---
        String ayuda = "[E] Consola de código   [R] Reintentar   [ESC] Pausa   [M] Sonido" + (Sonido.isSilenciado() ? " (off)" : "");
        if (!ayudaCompleta) ayuda = "[E] Consola de código";
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(4, 342, g.getFontMetrics().stringWidth(ayuda) + 12, 14);
        boolean resaltar = ayudaCompleta && (frame / 25) % 2 == 0;
        Ui.texto(g, "[E] Consola de código", 10, 352, resaltar ? Ui.AMARILLO : Ui.TEXTO);
        int ancho = g.getFontMetrics().stringWidth("[E] Consola de código");
        Ui.texto(g, ayuda.substring("[E] Consola de código".length()), 10 + ancho, 352, Ui.TEXTO);
        String cont = "Caídas: " + muertes;
        Ui.texto(g, cont, 636 - g.getFontMetrics().stringWidth(cont), 352, Ui.TEXTO);

        // --- Mensaje de tutorial ---
        String msg = nivel.getMensaje();
        if (msg != null) dibujarMensaje(g, msg, nivel.getMensajeTimer());

        // --- Avisos especiales ---
        for (PlataformaDinamica pl : nivel.getPlataformas()) {
            if (pl instanceof PuenteDerrumbe && ((PuenteDerrumbe) pl).estaCrujiendo()) {
                PuenteDerrumbe pd = (PuenteDerrumbe) pl;
                g.setFont(Ui.FUENTE);
                String s = String.format("¡EL SUELO SE DERRUMBA!  %.1f s", pd.segundosRestantes());
                Ui.textoCentrado(g, s, 320, 92, (frame / 6) % 2 == 0 ? Ui.ROJO : Ui.AMARILLO);
                int bw = 160;
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(320 - bw / 2, 97, bw, 5);
                g.setColor(Ui.ROJO);
                g.fillRect(320 - bw / 2, 97, (int) (bw * (1 - pd.getProgreso())), 5);
            }
        }
        if (p.enCorriente && !p.muerto) {
            float g1 = fisica.getValor(PhysicsConfig.GRAVEDAD);
            float empuje = Level.FUERZA_CORRIENTE / PhysicsConfig.FACTOR_GRAVEDAD;
            g.setFont(Ui.FUENTE);
            String s = String.format("CORRIENTE DE AIRE: empuje %.1f  vs  gravedad %.1f", empuje, g1);
            Ui.textoCentrado(g, s, 320, 92, g1 > empuje ? Ui.VERDE : Ui.AMARILLO);
            String s2 = g1 > empuje ? "Pesas lo suficiente: ¡bajas!" : "La corriente te empuja hacia arriba...";
            g.setFont(Ui.FUENTE_PEQ);
            Ui.textoCentrado(g, s2, 320, 104, Ui.TEXTO);
        }

        // --- Aviso temporal (cambios aplicados, etc.) ---
        if (aviso != null && avisoTimer > 0) {
            g.setFont(Ui.FUENTE);
            int w = g.getFontMetrics().stringWidth(aviso) + 20;
            int ay = 320;
            Ui.panel(g, 320 - w / 2, ay - 13, w, 20, new Color(0x1E1E2E), colorAviso);
            Ui.textoCentrado(g, aviso, 320, ay + 1, colorAviso);
        }
    }

    /** Caja de diálogo del tutorial en la parte superior. */
    public void dibujarMensaje(Graphics2D g, String msg, int timer) {
        g.setFont(Ui.FUENTE);
        List<String> lineas = Ui.ajustar(g, msg, 520);
        int h = lineas.size() * 13 + 12;
        int w = 560, x = 40, y = 24;
        Composite antes = g.getComposite();
        if (timer < 30) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, timer / 30f)));
        Ui.panel(g, x, y, w, h, new Color(0x2B1D12), new Color(0xE0B040));
        // retrato de Coco
        g.drawImage(Sprites.COCO_QUIETO, x + 6, y + (h - 20) / 2, null);
        for (int i = 0; i < lineas.size(); i++) {
            Ui.texto(g, lineas.get(i), x + 32, y + 16 + i * 13, Ui.TEXTO);
        }
        g.setComposite(antes);
    }
}
