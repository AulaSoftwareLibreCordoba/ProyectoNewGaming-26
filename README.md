# Córdoba: Código Salvaje 🐊

Demo de un videojuego de plataformas 2D en **Java + Swing** (sin librerías externas) cuyo núcleo es
**modificar el código del propio juego** para cambiar sus físicas y superar los niveles.

Coco, un pequeño cocodrilo cordobés, recorre la ribera del Guadalquivir junto al Puente Romano y el casco
histórico hasta la Mezquita-Catedral. Para avanzar tendrás que abrir la consola de código y reprogramar
`gravedad`, `velocidad` y `fuerzaSalto`.

> 🎓 **Proyecto educativo de Software Libre**
>
> Este proyecto se ha llevado a cabo en el marco del **Aula de Software Libre de Córdoba FP** y se
> presenta como **proyecto de alumnos de 2º de DAM** (Desarrollo de Aplicaciones Multiplataforma),
> en este caso del **CES Lope de Vega** (Córdoba).

---

## 1. Cómo ejecutarlo

Requisito: **JDK 17 o superior**.

### Desde la terminal
```bash
# Windows
ejecutar.bat

# Linux / macOS
./ejecutar.sh
```
o a mano:
```bash
javac -encoding UTF-8 -d out -sourcepath src src/cordoba/Main.java
java -cp out cordoba.Main
```

### Desde IntelliJ IDEA
1. `File > Open…` y selecciona la carpeta del proyecto.
2. Clic derecho sobre `src` → `Mark Directory as > Sources Root` (si no lo hace solo).
3. Configura el SDK del proyecto a Java 17+ (`File > Project Structure > Project`).
4. Abre `src/cordoba/Main.java` y pulsa ▶. **Working directory** = carpeta del proyecto (es el valor por defecto).

### Desde Eclipse
1. `File > Import… > General > Existing Projects into Workspace` y elige la carpeta
   (ya incluye `.project` y `.classpath`).
2. Clic derecho en `src/cordoba/Main.java` → `Run As > Java Application`.

> Si el texto sale con caracteres raros, configura la codificación del proyecto en **UTF-8**.

---

## 2. Controles

| Tecla | Acción |
|---|---|
| A / D o ← / → | Mover a Coco |
| ESPACIO (o W / ↑) | Saltar |
| **E** | Abrir / cerrar la **consola de código** |
| Ratón | Usar los botones de la consola y los menús |
| R | Reintentar desde la última farola |
| ESC | Pausa (o cerrar la consola) |
| M | Activar / silenciar el sonido |

Dentro de la consola también puedes usar el teclado: **W/S** (o ↑/↓) para elegir variable, **A/D** (o ←/→)
para cambiar el valor (mantén pulsado para repetir) y **ENTER** para aplicar.

> Atajo para profesores: en el menú principal pulsa **2** para ir directamente al nivel 2.

---

## 2.1. Modo competición: ranking TOP 10

Al terminar la partida, el jugador **escribe su nombre** (máx. 12 caracteres) y su resultado se guarda
en el ranking. La tabla con los **10 mejores** sale justo después y también se puede consultar desde el
botón **RANKING** del menú principal.

| Columna | Significado |
|---|---|
| `#` | Puesto (oro, plata y bronce para el podio) |
| `NOMBRE` | Nombre del jugador |
| `TIEMPO` | Tiempo total de la partida (`m:ss.cc`) |
| `CAÍDAS` | Veces que ha caído o se ha pinchado |
| `COMPILACIONES` | Veces que ha aplicado cambios en la consola de código |

* **Criterio:** gana quien tarde **menos**. Si hay empate, menos caídas; después, menos compilaciones;
  y si aun así empatan, queda delante quien lo consiguió antes.
* Mientras escribe el nombre, el jugador ve el puesto provisional que va a conseguir.
* Los resultados se guardan en `ranking.txt`, en la carpeta desde la que se ejecuta el juego, con una línea por
  partida (`nombre;fotogramas;caídas;compilaciones`). Se guardan **todas** las partidas, aunque solo se
  muestran las 10 mejores. Para empezar una competición nueva basta con borrar ese fichero.
* `ranking.txt` está en `.gitignore`: los récords de cada ordenador no se suben al repositorio.

---

## 3. La mecánica principal: la consola de código

Al pulsar **E** se abre un pequeño IDE que muestra `PhysicsConfig.java`:

```java
// CONFIGURACIÓN FÍSICA DEL NIVEL
public class PhysicsConfig {
    // Atracción hacia el suelo. Menos = flotas más [3.0 - 20.0]
    float gravedad = 9.8f;            [-] [+]
    // Rapidez al andar. Más = corres más lejos [2.0 - 10.0]
    float velocidad = 5.0f;           [-] [+]
    // Impulso del salto. Más = saltas más alto [8.0 - 16.0]
    float fuerzaSalto = 12.0f;        [-] [+]
}
```

