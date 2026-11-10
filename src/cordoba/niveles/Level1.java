package cordoba.niveles;

import cordoba.fisica.PhysicsConfig;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.plataformas.PuenteDerrumbe;

/**
 * NIVEL 1 — Los primeros pasos por Córdoba (TUTORIAL)
 *
 * Ribera del Guadalquivir, junto al Puente Romano. Enseña a:
 *   1. Moverse y saltar.
 *   2. Abrir la consola (E), bajar la GRAVEDAD y aplicar cambios para cruzar el río.
 *   3. Comprobar que con poca gravedad también se salta más alto (muralla).
 *   4. Desbloquear y subir la VELOCIDAD para cruzar una pasarela que se derrumba.
 *   5. Llegar a la Torre de la Calahorra.
 *
 * Plano del nivel (fila 18 = superficie del suelo):
 *   col 0-14 suelo | 15-16 agua | 17-24 suelo (escalón) | 25-27 agua | 28-35 suelo
 *   36-43 RÍO ANCHO (8 bloques) | 44-65 suelo con MURALLA (53-60, 6 bloques de alto)
 *   64-79 Puente Romano (techo) con PASARELA que se derrumba (66-77)
 *   78-92 suelo con pinchos | 93-95 agua | 96-103 Torre de la Calahorra (meta)
 */
public class Level1 extends Level {

    private static final int S = 18;  // fila del suelo

    private boolean ayudaGravedadMostrada, ayudaVelocidadMostrada;

    public Level1() {
        super(1, "Los primeros pasos por Córdoba", Tema.RIO, 104, 23);

        inicio(3, S);

        // --- Primer tramo: aprender a moverse y saltar ---
        suelo(0, 14, S);
        agua(15, 16, S + 1);
        suelo(17, 24, S);
        rellenar(21, S - 2, 24, S - 1, 'S');     // escalón de piedra
        agua(25, 27, S + 1);
        suelo(28, 35, S);

        // --- El Guadalquivir: demasiado ancho para la gravedad normal ---
        agua(36, 43, S + 1);

        // --- Orilla con la muralla alta ---
        suelo(44, 65, S);
        rellenar(53, S - 6, 60, S - 1, 'S');     // muralla de 6 bloques

        // --- Bajo los ojos del Puente Romano: pasarela que se derrumba ---
        rellenar(64, 0, 79, S - 4, 'S');         // masa del puente (techo bajo)
        agua(66, 77, S + 1);
        plataformas.add(new PuenteDerrumbe(66 * TILE, S * TILE, 12, 75));

        // --- Tramo final hasta la Calahorra ---
        suelo(78, 92, S);
        pinchos(87, 89, S - 1);
        agua(93, 95, S + 1);
        suelo(96, 103, S);
        meta(99, S, 2, 3);

        // --- Puntos de control ---
        farola(19, S);
        farola(30, S);
        farola(48, S);
        farola(63, S);
        farola(82, S);

        // --- Carteles del tutorial ---
        cartel(5, S, "¡Hola! Soy Coco, un cocodrilo cordobés. Muévete con A/D o las flechas y salta con ESPACIO.");
        cartel(18, S, "Las farolas son puntos de control: si caes al agua volverás a la última farola encendida.");
        cartel(33, S, "Uf... el Guadalquivir es demasiado ancho para mi salto. Pulsa [E] para abrir la consola de código.");
        cartel(47, S, "¡Lo lograste! Con poca gravedad también saltas MÁS ALTO. Sube a lo alto de la muralla.");
        cartel(62, S, "¡Nueva variable desbloqueada: velocidad! La pasarela se hunde en cuanto entras: súbela a 6.5 o más.",
                () -> fisica.getVariable(PhysicsConfig.VELOCIDAD).setBloqueada(false));
        cartel(80, S, "¡Genial! Ya sabes cambiar el código del juego. Llega a la Torre de la Calahorra.");

        // --- Decoración ---
        decoracion(Decoracion.Tipo.PALMERA, 1, S, 0);
        decoracion(Decoracion.Tipo.MACETA, 8, S, 0);
        decoracion(Decoracion.Tipo.MACETA, 10, S, 1);
        decoracion(Decoracion.Tipo.CANAS, 14, S, 0);
        decoracion(Decoracion.Tipo.CANAS, 17, S, 0);
        decoracion(Decoracion.Tipo.CANAS, 35, S, 0);
        decoracion(Decoracion.Tipo.CANAS, 45, S, 0);
        decoracion(Decoracion.Tipo.PALMERA, 50, S, 0);
        decoracion(Decoracion.Tipo.MACETA, 55, S - 6, 2);
        decoracion(Decoracion.Tipo.MACETA, 58, S - 6, 0);
        decoracion(Decoracion.Tipo.NARANJO, 85, S, 0);
        decoracion(Decoracion.Tipo.MACETA, 91, S, 1);
        decoracion(Decoracion.Tipo.PALMERA, 97, S, 0);
    }

    /** En el tutorial solo empieza desbloqueada la gravedad. */
    @Override
    public void configurarFisica(PhysicsConfig f) {
        super.configurarFisica(f);
        f.getVariable(PhysicsConfig.VELOCIDAD).setBloqueada(true);
        f.getVariable(PhysicsConfig.FUERZA_SALTO).setBloqueada(true);
    }

    @Override
    public void alAbrirEditor() {
        boolean velocidadLibre = !fisica.getVariable(PhysicsConfig.VELOCIDAD).isBloqueada();
        if (!velocidadLibre && !ayudaGravedadMostrada) {
            ayudaGravedadMostrada = true;
            mostrarEvento("El juego está en pausa. Pulsa [-] en 'gravedad' hasta 5.0 o menos y luego APLICAR CAMBIOS.");
        } else if (velocidadLibre && !ayudaVelocidadMostrada) {
            ayudaVelocidadMostrada = true;
            mostrarEvento("Pulsa [+] en 'velocidad' hasta 6.5 o más y luego APLICAR CAMBIOS.");
        }
    }

    @Override
    public void alAplicarCambios() {
        boolean velocidadLibre = !fisica.getVariable(PhysicsConfig.VELOCIDAD).isBloqueada();
        if (!velocidadLibre) {
            if (fisica.getValor(PhysicsConfig.GRAVEDAD) <= 5.0f)
                mostrarEvento("¡Compilado! Ahora Coco pesa menos y pasa más tiempo en el aire. Cierra con [E] y salta el río.");
            else
                mostrarEvento("Coco aún pesa demasiado para cruzar el río. Baja más la gravedad (5.0 o menos).");
        } else {
            if (fisica.getValor(PhysicsConfig.VELOCIDAD) >= 6.5f)
                mostrarEvento("¡Más rápido que nunca! Cierra la consola y cruza la pasarela sin pararte.");
            else
                mostrarEvento("Todavía vas lento: la pasarela caerá antes. Sube la velocidad a 6.5 o más.");
        }
    }
}
