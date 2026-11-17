package cordoba.niveles;

import cordoba.fisica.PhysicsConfig;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.elementos.ObstaculoMovil;
import cordoba.niveles.plataformas.PlataformaMovil;
import cordoba.niveles.plataformas.PlataformaTemporal;
import cordoba.niveles.plataformas.PuenteDerrumbe;

/*
 * Nivel 2, la Mezquita. Ya no hay pistas: cada zona pide otros valores.
 *  A. calle con fosos (calentamiento)
 *  B. muro de 8 bloques        -> menos gravedad y/o más fuerzaSalto
 *  C. pasillo con pinchos arriba -> no saltar tanto (volver a lo normal)
 *  D. pozo con corriente de aire -> gravedad por encima de 12.5
 *  E. suelo que se hunde        -> velocidad alta (8 o más)
 *  F. columnas estrechas        -> velocidad baja para caer justo
 *  G. plataformas temporales, plataforma móvil y bolas de pinchos
 *  H. puerta de la Mezquita (meta)
 */
public class Level2 extends Level {

    private static final int CALLE = 18;   // suelo de la calle
    private static final int ALTO = 10;    // suelo del muro y del pasillo de arriba
    private static final int BAJO = 31;    // suelo de la galería de abajo
    private static final int COL = 29;     // altura de columnas y plataformas

