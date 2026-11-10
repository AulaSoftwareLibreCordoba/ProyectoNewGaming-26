# Atajos de teclado y ratón — Córdoba: Código Salvaje 🐊

Referencia rápida de todas las teclas del juego, sacada directamente del código
(`Game.java`, `GameMenu.java`, `CodeEditor.java` y `PantallaRanking.java`).

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
| Botón **RANKING** | Ver el TOP 10 |
| Botón **SALIR** | Cerrar el juego |

---

## 🎮 Jugando

| Tecla | Acción |
|---|---|
| **A** / **←** | Moverse a la izquierda |
| **D** / **→** | Moverse a la derecha |
| **ESPACIO** / **W** / **↑** | Saltar |
| **E** | Abrir la consola de código (el juego se pausa) |
| **R** | Reintentar desde la última farola *(cuenta como una caída)* |
| **ESC** | Pausa |

> 💡 **Ayudas al salto:** puedes saltar hasta 6 fotogramas después de salir de un borde
> (*coyote time*) y, si pulsas saltar un poco antes de tocar el suelo, el salto se guarda
> (*jump buffer*).

---

## 💻 Consola de código (tecla E)

| Tecla | Acción |
|---|---|
| **W** / **↑** | Seleccionar la variable de arriba |
| **S** / **↓** | Seleccionar la variable de abajo |
| **A** / **←** | Bajar el valor (−0.5) |
| **D** / **→** | Subir el valor (+0.5) |
| *Mantener* **A/D** o **←/→** | Repetir el cambio rápidamente |
| **ENTER** | **APLICAR CAMBIOS** (compilar) |
| **E** / **ESC** | Cerrar la consola |

| Ratón | Acción |
|---|---|
| Clic en **[-]** / **[+]** | Bajar / subir el valor de esa variable |
| *Mantener pulsado* **[-]** / **[+]** | Repetir el cambio rápidamente |
| Clic en una línea de código | Seleccionar esa variable |
| Botón **APLICAR CAMBIOS** | Compilar los valores |
| Botón **RESTAURAR** | Cargar los valores por defecto en el editor (hay que aplicarlos después) |
| Botón **CERRAR [E]** | Cerrar la consola |

> ⚠️ Los cambios no tienen efecto hasta que pulsas **APLICAR CAMBIOS** (o **ENTER**).
> Si cierras sin aplicar, el juego te avisa.

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
| **ENTER** / **ESC** o botón **MENÚ** | Volver al menú principal (listo para el siguiente jugador) |

---

## 🛠️ Línea de comandos

| Comando | Acción |
|---|---|
| `ejecutar.bat` (Windows) / `./ejecutar.sh` (Linux/macOS) | Compilar y jugar |
| `java -cp out cordoba.Main --exportar-assets` | Exportar todos los gráficos como PNG a `assets/` |
