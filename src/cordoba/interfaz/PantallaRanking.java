package cordoba.interfaz;

import cordoba.graficos.GameRenderer;
import cordoba.graficos.Sprites;
import cordoba.niveles.Level;
import cordoba.nucleo.Input;
import cordoba.ranking.Puntuacion;
import cordoba.ranking.Ranking;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Pantallas de la COMPETICIÓN:
 *   1. Final de la partida: estadísticas + escribir el nombre para el ranking.
 *   2. Tabla con el TOP 10 (también accesible desde el menú principal).
 */
public class PantallaRanking {

    private static final Color ORO    = new Color(0xFFD54F);
    private static final Color PLATA  = new Color(0xC8D0DC);
    private static final Color BRONCE = new Color(0xD9925B);
    private static final Color AZUL   = new Color(0x82AAFF);

    private final GameRenderer renderer;
    private final Ranking ranking;

    private final StringBuilder nombre = new StringBuilder();
    private int resaltado = -1;   // puesto recién conseguido (-1 = ninguno)

    private final Boton bGuardar = new Boton(255, 278, 130, 22, "GUARDAR", new Color(0x2E8B57));
    private final Boton bMenu    = new Boton(255, 298, 130, 22, "MENÚ", new Color(0x2E8B57));

    public PantallaRanking(GameRenderer renderer, Ranking ranking) {
        this.renderer = renderer;
        this.ranking = ranking;
    }

    /** Prepara la pantalla final para que el siguiente jugador escriba su nombre. */
    public void empezarNombre() {
        nombre.setLength(0);
        resaltado = -1;
    }

    /** Abre la tabla sin resaltar a nadie (desde el menú principal). */
    public void verTabla() {
        resaltado = -1;
    }

    // =====================================================================
    //  1. INTRODUCIR NOMBRE
    // =====================================================================

    /** Devuelve true cuando el jugador ha guardado su puntuación. */
    public boolean actualizarNombre(Input in, int frames, int muertes, int compilaciones) {
        boolean confirmar = false;
        for (char c : in.tomarEscrito().toCharArray()) {
            if (c == '\b') {
                if (nombre.length() > 0) nombre.setLength(nombre.length() - 1);
            } else if (c == '\n' || c == '\r') {
                confirmar = true;
            } else if (esValido(c) && nombre.length() < Ranking.MAX_NOMBRE
                    && !(c == ' ' && nombre.length() == 0)) {
                nombre.append(c);
            }
        }
        String limpio = Puntuacion.limpiar(nombre.toString());
        bGuardar.activo = !limpio.isEmpty();
        if (bGuardar.clicado(in)) confirmar = true;

        if (confirmar && bGuardar.activo) {
            resaltado = ranking.registrar(new Puntuacion(limpio, frames, muertes, compilaciones));
            cordoba.audio.Sonido.checkpoint();
            return true;
        }
        return false;
    }

    private static boolean esValido(char c) {
        return Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_' || c == '.';
    }

    public void dibujarNombre(Graphics2D g, Input in, int frames, int muertes, int compilaciones, int frame) {
        dibujarFondo(g, frame);
        Ui.panel(g, 130, 30, 380, 300, Ui.PANEL, Ui.BORDE);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "¡DEMO COMPLETADA!", 320, 62, Ui.BORDE);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Coco ha llegado a la Mezquita-Catedral", 320, 82, Ui.TEXTO);
        Ui.textoCentrado(g, "reprogramando las leyes de la física.", 320, 95, Ui.TEXTO);

        BufferedImage coco = (frame / 12) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        int bote = (frame / 12) % 2 == 0 ? 0 : -8;
        g.drawImage(coco, 292, 104 + bote, 56, 56, null);

