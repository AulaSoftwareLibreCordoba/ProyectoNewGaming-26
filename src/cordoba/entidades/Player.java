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

// Coco. Su física sale de PhysicsConfig.
public class Player {

    // caja de colisión (el dibujo es un poco más grande)
    public static final int ANCHO = 12, ALTO = 16;

    // lo rápido que llega a la velocidad que quieres (1 = al instante)
    private static final float CONTROL_SUELO = 0.30f;
    private static final float CONTROL_AIRE  = 0.08f;

    // ayudas para que saltar no sea frustrante (en fotogramas)
    private static final int TIEMPO_COYOTE = 6;   // saltar justo después del borde
    private static final int BUFFER_SALTO  = 6;   // saltar pulsando un poco antes de tocar suelo

    // en píxeles y píxeles/fotograma
    public float x, y, vx, vy;
    public boolean enSuelo;
    public boolean mirandoDerecha = true;
    public PlataformaDinamica plataformaBajo;   // plataforma que pisa (o null)
    public boolean enCorriente;

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

    // lo pone en un sitio y resetea todo
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

    public void actualizar(boolean izquierda, boolean derecha, boolean saltoPulsado,
                           PhysicsConfig fisica, Level nivel, CollisionManager colisiones,
                           Particulas particulas) {
        // muriendo: saltito y se cae de la pantalla
        if (muerto) {
            timerMuerte++;
            if (timerMuerte > 12) {
                vy += 0.35f;
                y += vy;
            }
            return;
        }

        // celebrando en la meta
        if (celebrando) {
            timerCelebracion++;
            vx = 0;
            izquierda = derecha = false;
            saltoPulsado = enSuelo && timerCelebracion % 40 == 0;
        }

        // 1. moverse (velocidad)
        float objetivo = 0;
        if (izquierda) objetivo -= fisica.velocidadPx();
        if (derecha)   objetivo += fisica.velocidadPx();
        float control = enSuelo ? CONTROL_SUELO : CONTROL_AIRE;
        vx += (objetivo - vx) * control;
        if (objetivo == 0 && Math.abs(vx) < 0.05f) vx = 0;
        if (objetivo > 0) mirandoDerecha = true;
        else if (objetivo < 0) mirandoDerecha = false;

        // 2. saltar (fuerzaSalto)
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

        // 3. gravedad
        vy += fisica.gravedadPx();

        // la corriente de aire empuja hacia arriba siempre igual
        enCorriente = nivel.esCorriente(x + ANCHO / 2f, y + ALTO / 2f);
        if (enCorriente) {
            vy -= Level.FUERZA_CORRIENTE;
            vy *= Level.ROZAMIENTO_CORRIENTE;   // el aire frena
            if (vy < -2.5f) vy = -2.5f;
        }
        if (vy > PhysicsConfig.CAIDA_MAXIMA) vy = PhysicsConfig.CAIDA_MAXIMA;

        // 4. moverse comprobando choques
        boolean estabaEnSuelo = enSuelo;
        float vyAntes = vy;
        colisiones.moverJugador(this);

        if (!estabaEnSuelo && enSuelo && vyAntes > 3f) {
            particulas.emitir(x + ANCHO / 2f, y + ALTO, 8, new Color(0xD8CBA8), 1.4f, 0.06f, 20);
            Sonido.aterrizar();
        }

        // 5. datos para la animación
        if (enSuelo && Math.abs(vx) > 0.2f) {
            distanciaAndada += Math.abs(vx);
            tiempoQuieto = 0;
        } else {
            tiempoQuieto++;
        }
    }

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

    public BufferedImage fotogramaActual() {
        BufferedImage img = fotogramaBase();
        return mirandoDerecha ? img : Sprites.volteada(img);
    }

    // el dibujo sin voltear; el fantasma también lo usa
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
            // parpadea de vez en cuando
            img = (tiempoQuieto % 180) > 170 ? Sprites.COCO_PARPADEO : Sprites.COCO_QUIETO;
        }
        return img;
    }

    // el sprite mide 20 px y la caja 12: esto los cuadra
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
