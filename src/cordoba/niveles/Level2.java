package cordoba.niveles;

import cordoba.niveles.elementos.Coleccionable;
import cordoba.niveles.elementos.Decoracion;
import cordoba.niveles.elementos.MacetaQueCae;

/*
 * Nivel 2: la Calleja de las Flores. Se sube de abajo arriba entre paredes encaladas.
 *  A. la calle con escalones de empedrado    -> salto normal
 *  B. balcones cada 5 casillas               -> subir fuerzaSalto o bajar gravedad
 *  C. balcones con macetas que caen          -> esperar a que caiga y saltar
 *  D. pasillo con rejas de pinchos arriba    -> volver al salto normal (si no, te pinchas)
 *  E. patio estrecho hasta la plazuela       -> salto normal
 *  F. zaguán y patio cordobés con alberca     -> el puente de azulejos se hunde: subir velocidad (o saltar lejos)
 *  G. el pozo del patio: hay que tirarse      -> bajar gravedad para caer despacio y esquivar los pinchos
 *  H. el aljibe del fondo                     -> caer sobre un pilar (no al agua) y salir por la puerta (meta)
 *
 * Plano (80 x 96, fila 0 arriba). Paredes encaladas 'S', empedrado o piedra '#', fondo de piedra 'p'.
 *   filas 0-25   plazuela abierta al cielo (suelo en la fila 26)
 *   filas 26-48  patio estrecho (columnas 4-7) con repisas a un lado y a otro
 *   filas 43-48  pasillo con pinchos en el techo (columnas 8-29), suelo en la fila 49
 *   filas 52-90  la calleja: balcones en zigzag; suelo de la calle en la fila 91
 *   columnas 40-75, filas 0-25: patio cordobés (suelo en la fila 26); el pozo baja por las columnas 50-59
 *   filas 81-95: aljibe con agua y pilares; la salida está a la derecha
 */
public class Level2 extends Level {