        // Estadísticas (las mismas columnas que tendrá el ranking)
        Ui.textoCentrado(g, "Tiempo: " + Puntuacion.formatearTiempo(frames), 320, 178, Ui.TEXTO);
        Ui.textoCentrado(g, "Caídas: " + muertes, 320, 191, Ui.TEXTO);
        Ui.textoCentrado(g, "Cambios de código compilados: " + compilaciones, 320, 204, AZUL);

        // Puesto provisional
        int pos = ranking.posicionPara(frames, muertes, compilaciones);
        if (pos <= Ranking.TOP)
            Ui.textoCentrado(g, "¡Entras en el TOP 10 en el puesto " + pos + "º!", 320, 222, colorPuesto(pos, Ui.VERDE));
        else
            Ui.textoCentrado(g, "Puesto provisional: " + pos + "º", 320, 222, Ui.AMARILLO);

        // Caja de texto
        Ui.textoCentrado(g, "Escribe tu nombre para el ranking:", 320, 240, Ui.TEXTO);
        int bx = 220, by = 248, bw = 200, bh = 20;
        g.setColor(new Color(0x101018));
        g.fillRect(bx, by, bw, bh);
        g.setColor(Ui.VERDE);
        g.drawRect(bx, by, bw - 1, bh - 1);
        g.setColor(Ui.VERDE.darker().darker());
        g.drawRect(bx + 1, by + 1, bw - 3, bh - 3);

        g.setFont(Ui.FUENTE_CODIGO);
        int tx = bx + 8, ty = by + 14;
        if (nombre.length() == 0) {
            Ui.texto(g, "TU NOMBRE", tx, ty, new Color(0x55556A));
        } else {
            Ui.texto(g, nombre.toString(), tx, ty, Ui.VERDE);
        }
        if ((frame / 30) % 2 == 0) {   // cursor parpadeante
            int cx = tx + (nombre.length() == 0 ? 0 : g.getFontMetrics().stringWidth(nombre.toString()) + 1);
            g.setColor(Ui.TEXTO);
            g.fillRect(cx, by + 5, 2, 11);
        }
        g.setFont(Ui.FUENTE_PEQ);
        String cuenta = nombre.length() + "/" + Ranking.MAX_NOMBRE;
        Ui.texto(g, cuenta, bx + bw - 8 - g.getFontMetrics().stringWidth(cuenta), ty, Ui.GRIS);

