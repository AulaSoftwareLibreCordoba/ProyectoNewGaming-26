package cordoba.niveles;

import cordoba.fisica.PhysicsConfig;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.elementos.ObstaculoMovil;
import cordoba.niveles.plataformas.PlataformaMovil;
import cordoba.niveles.plataformas.PlataformaTemporal;
import cordoba.niveles.plataformas.PuenteDerrumbe;

/**
 * NIVEL 2 — El desafío de la Mezquita
 *
 * Calles del casco histórico y el interior de la Mezquita-Catedral.
 * Ya no hay instrucciones paso a paso: cada zona pide una combinación distinta
 * de valores y obliga a cambiarlos varias veces.
 *
 *  A. Calle encalada con fosos de pinchos (calentamiento).
 *  B. MURO de 8 bloques       -> bajar gravedad y/o subir fuerzaSalto.
 *  C. PASILLO con pinchos en el techo -> NO saltar demasiado alto (volver a valores normales).
 *  D. POZO con corriente de aire y bolas de pinchos -> SUBIR gravedad por encima de 12.5.
 *  E. SUELO QUE SE HUNDE muy largo -> velocidad alta (8.0 o más).
 *  F. COLUMNAS estrechas      -> velocidad baja para aterrizar con precisión.
 *  G. Plataformas temporales, plataforma móvil y bolas de pinchos.
 *  H. Puerta de la Mezquita (meta).
 */
public class Level2 extends Level {

    private static final int CALLE = 18;   // suelo de la calle
    private static final int ALTO = 10;    // suelo del muro y del pasillo superior
    private static final int BAJO = 31;    // suelo de la galería inferior
    private static final int COL = 29;     // altura de las columnas y plataformas

    public Level2() {
        super(2, "El desafío de la Mezquita", Tema.MEZQUITA, 162, 34);

        inicio(2, CALLE);

        // ---------------- A. Calle con fosos ----------------
        suelo(0, 20, CALLE);
        foso(8, 10);
        foso(14, 16);

        // ---------------- B. Muro de 8 bloques ----------------
        rellenar(21, ALTO, 30, filas - 1, 'S');

        // ---------------- C. Pasillo con pinchos en el techo ----------------
        rellenar(31, 0, 60, 3, 'S');                  // techo
        pinchosTecho(31, 56, 4);
        rellenar(31, ALTO, 56, filas - 1, 'S');       // suelo macizo del pasillo
        for (int c : new int[] { 35, 42, 49 }) {      // fosos de pinchos
            vaciar(c, ALTO, c + 2, ALTO);
            pinchos(c, c + 2, ALTO + 1);
        }

        // ---------------- D. Pozo con corriente de aire ----------------
        rellenar(61, 0, 64, BAJO - 4, 'S');           // pared derecha del pozo
        rellenar(57, ALTO + 1, 60, BAJO - 1, 'u');    // corriente de aire
        rellenar(57, BAJO, 68, filas - 1, 'S');       // fondo del pozo
        obstaculos.add(new ObstaculoMovil(57 * TILE + 8, 17 * TILE, 59 * TILE + 8, 17 * TILE, 110, 0));
        obstaculos.add(new ObstaculoMovil(58 * TILE + 8, 23 * TILE, 60 * TILE + 8, 23 * TILE, 110, 55));

        // ---------------- E. Galería con suelo que se hunde ----------------
        rellenar(65, 0, 88, BAJO - 4, 'S');           // techo bajo de la galería
        plataformas.add(new PuenteDerrumbe(69 * TILE, BAJO * TILE, 18, 95));
        pinchos(69, 86, filas - 1);
        rellenar(87, BAJO, 89, filas - 1, 'S');

        // ---------------- F. Columnas estrechas ----------------
        rellenar(89, 0, 118, 21, 'S');                // techo de la sala
        pinchos(90, 112, filas - 1);
        for (int c = 93; c <= 108; c += 5) rellenar(c, BAJO, c + 1, filas - 1, 'S');   // columnas de 2 bloques
        rellenar(113, BAJO, 118, filas - 1, 'S');

        // ---------------- G. Plataformas temporales y obstáculos ----------------
        // Todo sigue el mismo RITMO de 150 fotogramas (2,5 s): hay que observarlo.
        rellenar(119, 0, 150, 17, 'S');               // techo
        pinchos(119, 148, filas - 1);
        //   azulejo A visible en [20,120) · azulejo B en [75,175) · plataforma móvil en el extremo
        //   izquierdo en t=40 y en el derecho en t=115 · azulejo C visible en [90,210)
        plataformas.add(new PlataformaTemporal(121 * TILE, COL * TILE, 3, 150, 100, 130));
        plataformas.add(new PlataformaTemporal(127 * TILE, COL * TILE, 3, 150, 100, 75));
        plataformas.add(new PlataformaMovil(132 * TILE, 138 * TILE, COL * TILE, 3, 1.28f, 110));
        plataformas.add(new PlataformaTemporal(144 * TILE, COL * TILE, 3, 150, 120, 60));
        obstaculos.add(new ObstaculoMovil(125 * TILE + 8, 22 * TILE, 125 * TILE + 8, 31 * TILE, 150, 33));
        obstaculos.add(new ObstaculoMovil(148 * TILE, 22 * TILE, 148 * TILE, 31 * TILE, 75, 40));

        // ---------------- H. Puerta de la Mezquita ----------------
        rellenar(149, COL, 161, filas - 1, 'S');
        rellenar(154, COL - 1, 161, COL - 1, 'S');
        rellenar(156, COL - 2, 161, COL - 2, 'S');
        meta(158, COL - 2, 2, 3);

        // ---------------- Puntos de control ----------------
        farola(3, CALLE);
        farola(19, CALLE);
        farola(29, ALTO);
        farola(53, ALTO);
        farola(66, BAJO);
        farola(88, BAJO);
        farola(115, BAJO);
        farola(151, COL);

        // ---------------- Carteles ----------------
        cartel(4, CALLE, "NIVEL 2. Nueva variable desbloqueada: fuerzaSalto. Aquí nadie te dirá qué valores usar...");
        cartel(18, CALLE, "Este muro mide 8 bloques. Ni la gravedad ni el salto bastan solos: combínalos.");
        cartel(31, ALTO, "¡Pinchos en el techo! Si saltas demasiado alto te pincharás. Ajusta tus físicas.");
        cartel(54, ALTO, "Del pozo sube aire caliente (empuje 12.5). Si pesas poco, nunca bajarás...");
        cartel(67, BAJO, "Este suelo es antiguo y se hunde enseguida. Vas a necesitar MUCHA velocidad.");
        cartel(89, BAJO, "Columnas estrechas: si vas muy rápido te pasarás de largo. Precisión ante todo.");
        cartel(116, BAJO, "Azulejos que desaparecen y bolas de pinchos. Observa el ritmo antes de saltar.");
        cartel(152, COL, "¡La Mezquita-Catedral! Entra por la puerta para terminar la demo.");

        // ---------------- Decoración ----------------
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

    /** Hueco de 2 bloques de profundidad con pinchos en el fondo. */
    private void foso(int c1, int c2) {
        vaciar(c1, CALLE, c2, CALLE + 1);
        pinchos(c1, c2, CALLE + 2);
    }

    /** En el nivel 2 todas las variables están desbloqueadas y vuelven a su valor normal. */
    @Override
    public void configurarFisica(PhysicsConfig f) {
        super.configurarFisica(f);
    }
}
