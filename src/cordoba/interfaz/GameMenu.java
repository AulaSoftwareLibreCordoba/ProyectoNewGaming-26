package cordoba.interfaz;

import cordoba.graficos.GameRenderer;
import cordoba.graficos.Sprites;
import cordoba.niveles.Level;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Pantallas de menú: inicio, pausa, nivel completado y final de la demo.
 * Cada método actualizarX() devuelve la acción elegida por el jugador.
 */
public class GameMenu {

    public enum Accion { NINGUNA, JUGAR, SALIR, CONTINUAR, REINICIAR, MENU, NIVEL2 }

    private final GameRenderer renderer;

    // Pantalla de inicio
    private final Boton bJugar = new Boton(250, 236, 140, 24, "JUGAR", new Color(0x2E8B57));
    private final Boton bSalir = new Boton(250, 268, 140, 24, "SALIR", new Color(0xB0413E));

    // Pausa
    private final Boton bContinuar = new Boton(240, 150, 160, 22, "CONTINUAR", new Color(0x2E8B57));
    private final Boton bReiniciar = new Boton(240, 180, 160, 22, "REINICIAR NIVEL", new Color(0x5A6070));
    private final Boton bMenuPausa = new Boton(240, 210, 160, 22, "MENÚ PRINCIPAL", new Color(0xB0413E));

    // Victoria
    private final Boton bSiguiente = new Boton(180, 262, 130, 24, "CONTINUAR", new Color(0x2E8B57));
    private final Boton bMenuVict  = new Boton(330, 262, 130, 24, "MENÚ", new Color(0x5A6070));

    // Final
    private final Boton bMenuFin  = new Boton(180, 270, 130, 24, "MENÚ", new Color(0x2E8B57));
    private final Boton bSalirFin = new Boton(330, 270, 130, 24, "SALIR", new Color(0xB0413E));

    public GameMenu(GameRenderer renderer) {
        this.renderer = renderer;
    }

    // =====================================================================
    //  PANTALLA DE INICIO
    // =====================================================================

    public Accion actualizarInicio(Input in) {
        if (bJugar.clicado(in) || in.pulsada(KeyEvent.VK_ENTER) || in.pulsada(KeyEvent.VK_SPACE)) return Accion.JUGAR;
        if (bSalir.clicado(in)) return Accion.SALIR;
        if (in.pulsada(KeyEvent.VK_2)) return Accion.NIVEL2;   // atajo para profesores
        return Accion.NINGUNA;
    }