    public Level2() {
        super(2, "El desafío de la Mezquita", Tema.MEZQUITA, 162, 34);

        inicio(2, CALLE);

        // A. calle con fosos
        suelo(0, 20, CALLE);
        foso(8, 10);
        foso(14, 16);

        // B. muro de 8 bloques
        rellenar(21, ALTO, 30, filas - 1, 'S');

        // C. pasillo con pinchos en el techo
        rellenar(31, 0, 60, 3, 'S');                  // techo
        pinchosTecho(31, 56, 4);
        rellenar(31, ALTO, 56, filas - 1, 'S');       // suelo del pasillo
        for (int c : new int[] { 35, 42, 49 }) {      // fosos de pinchos
            vaciar(c, ALTO, c + 2, ALTO);
            pinchos(c, c + 2, ALTO + 1);
        }

        // D. pozo con corriente de aire
        rellenar(61, 0, 64, BAJO - 4, 'S');           // pared derecha del pozo
        rellenar(57, ALTO + 1, 60, BAJO - 1, 'u');    // corriente
        rellenar(57, BAJO, 68, filas - 1, 'S');       // fondo del pozo
        obstaculos.add(new ObstaculoMovil(57 * TILE + 8, 17 * TILE, 59 * TILE + 8, 17 * TILE, 110, 0));
        obstaculos.add(new ObstaculoMovil(58 * TILE + 8, 23 * TILE, 60 * TILE + 8, 23 * TILE, 110, 55));

        // E. galería con suelo que se hunde
        rellenar(65, 0, 88, BAJO - 4, 'S');           // techo bajo
        plataformas.add(new PuenteDerrumbe(69 * TILE, BAJO * TILE, 18, 95));
        pinchos(69, 86, filas - 1);
        rellenar(87, BAJO, 89, filas - 1, 'S');

        // F. columnas estrechas
        rellenar(89, 0, 118, 21, 'S');                // techo de la sala
        pinchos(90, 112, filas - 1);
        for (int c = 93; c <= 108; c += 5) rellenar(c, BAJO, c + 1, filas - 1, 'S');   // columnas de 2 bloques
        rellenar(113, BAJO, 118, filas - 1, 'S');

        // G. plataformas temporales y obstáculos
        // todo va al mismo ritmo de 150 fotogramas (2,5 s): hay que fijarse
        rellenar(119, 0, 150, 17, 'S');               // techo
        pinchos(119, 148, filas - 1);
        // azulejo A visible en [20,120) · B en [75,175) · C en [90,210)
        // la plataforma móvil está a la izquierda en t=40 y a la derecha en t=115
        plataformas.add(new PlataformaTemporal(121 * TILE, COL * TILE, 3, 150, 100, 130));
        plataformas.add(new PlataformaTemporal(127 * TILE, COL * TILE, 3, 150, 100, 75));
        plataformas.add(new PlataformaMovil(132 * TILE, 138 * TILE, COL * TILE, 3, 1.28f, 110));
        plataformas.add(new PlataformaTemporal(144 * TILE, COL * TILE, 3, 150, 120, 60));
        obstaculos.add(new ObstaculoMovil(125 * TILE + 8, 22 * TILE, 125 * TILE + 8, 31 * TILE, 150, 33));
        obstaculos.add(new ObstaculoMovil(148 * TILE, 22 * TILE, 148 * TILE, 31 * TILE, 75, 40));

        // H. puerta de la Mezquita
        rellenar(149, COL, 161, filas - 1, 'S');
        rellenar(154, COL - 1, 161, COL - 1, 'S');
        rellenar(156, COL - 2, 161, COL - 2, 'S');
        meta(158, COL - 2, 2, 3);

        // checkpoints
        farola(3, CALLE);
        farola(19, CALLE);
        farola(29, ALTO);
        farola(53, ALTO);
        farola(66, BAJO);
        farola(88, BAJO);
        farola(115, BAJO);
        farola(151, COL);

        // carteles
        cartel(4, CALLE, "NIVEL 2. Nueva variable desbloqueada: fuerzaSalto. Aquí nadie te dirá qué valores usar...");
        cartel(18, CALLE, "Este muro mide 8 bloques. Ni la gravedad ni el salto bastan solos: combínalos.");
        cartel(31, ALTO, "¡Pinchos en el techo! Si saltas demasiado alto te pincharás. Ajusta tus físicas.");
        cartel(54, ALTO, "Del pozo sube aire caliente (empuje 12.5). Si pesas poco, nunca bajarás...");
        cartel(67, BAJO, "Este suelo es antiguo y se hunde enseguida. Vas a necesitar MUCHA velocidad.");
        cartel(89, BAJO, "Columnas estrechas: si vas muy rápido te pasarás de largo. Precisión ante todo.");
        cartel(116, BAJO, "Azulejos que desaparecen y bolas de pinchos. Observa el ritmo antes de saltar.");
        cartel(152, COL, "¡La Mezquita-Catedral! Entra por la puerta para terminar la demo.");

        // decoración
        decoracion(Decoracion.Tipo.NARANJO, 1, CALLE, 0);
        decoracion(Decoracion.Tipo.MACETA, 6, CALLE, 0);
        decoracion(Decoracion.Tipo.MACETA, 12, CALLE, 1);
        decoracion(Decoracion.Tipo.NARANJO, 18, CALLE, 0);
        decoracion(Decoracion.Tipo.MACETA, 24, ALTO, 2);
        decoracion(Decoracion.Tipo.MACETA, 27, ALTO, 1);
        decoracion(Decoracion.Tipo.MACETA, 33, ALTO, 0);
        decoracion(Decoracion.Tipo.MACETA, 117, BAJO, 1);
        decoracion(Decoracion.Tipo.NARANJO, 150, COL, 0);
        decoracion(Decoracion.Tipo.PALMERA, 153, COL, 0);
        decoracion(Decoracion.Tipo.MACETA, 157, COL - 2, 2);
        decoracion(Decoracion.Tipo.MACETA, 160, COL - 2, 0);
    }

    // hoyo de 2 de hondo con pinchos al fondo
    private void foso(int c1, int c2) {
        vaciar(c1, CALLE, c2, CALLE + 1);
        pinchos(c1, c2, CALLE + 2);
    }

    // aquí todo está desbloqueado y vuelve a su valor normal
    @Override
    public void configurarFisica(PhysicsConfig f) {
        super.configurarFisica(f);
    }
}