* Cada variable muestra su nombre, su valor, una explicación y su rango permitido.
* Los botones **[-] / [+]** cambian el valor (en pasos de 0.5) **solo en el editor**.
* Los cambios pendientes salen en amarillo con `// * sin aplicar`.
* **APLICAR CAMBIOS** "compila": copia los valores a `PhysicsConfig` y la salida muestra
  `BUILD SUCCESSFUL` con la lista de cambios. Si cierras sin aplicar, el juego te avisa de que no se ha compilado nada.
* **RESTAURAR** carga los valores por defecto en el editor.
* Las variables `final` están **bloqueadas** (en el tutorial se van desbloqueando).
* Mientras la consola está abierta el juego queda en pausa (como un *breakpoint*), así que puedes
  cambiar la física **en mitad de un salto**.

Los cambios afectan de verdad al cocodrilo: `Player` lee `PhysicsConfig` en **cada fotograma**.

| Variable | Conversión interna (60 FPS) | Efecto |
|---|---|---|
| `gravedad` | `× 0.04` px/fotograma² | Menos = saltos más altos y largos, caídas lentas |
| `velocidad` | `× 0.40` px/fotograma | Desplazamiento horizontal |
| `fuerzaSalto` | `× 0.55` px/fotograma | Velocidad vertical inicial del salto |

---

## 4. Los niveles

### Nivel 1 — Los primeros pasos por Córdoba (tutorial)
Ribera del Guadalquivir, bajo el Puente Romano, hasta la Torre de la Calahorra.

1. Moverse y saltar pequeños huecos.
2. **El río es demasiado ancho**: abrir la consola, bajar la **gravedad** a 5.0 o menos y aplicar.
3. **Muralla de 6 bloques**: con poca gravedad también se salta más alto.
4. Se desbloquea **velocidad**: una pasarela bajo el puente se derrumba 1,25 s después de entrar → subirla a 6.5 o más.
5. Pinchos y un último salto hasta la Calahorra.

### Nivel 2 — El desafío de la Mezquita
Calles encaladas con macetas y el interior de la Mezquita-Catedral. Ya no se dan valores exactos.

| Zona | Reto | Qué hay que programar |
|---|---|---|
| Muro de 8 bloques | Plataforma muy elevada | Bajar `gravedad` **y/o** subir `fuerzaSalto` (p. ej. 4.0/12 o 5.5/14) |
| Pasillo con pinchos en el techo | Si saltas muy alto te pinchas | Volver a valores moderados (la solución del muro aquí mata) |
| Pozo con corriente de aire | El aire empuja con fuerza 12.5 y hay bolas de pinchos | **Subir** `gravedad` por encima de 12.5 (mejor 15–20) para caer rápido |
| Galería que se hunde | 18 bloques que caen en ~1,6 s | `velocidad` ≥ 7.5 aprox. |
| Columnas estrechas | Huecos de 3 bloques entre columnas de 2 | **Bajar** `velocidad` (4–6) y gravedad normal para no pasarse |
| Azulejos y plataforma móvil | Todo sigue un ritmo de 2,5 s | Observar el ritmo y saltar en el momento justo |

Todos los retos se han comprobado con simulaciones automáticas: se superan con los valores indicados y
**no** se superan con los valores por defecto (salvo las columnas, que se diseñaron para resolverse
precisamente volviendo a valores "normales").

---

## 5. Arquitectura

