package cordoba.interfaz;

import cordoba.audio.Sonido;
import cordoba.fisica.PhysicsConfig;
import cordoba.fisica.VariableFisica;
import cordoba.niveles.Level;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * CONSOLA DE CÓDIGO integrada en el juego.
 *
 * Simula un pequeño IDE que muestra PhysicsConfig.java. El jugador cambia los
 * valores con los botones [-] y [+], con A/D o ESCRIBIENDO el número, y pulsa
 * APLICAR CAMBIOS para "compilar". Solo al aplicar se modifican las físicas.
 *
 * ¡El juego NO se pausa! Mientras la consola está abierta Coco se sigue
 * moviendo (con las flechas y ESPACIO) y el cronómetro sigue corriendo:
 * como programar "en caliente" con el programa en marcha.
 *
 * Controles: ratón · W/S para elegir variable · A/D para cambiar el valor ·
 *            números para escribirlo · ENTER o Ctrl+S para aplicar · E o ESC para cerrar.
 */
public class CodeEditor {

    /** Lo que ha pasado en este fotograma (Game reacciona a ello). */
    public enum Resultado { NADA, APLICADO, CERRAR }

    // Posición del panel (resolución interna 640x360). Va ARRIBA para que se vea a Coco.
    private static final int PX = 60, PY = 4, PW = 520;
    private static final int LINEA = 13;
    private static final int Y_CODIGO = PY + 34;
    private static final int MAX_ESCRITURA = 5;
    private int ph;   // alto: depende de cuántas variables haya

    // Colores de "resaltado de sintaxis"
    private static final Color C_COMENTARIO = new Color(0x6A9955);
    private static final Color C_CLAVE      = new Color(0xC792EA);
    private static final Color C_NOMBRE     = new Color(0x82AAFF);
    private static final Color C_NUMERO     = new Color(0xF78C6C);
    private static final Color C_SIMBOLO    = new Color(0xD4D4D4);
    private static final Color C_BLOQUEADO  = new Color(0x6C6C80);
    private static final Color FONDO        = new Color(0x1E, 0x1E, 0x2E, 232);   // un poco transparente

    private PhysicsConfig fisica;
    private Level nivel;
    private int[] borrador;       // valores en edición (todavía sin aplicar)
    private int seleccion;
    private String escritura;     // número que se está tecleando (null = no se está escribiendo)

    private Boton botonAplicar, botonDefecto, botonCerrar;
    private Boton[] botonesMenos, botonesMas;

    // Mensajes de la "salida del compilador"
    private String salida1 = "", salida2 = "";
    private Color colorSalida = Ui.GRIS;
    private int destello;          // brillo verde tras compilar
    private String ultimoResumen = "";

    // Repetición al mantener pulsado
    private int framesMantenido;
    private int[] cambioPorFrame;  // para resaltar el valor recién cambiado

    /** Abre la consola copiando los valores actuales al borrador. */
    public void abrir(PhysicsConfig fisica, Level nivel) {
        this.fisica = fisica;
        this.nivel = nivel;
        int n = fisica.getCantidad();
        borrador = new int[n];
        cambioPorFrame = new int[n];
        botonesMenos = new Boton[n];
        botonesMas = new Boton[n];
        for (int i = 0; i < n; i++) {
            borrador[i] = fisica.getVariable(i).getDecimas();
            int y = yLineaCodigo(i) - 10;
            botonesMenos[i] = new Boton(PX + PW - 84, y, 30, 13, "-", new Color(0x8C3B3B));
            botonesMas[i]   = new Boton(PX + PW - 48, y, 30, 13, "+", new Color(0x2E7D4F));
        }
        // Alto del panel: código + salida del compilador + botones
        int finCodigo = Y_CODIGO + (totalLineas() - 1) * LINEA;
        ph = finCodigo - PY + 64;
        botonAplicar = new Boton(PX + 14,  PY + ph - 30, 160, 22, "APLICAR [Ctrl+S]", new Color(0x2E8B57));
        botonDefecto = new Boton(PX + 184, PY + ph - 30, 110, 22, "RESTAURAR", new Color(0x5A6070));
        botonCerrar  = new Boton(PX + PW - 124, PY + ph - 30, 110, 22, "CERRAR [E]", new Color(0xB0413E));

        if (fisica.getVariable(seleccion).isBloqueada()) seleccion = 0;
        escritura = null;
        salida1 = "> W/S elige variable · A/D o escribe un número · ENTER o Ctrl+S compila · E cierra";
        salida2 = "  ¡El juego sigue! Mueve a Coco con las flechas y salta con ESPACIO.";
        colorSalida = Ui.GRIS;
        destello = 0;
        nivel.alAbrirEditor();
    }

