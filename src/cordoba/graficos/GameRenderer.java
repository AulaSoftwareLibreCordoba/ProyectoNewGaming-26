package cordoba.graficos;

import cordoba.audio.Sonido;
import cordoba.entidades.Fantasma;
import cordoba.entidades.Player;
import cordoba.fisica.PhysicsConfig;
import cordoba.fisica.VariableFisica;
import cordoba.graficos.ambiente.Agua;
import cordoba.graficos.ambiente.Ambiente;
import cordoba.graficos.luz.Iluminacion;
import cordoba.graficos.luz.PrimerPlano;
import cordoba.graficos.luz.Profundidad;
import cordoba.graficos.luz.SalaProfunda;
import cordoba.interfaz.Ui;
import cordoba.ranking.Puntuacion;
import cordoba.niveles.Level;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.elementos.Farola;
import cordoba.niveles.elementos.ObstaculoMovil;
import cordoba.niveles.plataformas.PlataformaDinamica;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

// Pinta la partida (fondo, tiles, objetos, Coco y HUD). Solo lee el estado, no cambia nada.
public class GameRenderer {

    private static final int T = Level.TILE;

    // efectos de luz (graficos/luz): pura decoración
    private final Profundidad profundidad = new Profundidad();
    private final Iluminacion iluminacion = new Iluminacion();

    // ---- escenario ----

    public void dibujarEscenario(Graphics2D g, Level nivel, Player p, Particulas part,
                                 int camX, int camY, int frame) {
        dibujarEscenario(g, nivel, p, part, null, 0, camX, camY, frame);
    }

    // igual, pero con el fantasma del récord detrás de Coco
    public void dibujarEscenario(Graphics2D g, Level nivel, Player p, Particulas part,
                                 Fantasma fantasma, int frameNivel, int camX, int camY, int frame) {
        dibujarFondo(g, nivel.getTema(), camX, camY, nivel.getAltoPx());
        iluminacion.fondo(g, nivel.getTema(), camX, camY);
        dibujarCorrientes(g, nivel, camX, camY, frame);
        dibujarDecoracion(g, nivel.getDecoraciones(), nivel.getTema(), camX, camY);
        profundidad.sombraTerreno(g, nivel, camX, camY);
        dibujarMeta(g, nivel, camX, camY, frame);
        dibujarTiles(g, nivel, camX, camY, frame);
        profundidad.volumen(g, nivel, camX, camY);
        for (Farola f : nivel.getFarolas()) dibujarFarola(g, f, nivel.getTema(), camX, camY, frame);
        for (PlataformaDinamica pl : nivel.getPlataformas()) pl.dibujar(g, camX, camY, frame);
        for (ObstaculoMovil o : nivel.getObstaculos()) o.dibujar(g, camX, camY, frame);
        if (fantasma != null) dibujarFantasma(g, fantasma, frameNivel, camX, camY);
        dibujarJugador(g, p, camX, camY, frame);
        iluminacion.aplicar(g, nivel, p, camX, camY, frame);
        part.dibujar(g, camX, camY);
        // cosas entre Coco y la cámara (desenfocadas) para dar profundidad
        Rectangle coco = new Rectangle(Math.round(p.x) - camX, Math.round(p.y) - camY, Player.ANCHO, Player.ALTO);
        PrimerPlano.dibujar(g, nivel.getTema(), camX, interiorMezquita(nivel.getTema(), camY), coco, frame);
        iluminacion.vineta(g, nivel.getTema());
    }

    // cuánto se está dentro de la Mezquita (0 fuera, 1 en la sala de columnas); misma cuenta que el fondo
    private static float interiorMezquita(Level.Tema tema, int camY) {
        if (tema != Level.Tema.MEZQUITA) return 0f;
        return Math.max(0f, Math.min(1f, (camY - 110) / 60f));
    }

    public void dibujarFondo(Graphics2D g, Level.Tema tema, int camX, int camY, int altoNivel) {
        dibujarFondo(g, tema, camX, camY, altoNivel, false);
    }

