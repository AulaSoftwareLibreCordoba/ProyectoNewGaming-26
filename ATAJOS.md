# Atajos de teclado y ratón — Córdoba: Código Salvaje 🐊

Referencia rápida de todas las teclas del juego, sacada directamente del código
(`Game.java`, `GameMenu.java`, `CodeEditor.java` y `PantallaRanking.java`).

---

## ⭐ Lo más importante

| Atajo | Dónde | Qué hace | Por qué importa |
|---|---|---|---|
| **2** | Menú principal | Empieza directamente en el **nivel 2** | Te saltas el nivel 1 entero: el tiempo total sale mucho más bajo y la partida **también entra en el ranking** |
| **ESC** → **REINICIAR NIVEL** | Pausa | Vuelve al principio del nivel actual | **Borra el tiempo, las caídas y las compilaciones de ese nivel** (lo de niveles anteriores se mantiene) |
| **E** | Jugando | Abre la consola de código | **El juego NO se pausa**: Coco se sigue moviendo con las flechas y **el tiempo corre**. Hay que programar rápido |
| **ESC** | Jugando | Pausa | Mientras estás en pausa **el tiempo no corre**: es el único sitio para pensar con calma |
| **Números** + **ENTER** | Consola de código | Escribe el valor y lo compila de golpe | Mucho más rápido que pulsar [+]/[-] varias veces (`4,5` ENTER) |
| **Ctrl+S** | Consola de código | Guarda y aplica los cambios (igual que ENTER) | El atajo de "guardar" de cualquier IDE |
| **G** | Nivel 2 | Muestra / oculta el fantasma del récord | El fantasma enseña por dónde va el más rápido… o te distrae |
| **R** | Jugando | Vuelve a la última farola | Útil si te quedas atascado, pero **suma una caída** |
| **M** | Siempre | Silencia / activa el sonido | Imprescindible si hay varios jugando a la vez en la misma sala |

---

## 🌍 En cualquier momento

| Tecla | Acción |
|---|---|
| **M** | Activar / silenciar el sonido *(no funciona mientras escribes tu nombre, para poder usar la letra M)* |
| **Ratón** | Pulsar cualquier botón de la pantalla |

---

## 🏠 Menú principal

| Tecla | Acción |
|---|---|
| **ENTER** o **ESPACIO** | Jugar (empieza en el nivel 1) |
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
| **E** | Abrir la consola de código (**el juego sigue en marcha**) |
| **R** | Reintentar desde la última farola *(cuenta como una caída)* |
| **G** | Mostrar / ocultar el fantasma del récord *(solo en el nivel 2, si hay récord)* |
| **ESC** | Pausa (el cronómetro se detiene) |

> ⏱️ El **cronómetro** de la partida está arriba a la izquierda. En el nivel 2 también muestra el
> tiempo del nivel frente al récord: en azul si vas por delante del fantasma, en rojo si vas por detrás.

> 💡 **Ayudas al salto:** puedes saltar hasta 6 fotogramas después de salir de un borde
> (*coyote time*) y, si pulsas saltar un poco antes de tocar el suelo, el salto se guarda
> (*jump buffer*).

---

## 💻 Consola de código (tecla E)

> ⚠️ **El juego no se pausa con la consola abierta.** Las flechas mueven a Coco y W/A/S/D manejan la consola.

**Mover a Coco con la consola abierta**

| Tecla | Acción |
|---|---|
| **←** / **→** | Mover a Coco |
| **ESPACIO** / **↑** | Saltar |

**Manejar la consola**

| Tecla | Acción |
|---|---|
| **W** | Seleccionar la variable de arriba |
| **S** | Seleccionar la variable de abajo |
| **A** | Bajar el valor (−0.5) |
| **D** | Subir el valor (+0.5) |
| *Mantener* **A/D** | Repetir el cambio rápidamente |
| **0-9**, **.** o **,** | Escribir el valor de la variable seleccionada (ej.: `4,5`) |
| **RETROCESO** | Borrar mientras escribes |
| **ENTER** o **Ctrl+S** | **APLICAR CAMBIOS** (compilar), incluido lo que estés escribiendo |
| **ESC** | Cancelar lo que estás escribiendo (o cerrar la consola si no escribes nada) |
| **E** | Cerrar la consola |

| Ratón | Acción |
|---|---|
| Clic en **[-]** / **[+]** | Bajar / subir el valor de esa variable |
| *Mantener pulsado* **[-]** / **[+]** | Repetir el cambio rápidamente |
| Clic en una línea de código | Seleccionar esa variable |
| Botón **APLICAR [Ctrl+S]** | Compilar los valores |
| Botón **RESTAURAR** | Cargar los valores por defecto en el editor (hay que aplicarlos después) |
| Botón **CERRAR [E]** | Cerrar la consola |

> ⚠️ Los cambios no tienen efecto hasta que pulsas **APLICAR CAMBIOS** (o **ENTER**).
> Si cierras sin aplicar, el juego te avisa. Si escribes un número fuera de rango, se ajusta al límite.

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
