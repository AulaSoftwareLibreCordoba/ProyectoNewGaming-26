package cordoba.nucleo;

/**
 * Estados posibles del juego. Game decide qué actualizar y qué dibujar
 * según el estado actual (patrón "máquina de estados").
 */
public enum GameState {
    MENU,               // pantalla de inicio
    JUGANDO,            // partida en marcha
    EDITOR,             // consola de código abierta (juego en "breakpoint")
    PAUSA,              // menú de pausa (ESC)
    NIVEL_COMPLETADO,   // pantalla de victoria entre niveles
    JUEGO_COMPLETADO,   // pantalla final: escribir el nombre para el ranking
    RANKING             // tabla con el TOP 10
}