    /** Parte inferior del panel (para colocar cosas debajo). */
    public int getAbajo() {
        return PY + ph;
    }

    private int totalLineas() {
        // "public class ... {", 2 líneas por variable (comentario + declaración) y "}"
        return 2 + borrador.length * 2;
    }

    /** Línea (y base del texto) de la declaración de la variable i. */
    private int yLineaCodigo(int i) {
        return Y_CODIGO + (2 + i * 2) * LINEA;
    }

    public Resultado actualizar(Input in) {
        if (destello > 0) destello--;
        for (int i = 0; i < cambioPorFrame.length; i++) if (cambioPorFrame[i] > 0) cambioPorFrame[i]--;

        // ----- Escribir el valor con el teclado -----
        for (char c : in.tomarEscrito().toCharArray()) {
            if (Character.isDigit(c) || c == '.' || c == ',') teclear(c);
            else if (c == '\b' && escritura != null) {
                escritura = escritura.isEmpty() ? null : escritura.substring(0, escritura.length() - 1);
            }
        }

        // ----- Teclado (las FLECHAS quedan libres para mover a Coco) -----
        if (in.pulsada(KeyEvent.VK_ESCAPE) && escritura != null) {
            escritura = null;
            salida1 = "> Escritura cancelada.";
            salida2 = "";
            colorSalida = Ui.GRIS;
        } else if (in.pulsada(KeyEvent.VK_E) || in.pulsada(KeyEvent.VK_ESCAPE)) {
            confirmarEscritura();
            return Resultado.CERRAR;
        }
        if (in.pulsada(KeyEvent.VK_W)) {
            confirmarEscritura();
            seleccion = (seleccion + borrador.length - 1) % borrador.length;
            Sonido.clic();
        }
        boolean ctrl = in.abajo(KeyEvent.VK_CONTROL);
        boolean guardar = ctrl && in.pulsada(KeyEvent.VK_S);   // Ctrl+S = aplicar, como en un IDE
        if (in.pulsada(KeyEvent.VK_S) && !ctrl) {
            confirmarEscritura();
            seleccion = (seleccion + 1) % borrador.length;
            Sonido.clic();
        }
        boolean izq = in.abajo(KeyEvent.VK_A);
        boolean der = in.abajo(KeyEvent.VK_D);
        boolean izqP = in.pulsada(KeyEvent.VK_A);
        boolean derP = in.pulsada(KeyEvent.VK_D);

        // ----- Ratón -----
        int mx = in.getRatonX(), my = in.getRatonY();
        int mantenidoIndice = -1, mantenidoDir = 0;
        for (int i = 0; i < borrador.length; i++) {
            boolean bloqueada = fisica.getVariable(i).isBloqueada();
            botonesMenos[i].activo = !bloqueada;
            botonesMas[i].activo = !bloqueada;
            if (in.clic()) {
                if (botonesMenos[i].contiene(mx, my)) { confirmarEscritura(); seleccion = i; cambiar(i, -1); }
                else if (botonesMas[i].contiene(mx, my)) { confirmarEscritura(); seleccion = i; cambiar(i, +1); }
                else if (my > yLineaCodigo(i) - LINEA - 9 && my <= yLineaCodigo(i) + 3 && mx > PX && mx < PX + PW) {
                    if (i != seleccion) confirmarEscritura();
                    seleccion = i;
                }
            }
            if (in.botonPulsado()) {
                if (botonesMenos[i].contiene(mx, my)) { mantenidoIndice = i; mantenidoDir = -1; }
                if (botonesMas[i].contiene(mx, my))   { mantenidoIndice = i; mantenidoDir = +1; }
            }
        }

        // ----- Cambios con teclado (con repetición si se mantiene) -----
        if (izqP || derP) confirmarEscritura();
        if (izqP) cambiar(seleccion, -1);
        if (derP) cambiar(seleccion, +1);

        boolean algoMantenido = (izq || der) || mantenidoIndice >= 0;
        if (algoMantenido) {
            framesMantenido++;
            if (framesMantenido > 18 && framesMantenido % 4 == 0) {
                if (mantenidoIndice >= 0) cambiar(mantenidoIndice, mantenidoDir);
                else cambiar(seleccion, izq ? -1 : +1);
            }
        } else {
            framesMantenido = 0;
        }

        // ----- Botones inferiores -----
        if (botonAplicar.clicado(in) || in.pulsada(KeyEvent.VK_ENTER) || guardar) {
            boolean escrito = escritura != null;
            if (!confirmarEscritura() && escrito) return Resultado.NADA;   // número no válido
            return aplicar() ? Resultado.APLICADO : Resultado.NADA;
        }
        if (botonDefecto.clicado(in)) {
            escritura = null;
            for (int i = 0; i < borrador.length; i++) {
                if (!fisica.getVariable(i).isBloqueada()) borrador[i] = fisica.getVariable(i).getPorDefecto();
            }
            salida1 = "> Valores por defecto cargados en el editor.";
            salida2 = "  (Recuerda: no tienen efecto hasta que pulses APLICAR CAMBIOS)";
            colorSalida = Ui.AMARILLO;
            Sonido.clic();
        }
        if (botonCerrar.clicado(in)) {
            confirmarEscritura();
            return Resultado.CERRAR;
        }
        return Resultado.NADA;
    }

