package cordoba.entidades;

import cordoba.audio.Sonido;
import cordoba.fisica.CollisionManager;
import cordoba.fisica.PhysicsConfig;
import cordoba.graficos.Particulas;
import cordoba.graficos.Sprites;
import cordoba.niveles.Level;
import cordoba.niveles.plataformas.PlataformaDinamica;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * COCO, el cocodrilo protagonista.
 *
 * Toda su física sale de PhysicsConfig:
 *   - gravedad    -> cuánto aumenta la velocidad de caída cada fotograma
 *   - velocidad   -> velocidad horizontal máxima al andar
 *   - fuerzaSalto -> velocidad vertical inicial del salto
 * Si cambias esos valores en la consola, Coco se comporta de otra forma al instante.
 */
public class Player {

    /** Tamaño de la caja de colisión (el dibujo es algo más grande). */
    public static final int ANCHO = 12, ALTO = 16;

    /** Qué tan rápido alcanza la velocidad deseada (1 = instantáneo). */
    private static final float CONTROL_SUELO = 0.30f;
    private static final float CONTROL_AIRE  = 0.08f;

    /** Ayudas para que saltar sea cómodo (en fotogramas). */
    private static final int TIEMPO_COYOTE = 6;   // aún puedes saltar justo después de salir del borde
    private static final int BUFFER_SALTO  = 6;   // si pulsas un poco antes de tocar el suelo, también salta

    // Estado físico (píxeles y píxeles/fotograma)
    public float x, y, vx, vy;
    public boolean enSuelo;
    public boolean mirandoDerecha = true;
    public PlataformaDinamica plataformaBajo;   // plataforma sobre la que está (o null)
    public boolean enCorriente;                 // dentro de una corriente de aire

    // Estados especiales
    public boolean muerto;
    public int timerMuerte;
    public boolean celebrando;

    private int coyote, bufferSalto;
    private float distanciaAndada;
    private int tiempoQuieto;
    private int timerCelebracion;

    public Player(float x, float y) {
        colocar(x, y);
    }

    /** Coloca al jugador en una posición y resetea su estado. */
    public void colocar(float x, float y) {
        this.x = x;
        this.y = y;
        vx = vy = 0;
        enSuelo = false;
        muerto = false;
        celebrando = false;
        timerMuerte = 0;
        plataformaBajo = null;
        coyote = bufferSalto = 0;
    }

