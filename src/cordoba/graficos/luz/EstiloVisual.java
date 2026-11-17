package cordoba.graficos.luz;

import cordoba.niveles.Level;

// Valores de luz y profundidad de cada tema. Colores 0xRRGGBB, intensidades de 0 a 1, distancias en px.
public final class EstiloVisual {

    // ---- profundidad ----

    // neblina que aclara lo lejano (perspectiva atmosférica)
    public int colorNiebla;
    // neblina de cada capa del fondo (0 = cielo, 1 = lejos, 2 = medio, 3 = cerca)
    public float[] nieblaCapa = new float[4];

    // sombra que echa el terreno sobre el fondo
    public int colorSombra;
    public float alfaSombra;
    // hacia dónde cae (depende de dónde esté el sol)
    public int sombraDx, sombraDy;

    // oscurece el interior de los bloques grandes para que parezcan macizos
    public float volumenPaso;   // lo que oscurece cada bloque de profundidad
    public float volumenMax;    // como mucho

    // sombrita bajo macetas, farolas, carteles y Coco
    public float alfaSombraContacto;

    // ---- iluminación ----

    // penumbra general al aire libre (0 = pleno día)
    public int colorAmbiente;
    public float oscuridadExterior;

    // penumbra dentro de la Mezquita
    public int colorInterior;
    public float oscuridadInterior;
    // niebla de cada plano de la sala, de lejos a cerca
    public float[] nieblaSala = new float[3];
    public int colorNieblaSala;

    // sol del fondo: posición, tamaño del resplandor y fuerza
    public int solX, solY, solRadio, colorSol;
    public float brilloSol;
    // el sol baja con la cámara como su capa (true en el río)
    public boolean solSigueCamara;

    // farolas encendidas
    public int colorFarola, radioFarola;
    public float brilloFarola;

    // puerta de la meta
    public int colorMeta, radioMeta;
    public float brilloMeta;

    // lucecita alrededor de Coco para que no se pierda en la oscuridad
    public int radioJugador;

    // lámparas de la sala de columnas
    public int colorLampara, radioLampara;
    public float brilloLampara;

    // esquinas de la pantalla más oscuras (viñeta)
    public float vineta;

    private static final EstiloVisual RIO = rio(), MEZQUITA = mezquita();

    private EstiloVisual() { }

    public static EstiloVisual de(Level.Tema tema) {
        return tema == Level.Tema.RIO ? RIO : MEZQUITA;
    }

    // nivel 1: mañana soleada en la ribera
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

    // nivel 2: atardecer fuera y penumbra dentro de la Mezquita
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

    // 0 = fuera, 1 = dentro de la sala de columnas (mismo criterio que el fondo de columnas)
    public static float factorInterior(Level.Tema tema, int camY) {
        if (tema != Level.Tema.MEZQUITA) return 0f;
        return Math.max(0f, Math.min(1f, (camY - 110) / 60f));
    }
}
