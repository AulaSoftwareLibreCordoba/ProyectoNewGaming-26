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
import cordoba.niveles.elementos.Coleccionable;
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
        dibujarFondo(g, nivel.getFondo(), camX, camY, nivel.getAltoPx());
        iluminacion.fondo(g, nivel.getFondo(), camX, camY);
        dibujarCorrientes(g, nivel, camX, camY, frame);
        dibujarFondoPiedra(g, nivel, camX, camY);
        dibujarBrisas(g, nivel, camX, camY, frame);
        dibujarDecoracion(g, nivel.getDecoraciones(), nivel.getTema(), camX, camY);
        profundidad.sombraTerreno(g, nivel, camX, camY);
        dibujarMeta(g, nivel, camX, camY, frame);
        dibujarTiles(g, nivel, camX, camY, frame);
        profundidad.volumen(g, nivel, camX, camY);
        for (Farola f : nivel.getFarolas()) dibujarFarola(g, f, nivel.getTema(), camX, camY, frame);
        for (Coleccionable c : nivel.getColeccionables()) dibujarColeccionable(g, c, camX, camY, frame);
        for (PlataformaDinamica pl : nivel.getPlataformas()) pl.dibujar(g, camX, camY, frame);
        for (ObstaculoMovil o : nivel.getObstaculos()) o.dibujar(g, camX, camY, frame);
        if (fantasma != null) dibujarFantasma(g, fantasma, frameNivel, camX, camY);
        dibujarJugador(g, p, camX, camY, frame);
        iluminacion.aplicar(g, nivel, p, camX, camY, frame);
        part.dibujar(g, camX, camY);
        // cosas entre Coco y la cámara (desenfocadas) para dar profundidad
        Rectangle coco = new Rectangle(Math.round(p.x) - camX, Math.round(p.y) - camY, Player.ANCHO, Player.ALTO);
        PrimerPlano.dibujar(g, nivel.getFondo(), camX, camY, interiorMezquita(nivel.getFondo(), camY), coco, frame);
        iluminacion.vineta(g, nivel.getFondo());
    }

    // el nivel tal cual para el creador de niveles: sin luces ni primer plano, para ver bien cada casilla.
    // p = Coco en su sitio de salida (o null si todavía no se ha puesto)
    public void dibujarVistaEditor(Graphics2D g, Level nivel, Player p, int camX, int camY, int frame) {
        dibujarFondo(g, nivel.getFondo(), camX, camY, nivel.getAltoPx());
        dibujarCorrientes(g, nivel, camX, camY, frame);
        dibujarFondoPiedra(g, nivel, camX, camY);
        dibujarBrisas(g, nivel, camX, camY, frame);
        dibujarDecoracion(g, nivel.getDecoraciones(), nivel.getTema(), camX, camY);
        if (nivel.getMeta().width > 0) dibujarMeta(g, nivel, camX, camY, frame);
        dibujarTiles(g, nivel, camX, camY, frame);
        for (Farola f : nivel.getFarolas()) dibujarFarola(g, f, nivel.getTema(), camX, camY, frame);
        for (Coleccionable c : nivel.getColeccionables()) dibujarColeccionable(g, c, camX, camY, frame);
        if (p != null) dibujarJugador(g, p, camX, camY, frame);
    }

    // 0 fuera de la Mezquita, 1 dentro
    private static float interiorMezquita(Level.Tema tema, int camY) {
        if (tema != Level.Tema.MEZQUITA) return 0f;
        return Math.max(0f, Math.min(1f, (camY - 110) / 60f));
    }

    // Calleja (nivel vertical): el cielo quieto, la torre se hunde al bajar y las fachadas, más altas que
    // la pantalla, recorren toda su altura mientras la cámara recorre la del nivel
    private static int dyCalleja(int capa, int camY, int altoNivel, int altoCapa) {
        if (capa == 0) return 0;
        if (capa == 1) return camY / 4;
        float recorrido = Math.max(1, altoNivel - 360);
        return -Math.round(Math.min(1f, camY / recorrido) * (altoCapa - 360));
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
            if (tema == Level.Tema.CALLEJA) dy = dyCalleja(i, camY, altoNivel, capas[i].getHeight());
            if (tema == Level.Tema.MEZQUITA && i == 3) {
                // la sala de columnas aparece al bajar dentro de la Mezquita
                alfa = interiorMezquita(tema, camY);
                if (alfa <= 0) continue;
                dy = 90;
            }
            int desp = (int) (camX * Fondos.PARALLAX[i]) % Fondos.ANCHO;
            Composite antes = g.getComposite();
            if (alfa < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
            if (dy > 0 && tema == Level.Tema.MEZQUITA) {
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
                        if (junto(nivel, c, f, 'p')) g.drawImage(Sprites.tile(tema, 'p', 0, false, false), x, y, null);
                        g.drawImage(Sprites.tile(tema, t, 0, false, false), x, y, null);
                        break;
                    case 'p':   // fondo de piedra: ya se ha pintado antes, detrás de todo (ver dibujarFondoPiedra)
                        break;
                    case '~': {
                        if (junto(nivel, c, f, 'p')) g.drawImage(Sprites.tile(tema, 'p', 1, false, false), x, y, null);
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
        for (Rectangle r : nivel.getParedesFloridas()) dibujarParedFlorida(g, r, camX, camY);
    }

    // fondo de piedra de pozos y aljibes ('p'): se pinta antes que la decoración y la meta para que no las tape
    private void dibujarFondoPiedra(Graphics2D g, Level nivel, int camX, int camY) {
        int c1 = Math.max(0, camX / T), c2 = Math.min(nivel.getColumnas() - 1, (camX + 640) / T);
        int f1 = Math.max(0, camY / T), f2 = Math.min(nivel.getFilas() - 1, (camY + 360) / T);
        for (int f = f1; f <= f2; f++)
            for (int c = c1; c <= c2; c++)
                if (nivel.tile(c, f) == 'p')
                    g.drawImage(Sprites.tile(nivel.getTema(), 'p', (c * 5 + f * 3) % 2, false, false), c * T - camX, f * T - camY, null);
    }

    // brisa del pozo: un velo claro y rayitas que suben despacio
    private void dibujarBrisas(Graphics2D g, Level nivel, int camX, int camY, int frame) {
        for (Rectangle r : nivel.getBrisas()) {
            int x0 = r.x - camX, y0 = r.y - camY;
            if (x0 > 640 || x0 + r.width < 0 || y0 > 360 || y0 + r.height < 0) continue;
            g.setColor(new Color(200, 225, 255, 22));
            g.fillRect(x0, Math.max(0, y0), r.width, Math.min(r.height, 360));
            g.setColor(new Color(225, 240, 255, 120));
            for (int k = 0; k < r.width / 6; k++) {
                int lx = x0 + 3 + (k * 37) % r.width;
                int largo = 6 + (k % 3) * 3;
                int recorrido = r.height + largo;
                int ly = y0 + r.height - Math.floorMod(frame + k * 53, recorrido);   // suben a 1 px por fotograma
                if (ly + largo < 0 || ly > 360) continue;
                int o = (int) Math.round(Math.sin((frame + k * 20) / 18.0) * 2);   // se mece un poco
                g.fillRect(lx + o, ly, 1, largo);
            }
        }
    }

    // pared de patio cordobés: cal, macetas azules con flores, una ventana con contraventanas y jardinera,
    // una reja, hiedra y un arco de ladrillo abajo. Va encima de las casillas 'S' (solo es dibujo)
    private void dibujarParedFlorida(Graphics2D g, Rectangle r, int camX, int camY) {
        int x0 = r.x - camX, y0 = r.y - camY, w = r.width, h = r.height;
        if (x0 > 640 || x0 + w < 0 || y0 > 360 || y0 + h < 0) return;
        Rectangle ventana = new Rectangle(w / 2 - 22, 26, 44, 60);
        Rectangle reja = new Rectangle(14, 40, 32, 44);
        Rectangle arco = new Rectangle(w / 2 - 34, h - 96, 68, 96);

        // macetas azules colgadas por toda la pared, al tresbolillo
        for (int fila = 0, y = 8; y < h - 20; fila++, y += 28) {
            for (int x = 6 + (fila % 2) * 11; x < w - 14; x += 22) {
                Rectangle hueco = new Rectangle(x - 4, y - 4, 20, 22);
                if (hueco.intersects(ventana) || hueco.intersects(reja) || hueco.intersects(arco)) continue;
                g.setColor(new Color(0x4A3A2A));
                g.fillRect(x0 + x + 5, y0 + y - 2, 2, 2);                 // el clavo
                g.drawImage(Sprites.maceta(Math.floorMod(x * 7 + fila * 3, 4)), x0 + x, y0 + y, null);
            }
        }
        // hiedra por el borde izquierdo
        for (int y = 4; y < h; y += 5) {
            int dx = (int) Math.round(Math.sin(y / 9.0) * 3);
            g.setColor(y % 10 == 4 ? new Color(0x2E7D32) : new Color(0x43A047));
            g.fillRect(x0 + 2 + dx, y0 + y, 5, 4);
            if (y % 15 == 4) g.fillRect(x0 + 7 + dx, y0 + y + 1, 4, 3);
        }
        // ventana con marco de madera, contraventanas abiertas y jardinera
        int vx = x0 + ventana.x, vy = y0 + ventana.y;
        g.setColor(new Color(0x8B4A2B));
        g.fillRect(vx - 8, vy, 8, 46);
        g.fillRect(vx + ventana.width, vy, 8, 46);
        g.fillRect(vx, vy, ventana.width, 48);
        g.setColor(new Color(0x5B7FA6));
        g.fillRect(vx + 4, vy + 4, ventana.width - 8, 40);
        g.setColor(new Color(0x6B3A20));
        g.fillRect(vx + ventana.width / 2 - 1, vy + 4, 2, 40);
        for (int k = 1; k < 4; k++) g.fillRect(vx + 4, vy + 4 + k * 10, ventana.width - 8, 1);
        g.setColor(new Color(0x7A4A2A));                                    // jardinera
        g.fillRect(vx - 6, vy + 46, ventana.width + 12, 8);
        Color[] flores = { new Color(0xE8344A), new Color(0xFF6FA8), new Color(0xD81B60) };
        for (int k = 0; k < 9; k++) {
            g.setColor(new Color(0x2E7D32));
            g.fillRect(vx - 4 + k * 6, vy + 42, 5, 5);
            g.setColor(flores[k % flores.length]);
            g.fillRect(vx - 3 + k * 6, vy + 39 + (k % 2) * 2, 4, 4);
        }
        // ventana con reja y macetas dentro
        int rx = x0 + reja.x, ry = y0 + reja.y;
        g.setColor(new Color(0x2E3446));
        g.fillRect(rx, ry, reja.width, reja.height);
        g.drawImage(Sprites.maceta(1), rx + 3, ry + reja.height - 16, null);
        g.drawImage(Sprites.maceta(2), rx + 17, ry + reja.height - 16, null);
        g.setColor(new Color(0x15161C));
        for (int k = 0; k <= reja.width; k += 5) g.fillRect(rx + k, ry - 2, 1, reja.height + 4);
        g.fillRect(rx - 2, ry - 2, reja.width + 4, 2);
        g.fillRect(rx - 2, ry + reja.height / 2, reja.width + 4, 1);
        g.fillRect(rx - 2, ry + reja.height + 1, reja.width + 4, 2);
        // arco de ladrillo con el zaguán en penumbra y un farol colgado
        int ax = x0 + arco.x, ay = y0 + arco.y, aw = arco.width, ah = arco.height;
        g.setColor(new Color(0xB8703E));
        g.fillRect(ax, ay + aw / 2, aw, ah - aw / 2);
        g.fillOval(ax, ay, aw, aw);
        g.setColor(new Color(0xD9935A));
        for (int k = 0; k < 9; k++) {                                       // dovelas de ladrillo
            double a = Math.PI + k * Math.PI / 8;
            g.fillRect(ax + aw / 2 + (int) (Math.cos(a) * (aw / 2 - 3)) - 2, ay + aw / 2 + (int) (Math.sin(a) * (aw / 2 - 3)) - 2, 4, 4);
        }
        g.setColor(new Color(0x3A2A20));
        g.fillRect(ax + 8, ay + aw / 2, aw - 16, ah - aw / 2);
        g.fillOval(ax + 8, ay + 8, aw - 16, aw - 16);
        g.setColor(new Color(0x22252B));                                    // farol
        g.fillRect(ax + aw / 2, ay + 14, 1, 10);
        g.fillRect(ax + aw / 2 - 3, ay + 24, 7, 9);
        g.setColor(new Color(0xFFD27A));
        g.fillRect(ax + aw / 2 - 2, ay + 26, 5, 5);
        g.setColor(new Color(0x6B4A2A));                                    // banco del zaguán
        g.fillRect(ax + 14, ay + ah - 14, 22, 4);
        g.fillRect(ax + 15, ay + ah - 10, 2, 10);
        g.fillRect(ax + 33, ay + ah - 10, 2, 10);
        g.drawImage(Sprites.maceta(0), ax + aw - 22, ay + ah - 13, null);
    }

    private static boolean enParedFlorida(Level nivel, int c, int f) {
        for (Rectangle r : nivel.getParedesFloridas())
            if (r.intersects(c * T - 16, f * T - 16, 64, 80)) return true;
        return false;
    }

    // ¿hay un tile de ese tipo al lado (izquierda, derecha o encima)?
    private static boolean junto(Level nivel, int c, int f, char tipo) {
        return nivel.tile(c - 1, f) == tipo || nivel.tile(c + 1, f) == tipo || nivel.tile(c, f - 1) == tipo;
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
                if (!macizo || enParedFlorida(nivel, c, f)) continue;
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
                } else if (nivel.getTema() == Level.Tema.CALLEJA) {
                    // ventana con reja y macetas azules
                    g.setColor(new Color(0xE0B85C));
                    g.fillRect(x + 5, y + 8, 22, 30);
                    g.setColor(new Color(0x2E3446));
                    g.fillRect(x + 7, y + 10, 18, 26);
                    g.setColor(new Color(0x15161C));
                    for (int k = 0; k < 18; k += 4) g.fillRect(x + 7 + k, y + 10, 1, 26);
                    g.fillRect(x + 7, y + 22, 18, 1);
                    g.fillRect(x + 3, y + 36, 26, 2);
                    g.drawImage(Sprites.maceta(Math.floorMod(c + f, 4)), x + 3, y + 25, null);
                    g.drawImage(Sprites.maceta(Math.floorMod(c + f + 1, 4)), x + 17, y + 25, null);
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

    // comida flotando con un brillo
    private void dibujarColeccionable(Graphics2D g, Coleccionable c, int camX, int camY, int frame) {
        if (c.recogido) return;
        int cx = Math.round(c.x) - camX;
        int cy = Math.round(c.y) - camY + (int) Math.round(Math.sin(frame * 0.08 + c.x * 0.1) * 2);
        if (cx < -20 || cx > 660 || cy < -20 || cy > 380) return;
        int r = 11 + (frame / 12) % 2;
        g.setColor(new Color(255, 226, 122, 40));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(new Color(255, 240, 180, 55));
        g.fillOval(cx - 7, cy - 7, 14, 14);
        BufferedImage img = c.tipo == Coleccionable.Tipo.SALMOREJO ? Sprites.salmorejo() : Sprites.flamenquin();
        g.drawImage(img, cx - 8, cy - 8, null);
        // destello
        int fase = (frame + (int) c.x) % 120;
        if (fase < 8) {
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRect(cx - 6 + fase * 2, cy - 5, 1, 1);
            g.fillRect(cx - 7 + fase * 2, cy - 4, 1, 1);
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
    // bandera de Andalucía; base = pie del mástil
    private void dibujarBandera(Graphics2D g, int fx, int base, int frame) {
        int arriba = base - 20;
        g.setColor(new Color(0x3A2A1A));
        g.fillRect(fx, arriba, 1, 20);
        int ondula = (frame / 10) % 2;
        g.setColor(new Color(0x1E8E3E));
        g.fillRect(fx + 1, arriba + ondula, 12, 3);
        g.setColor(Color.WHITE);
        g.fillRect(fx + 1, arriba + 3 + ondula, 12, 3);
        g.setColor(new Color(0x1E8E3E));
        g.fillRect(fx + 1, arriba + 6 + ondula, 12, 3);
    }

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
            dibujarBandera(g, bx + 10, suelo - 108, frame);
            // puerta
            g.setColor(new Color(0x3A2416));
            g.fillRect(x, y, m.width, m.height);
            g.fillOval(x, y - m.width / 2, m.width, m.width);
        } else if (nivel.getTema() == Level.Tema.CALLEJA) {
            // casa encalada con portón, macetas azules y bandera (bajo tierra, portal de piedra: la salida del aljibe)
            boolean bajoTierra = nivel.tile(m.x / T, m.y / T - 1) == 'p';
            int bx = x - 22, w = m.width + 44, alto = 82;
            g.setColor(bajoTierra ? new Color(0x9A8C76) : new Color(0xF6F2EA));
            g.fillRect(bx, suelo - alto, w, alto);
            g.setColor(bajoTierra ? new Color(0x7A6E5C) : new Color(0xDCD3C2));
            g.fillRect(bx + w - 4, suelo - alto, 4, alto);
            g.setColor(new Color(0xE0B85C));                       // zócalo de albero
            g.fillRect(bx, suelo - 10, w, 10);
            g.setColor(new Color(0xB5553C));                       // alero de teja
            g.fillRect(bx - 4, suelo - alto - 6, w + 8, 7);
            g.setColor(new Color(0x8E3E2B));
            for (int tx = bx - 4; tx < bx + w + 4; tx += 4) g.fillRect(tx, suelo - alto - 6, 1, 7);
            dibujarBandera(g, bx + 8, suelo - alto - 6, frame);
            // marco de albero y portón de madera con arco
            g.setColor(new Color(0xD9A441));
            g.fillRect(x - 4, y - 2, m.width + 8, m.height + 2);
            g.fillOval(x - 4, y - m.width / 2 - 4, m.width + 8, m.width + 8);
            g.setColor(new Color(0x6B3E1F));
            g.fillRect(x, y, m.width, m.height);
            g.fillOval(x, y - m.width / 2, m.width, m.width);
            g.setColor(new Color(0x4A2A14));
            g.fillRect(x + m.width / 2, y - m.width / 2 + 2, 1, m.height + m.width / 2 - 2);
            g.setColor(new Color(0xE0C060));
            g.fillRect(x + m.width / 2 - 4, y + m.height / 2, 2, 2);
            g.fillRect(x + m.width / 2 + 3, y + m.height / 2, 2, 2);
            // macetas a los lados del portón
            BufferedImage maceta = Sprites.maceta(0), maceta2 = Sprites.maceta(1);
            g.drawImage(maceta, bx + 4, suelo - 48, null);
            g.drawImage(maceta2, bx + w - 16, suelo - 48, null);
            g.drawImage(maceta2, bx + 4, suelo - 70, null);
            g.drawImage(maceta, bx + w - 16, suelo - 70, null);
        } else {
            // puerta de la Mezquita con arco bicolor
            int cx = x + m.width / 2;
            g.setColor(new Color(0xE0C590));
            g.fillRect(x - 18, suelo - 76, m.width + 36, 76);
            g.setColor(new Color(0xF6E7C4));
            g.fillRect(x - 18, suelo - 76, m.width + 36, 2);
            for (int i = 0; i < m.width + 36; i += 6) g.fillRect(x - 18 + i, suelo - 80, 4, 4);
            // bandera
            dibujarBandera(g, x - 18 + 10, suelo - 80, frame);
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
                           Fantasma fantasma, boolean fantasmaVisible, int timerBonus) {
        dibujarCronometro(g, 4, 4, framesPartida, framesNivel, fantasma, fantasmaVisible);

        // "-3s" al coger comida
        if (timerBonus > 0 && (timerBonus > 30 || (timerBonus / 4) % 2 == 0)) {
            g.setFont(Ui.FUENTE_HUD);
            String txt = "-" + Coleccionable.BONUS_FRAMES / 60 + "s";
            int w = g.getFontMetrics().stringWidth(txt) + 10;
            int bx = 4 + Ui.ANCHO_CRONOMETRO + 3;
            g.setColor(Ui.PANEL);
            g.fillRect(bx, 4, w, 17);
            Ui.texto(g, txt, bx + 5, 17, new Color(0x7BE07B));
        }

        // HUD limpio: sin variables de física ni nombre del nivel (se ven en la consola)
        g.setFont(Ui.FUENTE_PEQ);

        // ayuda de controles (completa al principio, luego solo la consola)
        String ayuda = "[E/ENTER] Código   [R] Reintentar   [ESC] Pausa   [M] Sonido" + (Sonido.isSilenciado() ? " (off)" : "")
                + (fantasma != null ? "   [G] Fantasma" : "");
        if (!ayudaCompleta) ayuda = "[E/ENTER] Código";
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

    // diferencia en fotogramas -> "-12.3s" / "+3.1s"
    static String diferencia(int frames) {
        int decimas = Math.round(Math.abs(frames) / 6f);
        return (frames > 0 ? "+" : "-") + decimas / 10 + "." + decimas % 10 + "s";
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
            Color colorNivel = ganando ? new Color(0xB8D8FF) : Ui.ROJO;
            Ui.texto(g, "NIVEL", x + 6, y + 33, Ui.GRIS);
            Ui.tiempo(g, Puntuacion.formatearTiempo(framesNivel), x + 43, y + 33, colorNivel);
            // diferencia con el récord (con signo, no solo color)
            Ui.texto(g, diferencia(framesNivel - fantasma.getFramesNivel()), x + 43 + Ui.ANCHO_TIEMPO + 6, y + 33, colorNivel);
            Ui.texto(g, "RÉCORD", x + 210, y + 33, Ui.GRIS);
            Ui.tiempo(g, Puntuacion.formatearTiempo(fantasma.getFramesNivel()), x + 259, y + 33, Ui.TEXTO);
            Ui.texto(g, fantasma.getNombre() + (fantasmaVisible ? "" : " (oculto)"), x + 371, y + 33, Ui.GRIS);
        }
    }
}
