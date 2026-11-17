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

    private static final String[] PESTANAS = { "GENERAL", "NIVEL 1", "NIVEL 2" };

    private final GameRenderer renderer;
    private final Ranking ranking;

    private final StringBuilder nombre = new StringBuilder();
    private Puntuacion resaltada;   // la partida que se acaba de guardar (o null)
    private int puesto;             // su puesto en el general
    private int pestana;            // pestaña activa (el foco va en navegacion)

    private final Boton bGuardar = new Boton(255, 278, 130, 22, "GUARDAR", Andalusi.BOTON_VERDE).andalusi();
    private final Boton bMenu    = new Boton(255, 300, 130, 22, "MENÚ", Andalusi.BOTON_VERDE).andalusi();
    private final Boton[] bPestanas = new Boton[PESTANAS.length];
    private final GameMenu.NavegacionMenu navegacion;
    private boolean iniciarFoco = true;

    public PantallaRanking(GameRenderer renderer, Ranking ranking) {
        this.renderer = renderer;
        this.ranking = ranking;
        for (int i = 0; i < PESTANAS.length; i++) {
            bPestanas[i] = new Boton(170 + i * 102, 68, 96, 16, PESTANAS[i], new Color(0x5A6070)).andalusi();
        }
        Boton[] opciones = new Boton[bPestanas.length + 1];
        System.arraycopy(bPestanas, 0, opciones, 0, bPestanas.length);
        opciones[bPestanas.length] = bMenu;
        navegacion = new GameMenu.NavegacionMenu(opciones);
    }

    // deja la pantalla lista para el siguiente jugador
    public void empezarNombre() {
        nombre.setLength(0);
        resaltada = null;
        puesto = -1;
    }

    // desde el menú: sin resaltar a nadie
    public void verTabla() {
        resaltada = null;
        puesto = -1;
        pestana = 0;
        reiniciarFoco();
    }

    // ---- 1. escribir el nombre ----

    // devuelve la partida guardada, o null si aún no se ha guardado
    public Puntuacion actualizarNombre(Input in, int frames, int muertes, int compilaciones, int n1, int n2) {
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
            resaltada = new Puntuacion(limpio, frames, muertes, compilaciones, n1, n2);
            puesto = ranking.registrar(resaltada);
            pestana = 0;
            reiniciarFoco();
            Sonido.checkpoint();
            return resaltada;
        }
        return null;
    }

    private static boolean esValido(char c) {
        return Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_' || c == '.';
    }

    public void dibujarNombre(Graphics2D g, Input in, int frames, int muertes, int compilaciones,
                              int n1, int n2, int frame) {
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
        Ui.textoCentrado(g, "Nivel 1: " + (n1 > 0 ? Puntuacion.formatearTiempo(n1) : "--")
                + "   ·   Nivel 2: " + Puntuacion.formatearTiempo(n2), 320, 166, Ui.GRIS);
        g.setFont(Ui.FUENTE);
        Ui.datoCentrado(g, "Caídas: ", String.valueOf(muertes), 320, 180, Ui.TEXTO);
        Ui.datoCentrado(g, "Cambios de código compilados: ", String.valueOf(compilaciones), 320, 193, AZUL);

        // puesto provisional y récords de nivel
        int pos = ranking.posicionPara(frames, muertes, compilaciones);
        if (pos <= Ranking.TOP)
            Ui.textoCentrado(g, "¡Entras en el TOP 10 en el puesto " + pos + "º!", 320, 209, colorPuesto(pos, Ui.VERDE));
        else
            Ui.textoCentrado(g, "Puesto provisional: " + pos + "º", 320, 209, Ui.AMARILLO);
        g.setFont(Ui.FUENTE_PEQ);
        if (ranking.esRecordNivel(2, n2))
            Ui.textoCentrado(g, "¡RÉCORD DEL NIVEL 2! Serás el fantasma a batir", 320, 222,
                    (frame / 20) % 2 == 0 ? ORO : AZUL);
        else if (ranking.esRecordNivel(1, n1))
            Ui.textoCentrado(g, "¡NUEVO RÉCORD DEL NIVEL 1!", 320, 222, ORO);

        // caja de texto
        g.setFont(Ui.FUENTE);
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
            Ui.texto(g, "TU NOMBRE", tx, ty, APAGADO);
        } else {
            Ui.texto(g, nombre.toString(), tx, ty, Ui.VERDE);
        }
        if ((frame / 30) % 2 == 0) {   // cursor parpadeando
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
                : pestana == 1 ? "Mejores tiempos del nivel 1"
                : "Mejores tiempos del nivel 2 · el nº1 corre como FANTASMA";
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
            if (nivel == 2 && i == 0) {
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