    // orilla = true pinta arena bajo las palmeras (en el menú no hay suelo que las sostenga)
    public void dibujarFondo(Graphics2D g, Level.Tema tema, int camX, int camY, int altoNivel, boolean orilla) {
        BufferedImage[] capas = Fondos.capas(tema);
        for (int i = 0; i < capas.length; i++) {
            float alfa = 1f;
            int dy = 0;
            if (tema == Level.Tema.RIO) dy = -camY / 4;
            if (tema == Level.Tema.MEZQUITA && i == 3) {
                // la sala de columnas aparece al bajar dentro de la Mezquita
                alfa = interiorMezquita(tema, camY);
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
            if (orilla && tema == Level.Tema.RIO && i == 3) Ambiente.orilla(g, desp, dy);
            if (tema == Level.Tema.MEZQUITA && i == 3) {
                SalaProfunda.dibujar(g, camX, dy, alfa);
                g.setComposite(antes);
                continue;
            }
            BufferedImage capa = Iluminacion.prepararCapa(tema, i, Ambiente.prepararCapa(tema, i, capas[i]));
            g.drawImage(capa, -desp, dy, null);
            g.drawImage(capa, -desp + Fondos.ANCHO, dy, null);
            g.setComposite(antes);
            Ambiente.trasCapa(g, tema, i, camX, dy);   // nubes y brillos del río
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
                    case '~': {
                        int prof = 0;   // bloques de agua que tiene encima
                        while (prof < 3 && nivel.tile(c, f - 1 - prof) == '~') prof++;
                        Agua.dibujarBloque(g, x, y, c, prof, frame);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        dibujarArcosCiegos(g, nivel, camX, camY, c1, c2, f1, f2);
    }

    // arcos ciegos de adorno en los muros grandes (como en la Mezquita o el Puente Romano)
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

    // arco de herradura con dovelas de dos colores
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

    // corriente de aire: rayitas blancas que suben
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

    private void dibujarDecoracion(Graphics2D g, List<Decoracion> lista, Level.Tema tema, int camX, int camY) {
        for (Decoracion d : lista) {
            BufferedImage img = d.imagen();
            int x = (int) d.x - img.getWidth() / 2 - camX;
            int y = (int) d.ySuelo - img.getHeight() - camY;
            if (x > 640 || x + img.getWidth() < 0) continue;
            Profundidad.sombraContacto(g, tema, (int) d.x - camX, (int) d.ySuelo - camY, Math.min(img.getWidth(), 24));
            g.drawImage(img, x, y, null);
        }
    }

    private void dibujarFarola(Graphics2D g, Farola f, Level.Tema tema, int camX, int camY, int frame) {
        int x = (int) f.x - 6 - camX, y = (int) f.ySuelo - 36 - camY;
        Profundidad.sombraContacto(g, tema, (int) f.x - camX, (int) f.ySuelo - camY, 10);
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

    // meta: la Calahorra (nivel 1) o la puerta de la Mezquita (nivel 2)
    private void dibujarMeta(Graphics2D g, Level nivel, int camX, int camY, int frame) {
        Rectangle m = nivel.getMeta();
        int x = m.x - camX, y = m.y - camY;
        int suelo = m.y + m.height - camY;
        if (nivel.getTema() == Level.Tema.RIO) {
            // Calahorra: dos torres unidas por un arco
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
            // bandera verde y blanca
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
            // puerta
            g.setColor(new Color(0x3A2416));
            g.fillRect(x, y, m.width, m.height);
            g.fillOval(x, y - m.width / 2, m.width, m.width);
        } else {
            // puerta de la Mezquita con arco bicolor
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
        // destello para que se vea dónde está la salida
        if ((frame / 20) % 2 == 0) {
            g.setColor(new Color(255, 240, 160, 140));
            g.fillRect(x + m.width / 2 - 1, y - 14, 2, 6);
            g.fillRect(x + m.width / 2 - 3, y - 12, 6, 2);
        }
    }

    // Coco transparente con el nombre del récord encima
    private void dibujarFantasma(Graphics2D g, Fantasma f, int frameNivel, int camX, int camY) {
        int fx = f.x(frameNivel), fy = f.y(frameNivel);
        int x = (f.miraDerecha(frameNivel) ? fx - 3 : fx - 5) - camX;
        int y = fy + Player.ALTO - 20 - camY;
        Composite antes = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.42f));
        g.drawImage(f.dibujo(frameNivel), x, y, null);
        g.setFont(Ui.FUENTE_PEQ);
        String etiqueta = f.terminado(frameNivel) ? f.getNombre() + " ¡META!" : f.getNombre();
        int w = g.getFontMetrics().stringWidth(etiqueta);
        Ui.texto(g, etiqueta, fx + Player.ANCHO / 2 - w / 2 - camX, y - 3, new Color(0xB8D8FF));
        g.setComposite(antes);
    }

    private void dibujarJugador(Graphics2D g, Player p, int camX, int camY, int frame) {
        BufferedImage img = p.fotogramaActual();
        int x = p.xDibujo() - camX, y = p.yDibujo() - camY;
        if (p.muerto && (frame / 3) % 2 == 0) return;   // parpadea al morir
        // sombra en el suelo
        if (p.enSuelo) {
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRect(Math.round(p.x) - camX, Math.round(p.y) + Player.ALTO - 1 - camY, Player.ANCHO, 2);
        }
        g.drawImage(img, x, y, null);
    }

    // ---- HUD ----

    public void dibujarHUD(Graphics2D g, Level nivel, Player p, PhysicsConfig fisica,
                           int frame, int muertes,
                           boolean ayudaCompleta, int framesPartida, int framesNivel,
                           Fantasma fantasma, boolean fantasmaVisible) {
        dibujarCronometro(g, 4, 22, framesPartida, framesNivel, fantasma, fantasmaVisible);

        // mini consola con las variables actuales
        g.setFont(Ui.FUENTE_PEQ);
        int x = 636;
        for (int i = fisica.getCantidad() - 1; i >= 0; i--) {
            VariableFisica v = fisica.getVariable(i);
            String etiqueta = v.getNombre() + "=";
            String valor = VariableFisica.formatear(v.getDecimas());
            int w = g.getFontMetrics().stringWidth(etiqueta) + 5 * Ui.CELDA_NUMERO + 8;
            x -= w + 2;
            g.setColor(new Color(0x1E1E2E));
            g.fillRect(x, 22, w, 17);
            g.setColor(v.isBloqueada() ? new Color(0x7D8195) : new Color(0x82AAFF));
            g.drawRect(x, 22, w - 1, 16);
            Ui.dato(g, etiqueta, valor, x + 4, 35, v.isBloqueada() ? Ui.GRIS : Ui.TEXTO);
        }

        // el título va en su zona y los indicadores siempre tienen su hueco
        g.setFont(Ui.FUENTE_HUD);
        String titulo = Ui.recortar(g, "NIVEL " + nivel.getNumero() + " · " + nivel.getNombre(), 616);
        int tw = g.getFontMetrics().stringWidth(titulo);
        g.setColor(Ui.PANEL);
        g.fillRect(4, 4, tw + 12, 16);
        Ui.texto(g, titulo, 10, 16, Ui.AMARILLO);
        g.setFont(Ui.FUENTE_PEQ);

        // ayuda de controles (completa al principio, luego solo la consola)
        String ayuda = "[E] Código   [R] Reintentar   [ESC] Pausa   [M] Sonido" + (Sonido.isSilenciado() ? " (off)" : "")
                + (fantasma != null ? "   [G] Fantasma" : "");
        if (!ayudaCompleta) ayuda = "[E] Código";
        String cont = String.valueOf(muertes);
        int cw = Ui.anchoDato(g, "Caídas: ", cont);
        ayuda = Ui.recortar(g, ayuda, 610 - cw);
        g.setColor(Ui.PANEL);
        g.fillRect(4, 340, g.getFontMetrics().stringWidth(ayuda) + 12, 18);
        boolean resaltar = ayudaCompleta && (frame / 25) % 2 == 0;
        Ui.texto(g, ayuda, 10, 352, resaltar ? Ui.AMARILLO : Ui.TEXTO);
        g.setColor(Ui.PANEL);
        g.fillRect(630 - cw, 340, cw + 10, 18);
        Ui.dato(g, "Caídas: ", cont, 635 - cw, 353, Ui.TEXTO);
    }

    // cronómetro; en el nivel 2 con fantasma, también tu tiempo del nivel contra el récord
    public void dibujarCronometro(Graphics2D g, int x, int y, int framesPartida, int framesNivel,
                                  Fantasma fantasma, boolean fantasmaVisible) {
        g.setColor(Ui.PANEL);
        g.fillRect(x, y, Ui.ANCHO_CRONOMETRO, 17);
        // relojito en pixel art
        g.setColor(Ui.TEXTO);
        g.drawRect(x + 4, y + 4, 7, 7);
        g.fillRect(x + 7, y + 5, 1, 3);
        g.fillRect(x + 8, y + 7, 2, 1);
        g.setFont(Ui.FUENTE_HUD);
        Ui.texto(g, "TIEMPO", x + 16, y + 13, Ui.AMARILLO);
        Ui.tiempo(g, Puntuacion.formatearTiempo(framesPartida), x + Ui.X_TIEMPO_HUD, y + 13, Ui.TEXTO);
        if (fantasma != null) {
            g.setColor(Ui.PANEL);
            g.fillRect(x, y + 20, 632, 17);
            g.setFont(Ui.FUENTE);
            boolean ganando = framesNivel <= fantasma.getFramesNivel();
            Ui.texto(g, "NIVEL", x + 6, y + 33, Ui.GRIS);
            Ui.tiempo(g, Puntuacion.formatearTiempo(framesNivel), x + 43, y + 33,
                    ganando ? new Color(0xB8D8FF) : Ui.ROJO);
            Ui.texto(g, "RÉCORD", x + 151, y + 33, Ui.GRIS);
            Ui.tiempo(g, Puntuacion.formatearTiempo(fantasma.getFramesNivel()), x + 200, y + 33, Ui.TEXTO);
            Ui.texto(g, fantasma.getNombre() + (fantasmaVisible ? "" : " (oculto)"), x + 312, y + 33, Ui.GRIS);
        }
    }
}
