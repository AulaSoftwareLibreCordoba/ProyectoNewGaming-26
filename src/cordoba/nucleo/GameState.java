package cordoba.nucleo;

// Estados del juego: Game mira el estado para saber qué actualizar y qué pintar.
public enum GameState {
    MENU,               // pantalla de inicio
    JUGANDO,            // jugando
    EDITOR,             // consola de código abierta (el juego sigue)
    PAUSA,              // pausa (ESC)
    NIVEL_COMPLETADO,   // victoria entre niveles
    JUEGO_COMPLETADO,   // final: escribir el nombre
    RANKING             // TOP 10
}