    public void dibujarInicio(Graphics2D g, Input in, int frame) {
        // Fondo del río desplazándose lentamente
        renderer.dibujarFondo(g, Level.Tema.RIO, frame / 2, 0, 368);
        // suelo decorativo
        for (int x = 0; x < 640; x += 16) {
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16) % 2, true, false), x, 312, null);
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16 + 1) % 2, false, false), x, 328, null);
            g.drawImage(Sprites.tile(Level.Tema.RIO, '#', (x / 16) % 2, false, false), x, 344, null);
        }
        g.setColor(new Color(0, 0, 0, 60));
        g.fillRect(0, 0, 640, 360);

        // Título
        g.setFont(Ui.FUENTE_TITULO);
        int dy = (int) (Math.sin(frame / 25.0) * 3);
        String t1 = "CÓRDOBA:", t2 = "CÓDIGO SALVAJE";
        FontMetrics fm = g.getFontMetrics();
        Ui.textoContorno(g, t1, 320 - fm.stringWidth(t1) / 2, 70 + dy, new Color(0xF2E38A), new Color(0x3A1E0E));
        Ui.textoContorno(g, t2, 320 - fm.stringWidth(t2) / 2, 104 + dy, new Color(0x7BE07B), new Color(0x0E2A14));
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Un plataformas donde programas las leyes de la física", 320, 128, Ui.TEXTO);

        // Coco grande (escala x4) caminando sobre el sitio
        BufferedImage coco = (frame / 40) % 4 == 3 ? Sprites.COCO_VICTORIA1
                : Sprites.COCO_ANDAR[(frame / 8) % Sprites.COCO_ANDAR.length];
        int bote = (frame / 8) % 2 == 0 ? 0 : -2;
        g.drawImage(coco, 120, 232 + bote, 80, 80, null);

        // Fragmento de código decorativo
        Ui.panel(g, 430, 150, 180, 66, Ui.PANEL, new Color(0x5C5C80));
        g.setFont(Ui.FUENTE_PEQ);
        Ui.texto(g, "// ¡Cámbiame!", 438, 166, new Color(0x6A9955));
        Ui.texto(g, "float gravedad = 9.8f;", 438, 180, new Color(0x82AAFF));
        Ui.texto(g, "float velocidad = 5.0f;", 438, 193, new Color(0x82AAFF));
        Ui.texto(g, "float fuerzaSalto = 12.0f;", 438, 206, new Color(0x82AAFF));
        if ((frame / 30) % 2 == 0) {
            g.setColor(Ui.TEXTO);
            g.fillRect(596, 198, 2, 10);
        }

        bJugar.dibujar(g, in);
        bSalir.dibujar(g, in);

        g.setFont(Ui.FUENTE_PEQ);
        Ui.textoCentrado(g, "A/D o flechas: mover  ·  ESPACIO: saltar  ·  E: consola de código  ·  ESC: pausa", 320, 334, Ui.TEXTO);
        Ui.textoCentrado(g, "Demo para 2.º DAM · Java + Swing", 320, 352, new Color(0xF2E38A));
    }

    // =====================================================================
    //  PAUSA
    // =====================================================================

    public Accion actualizarPausa(Input in) {
        if (bContinuar.clicado(in) || in.pulsada(KeyEvent.VK_ESCAPE)) return Accion.CONTINUAR;
        if (bReiniciar.clicado(in)) return Accion.REINICIAR;
        if (bMenuPausa.clicado(in)) return Accion.MENU;
        return Accion.NINGUNA;
    }

    public void dibujarPausa(Graphics2D g, Input in) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, 640, 360);
        Ui.panel(g, 220, 100, 200, 148, Ui.PANEL, Ui.BORDE);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "PAUSA", 320, 132, Ui.BORDE);
        bContinuar.dibujar(g, in);
        bReiniciar.dibujar(g, in);
        bMenuPausa.dibujar(g, in);
    }

    // =====================================================================
    //  NIVEL COMPLETADO
    // =====================================================================

    public Accion actualizarVictoria(Input in) {
        if (bSiguiente.clicado(in) || in.pulsada(KeyEvent.VK_ENTER)) return Accion.CONTINUAR;
        if (bMenuVict.clicado(in) || in.pulsada(KeyEvent.VK_ESCAPE)) return Accion.MENU;
        return Accion.NINGUNA;
    }

    public void dibujarVictoria(Graphics2D g, Input in, Level nivel, int frames, int muertes,
                                int compilaciones, int frame) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, 640, 360);
        Ui.panel(g, 150, 60, 340, 240, Ui.PANEL, Ui.VERDE);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "¡NIVEL COMPLETADO!", 320, 94, Ui.VERDE);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Nivel " + nivel.getNumero() + ": " + nivel.getNombre(), 320, 114, Ui.TEXTO);

        BufferedImage coco = (frame / 15) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        int bote = (frame / 15) % 2 == 0 ? 0 : -6;
        g.drawImage(coco, 290, 124 + bote, 60, 60, null);

        dibujarEstadisticas(g, frames, muertes, compilaciones, 204);
        Ui.textoCentrado(g, "Siguiente: Nivel 2 — El desafío de la Mezquita", 320, 252, Ui.AMARILLO);
        bSiguiente.dibujar(g, in);
        bMenuVict.dibujar(g, in);
    }

    // =====================================================================
    //  FINAL DE LA DEMO
    // =====================================================================

    public Accion actualizarFinal(Input in) {
        if (bMenuFin.clicado(in) || in.pulsada(KeyEvent.VK_ENTER)) return Accion.MENU;
        if (bSalirFin.clicado(in)) return Accion.SALIR;
        return Accion.NINGUNA;
    }

    public void dibujarFinal(Graphics2D g, Input in, int frames, int muertes, int compilaciones, int frame) {
        renderer.dibujarFondo(g, Level.Tema.MEZQUITA, frame / 3, 0, 544);
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, 640, 360);
        Ui.panel(g, 130, 40, 380, 268, Ui.PANEL, Ui.BORDE);
        g.setFont(Ui.FUENTE_GRANDE);
        Ui.textoCentrado(g, "¡DEMO COMPLETADA!", 320, 74, Ui.BORDE);
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, "Coco ha llegado a la Mezquita-Catedral", 320, 94, Ui.TEXTO);
        Ui.textoCentrado(g, "reprogramando las leyes de la física.", 320, 107, Ui.TEXTO);

        BufferedImage coco = (frame / 12) % 2 == 0 ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        int bote = (frame / 12) % 2 == 0 ? 0 : -8;
        g.drawImage(coco, 290, 118 + bote, 60, 60, null);
        dibujarEstadisticas(g, frames, muertes, compilaciones, 200);
        Ui.textoCentrado(g, "¡Ahora modifica el código y crea más niveles!", 320, 256, Ui.AMARILLO);
        bMenuFin.dibujar(g, in);
        bSalirFin.dibujar(g, in);
    }

    private void dibujarEstadisticas(Graphics2D g, int frames, int muertes, int compilaciones, int y) {
        int seg = frames / 60;
        g.setFont(Ui.FUENTE);
        Ui.textoCentrado(g, String.format("Tiempo: %d:%02d", seg / 60, seg % 60), 320, y, Ui.TEXTO);
        Ui.textoCentrado(g, "Caídas: " + muertes, 320, y + 14, Ui.TEXTO);
        Ui.textoCentrado(g, "Cambios de código compilados: " + compilaciones, 320, y + 28, new Color(0x82AAFF));
    }
}