        bGuardar.dibujar(g, in);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "ENTER: guardar  ·  RETROCESO: borrar", 320, 316, Ui.GRIS);
    }

    // =====================================================================
    //  2. TABLA TOP 10
    // =====================================================================

    /** Devuelve true cuando el jugador quiere volver al menú. */
    public boolean actualizarTabla(Input in) {
        return bMenu.clicado(in) || in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_ESCAPE);
    }

    public void dibujarTabla(Graphics2D g, Input in, int frame) {
        dibujarFondo(g, frame);
        Ui.panel(g, 80, 18, 480, 324, Ui.PANEL, Ui.BORDE);

        // Título con Coco a ambos lados
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "RANKING · TOP 10", 320, 48, Ui.BORDE);
        BufferedImage coco = (frame / 20) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        g.drawImage(coco, 150, 24, 30, 30, null);
        g.drawImage(Sprites.volteada(coco), 460, 24, 30, 30, null);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "Menos tiempo gana · empate: menos caídas y compilaciones", 320, 64, Ui.GRIS);

        // Cabecera
        final int cPos = 100, cNombre = 136, cTiempo = 290, cCaidas = 375, cComp = 480;
        g.setFont(Ui.FUENTE);
        int y = 86;
        Ui.texto(g, "#", cPos, y, Ui.AMARILLO);
        Ui.texto(g, "NOMBRE", cNombre, y, Ui.AMARILLO);
        Ui.textoCentrado(g, "TIEMPO", cTiempo, y, Ui.AMARILLO);
        Ui.textoCentrado(g, "CAÍDAS", cCaidas, y, Ui.AMARILLO);
        Ui.textoCentrado(g, "COMPILACIONES", cComp, y, Ui.AMARILLO);
        g.setColor(Ui.BORDE.darker());
        g.fillRect(96, y + 5, 448, 1);

        List<Puntuacion> mejores = ranking.mejores();
        for (int i = 0; i < Ranking.TOP; i++) {
            int puesto = i + 1;
            int fy = 106 + i * 17;
            if (i % 2 == 1) {   // filas alternas, como un listado de IDE
                g.setColor(Ui.PANEL_2);
                g.fillRect(96, fy - 12, 448, 17);
            }
            if (i >= mejores.size()) {
                Ui.texto(g, puesto + ".", cPos, fy, new Color(0x55556A));
                Ui.texto(g, "---", cNombre, fy, new Color(0x55556A));
                continue;
            }
            Puntuacion p = mejores.get(i);
            boolean esNuevo = puesto == resaltado;
            if (esNuevo) {
                boolean brillo = (frame / 15) % 2 == 0;
                g.setColor(brillo ? new Color(0x2E5A3A) : new Color(0x244A30));
                g.fillRect(96, fy - 12, 448, 17);
                g.setColor(Ui.VERDE);
                g.drawRect(96, fy - 12, 447, 16);
            }
            if (puesto <= 3) dibujarMedalla(g, cPos + 14, fy - 9, colorPuesto(puesto, Ui.TEXTO));
            Color color = esNuevo ? Ui.VERDE : colorPuesto(puesto, Ui.TEXTO);
            Ui.texto(g, puesto + ".", cPos, fy, color);
            Ui.texto(g, p.nombre(), cNombre, fy, esNuevo ? Ui.VERDE : Ui.TEXTO);
            Ui.textoCentrado(g, p.tiempo(), cTiempo, fy, color);
            Ui.textoCentrado(g, String.valueOf(p.muertes()), cCaidas, fy, esNuevo ? Ui.VERDE : Ui.TEXTO);
            Ui.textoCentrado(g, String.valueOf(p.compilaciones()), cComp, fy, esNuevo ? Ui.VERDE : AZUL);
        }

        // Mensaje inferior
        int my = 286;
        if (!ranking.isGuardadoOk()) {
            Ui.textoCentrado(g, "¡Error! No se pudo guardar el fichero ranking.txt", 320, my, Ui.ROJO);
        } else if (resaltado > 0 && resaltado <= Ranking.TOP) {
            Ui.textoCentrado(g, "¡Enhorabuena! Has quedado en el puesto " + resaltado + "º", 320, my, Ui.VERDE);
        } else if (resaltado > Ranking.TOP) {
            Ui.textoCentrado(g, "Has quedado en el puesto " + resaltado + "º · ¡a por el TOP 10!", 320, my, Ui.AMARILLO);
        } else if (mejores.isEmpty()) {
            Ui.textoCentrado(g, "Aún no hay récords. ¡Sé el primero!", 320, my, Ui.AMARILLO);
        }
        bMenu.dibujar(g, in);
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    private void dibujarFondo(Graphics2D g, int frame) {
        renderer.dibujarFondo(g, Level.Tema.MEZQUITA, frame / 3, 0, 544);
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, 640, 360);
    }

    private static Color colorPuesto(int puesto, Color otro) {
        switch (puesto) {
            case 1:  return ORO;
            case 2:  return PLATA;
            case 3:  return BRONCE;
            default: return otro;
        }
    }

    /** Medalla de 8x8 en pixel art. */
    private static void dibujarMedalla(Graphics2D g, int x, int y, Color color) {
        g.setColor(color.darker().darker());
        g.fillRect(x + 1, y, 6, 8);
        g.fillRect(x, y + 1, 8, 6);
        g.setColor(color);
        g.fillRect(x + 1, y + 1, 6, 6);
        g.setColor(color.brighter());
        g.fillRect(x + 2, y + 2, 2, 2);
    }
}
