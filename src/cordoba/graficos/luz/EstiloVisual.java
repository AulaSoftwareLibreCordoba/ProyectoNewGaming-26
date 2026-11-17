package cordoba.graficos.luz;

import cordoba.niveles.Level;

/**
 * ★ PANEL DE CONTROL DEL ARTE ★
 *
 * Aquí están TODOS los valores de iluminación y profundidad de cada tema.
 * Para cambiar el aspecto del juego basta con tocar estos números: no hace
 * falta entender el resto del código.
 *
 *  - Colores en hexadecimal 0xRRGGBB.
 *  - Intensidades entre 0 (nada) y 1 (máximo).
 *  - Distancias en píxeles de la resolución interna (640x360).
 */
public final class EstiloVisual {

    // ---------------- PROFUNDIDAD ----------------

    /** Color de la "neblina" que aclara lo que está lejos (perspectiva atmosférica). */
    public int colorNiebla;
    /** Cuánta neblina lleva cada capa del fondo (0 = cielo, 1 = lejos, 2 = medio, 3 = cerca). */
    public float[] nieblaCapa = new float[4];

    /** Sombra que proyecta el terreno sobre el fondo. */
    public int colorSombra;
    public float alfaSombra;
    /** Hacia dónde cae la sombra (depende de dónde está el sol). */
    public int sombraDx, sombraDy;

    /** Oscurece el interior de los bloques grandes para que parezcan macizos. */
    public float volumenPaso;   // oscuridad que se suma por cada bloque de profundidad
    public float volumenMax;    // oscuridad máxima

    /** Sombra suave bajo macetas, farolas, carteles y Coco. */
    public float alfaSombraContacto;

    // ---------------- ILUMINACIÓN ----------------

    /** Penumbra general de la escena al aire libre (0 = pleno día). */
    public int colorAmbiente;
    public float oscuridadExterior;

    /** Penumbra cuando se baja al interior de la Mezquita (sala de columnas). */
    public int colorInterior;
    public float oscuridadInterior;
    /**
     * Sala de columnas del interior (ver SalaProfunda): niebla de cada plano, del
     * más lejano al más cercano. Más niebla = más oscuro = se ve más lejos.
     */
    public float[] nieblaSala = new float[3];
    public int colorNieblaSala;

    /** Sol del fondo: posición en pantalla, tamaño del resplandor y fuerza. */
    public int solX, solY, solRadio, colorSol;
    public float brilloSol;
    /** El sol del fondo baja con la cámara como su capa (true en el río). */
    public boolean solSigueCamara;

    /** Farolas encendidas (puntos de control). */
    public int colorFarola, radioFarola;
    public float brilloFarola;

    /** Puerta de la meta. */
    public int colorMeta, radioMeta;
    public float brilloMeta;

    /** Pequeña luz alrededor de Coco para que nunca se pierda en la oscuridad. */
    public int radioJugador;

    /** Lámparas colgantes de la sala de columnas. */
    public int colorLampara, radioLampara;
    public float brilloLampara;

    /** Oscurecimiento de las esquinas de la pantalla (viñeta). */
    public float vineta;

    // =====================================================================

    private static final EstiloVisual RIO = rio(), MEZQUITA = mezquita();

    private EstiloVisual() { }

    public static EstiloVisual de(Level.Tema tema) {
        return tema == Level.Tema.RIO ? RIO : MEZQUITA;
    }

    /** Nivel 1: mañana soleada en la ribera del Guadalquivir. */
    private static EstiloVisual rio() {
        EstiloVisual e = new EstiloVisual();
        e.colorNiebla = 0xD6ECF5;
        e.nieblaCapa = new float[] { 0f, 0.38f, 0.16f, 0f };

        e.colorSombra = 0x1A2A3A;
        e.alfaSombra = 0.32f;
        e.sombraDx = -5;
        e.sombraDy = 5;

        e.volumenPaso = 0.07f;
        e.volumenMax = 0.20f;
        e.alfaSombraContacto = 0.30f;

        e.colorAmbiente = 0x203050;
        e.oscuridadExterior = 0f;            // de día no hay penumbra
        e.colorInterior = 0x100808;
        e.oscuridadInterior = 0f;
        e.nieblaSala = new float[] { 0f, 0f, 0f };
        e.colorNieblaSala = 0x100808;

        e.solX = 502; e.solY = 70; e.solRadio = 120; e.colorSol = 0xFFF2B8;
        e.brilloSol = 0.30f;
        e.solSigueCamara = true;

        e.colorFarola = 0xFFD27A; e.radioFarola = 34; e.brilloFarola = 0.22f;
        e.colorMeta = 0xFFE8A0;   e.radioMeta = 56;   e.brilloMeta = 0.20f;
        e.radioJugador = 48;
        e.colorLampara = 0xFFC860; e.radioLampara = 0; e.brilloLampara = 0f;

        e.vineta = 0.22f;
        return e;
    }

    /** Nivel 2: atardecer en el casco histórico y penumbra dentro de la Mezquita. */
    private static EstiloVisual mezquita() {
        EstiloVisual e = new EstiloVisual();
        e.colorNiebla = 0xF7A783;
        e.nieblaCapa = new float[] { 0f, 0.32f, 0.14f, 0f };

        e.colorSombra = 0x2A1030;
        e.alfaSombra = 0.38f;
        e.sombraDx = -6;
        e.sombraDy = 4;

        e.volumenPaso = 0.08f;
        e.volumenMax = 0.24f;
        e.alfaSombraContacto = 0.34f;

        e.colorAmbiente = 0x3A1E50;
        e.oscuridadExterior = 0.16f;         // tono violeta del atardecer
        e.colorInterior = 0x120604;
        e.oscuridadInterior = 0.30f;
        e.nieblaSala = new float[] { 0.74f, 0.50f, 0.16f };
        e.colorNieblaSala = 0x1A0C08;

        e.solX = 422; e.solY = 232; e.solRadio = 150; e.colorSol = 0xFFB070;
        e.brilloSol = 0.30f;
        e.solSigueCamara = false;

        e.colorFarola = 0xFFC86A; e.radioFarola = 60; e.brilloFarola = 0.30f;
        e.colorMeta = 0xFFD890;   e.radioMeta = 70;   e.brilloMeta = 0.32f;
        e.radioJugador = 56;
        e.colorLampara = 0xFFC060; e.radioLampara = 44; e.brilloLampara = 0.35f;

        e.vineta = 0.30f;
        return e;
    }

    /**
     * 0 = fuera, 1 = dentro de la sala de columnas de la Mezquita.
     * (Mismo criterio con el que aparece el fondo de columnas.)
     */
    public static float factorInterior(Level.Tema tema, int camY) {
        if (tema != Level.Tema.MEZQUITA) return 0f;
        return Math.max(0f, Math.min(1f, (camY - 110) / 60f));
    }
}
