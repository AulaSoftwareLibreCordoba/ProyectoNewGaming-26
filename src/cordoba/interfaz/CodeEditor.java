package cordoba.interfaz;

import cordoba.audio.Sonido;
import cordoba.fisica.PhysicsConfig;
import cordoba.fisica.VariableFisica;
import cordoba.niveles.Level;
import cordoba.nucleo.Input;

import java.awt.*;
import java.awt.event.KeyEvent;

// Consola de código: edita PhysicsConfig y al cerrar con ENTER se aplica. El tiempo sigue corriendo.
public class CodeEditor {

    // qué ha pasado este fotograma (Game reacciona a esto). APLICADO = se cierra y había cambios
    public enum Resultado { NADA, APLICADO, CERRAR }

    // va arriba para que se vea a Coco
    private static final int PX = 60, PY = 4, PW = 520;
    private static final int LINEA = Ui.INTERLINEADO;
    private static final int Y_CODIGO = PY + 34;
    private static final int MAX_ESCRITURA = 5;
    private int ph;   // el alto depende de cuántas variables haya

    // colores de "resaltado de sintaxis"
    private static final Color C_COMENTARIO = new Color(0xA6C98A);
    private static final Color C_CLAVE      = new Color(0xC792EA);
    private static final Color C_NOMBRE     = new Color(0x82AAFF);
    private static final Color C_NUMERO     = new Color(0xF78C6C);
    private static final Color C_SIMBOLO    = new Color(0xD4D4D4);
    private static final Color C_BLOQUEADO  = new Color(0xB0B4C4);
    private static final Color FONDO        = new Color(0x1E, 0x1E, 0x2E, 232);

    private PhysicsConfig fisica;
    private Level nivel;
    private int[] borrador;       // valores editados pero sin aplicar todavía
    private int seleccion;
    private String escritura;     // número que estás tecleando (null = no estás escribiendo)

    private Boton botonDefecto, botonCerrar;
    private Boton[] botonesMenos, botonesMas;

    // la "salida del compilador"
    private String salida1 = "", salida2 = "";
    private Color colorSalida = Ui.GRIS;

    // repetir al mantener pulsado
    private int framesMantenido;
    private int[] cambioPorFrame;  // para resaltar el valor que acaba de cambiar

    // doble toque = modo rápido (pasos dobles)
    private static final int VENTANA_DOBLE = 15;   // fotogramas
    private int framesDesdeToque = 99, ultimaDir, ultimaVar = -1;
    private boolean rapido;

    // copia los valores actuales al borrador
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
        // alto = código + salida del compilador + botones
        int finCodigo = Y_CODIGO + (totalLineas() - 1) * LINEA;
        ph = finCodigo - PY + 76;
        botonDefecto = new Boton(PX + 14, PY + ph - 30, 140, 22, "RESTAURAR [R]",new Color(0x5A6070));
        botonCerrar  = new Boton(PX + PW - 164, PY + ph - 30, 150, 22, "CERRAR [ENTER]", new Color(0x2E8B57));