    /** Añade un carácter al número que se está escribiendo en la variable seleccionada. */
    private void teclear(char c) {
        VariableFisica v = fisica.getVariable(seleccion);
        if (v.isBloqueada()) {
            salida1 = "> error: '" + v.getNombre() + "' es final (BLOQUEADA en esta zona).";
            salida2 = "";
            colorSalida = Ui.ROJO;
            Sonido.error();
            return;
        }
        if (c == ',') c = '.';
        if (escritura == null) escritura = "";
        if (c == '.' && escritura.contains(".")) return;
        if (escritura.length() >= MAX_ESCRITURA) return;
        escritura += c;
        Sonido.clic();
        salida1 = "> Escribiendo " + v.getNombre() + " [" + VariableFisica.formatear(v.getMinimo()) + " - "
                + VariableFisica.formatear(v.getMaximo()) + "]...";
        salida2 = "  ENTER = compilar · RETROCESO = borrar · ESC = cancelar";
        colorSalida = Ui.AMARILLO;
    }

    /**
     * Pasa el número escrito al borrador (sin compilar todavía).
     * Si se sale del rango, se ajusta al límite. Devuelve false si no era un número válido.
     */
    private boolean confirmarEscritura() {
        if (escritura == null) return true;
        String txt = escritura;
        escritura = null;
        VariableFisica v = fisica.getVariable(seleccion);
        float valor;
        try {
            valor = Float.parseFloat(txt);
        } catch (NumberFormatException e) {
            salida1 = "> error: '" + txt + "' no es un número válido.";
            salida2 = "";
            colorSalida = Ui.ROJO;
            Sonido.error();
            return false;
        }
        int decimas = Math.round(valor * 10);
        int ajustado = v.limitar(decimas);
        borrador[seleccion] = ajustado;
        cambioPorFrame[seleccion] = 10;
        if (ajustado != decimas) {
            salida1 = "> aviso: " + v.getNombre() + " debe estar entre " + VariableFisica.formatear(v.getMinimo())
                    + " y " + VariableFisica.formatear(v.getMaximo()) + ": se ajusta a " + VariableFisica.formatear(ajustado) + ".";
            salida2 = "";
            colorSalida = Ui.AMARILLO;
            Sonido.error();
        } else if (hayCambiosPendientes()) {
            salida1 = "* Hay cambios sin aplicar. Pulsa APLICAR CAMBIOS (ENTER o Ctrl+S) para compilar.";
            salida2 = "";
            colorSalida = Ui.AMARILLO;
        }
        return true;
    }

    /** Sube (+1) o baja (-1) el valor en borrador de la variable i. */
    private void cambiar(int i, int dir) {
        VariableFisica v = fisica.getVariable(i);
        if (v.isBloqueada()) {
            salida1 = "> error: '" + v.getNombre() + "' es final (BLOQUEADA en esta zona).";
            salida2 = "";
            colorSalida = Ui.ROJO;
            Sonido.error();
            return;
        }
        int nuevo = dir > 0 ? v.siguienteArriba(borrador[i]) : v.siguienteAbajo(borrador[i]);
        if (nuevo == borrador[i]) {
            salida1 = "> aviso: " + v.getNombre() + " ya está en su límite ("
                    + VariableFisica.formatear(v.getMinimo()) + " - " + VariableFisica.formatear(v.getMaximo()) + ").";
            salida2 = "";
            colorSalida = Ui.AMARILLO;
            Sonido.error();
            return;
        }
        borrador[i] = nuevo;
        cambioPorFrame[i] = 10;
        Sonido.clic();
        if (hayCambiosPendientes()) {
            salida1 = "* Hay cambios sin aplicar. Pulsa APLICAR CAMBIOS (ENTER o Ctrl+S) para compilar.";
            salida2 = "";
            colorSalida = Ui.AMARILLO;
        }
    }

