package cordoba.interfaz;

import cordoba.audio.Sonido;
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

// Pantalla final (nombre) y TOP 10.
public class PantallaRanking {

    private static final Color ORO    = new Color(0xFFD54F);
    private static final Color PLATA  = new Color(0xC8D0DC);
    private static final Color BRONCE = new Color(0xD9925B);
    private static final Color AZUL   = new Color(0x82AAFF);
    private static final Color APAGADO = Ui.GRIS;

    private static final String[] PESTANAS = { "GENERAL", "NIVEL 1", "NIVEL 2", "NIVEL 3" };

    private final GameRenderer renderer;
    private final Ranking ranking;

    private final StringBuilder nombre = new StringBuilder();
    private Puntuacion resaltada;   // la partida que se acaba de guardar (o null)
    private int puesto;             // su puesto en el general
    private int pestana;            // pestaña activa (el foco va en navegacion)

    private final Boton bEmpezar = new Boton(240, 210, 160, 32, "¡A JUGAR!", Andalusi.BOTON_VERDE).grande();
    private final Boton bVerRanking = new Boton(206, 270, 228, 32, "VER RANKING", Andalusi.BOTON_ALBERO).grande();
    private final Boton bMenu    = new Boton(255, 300, 130, 22, "MENÚ", Andalusi.BOTON_VERDE).andalusi();
    private final Boton[] bPestanas = new Boton[PESTANAS.length];
    private final GameMenu.NavegacionMenu navegacion;
    private boolean iniciarFoco = true;

    public PantallaRanking(GameRenderer renderer, Ranking ranking) {
        this.renderer = renderer;
        this.ranking = ranking;
        for (int i = 0; i < PESTANAS.length; i++) {
            bPestanas[i] = new Boton(320 - PESTANAS.length * 51 + 3 + i * 102, 68, 96, 16, PESTANAS[i], new Color(0x5A6070)).andalusi();
        }
        Boton[] opciones = new Boton[bPestanas.length + 1];
        System.arraycopy(bPestanas, 0, opciones, 0, bPestanas.length);
        opciones[bPestanas.length] = bMenu;
        navegacion = new GameMenu.NavegacionMenu(opciones);
    }

    // desde el menú: sin resaltar a nadie
    public void verTabla() {
        resaltada = null;
        puesto = -1;
        pestana = 0;
        reiniciarFoco();
    }

    // ---- 1. el nombre, al empezar la partida ----

    // deja la caja con el nombre anterior (para que el mismo jugador solo tenga que pulsar ENTER)
    public void pedirNombre(Input in, String anterior) {
        nombre.setLength(0);
        nombre.append(anterior == null ? "" : anterior);
        in.tomarEscrito();   // lo que se tecleó antes (p. ej. el ENTER de JUGAR) no cuenta
        resaltada = null;
        puesto = -1;
    }

    // el nombre ya limpio cuando se confirma; null mientras se escribe
    public String actualizarPedirNombre(Input in) {
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
        bEmpezar.activo = !limpio.isEmpty();
        if (bEmpezar.clicado(in)) confirmar = true;
        if (confirmar && bEmpezar.activo) {
            Sonido.checkpoint();
            return limpio;
        }
        return null;
    }