```text
CordobaCodigoSalvaje/
├── src/cordoba/
│   ├── Main.java                   Punto de entrada; crea la ventana (y exporta assets con --exportar-assets)
│   ├── nucleo/                     Bucle principal y entrada
│   │   ├── Game.java               Bucle a 60 FPS fijos, máquina de estados, cámara, escalado pixel-perfect
│   │   ├── GameState.java          MENU, JUGANDO, EDITOR, PAUSA, NIVEL_COMPLETADO, JUEGO_COMPLETADO, RANKING
│   │   └── Input.java              Teclado y ratón (pulsada / mantenida)
│   ├── fisica/                     Física y colisiones
│   │   ├── PhysicsConfig.java      ★ Variables físicas centralizadas y su conversión a píxeles
│   │   ├── VariableFisica.java     Una variable editable: valor, límites, paso, bloqueo
│   │   └── CollisionManager.java   Colisiones por ejes separados + detección de peligros
│   ├── entidades/
│   │   └── Player.java             Coco: movimiento, salto, gravedad, animaciones
│   ├── niveles/                    Definición de niveles
│   │   ├── Level.java              Clase base: mapa de tiles, objetos, farolas, carteles, eventos
│   │   ├── Level1.java             Nivel 1 (tutorial)
│   │   ├── Level2.java             Nivel 2 (Mezquita)
│   │   ├── plataformas/
│   │   │   ├── PlataformaDinamica.java Base de plataformas no fijas
│   │   │   ├── PuenteDerrumbe.java     Suelo que se derrumba tras una cuenta atrás
│   │   │   ├── PlataformaTemporal.java Azulejos que aparecen y desaparecen
│   │   │   └── PlataformaMovil.java    Plataforma que va y viene (arrastra al jugador)
│   │   └── elementos/
│   │       ├── ObstaculoMovil.java Bola de pinchos con movimiento suave
│   │       ├── Farola.java         Punto de control
│   │       ├── Cartel.java         Mensaje de tutorial (+ acción opcional)
│   │       └── Decoracion.java     Macetas, palmeras, naranjos, cañas
│   ├── graficos/                   Todo lo que se dibuja
│   │   ├── GameRenderer.java       Dibuja escenario, objetos, jugador y HUD
│   │   ├── Sprites.java            Pixel art del cocodrilo (en texto), tiles y objetos
│   │   ├── Fondos.java             Fondos con parallax: Puente Romano, Mezquita, casas encaladas…
│   │   └── Particulas.java         Efectos visuales
│   ├── interfaz/                   Menús y consola de código
│   │   ├── CodeEditor.java         ★ La consola de código (IDE simulado)
│   │   ├── GameMenu.java           Pantallas de inicio, pausa y victoria
│   │   ├── PantallaRanking.java    Final de partida (escribir nombre) y tabla TOP 10
│   │   └── Ui.java / Boton.java    Utilidades de interfaz
│   ├── ranking/
│   │   ├── Ranking.java            Carga, ordena y guarda las partidas en ranking.txt
│   │   └── Puntuacion.java         Una partida: nombre, tiempo, caídas, compilaciones
│   └── audio/
│       └── Sonido.java             Efectos chiptune sintetizados (sin archivos de audio)
├── assets/                     PNG exportados (player, tiles, backgrounds, interface)
├── ejecutar.bat / ejecutar.sh
└── README.md
```

### Flujo de un fotograma
```
Game.actualizar()
 └─ JUGANDO → Game.paso()
      ├─ Level.actualizar()         plataformas, obstáculos, farolas, carteles
      ├─ Player.actualizar()        lee PhysicsConfig → vx, vy
      │    └─ CollisionManager.moverJugador()
      ├─ CollisionManager.tocaPeligro() → morir / reaparecer en la farola
      └─ ¿meta? → celebración → pantalla de victoria
 └─ EDITOR → CodeEditor.actualizar() → APLICAR copia los valores a PhysicsConfig
Game.renderizar() → imagen 640x360 → escalado sin suavizado a la ventana
```

### Los gráficos
Todo el pixel art se genera **por código** (no hace falta ningún archivo), pero `Sprites` y `Fondos`
buscan primero un PNG en `assets/`. Los PNG incluidos se generaron con:
```bash
java -cp out cordoba.Main --exportar-assets
```
Puedes editarlos con Aseprite, Piskel o GIMP y el juego usará tu versión.
**Ojo:** si modificas el dibujo en texto de `Sprites.java`, borra el PNG correspondiente
(o la carpeta `assets/`) para que se use el nuevo.

El cocodrilo está dibujado así en `Sprites.java` (cada letra es un color de la paleta):
```java
"......ooooo.........",
".....oWWWWWo........",
".....oWWWKKo........",
"....ooWWWKKoo.......",
"...oGGoooooGGoooo...",
```

---

## 6. Ideas para ampliarlo (ejercicios)

1. **Nueva variable** `friccion`: añádela al array de `PhysicsConfig` (la consola la mostrará sola) y
   úsala en `Player` en lugar de `CONTROL_SUELO`.
2. **Nuevo nivel** `Level3` (p. ej. el Patio de los Naranjos o la Calleja de las Flores) heredando de
   `Level` y usando `suelo()`, `agua()`, `pinchos()`, `farola()`, `cartel()`…
3. **Escribir el valor** con el teclado en la consola en lugar de usar [+]/[-].
4. ~~Guardar récords (tiempo y caídas) en un fichero.~~ ✅ Hecho: ver *Modo competición*.
5. Un enemigo que patrulle usando su propia `PhysicsConfig`.
6. Variables "trampa" que el jugador no debería tocar (p. ej. `tamañoCoco`).

---

## 7. Créditos y contexto

* **Iniciativa:** Aula de Software Libre de Córdoba FP.
* **Autoría:** alumnado de **2º de DAM** (Desarrollo de Aplicaciones Multiplataforma).
* **Centro:** CES Lope de Vega (Córdoba).
* **Finalidad:** proyecto de alumnos para su presentación dentro del programa del Aula de Software Libre.

Al ser un proyecto de software libre, cualquiera puede estudiar el código, modificarlo y proponer mejoras
mediante *issues* o *pull requests* en este repositorio.