    public Level2() {
        super(2, NOMBRES[2], Tema.CALLEJA, 80, 96);

        rellenar(0, 0, 79, 95, 'S');                 // todo pared y se van abriendo huecos
        rellenar(40, 27, 79, 95, '#');               // bajo el patio, piedra

        // huecos
        vaciar(4, 0, 35, 25);                        // plazuela
        vaciar(4, 26, 7, 48);                        // patio estrecho
        vaciar(8, 43, 29, 48);                       // pasillo
        vaciar(30, 45, 35, 53);                      // hueco encima del último balcón
        vaciar(4, 52, 35, 90);                       // la calleja

        // A. la calle y sus escalones (de 2 y 3 casillas: con el salto normal se suben)
        inicio(6, 91);
        rellenar(4, 91, 35, 95, '#');
        rellenar(13, 89, 17, 90, '#');
        rellenar(18, 86, 22, 90, '#');
        rellenar(23, 83, 27, 90, '#');
        rellenar(28, 80, 35, 90, '#');

        // B. balcones cada 5 casillas en zigzag (con el salto normal no se llega)
        rellenar(21, 75, 25, 76, 'S');
        rellenar(13, 70, 17, 71, 'S');
        rellenar(5, 65, 10, 66, 'S');

        // C. balcones cada 3-4 casillas con macetas que caen encima
        rellenar(13, 61, 17, 62, 'S');
        rellenar(21, 57, 25, 58, 'S');
        rellenar(28, 54, 35, 55, 'S');
        macetaQueCae(15, 52, 61, 160, 0, 0);
        macetaQueCae(23, 52, 57, 160, 80, 1);
        macetaQueCae(33, 45, 54, 170, 40, 3);

        // D. pasillo: suelo en la fila 49 con tres huecos de pinchos y rejas de pinchos en el techo.
        // Con el salto subido la cabeza llega a los pinchos de arriba
        pinchosTecho(10, 29, 43);
        for (int c : new int[] { 13, 19, 25 }) {
            vaciar(c, 49, c + 2, 49);
            pinchos(c, c + 2, 50);
        }

        // E. patio estrecho: repisas alternas cada 3 casillas hasta la plazuela
        int[] filas = { 46, 43, 40, 37, 34, 31, 28 };
        for (int i = 0; i < filas.length; i++) {
            int c = i % 2 == 0 ? 6 : 4;
            rellenar(c, filas[i], c + 1, filas[i], 'S');
        }
        rellenar(8, 26, 35, 26, '#');                // suelo de la plazuela

        // F. zaguán (portal en la casa) y patio cordobés abierto al cielo
        vaciar(36, 0, 39, 16);                       // sobre el zaguán, solo un dintel (filas 17-20)
        vaciar(36, 21, 39, 25);                      // zaguán
        vaciar(40, 0, 75, 25);                       // patio
        // al fondo, a la derecha del pozo, la fachada del patio: pared blanca llena de macetas azules
        rellenar(62, 6, 75, 25, 'S');
        paredFlorida(62, 7, 75, 25);
        rellenar(36, 26, 75, 26, '#');               // suelo del patio
        // alberca de 8 casillas con un puente de azulejos que se hunde: hace falta velocidad (o un salto largo)
        vaciar(42, 26, 49, 26);
        rellenar(42, 27, 49, 28, '~');
        plataformas.add(new cordoba.niveles.plataformas.PuenteDerrumbe(42 * TILE, 26 * TILE, 8, 45));

        // G. el pozo: brocal en el patio y un tiro de piedra con repisas de pinchos alternas.
        // Se baja cayendo; con poca gravedad da tiempo a esquivar
        vaciar(53, 26, 56, 26);
        rellenar(53, 26, 56, 31, 'p');               // cuello del pozo
        rellenar(51, 32, 58, 80, 'p');               // el tiro: 8 casillas de ancho
        brisa(53, 27, 56, 31);                       // brisa suave que sube por el pozo y frena la caída
        brisa(51, 32, 58, 80);
        decoraciones.add(new Decoracion(Decoracion.Tipo.POZO, 55 * TILE, 26 * TILE, 0));
        // repisas de pinchos que dejan un hueco de 3 casillas, una vez a cada lado, cada 12 filas:
        // para pasar de un hueco al otro hay que moverse 5 casillas mientras se cae, y con la gravedad
        // normal no da tiempo. Con poca gravedad se cae despacio y sí.
        repisa(51, 55, 38, true);                    // hueco a la derecha (56-58)
        repisa(54, 58, 50, true);                    // hueco a la izquierda (51-53)
        repisa(54, 56, 58, false);                   // repisa sin pinchos para descansar (farola)
        repisa(51, 55, 64, true);                    // hueco a la derecha: bajar de la repisa por la derecha
        obstaculos.add(new cordoba.niveles.elementos.ObstaculoMovil(52 * TILE, 70 * TILE, 58 * TILE, 70 * TILE, 120, 0));
        repisa(54, 58, 76, true);                    // hueco a la izquierda, encima de un pilar del aljibe

        // H. aljibe: agua y pilares; el pozo cae justo encima del agua, hay que dirigirse a un pilar
        rellenar(44, 81, 75, 92, 'p');
        rellenar(44, 89, 63, 95, '~');
        for (int c : new int[] { 50, 55, 60 }) rellenar(c, 86, c + 1, 95, '#');
        rellenar(64, 86, 75, 95, '#');               // orilla con la salida
        meta(72, 86, 2, 3);

        // checkpoints (en el orden en que se llega)
        farola(33, 80);
        farola(7, 65);
        farola(32, 54);
        farola(29, 49);
        farola(10, 26);
        farola(38, 26);
        farola(55, 58);
        farola(67, 86);

        // comida: hay que subir fuerzaSalto o bajar gravedad
        coleccionable(Coleccionable.Tipo.SALMOREJO, 7, 84);     // encima de la calle
        coleccionable(Coleccionable.Tipo.FLAMENQUIN, 20, 19);   // en lo alto de la plazuela

        // decoración
        decoracion(Decoracion.Tipo.MACETA, 4, 91, 0);
        decoracion(Decoracion.Tipo.MACETA, 11, 91, 1);
        decoracion(Decoracion.Tipo.MACETA, 25, 75, 2);
        decoracion(Decoracion.Tipo.MACETA, 13, 70, 3);
        decoracion(Decoracion.Tipo.MACETA, 5, 65, 1);
        decoracion(Decoracion.Tipo.MACETA, 21, 57, 0);
        decoracion(Decoracion.Tipo.MACETA, 35, 54, 2);
        decoracion(Decoracion.Tipo.NARANJO, 14, 26, 0);
        decoracion(Decoracion.Tipo.MACETA, 24, 26, 0);
        decoracion(Decoracion.Tipo.MACETA, 26, 26, 3);
    }

    // repisa de piedra dentro del pozo (de c1 a c2 en la fila f), con pinchos encima o sin ellos
    private void repisa(int c1, int c2, int f, boolean conPinchos) {
        rellenar(c1, f, c2, f, '#');
        if (conPinchos) pinchos(c1, c2, f - 1);
    }

    // maceta colgada en la fila filaArriba de la columna col que cae hasta el suelo de filaSuelo
    private void macetaQueCae(int col, int filaArriba, int filaSuelo, int periodo, int desfase, int variante) {
        obstaculos.add(new MacetaQueCae(col * TILE + TILE / 2f, filaArriba * TILE + 8, filaSuelo * TILE - 6,
                periodo, desfase, variante));
    }
}