    public void dibujarPedirNombre(Graphics2D g, Input in, int frame) {
        dibujarFondo(g, frame);
        Ui.panel(g, 150, 60, 340, 220, Ui.PANEL, Ui.BORDE);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "¿CÓMO TE LLAMAS?", 320, 96, Ui.BORDE);
        BufferedImage coco = (frame / 20) % 2 == 0 ? Sprites.COCO_QUIETO : Sprites.COCO_PARPADEO;
        g.drawImage(coco, 300, 106, 40, 40, null);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Con este nombre saldrás en el ranking", 320, 166, Ui.TEXTO);
        dibujarCaja(g, 220, 176, frame);
        bEmpezar.dibujar(g, in);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "ENTER: empezar  ·  RETROCESO: borrar  ·  ESC: volver", 320, 270, Ui.GRIS);
    }

    // caja de texto con el nombre, el cursor y la cuenta de letras
    private void dibujarCaja(Graphics2D g, int bx, int by, int frame) {
        int bw = 200, bh = 20;
        g.setColor(new Color(0x101018));
        g.fillRect(bx, by, bw, bh);
        g.setColor(Ui.VERDE);
        g.drawRect(bx, by, bw - 1, bh - 1);
        g.setColor(Ui.VERDE.darker().darker());
        g.drawRect(bx + 1, by + 1, bw - 3, bh - 3);
        g.setFont(Ui.FUENTE_CODIGO);
        int tx = bx + 8, ty = by + 14;
        if (nombre.length() == 0) Ui.texto(g, "TU NOMBRE", tx, ty, APAGADO);
        else Ui.texto(g, nombre.toString(), tx, ty, Ui.VERDE);
        if ((frame / 30) % 2 == 0) {   // cursor parpadeando
            int cx = tx + (nombre.length() == 0 ? 0 : g.getFontMetrics().stringWidth(nombre.toString()) + 1);
            g.setColor(Ui.TEXTO);
            g.fillRect(cx, by + 5, 2, 11);
        }
        g.setFont(Ui.FUENTE_PEQ);
        String cuenta = nombre.length() + "/" + Ranking.MAX_NOMBRE;
        Ui.texto(g, cuenta, bx + bw - 8 - g.getFontMetrics().stringWidth(cuenta), ty, Ui.GRIS);
    }

    // ---- TOP 5 del nivel al completarlo (con tu tiempo metido en su puesto) ----

    public void dibujarTop5Nivel(Graphics2D g, int nivel, String nombreJugador, int tiempo, int y, int frame) {
        List<Puntuacion> mejores = ranking.mejoresNivel(nivel);
        int tuPuesto = 1;   // en empate va delante quien lo hizo antes
        for (Puntuacion p : ranking.mejoresNivel(nivel)) if (p.framesNivel(nivel) <= tiempo) tuPuesto++;
        g.setFont(Ui.FUENTE_HUD);
        Ui.textoCentrado(g, "TOP 5 · NIVEL " + nivel, 320, y, Ui.AMARILLO);
        final int cPos = 196, cNombre = 222, cTiempo = 412;
        boolean parpadeo = (frame / 20) % 2 == 0;
        for (int fila = 1, i = 0; fila <= 5; fila++) {
            int fy = y + 6 + fila * 14;
            String nombre, t;
            Color color;
            if (fila == tuPuesto) {
                nombre = nombreJugador + "  (tú)";
                t = Puntuacion.formatearTiempo(tiempo);
                color = parpadeo ? Ui.VERDE : Ui.VERDE.brighter();
            } else if (i < mejores.size()) {
                Puntuacion p = mejores.get(i++);
                nombre = p.nombre();
                t = Puntuacion.formatearTiempo(p.framesNivel(nivel));
                color = colorPuesto(fila, Ui.TEXTO);
            } else {
                nombre = "---";
                t = "";
                color = APAGADO;
            }
            g.setFont(Ui.FUENTE);
            Ui.texto(g, fila + ".", cPos, fy, color);
            Ui.texto(g, nombre, cNombre, fy, color);
            g.setFont(Ui.FUENTE_MONO);
            Ui.texto(g, t, cTiempo, fy, color);
        }
        if (tuPuesto > 5) {
            g.setFont(Ui.FUENTE);
            Ui.textoCentrado(g, "Tú (" + nombreJugador + "): puesto " + tuPuesto + "º con "
                    + Puntuacion.formatearTiempo(tiempo), 320, y + 6 + 6 * 14 + 2, Ui.AMARILLO);
        }
    }

    // ---- 2. final de la partida: se guarda sola con el nombre del principio ----

    // t[1..3] = tiempo de cada nivel
    public Puntuacion guardarDirecto(String nombreJugador, int frames, int muertes, int compilaciones, int[] t) {
        resaltada = new Puntuacion(nombreJugador, frames, muertes, compilaciones, t[1], t[2], t[3]);
        puesto = ranking.registrar(resaltada);
        pestana = 0;
        reiniciarFoco();
        Sonido.checkpoint();
        return resaltada;
    }

    // true cuando quiere ver el ranking (ENTER o el botón)
    public boolean actualizarFinal(Input in) {
        in.tomarEscrito();
        return bVerRanking.clicado(in) || in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_SPACE);
    }

    private static boolean esValido(char c) {
        return Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_' || c == '.';
    }

    public void dibujarFinal(Graphics2D g, Input in, int frames, int muertes, int compilaciones,
                              int[] t, int frame) {
        dibujarFondo(g, frame);
        Ui.panel(g, 130, 30, 380, 300, Ui.PANEL, Ui.BORDE);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "¡DEMO COMPLETADA!", 320, 58, Ui.BORDE);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Coco ha llegado a la Mezquita-Catedral", 320, 76, Ui.TEXTO);

        BufferedImage coco = (frame / 12) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        int bote = (frame / 12) % 2 == 0 ? 0 : -8;
        g.drawImage(coco, 294, 86 + bote, 52, 52, null);

        // estadísticas (las mismas columnas que el ranking)
        Ui.datoCentrado(g, "Tiempo total: ", Puntuacion.formatearTiempo(frames), 320, 154, Ui.TEXTO);
        g.setFont(Ui.FUENTE_MONO);
        StringBuilder porNivel = new StringBuilder();
        for (int i = 1; i <= Level.TOTAL; i++) {
            if (i > 1) porNivel.append("  ·  ");
            porNivel.append("N").append(i).append(": ").append(t[i] > 0 ? Puntuacion.formatearTiempo(t[i]) : "--");
        }
        Ui.textoCentrado(g, porNivel.toString(), 320, 166, Ui.GRIS);
        g.setFont(Ui.FUENTE);
        Ui.datoCentrado(g, "Caídas: ", String.valueOf(muertes), 320, 180, Ui.TEXTO);
        Ui.datoCentrado(g, "Cambios de código compilados: ", String.valueOf(compilaciones), 320, 193, AZUL);

        // puesto (la partida ya está guardada) y récords de nivel
        int pos = puesto;
        if (pos <= Ranking.TOP)
            Ui.textoCentrado(g, "¡Entras en el TOP 10 en el puesto " + pos + "º!", 320, 209, colorPuesto(pos, Ui.VERDE));
        else
            Ui.textoCentrado(g, "Has quedado en el puesto " + pos + "º", 320, 209, Ui.AMARILLO);
        g.setFont(Ui.FUENTE_PEQ);
        // niveles en los que esta partida es el récord: allí será el fantasma a batir
        StringBuilder records = new StringBuilder();
        for (int i = 1; i <= Level.TOTAL; i++)
            if (resaltada != null && t[i] > 0 && ranking.mejorNivel(i) == resaltada)
                records.append(records.length() == 0 ? "" : ", ").append(i);
        if (records.length() > 0) {
            boolean varios = records.indexOf(",") >= 0;
            Ui.textoCentrado(g, "¡Récord en " + (varios ? "los niveles " : "el nivel ") + records
                    + "! Serás su fantasma a batir", 320, 222, (frame / 20) % 2 == 0 ? ORO : AZUL);
        }

        // guardada ya con el nombre del principio
        g.setFont(Ui.FUENTE);
        if (resaltada != null) {
            Ui.textoCentrado(g, "Guardado en el ranking como " + resaltada.nombre(), 320, 246, Ui.VERDE);
        }
        if (!ranking.isGuardadoOk()) {
            Ui.textoCentrado(g, "¡Error! No se pudo guardar el fichero ranking.txt", 320, 262, Ui.ROJO);
        }
        bVerRanking.dibujar(g, true);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "ENTER: ver el ranking", 320, 318, Ui.GRIS);
    }

    // ---- 2. tabla TOP 10 ----

    // true cuando quiere volver al menú
    public boolean actualizarTabla(Input in) {
        if (iniciarFoco) {
            navegacion.abrir(in);
            iniciarFoco = false;
        }
        if (in.pulsada(KeyEvent.VK_ESCAPE)) return true;

        boolean izquierda = in.pulsada(KeyEvent.VK_LEFT) || in.pulsada(KeyEvent.VK_A);
        boolean derecha = in.pulsada(KeyEvent.VK_RIGHT) || in.pulsada(KeyEvent.VK_D);
        boolean arriba = in.pulsada(KeyEvent.VK_UP), abajo = in.pulsada(KeyEvent.VK_DOWN);
        boolean tab = in.pulsada(KeyEvent.VK_TAB);
        if (arriba != abajo) {
            navegacion.seleccionar(abajo ? bPestanas.length : pestana);
        } else if (tab || (navegacion.getSeleccion() < bPestanas.length && izquierda != derecha)) {
            int origen = navegacion.getSeleccion() < bPestanas.length ? navegacion.getSeleccion() : pestana;
            int siguiente = Math.floorMod(origen + (tab || derecha ? 1 : -1), bPestanas.length);
            navegacion.seleccionar(siguiente);
            cambiarPestana(siguiente);
        }
        navegacion.actualizarRaton(in, izquierda || derecha || arriba || abajo || tab);
        int activado = navegacion.activado(in, false);
        if (activado == bPestanas.length) return true;
        if (activado >= 0) cambiarPestana(activado);
        return false;
    }

    private void reiniciarFoco() {
        navegacion.seleccionar(pestana);
        iniciarFoco = true;
    }

    private void cambiarPestana(int p) {
        if (p != pestana) Sonido.clic();
        pestana = p;
    }

    public void dibujarTabla(Graphics2D g, Input in, int frame) {
        dibujarFondo(g, frame);
        Ui.panel(g, 80, 18, 480, 324, Ui.PANEL, Ui.BORDE);

        // título con un Coco a cada lado
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "RANKING · TOP 10", 320, 46, Ui.BORDE);
        BufferedImage coco = (frame / 20) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        g.drawImage(coco, 150, 22, 30, 30, null);
        g.drawImage(Sprites.volteada(coco), 460, 22, 30, 30, null);
        g.setFont(Ui.FUENTE_PEQ);
        String sub = pestana == 0 ? "Menos tiempo gana · empate: menos caídas y compilaciones"
                : "Mejores tiempos del nivel " + pestana + " · el nº1 corre como FANTASMA";
        Ui.textoCentrado(g, sub, 320, 61, Ui.GRIS);

        // pestañas
        for (int i = 0; i < bPestanas.length; i++) {
            bPestanas[i].color = i == pestana ? Andalusi.BOTON_ALBERO : new Color(0x3A3A50);
        }
        navegacion.dibujar(g);

        if (pestana == 0) dibujarGeneral(g, frame);
        else dibujarNivel(g, frame, pestana);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.texto(g, "←/→ pestaña", 476, 336, APAGADO);
    }

    private void dibujarGeneral(Graphics2D g, int frame) {
        final int cPos = 100, cNombre = 136, cTiempo = 290, cCaidas = 375, cComp = 480;
        int y = cabecera(g, new String[] { "NOMBRE", "TIEMPO", "CAÍDAS", "COMPILACIONES" },
                new int[] { cNombre, cTiempo, cCaidas, cComp }, cPos);

        List<Puntuacion> mejores = ranking.mejores();
        for (int i = 0; i < Ranking.TOP; i++) {
            int fy = y + 18 + i * 17;
            Puntuacion p = i < mejores.size() ? mejores.get(i) : null;
            boolean esNuevo = fila(g, i, fy, p, frame, cPos, cNombre);
            if (p == null) continue;
            Color color = esNuevo ? Ui.VERDE : colorPuesto(i + 1, Ui.TEXTO);
            g.setFont(Ui.FUENTE_MONO);
            Ui.textoCentrado(g, p.tiempo(), cTiempo, fy, color);
            Ui.textoCentrado(g, String.valueOf(p.muertes()), cCaidas, fy, esNuevo ? Ui.VERDE : Ui.TEXTO);
            Ui.textoCentrado(g, String.valueOf(p.compilaciones()), cComp, fy, esNuevo ? Ui.VERDE : AZUL);
        }

        g.setFont(Ui.FUENTE);
        // mensaje de abajo
        int my = 290;
        if (!ranking.isGuardadoOk()) {
            Ui.textoCentrado(g, "¡Error! No se pudo guardar el fichero ranking.txt", 320, my, Ui.ROJO);
        } else if (resaltada != null && puesto <= Ranking.TOP) {
            Ui.textoCentrado(g, "¡Enhorabuena! Has quedado en el puesto " + puesto + "º", 320, my, Ui.VERDE);
        } else if (resaltada != null) {
            Ui.textoCentrado(g, "Has quedado en el puesto " + puesto + "º · ¡a por el TOP 10!", 320, my, Ui.AMARILLO);
        } else if (mejores.isEmpty()) {
            Ui.textoCentrado(g, "Aún no hay récords. ¡Sé el primero!", 320, my, Ui.AMARILLO);
        }
    }

    private void dibujarNivel(Graphics2D g, int frame, int nivel) {
        final int cPos = 100, cNombre = 136, cTiempo = 350, cTotal = 460;
        int y = cabecera(g, new String[] { "NOMBRE", "TIEMPO NIVEL", "TIEMPO TOTAL" },
                new int[] { cNombre, cTiempo, cTotal }, cPos);

        List<Puntuacion> mejores = ranking.mejoresNivel(nivel);
        for (int i = 0; i < Ranking.TOP; i++) {
            int fy = y + 18 + i * 17;
            Puntuacion p = i < mejores.size() ? mejores.get(i) : null;
            boolean esNuevo = fila(g, i, fy, p, frame, cPos, cNombre);
            if (p == null) continue;
            Color color = esNuevo ? Ui.VERDE : colorPuesto(i + 1, Ui.TEXTO);
            g.setFont(Ui.FUENTE_MONO);
            Ui.textoCentrado(g, Puntuacion.formatearTiempo(p.framesNivel(nivel)), cTiempo, fy, color);
            Ui.textoCentrado(g, p.tiempo(), cTotal, fy, esNuevo ? Ui.VERDE : Ui.GRIS);
            if (i == 0) {
                g.setFont(Ui.FUENTE_PEQ);
                Ui.texto(g, "FANTASMA", cNombre + 112, fy, AZUL);
                g.setFont(Ui.FUENTE);
            }
        }

        g.setFont(Ui.FUENTE);
        int my = 290;
        int idx = resaltada == null ? -1 : mejores.indexOf(resaltada);
        if (idx >= 0) {
            Ui.textoCentrado(g, "Has quedado " + (idx + 1) + "º en el nivel " + nivel, 320, my, Ui.VERDE);
        } else if (mejores.isEmpty()) {
            Ui.textoCentrado(g, "Aún nadie ha completado este nivel.", 320, my, Ui.AMARILLO);
        }
    }

    // cabecera de la tabla; devuelve su y
    private int cabecera(Graphics2D g, String[] titulos, int[] xs, int cPos) {
        g.setFont(Ui.FUENTE_HUD);
        int y = 100;
        Ui.texto(g, "#", cPos, y, Ui.AMARILLO);
        Ui.texto(g, titulos[0], xs[0], y, Ui.AMARILLO);
        for (int i = 1; i < titulos.length; i++) Ui.textoCentrado(g, titulos[i], xs[i], y, Ui.AMARILLO);
        g.setColor(Ui.BORDE.darker());
        g.fillRect(96, y + 5, 448, 1);
        return y;
    }

    // fondo, puesto, medalla y nombre de una fila. true si es la partida recién guardada
    private boolean fila(Graphics2D g, int i, int fy, Puntuacion p, int frame, int cPos, int cNombre) {
        int puestoFila = i + 1;
        g.setFont(Ui.FUENTE);
        if (i % 2 == 1) {   // filas alternas, como en un IDE
            g.setColor(Ui.PANEL_2);
            g.fillRect(96, fy - 12, 448, 17);
        }
        if (p == null) {
            Ui.texto(g, puestoFila + ".", cPos, fy, APAGADO);
            Ui.texto(g, "---", cNombre, fy, APAGADO);
            return false;
        }
        boolean esNuevo = p == resaltada;
        if (esNuevo) {
            boolean brillo = (frame / 15) % 2 == 0;
            g.setColor(brillo ? new Color(0x2E5A3A) : new Color(0x244A30));
            g.fillRect(96, fy - 12, 448, 17);
            g.setColor(Ui.VERDE);
            g.drawRect(96, fy - 12, 447, 16);
        }
        if (puestoFila <= 3) dibujarMedalla(g, cPos + 14, fy - 9, colorPuesto(puestoFila, Ui.TEXTO));
        g.setFont(Ui.FUENTE_MONO);
        Ui.texto(g, puestoFila + ".", cPos, fy, esNuevo ? Ui.VERDE : colorPuesto(puestoFila, Ui.TEXTO));
        g.setFont(Ui.FUENTE);
        Ui.texto(g, p.nombre(), cNombre, fy, esNuevo ? Ui.VERDE : Ui.TEXTO);
        return esNuevo;
    }

    // ---- utilidades ----

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

    // medalla de 8x8
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
