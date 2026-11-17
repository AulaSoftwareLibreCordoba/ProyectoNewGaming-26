package cordoba.nucleo;

// Estados del juego: Game mira el estado para saber qué actualizar y qué pintar.
public enum GameState {
    MENU,               // pantalla de inicio
    INTRO,              // cinemática: Coco sale de la alcantarilla
    JUGANDO,            // jugando
    EDITOR,             // consola abierta (Coco quieto, el tiempo sigue)
    PAUSA,              // pausa (ESC)
    NIVEL_COMPLETADO,   // victoria entre niveles
    JUEGO_COMPLETADO,   // final: escribir el nombre
    RANKING             // TOP 10
}
