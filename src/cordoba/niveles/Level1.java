package cordoba.niveles;

import cordoba.niveles.elementos.Coleccionable;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.plataformas.PuenteDerrumbe;

/*
 * Nivel 1: bajar la gravedad para cruzar el río, saltar la muralla, subir la velocidad para la
 * pasarela que se hunde y llegar a la Calahorra.
 *
 * Plano (fila 18 = suelo):
 *   0-14 suelo | 15-16 agua | 17-24 suelo con escalón | 25-27 agua | 28-35 suelo
 *   36-43 RÍO ANCHO | 44-65 suelo con MURALLA (53-60, 6 de alto)
 *   64-79 Puente Romano con PASARELA que se hunde (66-77) | 78-92 suelo con pinchos | 93-95 agua | 96-103 meta
 */
public class Level1 extends Level {

    private static final int S = 18;  // fila del suelo

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


        // comida: hay que subir fuerzaSalto o bajar gravedad
        coleccionable(Coleccionable.Tipo.SALMOREJO, 9, S - 7);      // en el primer tramo
        coleccionable(Coleccionable.Tipo.FLAMENQUIN, 94, S - 7);    // sobre el agua

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
}
