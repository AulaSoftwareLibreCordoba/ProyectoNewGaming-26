# Atajos de teclado y ratón — Córdoba: Código Salvaje 🐊

Referencia rápida de todas las teclas del juego, sacada directamente del código
(`Game.java`, `GameMenu.java`, `CodeEditor.java` y `PantallaRanking.java`).

---

## ⭐ Lo más importante

| Atajo | Dónde | Qué hace | Por qué importa |
|---|---|---|---|
| **2** | Menú principal | Empieza directamente en el **nivel 2** | Te saltas el nivel 1 entero: el tiempo total sale mucho más bajo y la partida **también entra en el ranking** |
| **ESC** → **REINICIAR NIVEL** | Pausa | Vuelve al principio del nivel actual | **Borra el tiempo, las caídas y las compilaciones de ese nivel** (lo de niveles anteriores se mantiene) |
| **E** o **ENTER** | Jugando | Abre la consola de código (se cierra con **ENTER**, que guarda solo) | Coco se queda quieto, pero **el tiempo sigue corriendo**. Hay que programar rápido |
| **ESC** | Jugando | Pausa | Mientras estás en pausa **el tiempo no corre**: es el único sitio para pensar con calma |
| **Números** + **ENTER** | Consola de código | Escribe el valor y lo compila de golpe | Mucho más rápido que pulsar [+]/[-] varias veces (`4,5` ENTER) |
| **G** | Nivel 2 | Muestra / oculta el fantasma del récord | El fantasma enseña por dónde va el más rápido… o te distrae |
| **R** | Jugando | Vuelve a la última farola | Útil si te quedas atascado, pero **suma una caída** |
| **M** | Siempre | Silencia / activa el sonido | Imprescindible si hay varios jugando a la vez en la misma sala |

---

## 🌍 En cualquier momento

| Tecla | Acción |
|---|---|
| **M** | Activar / silenciar el sonido *(no funciona mientras escribes tu nombre, para poder usar la letra M)* |
| **F11** | Pantalla completa / volver a ventana |
| **Ratón** | Pulsar cualquier botón de la pantalla |

---

## 🏠 Menú principal

| Tecla | Acción |
|---|---|
| **ENTER** o **ESPACIO** | Jugar (primero sale la cinemática de la alcantarilla y luego el nivel 1) |
| **ENTER**, **ESPACIO** o **ESC** | Durante la cinemática: saltarla e ir directo al nivel 1 |
| **2** | Empezar directamente en el nivel 2 *(atajo para profesores)* |
| Botón **RANKING** | Ver el TOP 10 (general, nivel 1 y nivel 2) |
| Botón **SALIR** | Cerrar el juego |

---

## 🎮 Jugando

| Tecla | Acción |
|---|---|
| **A** / **←** | Moverse a la izquierda |
| **D** / **→** | Moverse a la derecha |
| **ESPACIO** / **W** / **↑** | Saltar |
| **E** o **ENTER** | Abrir la consola de código (Coco se para, **el tiempo no**) |
| **R** | Reintentar desde la última farola *(cuenta como una caída)* |
| **G** | Mostrar / ocultar el fantasma del récord *(solo en el nivel 2, si hay récord)* |
| **ESC** | Pausa (el cronómetro se detiene) |

> ⏱️ El **cronómetro** de la partida está arriba a la izquierda. En el nivel 2 también muestra el
> tiempo del nivel frente al récord: en azul si vas por delante del fantasma, en rojo si vas por detrás.

> 💡 **Ayudas al salto:** puedes saltar hasta 6 fotogramas después de salir de un borde
> (*coyote time*) y, si pulsas saltar un poco antes de tocar el suelo, el salto se guarda
> (*jump buffer*).

---

## 💻 Consola de código (tecla E o ENTER)

> ⚠️ **Con la consola abierta Coco se queda quieto, pero el cronómetro sigue corriendo.**

| Tecla | Acción |
|---|---|
| **W** / **↑** | Seleccionar la variable de arriba |
| **S** / **↓** | Seleccionar la variable de abajo |
| **A** / **←** | Bajar el valor (−0.5) |
| **D** / **→** | Subir el valor (+0.5) |
| *Mantener* **A/D** o **←/→** | Repetir el cambio rápidamente |
| **0-9**, **.** o **,** | Escribir el valor de la variable seleccionada (ej.: `4,5`) |
| **RETROCESO** | Borrar mientras escribes |
| **ENTER** | **Guardar y cerrar** la consola (incluido lo que estés escribiendo) |
| **ESC** | Cancelar lo que estás escribiendo |

| Ratón | Acción |
|---|---|
| Clic en **[-]** / **[+]** | Bajar / subir el valor de esa variable |
| *Mantener pulsado* **[-]** / **[+]** | Repetir el cambio rápidamente |
| Clic en una línea de código | Seleccionar esa variable |
| **R** o botón **RESTAURAR [R]** | Cargar los valores por defecto en el editor (se guardan al cerrar) |
| Botón **CERRAR [ENTER]** | Guardar y cerrar la consola |

> ⚠️ Los cambios se guardan solos al cerrar con **ENTER** (no hay botón de aplicar).
> Si escribes un número fuera de rango, se ajusta al límite. Todas las variables están desbloqueadas desde el principio.

---

## ⏸️ Pausa (tecla ESC)

| Tecla | Acción |
|---|---|
| **ESC** | Continuar |
| Botón **REINICIAR NIVEL** | Empezar el nivel actual desde el principio |
| Botón **MENÚ PRINCIPAL** | Volver al menú |

---

## 🏁 Nivel completado

| Tecla | Acción |
|---|---|
| **ENTER** | Continuar al siguiente nivel |
| **ESC** | Volver al menú |

---

## 🏆 Fin de la partida y ranking

**Escribir el nombre**

| Tecla | Acción |
|---|---|
| Letras, números, espacio, `-` `_` `.` | Escribir el nombre (máx. 12 caracteres) |
| **RETROCESO** | Borrar el último carácter |
| **ENTER** o botón **GUARDAR** | Guardar la puntuación en el ranking |

**Tabla TOP 10**

| Tecla | Acción |
|---|---|
| **←** / **→**, **A** / **D** o **TAB** | Cambiar de pestaña: GENERAL · NIVEL 1 · NIVEL 2 |
| Clic en una pestaña | Ir a esa pestaña |
| **ENTER** / **ESC** o botón **MENÚ** | Volver al menú principal (listo para el siguiente jugador) |

---

## 🛠️ Línea de comandos

| Comando | Acción |
|---|---|
| `ejecutar.bat` (Windows) / `./ejecutar.sh` (Linux/macOS) | Compilar y jugar |
| `java -cp out cordoba.Main --exportar-assets` | Exportar todos los gráficos como PNG a `assets/` |