        if (fisica.getVariable(seleccion).isBloqueada()) seleccion = 0;
        escritura = null;
        rapido = false;
        framesDesdeToque = 99;
        ultimaVar = -1;
        salida1 = "> W/S o ↑/↓ elige · A/D o ←/→ cambia (o escribe el número)";
        salida2 = "  Doble toque = x2 · R restaura · ENTER guarda · ¡El tiempo corre!";
        colorSalida = Ui.GRIS;
    }

    private int totalLineas() {
        // "public class ... {", 2 líneas por variable y "}"
        return 2 + borrador.length * 2;
    }

    // línea donde va la declaración de la variable i
    private int yLineaCodigo(int i) {
        return Y_CODIGO + (2 + i * 2) * LINEA;
    }

    public Resultado actualizar(Input in) {
        for (int i = 0; i < cambioPorFrame.length; i++) if (cambioPorFrame[i] > 0) cambioPorFrame[i]--;
        if (framesDesdeToque < 99) framesDesdeToque++;

        // escribir el valor con el teclado
        for (char c : in.tomarEscrito().toCharArray()) {
            if (Character.isDigit(c) || c == '.' || c == ',') teclear(c);
            else if (c == '\b' && escritura != null) {
                escritura = escritura.isEmpty() ? null : escritura.substring(0, escritura.length() - 1);
            }
        }

        // teclado: WASD o flechas
        // ESC solo cancela lo que estés escribiendo (para cerrar, ENTER)
        if (in.pulsada(KeyEvent.VK_ESCAPE) && escritura != null) {
            escritura = null;
            salida1 = "> Escritura cancelada.";
            salida2 = "";
            colorSalida = Ui.GRIS;
        }
        if (in.pulsada(KeyEvent.VK_W) || in.pulsada(KeyEvent.VK_UP)) {
            confirmarEscritura();
            seleccion = (seleccion + borrador.length - 1) % borrador.length;
            Sonido.clic();
        }
        if (in.pulsada(KeyEvent.VK_S) || in.pulsada(KeyEvent.VK_DOWN)) {
            confirmarEscritura();
            seleccion = (seleccion + 1) % borrador.length;
            Sonido.clic();
        }
        boolean izq = in.abajo(KeyEvent.VK_A) || in.abajo(KeyEvent.VK_LEFT);
        boolean der = in.abajo(KeyEvent.VK_D) || in.abajo(KeyEvent.VK_RIGHT);
        boolean izqP = in.pulsada(KeyEvent.VK_A) || in.pulsada(KeyEvent.VK_LEFT);
        boolean derP = in.pulsada(KeyEvent.VK_D) || in.pulsada(KeyEvent.VK_RIGHT);

        // ratón
        int mx = in.getRatonX(), my = in.getRatonY();
        int mantenidoIndice = -1, mantenidoDir = 0;
        for (int i = 0; i < borrador.length; i++) {
            boolean bloqueada = fisica.getVariable(i).isBloqueada();
            botonesMenos[i].activo = !bloqueada;
            botonesMas[i].activo = !bloqueada;
            if (in.clic()) {
                if (botonesMenos[i].contiene(mx, my)) { confirmarEscritura(); seleccion = i; toque(i, -1); }
                else if (botonesMas[i].contiene(mx, my)) { confirmarEscritura(); seleccion = i; toque(i, +1); }
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

        // cambios con A/D o ←/→ (se repiten si mantienes)
        if (izqP || derP) confirmarEscritura();
        if (izqP) toque(seleccion, -1);
        if (derP) toque(seleccion, +1);

        boolean algoMantenido = (izq || der) || mantenidoIndice >= 0;
        if (algoMantenido) {
            framesMantenido++;
            if (framesMantenido > 18 && framesMantenido % 4 == 0) {
                if (mantenidoIndice >= 0) cambiar(mantenidoIndice, mantenidoDir);
                else cambiar(seleccion, izq ? -1 : +1);
            }
        } else {
            framesMantenido = 0;
            if (framesDesdeToque > VENTANA_DOBLE) rapido = false;
        }

        // ENTER o CERRAR: guarda solo y cierra
        if (botonCerrar.clicado(in) || in.pulsada(KeyEvent.VK_ENTER)) {
            if (!confirmarEscritura()) return Resultado.NADA;   // número no válido
            return aplicar() ? Resultado.APLICADO : Resultado.CERRAR;
        }
        if (botonDefecto.clicado(in) || in.pulsada(KeyEvent.VK_R)) {
            escritura = null;
            for (int i = 0; i < borrador.length; i++) {
                if (!fisica.getVariable(i).isBloqueada()) borrador[i] = fisica.getVariable(i).getPorDefecto();
            }
            salida1 = "> Valores por defecto cargados en el editor.";
            salida2 = "  Se guardan al cerrar con ENTER.";
            colorSalida = Ui.AMARILLO;
            Sonido.clic();
        }
        return Resultado.NADA;
    }

    // añade un carácter al número que estás escribiendo
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
        salida2 = "  ENTER = guardar y cerrar · RETROCESO = borrar · ESC = cancelar";
        colorSalida = Ui.AMARILLO;
    }

    // pasa lo escrito al borrador; false si no es un número
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
            salida1 = "* Cambios sin guardar: se guardan al cerrar con ENTER.";
            salida2 = "";
            colorSalida = Ui.AMARILLO;
        }
        return true;
    }

    // dos toques seguidos al mismo lado activan el modo rápido
    private void toque(int i, int dir) {
        rapido = dir == ultimaDir && i == ultimaVar && framesDesdeToque <= VENTANA_DOBLE;
        ultimaDir = dir;
        ultimaVar = i;
        framesDesdeToque = 0;
        cambiar(i, dir);
    }

    // +1 sube, -1 baja
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
        if (rapido && i == ultimaVar) nuevo = dir > 0 ? v.siguienteArriba(nuevo) : v.siguienteAbajo(nuevo);
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
            salida1 = "* Cambios sin guardar: se guardan al cerrar con ENTER.";
            salida2 = "";
            colorSalida = Ui.AMARILLO;
        }
    }

    // "compila": copia el borrador a PhysicsConfig. true si había cambios
    private boolean aplicar() {
        int n = 0;
        for (int i = 0; i < borrador.length; i++) {
            VariableFisica v = fisica.getVariable(i);
            if (borrador[i] != v.getDecimas()) {
                v.setDecimas(borrador[i]);
                n++;
            }
        }
        if (n > 0) Sonido.aplicar();
        return n > 0;
    }

    public boolean hayCambiosPendientes() {
        for (int i = 0; i < borrador.length; i++)
            if (borrador[i] != fisica.getVariable(i).getDecimas()) return true;
        return false;
    }

    // ---- dibujo ----

    // tiempo = cronómetro de la partida (sigue corriendo con la consola abierta)
    public void dibujar(Graphics2D g, Input in, int frame, String tiempo) {
        Color borde = new Color(0x5C5C80);
        Ui.panel(g, PX, PY, PW, ph, FONDO, borde);

        // barra de título estilo IDE
        g.setColor(Ui.PANEL_2);
        g.fillRect(PX + 3, PY + 3, PW - 6, 16);
        g.setColor(new Color(0xFF5F56)); g.fillRect(PX + 8, PY + 8, 6, 6);
        g.setColor(new Color(0xFFBD2E)); g.fillRect(PX + 17, PY + 8, 6, 6);
        g.setColor(new Color(0x27C93F)); g.fillRect(PX + 26, PY + 8, 6, 6);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.texto(g, "PhysicsConfig.java  -  Nivel " + nivel.getNumero(), PX + 40, PY + 15, Ui.TEXTO);
        // indicador "en vivo"
        int tx = PX + PW - 12 - Ui.ANCHO_TIEMPO;
        int vx = tx - 103;
        if ((frame / 30) % 2 == 0) {
            g.setColor(Ui.ROJO);
            g.fillRect(vx - 9, PY + 8, 5, 5);
        }
        g.setFont(Ui.FUENTE_HUD);
        Ui.texto(g, "EN EJECUCIÓN", vx, PY + 15, Ui.AMARILLO);
        Ui.tiempo(g, tiempo, tx, PY + 15, Ui.TEXTO);

        // números de línea
        g.setFont(Ui.FUENTE_MONO);
        int totalLineas = totalLineas();
        g.setColor(new Color(0x25, 0x25, 0x36, 232));
        g.fillRect(PX + 4, Y_CODIGO - 10, 24, totalLineas * LINEA + 4);
        for (int i = 0; i < totalLineas; i++) {
            Ui.texto(g, String.format("%2d", i + 1), PX + 7, Y_CODIGO + i * LINEA, Ui.GRIS);
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

            // línea seleccionada
            if (i == seleccion) {
                g.setColor(new Color(0x2C3550));
                g.fillRect(PX + 29, yc - LINEA - 9, PW - 33, LINEA * 2 + 2);
                Ui.texto(g, ">", PX + 29, yc, Ui.AMARILLO);
            }

            // comentario con la explicación y el rango
            String rango = "[" + VariableFisica.formatear(v.getMinimo()) + " - " + VariableFisica.formatear(v.getMaximo()) + "]";
            segmentos(g, x0, yc - LINEA, new String[] { "    // " + v.getDescripcion() + " " + rango },
                    new Color[] { bloqueada ? C_BLOQUEADO : C_COMENTARIO });

            // declaración de la variable
            String valor = VariableFisica.formatear(borrador[i]) + "f";
            Color colValor = pendiente ? Ui.AMARILLO : C_NUMERO;
            if (cambioPorFrame[i] > 0) colValor = Color.WHITE;
            String nota = pendiente ? "  // * sin guardar" : "";
            if (rapido && i == ultimaVar) nota = "  // >> x2 RÁPIDO";
            if (escribiendo) {
                valor = escritura + ((frame / 15) % 2 == 0 ? "_" : " ");
                colValor = Color.WHITE;
                nota = "  // ENTER = guardar";
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

        // salida del compilador
        int ys = PY + ph - 57;
        g.setColor(new Color(0x15151F));
        g.fillRect(PX + 6, ys - 12, PW - 12, 29);
        g.setFont(Ui.FUENTE_PEQ);
        Ui.texto(g, Ui.recortar(g, salida1, PW - 20), PX + 10, ys, colorSalida);
        Ui.texto(g, Ui.recortar(g, salida2, PW - 20), PX + 10, ys + 14, colorSalida);

        botonDefecto.dibujar(g, in);
        botonCerrar.dibujar(g, in);
    }

    // una línea de código con trozos de distintos colores
    private void segmentos(Graphics2D g, int x, int y, String[] partes, Color[] colores) {
        g.setFont(Ui.FUENTE_CODIGO);
        for (int i = 0; i < partes.length; i++) {
            g.setFont(!partes[i].isEmpty() && Character.isDigit(partes[i].charAt(0))
                    ? Ui.FUENTE_MONO : Ui.FUENTE_CODIGO);
            Ui.texto(g, partes[i], x, y, colores[i]);
            x += g.getFontMetrics().stringWidth(partes[i]);
        }
    }

    // barrita que enseña dónde está el valor dentro de su rango
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

}