    /** "Compila": copia el borrador a PhysicsConfig. Devuelve true si había cambios. */
    private boolean aplicar() {
        StringBuilder cambios = new StringBuilder();
        int n = 0;
        for (int i = 0; i < borrador.length; i++) {
            VariableFisica v = fisica.getVariable(i);
            if (borrador[i] != v.getDecimas()) {
                if (cambios.length() > 0) cambios.append(", ");
                cambios.append(v.getNombre()).append(' ')
                       .append(VariableFisica.formatear(v.getDecimas())).append(" -> ")
                       .append(VariableFisica.formatear(borrador[i]));
                v.setDecimas(borrador[i]);
                n++;
            }
        }
        if (n == 0) {
            salida1 = "> javac PhysicsConfig.java";
            salida2 = "  Nada que compilar: no has cambiado ningún valor.";
            colorSalida = Ui.GRIS;
            Sonido.clic();
            return false;
        }
        salida1 = "> javac PhysicsConfig.java ... BUILD SUCCESSFUL (" + n + (n == 1 ? " cambio)" : " cambios)");
        salida2 = "  " + cambios;
        colorSalida = Ui.VERDE;
        destello = 45;
        ultimoResumen = cambios.toString();
        Sonido.aplicar();
        nivel.alAplicarCambios();
        return true;
    }

    public boolean hayCambiosPendientes() {
        for (int i = 0; i < borrador.length; i++)
            if (borrador[i] != fisica.getVariable(i).getDecimas()) return true;
        return false;
    }

    public String getUltimoResumen() {
        return ultimoResumen;
    }

    // =====================================================================
    //  Dibujo
    // =====================================================================