    /**
     * Actualiza a Coco durante un fotograma.
     * @param izquierda    tecla izquierda mantenida
     * @param derecha      tecla derecha mantenida
     * @param saltoPulsado se ha pulsado SALTAR en este fotograma
     */
    public void actualizar(boolean izquierda, boolean derecha, boolean saltoPulsado,
                           PhysicsConfig fisica, Level nivel, CollisionManager colisiones,
                           Particulas particulas) {
        // --- Animación de muerte: pequeño salto y caída fuera de la pantalla ---
        if (muerto) {
            timerMuerte++;
            if (timerMuerte > 12) {
                vy += 0.35f;
                y += vy;
            }
            return;
        }

        // --- Celebración al llegar a la meta ---
        if (celebrando) {
            timerCelebracion++;
            vx = 0;
            izquierda = derecha = false;
            saltoPulsado = enSuelo && timerCelebracion % 40 == 0;
        }

        // --- 1. Movimiento horizontal (usa la VELOCIDAD de PhysicsConfig) ---
        float objetivo = 0;
        if (izquierda) objetivo -= fisica.velocidadPx();
        if (derecha)   objetivo += fisica.velocidadPx();
        float control = enSuelo ? CONTROL_SUELO : CONTROL_AIRE;
        vx += (objetivo - vx) * control;
        if (objetivo == 0 && Math.abs(vx) < 0.05f) vx = 0;
        if (objetivo > 0) mirandoDerecha = true;
        else if (objetivo < 0) mirandoDerecha = false;

        // --- 2. Salto (usa la FUERZA DE SALTO de PhysicsConfig) ---
        if (saltoPulsado) bufferSalto = BUFFER_SALTO;
        if (enSuelo) coyote = TIEMPO_COYOTE;
        else if (coyote > 0) coyote--;
        if (bufferSalto > 0) {
            bufferSalto--;
            if (coyote > 0) {
                vy = celebrando ? -3.5f : -fisica.fuerzaSaltoPx();
                coyote = 0;
                bufferSalto = 0;
                if (!celebrando) {
                    Sonido.salto();
                    particulas.emitir(x + ANCHO / 2f, y + ALTO, 6, new Color(0xE8DCC0), 1.2f, 0.05f, 18);
                }
            }
        }

        // --- 3. Gravedad (usa la GRAVEDAD de PhysicsConfig) ---
        vy += fisica.gravedadPx();

        // Corriente de aire: empuja hacia arriba con fuerza fija
        enCorriente = nivel.esCorriente(x + ANCHO / 2f, y + ALTO / 2f);
        if (enCorriente) {
            vy -= Level.FUERZA_CORRIENTE;
            vy *= Level.ROZAMIENTO_CORRIENTE;   // el aire frena: no vale "colarse" cayendo desde muy alto
            if (vy < -2.5f) vy = -2.5f;
        }
        if (vy > PhysicsConfig.CAIDA_MAXIMA) vy = PhysicsConfig.CAIDA_MAXIMA;

        // --- 4. Mover comprobando colisiones ---
        boolean estabaEnSuelo = enSuelo;
        float vyAntes = vy;
        colisiones.moverJugador(this);

        if (!estabaEnSuelo && enSuelo && vyAntes > 3f) {
            particulas.emitir(x + ANCHO / 2f, y + ALTO, 8, new Color(0xD8CBA8), 1.4f, 0.06f, 20);
            Sonido.aterrizar();
        }

        // --- 5. Datos para la animación ---
        if (enSuelo && Math.abs(vx) > 0.2f) {
            distanciaAndada += Math.abs(vx);
            tiempoQuieto = 0;
        } else {
            tiempoQuieto++;
        }
    }

    /** Empieza la animación de "perder". */
    public void morir() {
        if (muerto) return;
        muerto = true;
        timerMuerte = 0;
        vy = -5f;
        vx = 0;
        Sonido.muerte();
    }

    public void celebrar() {
        celebrando = true;
        timerCelebracion = 0;
    }

    /** Devuelve el fotograma de animación adecuado al estado actual. */
    public BufferedImage fotogramaActual() {
        BufferedImage img = fotogramaBase();
        return mirandoDerecha ? img : Sprites.volteada(img);
    }

    /** El dibujo actual sin voltear (mirando a la derecha). Lo usa también el fantasma. */
    public BufferedImage fotogramaBase() {
        BufferedImage img;
        if (muerto) {
            img = Sprites.COCO_HERIDO;
        } else if (celebrando) {
            img = enSuelo ? Sprites.COCO_VICTORIA1 : Sprites.COCO_VICTORIA2;
        } else if (!enSuelo) {
            img = vy < 0 ? Sprites.COCO_SALTO : Sprites.COCO_CAIDA;
        } else if (Math.abs(vx) > 0.2f) {
            int i = (int) (distanciaAndada / 6) % Sprites.COCO_ANDAR.length;
            img = Sprites.COCO_ANDAR[i];
        } else {
            // parpadeo de vez en cuando
            img = (tiempoQuieto % 180) > 170 ? Sprites.COCO_PARPADEO : Sprites.COCO_QUIETO;
        }
        return img;
    }

    /** Posición X donde dibujar el sprite de 20 px para que cuadre con la caja de 12 px. */
    public int xDibujo() {
        return mirandoDerecha ? Math.round(x) - 3 : Math.round(x) - 5;
    }

    public int yDibujo() {
        return Math.round(y) + ALTO - 20;
    }

    public float centroX() {
        return x + ANCHO / 2f;
    }

    public float centroY() {
        return y + ALTO / 2f;
    }
}
