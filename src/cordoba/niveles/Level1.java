package cordoba.niveles;

import cordoba.fisica.PhysicsConfig;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.plataformas.PuenteDerrumbe;

/*
 * Nivel 1, el tutorial: moverse, abrir la consola y bajar la gravedad para cruzar el río, saltar la
 * muralla, subir la velocidad para la pasarela que se hunde y llegar a la Calahorra.
 *
 * Plano (fila 18 = suelo):
 *   0-14 suelo | 15-16 agua | 17-24 suelo con escalón | 25-27 agua | 28-35 suelo
 *   36-43 RÍO ANCHO | 44-65 suelo con MURALLA (53-60, 6 de alto)
 *   64-79 Puente Romano con PASARELA que se hunde (66-77) | 78-92 suelo con pinchos | 93-95 agua | 96-103 meta
 */
public class Level1 extends Level {

    private static final int S = 18;  // fila del suelo

    private boolean ayudaGravedadMostrada, ayudaVelocidadMostrada;

    public Level1() {
        super(1, "Los primeros pasos por Córdoba", Tema.RIO, 104, 23);

        inicio(3, S);

        // primer tramo: moverse y saltar
        suelo(0, 14, S);
        agua(15, 16, S + 1);
        suelo(17, 24, S);
        rellenar(21, S - 2, 24, S - 1, 'S');     // escalón
        agua(25, 27, S + 1);
        suelo(28, 35, S);

        // el Guadalquivir: demasiado ancho con la gravedad normal
        agua(36, 43, S + 1);

        // orilla con la muralla
        suelo(44, 65, S);
        rellenar(53, S - 6, 60, S - 1, 'S');     // 6 bloques de alto

        // bajo el Puente Romano: pasarela que se hunde
        rellenar(64, 0, 79, S - 4, 'S');         // el puente (techo bajo)
        agua(66, 77, S + 1);
        plataformas.add(new PuenteDerrumbe(66 * TILE, S * TILE, 12, 75));

        // hasta la Calahorra
        suelo(78, 92, S);
        pinchos(87, 89, S - 1);
        agua(93, 95, S + 1);
        suelo(96, 103, S);
        meta(99, S, 2, 3);

        // checkpoints
        farola(19, S);
        farola(30, S);
        farola(48, S);
        farola(63, S);
        farola(82, S);

        // carteles
        cartel(5, S, "¡Hola, soy Coco! Muévete con A/D o las flechas y salta con ESPACIO.");
        cartel(18, S, "Las farolas son checkpoints: si caes, vuelves a la última encendida.");
        cartel(33, S, "El río es muy ancho para mi salto. Pulsa [E] para abrir el código.");
        cartel(47, S, "¡Bien! Con menos gravedad saltas más alto. Sube a la muralla.");
        cartelDesbloqueo(62, S, "velocidad", "La pasarela se hunde al pisarla: sube la velocidad a 6.5 o más.",
                () -> fisica.getVariable(PhysicsConfig.VELOCIDAD).setBloqueada(false));
        cartel(80, S, "¡Genial! Ahora llega a la Torre de la Calahorra.");

        // decoración
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

    // en el tutorial solo empieza libre la gravedad
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
            mostrarEvento("Baja 'gravedad' a 5.0 o menos y pulsa ENTER. ¡El tiempo corre!");
        } else if (velocidadLibre && !ayudaVelocidadMostrada) {
            ayudaVelocidadMostrada = true;
            mostrarEvento("Elige 'velocidad', súbela a 6.5 o más y pulsa ENTER.");
        }
    }

    @Override
    public void alAplicarCambios() {
        boolean velocidadLibre = !fisica.getVariable(PhysicsConfig.VELOCIDAD).isBloqueada();
        if (!velocidadLibre) {
            if (fisica.getValor(PhysicsConfig.GRAVEDAD) <= 5.0f)
                mostrarEvento("¡Compilado! Ahora pesas menos. Cierra con [E] y salta el río.");
            else
                mostrarEvento("Aún pesas mucho: baja la gravedad a 5.0 o menos.");
        } else {
            if (fisica.getValor(PhysicsConfig.VELOCIDAD) >= 6.5f)
                mostrarEvento("¡Compilado! Cierra con [E] y cruza la pasarela sin parar.");
            else
                mostrarEvento("Aún vas lento: sube la velocidad a 6.5 o más.");
        }
    }
}