    /** @param tiempo cronómetro de la partida (sigue corriendo con la consola abierta) */
    public void dibujar(Graphics2D g, Input in, int frame, String tiempo) {
        Color borde = destello > 0 && (destello / 4) % 2 == 0 ? Ui.VERDE : new Color(0x5C5C80);
        Ui.panel(g, PX, PY, PW, ph, FONDO, borde);

        // Barra de título estilo IDE
        g.setColor(Ui.PANEL_2);
        g.fillRect(PX + 3, PY + 3, PW - 6, 16);
        g.setColor(new Color(0xFF5F56)); g.fillRect(PX + 8, PY + 8, 6, 6);
        g.setColor(new Color(0xFFBD2E)); g.fillRect(PX + 17, PY + 8, 6, 6);
        g.setColor(new Color(0x27C93F)); g.fillRect(PX + 26, PY + 8, 6, 6);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.texto(g, "PhysicsConfig.java  -  Nivel " + nivel.getNumero(), PX + 40, PY + 15, Ui.TEXTO);
        // Indicador "en vivo": el programa sigue ejecutándose
        String vivo = "EN EJECUCIÓN · " + tiempo;
        int vw = g.getFontMetrics().stringWidth(vivo);
        int vx = PX + PW - 12 - vw;
        if ((frame / 30) % 2 == 0) {
            g.setColor(Ui.ROJO);
            g.fillRect(vx - 9, PY + 8, 5, 5);
        }
        Ui.texto(g, vivo, vx, PY + 15, Ui.AMARILLO);

        // Números de línea
        g.setFont(Ui.FUENTE_CODIGO);
        int totalLineas = totalLineas();
        g.setColor(new Color(0x25, 0x25, 0x36, 232));
        g.fillRect(PX + 4, Y_CODIGO - 10, 24, totalLineas * LINEA + 4);
        for (int i = 0; i < totalLineas; i++) {
            Ui.texto(g, String.format("%2d", i + 1), PX + 7, Y_CODIGO + i * LINEA, new Color(0x5C5C78));
        }

        int x0 = PX + 34;
        segmentos(g, x0, Y_CODIGO, new String[] { "public class ", "PhysicsConfig", " {", "   // ¡Edita y compila en caliente!" },
                new Color[] { C_CLAVE, C_NOMBRE, C_SIMBOLO, C_COMENTARIO });

        for (int i = 0; i < borrador.length; i++) {
            VariableFisica v = fisica.getVariable(i);
            int yc = yLineaCodigo(i);
            boolean bloqueada = v.isBloqueada();
            boolean pendiente = borrador[i] != v.getDecimas();
            boolean escribiendo = escritura != null && i == seleccion;

            // Resaltado de la línea seleccionada
            if (i == seleccion) {
                g.setColor(new Color(0x2C3550));
                g.fillRect(PX + 29, yc - LINEA - 9, PW - 33, LINEA * 2 + 2);
                Ui.texto(g, ">", PX + 29, yc, Ui.AMARILLO);
            }

            // Comentario con la explicación y el rango permitido
            String rango = "[" + VariableFisica.formatear(v.getMinimo()) + " - " + VariableFisica.formatear(v.getMaximo()) + "]";
            segmentos(g, x0, yc - LINEA, new String[] { "    // " + v.getDescripcion() + " " + rango },
                    new Color[] { bloqueada ? C_BLOQUEADO : C_COMENTARIO });

            // Declaración de la variable
            String valor = VariableFisica.formatear(borrador[i]) + "f";
            Color colValor = pendiente ? Ui.AMARILLO : C_NUMERO;
            if (cambioPorFrame[i] > 0) colValor = Color.WHITE;
            String nota = pendiente ? "  // * sin aplicar" : "";
            if (escribiendo) {
                valor = escritura + ((frame / 15) % 2 == 0 ? "_" : " ");
                colValor = Color.WHITE;
                nota = "  // ENTER = compilar";
            }
            if (bloqueada) {
                segmentos(g, x0, yc,
                        new String[] { "    final float ", v.getNombre(), " = ", valor, ";", "  // BLOQUEADA" },
                        new Color[] { C_BLOQUEADO, C_BLOQUEADO, C_BLOQUEADO, C_BLOQUEADO, C_BLOQUEADO, Ui.ROJO });
                dibujarCandado(g, PX + PW - 64, yc - 10);
            } else {
                segmentos(g, x0, yc,
                        new String[] { "    float ", v.getNombre(), " = ", valor, ";", nota },
                        new Color[] { C_CLAVE, C_NOMBRE, C_SIMBOLO, colValor, C_SIMBOLO, Ui.AMARILLO });
                botonesMenos[i].dibujar(g, in);
                botonesMas[i].dibujar(g, in);
                dibujarBarra(g, v, borrador[i], PX + PW - 84, yc + 4, 66);
            }
        }
        int yFin = Y_CODIGO + (totalLineas - 1) * LINEA;
        segmentos(g, x0, yFin, new String[] { "}" }, new Color[] { C_SIMBOLO });

        // Salida del compilador
        int ys = PY + ph - 50;
        g.setColor(new Color(0x15151F));
        g.fillRect(PX + 6, ys - 10, PW - 12, 24);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.texto(g, recortar(g, salida1, PW - 20), PX + 10, ys, colorSalida);
        Ui.texto(g, recortar(g, salida2, PW - 20), PX + 10, ys + 11, colorSalida);

        botonAplicar.dibujar(g, in);
        botonDefecto.dibujar(g, in);
        botonCerrar.dibujar(g, in);
    }

    /** Dibuja una línea de código con trozos de distintos colores. */
    private void segmentos(Graphics2D g, int x, int y, String[] partes, Color[] colores) {
        g.setFont(Ui.FUENTE_CODIGO);
        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < partes.length; i++) {
            Ui.texto(g, partes[i], x, y, colores[i]);
            x += fm.stringWidth(partes[i]);
        }
    }

    /** Pequeña barra que muestra dónde está el valor dentro de su rango. */
    private void dibujarBarra(Graphics2D g, VariableFisica v, int valor, int x, int y, int w) {
        g.setColor(new Color(0x33334A));
        g.fillRect(x, y, w, 3);
        float t = (valor - v.getMinimo()) / (float) (v.getMaximo() - v.getMinimo());
        g.setColor(new Color(0x82AAFF));
        g.fillRect(x, y, Math.max(1, (int) (w * t)), 3);
        float tDef = (v.getPorDefecto() - v.getMinimo()) / (float) (v.getMaximo() - v.getMinimo());
        g.setColor(Color.WHITE);
        g.fillRect(x + (int) (w * tDef), y - 1, 1, 5);
    }

    private void dibujarCandado(Graphics2D g, int x, int y) {
        g.setColor(new Color(0xB0B0C0));
        g.drawRect(x + 2, y, 6, 6);
        g.setColor(new Color(0xE0B040));
        g.fillRect(x, y + 4, 11, 8);
        g.setColor(new Color(0x5A4410));
        g.fillRect(x + 5, y + 7, 1, 3);
    }

    private String recortar(Graphics2D g, String s, int w) {
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(s) <= w) return s;
        while (s.length() > 3 && fm.stringWidth(s + "...") > w) s = s.substring(0, s.length() - 1);
        return s + "...";
    }
}
