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
| **E** | Abrir la **consola de código** (se cierra con **ENTER**) |
| Ratón | Usar los botones de la consola y los menús |
| R | Reintentar desde la última farola |
| ESC | Pausa |
| M | Activar / silenciar el sonido |
| G | Mostrar / ocultar el fantasma del récord (nivel 2) |
| F11 | Pantalla completa / volver a ventana |

Con la consola abierta **Coco se queda quieto, pero el cronómetro sigue corriendo**. La consola usa
**W/S** o **↑/↓** para elegir variable, **A/D** o **←/→** para cambiar el valor (mantén pulsado para
repetir), los **números** para escribirlo directamente y **ENTER** para guardar y cerrar.

Todos los atajos, pantalla por pantalla, están en [ATAJOS.md](ATAJOS.md).

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

La tabla tiene tres pestañas (se cambian con **← / →** o con el ratón):

* **GENERAL**: el ranking de la competición, con el criterio de abajo.
* **NIVEL 1** y **NIVEL 2**: los mejores tiempos de cada nivel por separado (solo cuenta el tiempo).
  Las partidas que empezaron con el atajo **2** no aparecen en el nivel 1.

* **Criterio:** gana quien tarde **menos**. Si hay empate, menos caídas; después, menos compilaciones;
  y si aun así empatan, queda delante quien lo consiguió antes.
* Mientras juega, el **cronómetro** de la partida se ve arriba a la izquierda (también con la consola abierta).
* Mientras escribe el nombre, el jugador ve el puesto provisional que va a conseguir y si ha batido algún récord de nivel.
* Los resultados se guardan en `ranking.txt`, en la carpeta desde la que se ejecuta el juego, con una línea por
  partida (`nombre;fotogramas;caídas;compilaciones;fotogramasNivel1;fotogramasNivel2`). Se guardan **todas**
  las partidas, aunque solo se muestran las 10 mejores. Para empezar una competición nueva basta con borrar
  ese fichero (y `fantasma.dat`).

### El fantasma del récord

En el **nivel 2** aparece un Coco **semitransparente** con el nombre del récord encima: es la partida más
rápida de ese nivel, repetida fotograma a fotograma. El HUD muestra el tiempo del nivel frente al récord
(en rojo si vas por detrás). Con **G** se oculta o se muestra.

* Se graba la **posición** de Coco en cada fotograma (no las teclas), así el fantasma es exacto aunque el
  récord cambiara la física a mitad de partida.
* Cuando alguien bate el récord del nivel 2 y guarda su nombre, su partida pasa a ser el nuevo fantasma
  (`fantasma.dat`). Si se borra `ranking.txt`, el fantasma antiguo deja de salir.
* `ranking.txt` y `fantasma.dat` están en `.gitignore`: los récords de cada ordenador no se suben al repositorio.

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
* Los botones **[-] / [+]** (o **A/D**) cambian el valor en pasos de 0.5 **solo en el editor**.
* También se puede **escribir el valor** con los números (`4.5`, `4,5`, `13`…): sale en blanco con un
  cursor, **RETROCESO** borra y **ESC** cancela. Si se sale del rango, se ajusta al límite.
* Los cambios pendientes salen en amarillo con `// * sin guardar`.
* **ENTER** (o el botón **CERRAR [ENTER]**) **guarda solo y cierra**: "compila" copiando los valores a
  `PhysicsConfig`. No hay botón de aplicar aparte.
* **RESTAURAR** carga los valores por defecto en el editor (se guardan al cerrar).
* Todas las variables están **desbloqueadas desde el principio**.
* **Coco se queda quieto pero el cronómetro sigue corriendo**: la consola aparece arriba y se maneja con
  W/A/S/D o con las flechas. Pensar cuesta tiempo, así que hay que decidir rápido… o abrir la pausa (ESC).

Los cambios afectan de verdad al cocodrilo: `Player` lee `PhysicsConfig` en **cada fotograma**.

| Variable | Conversión interna (60 FPS) | Efecto |
|---|---|---|
| `gravedad` | `× 0.04` px/fotograma² | Menos = saltos más altos y largos, caídas lentas |
| `velocidad` | `× 0.40` px/fotograma | Desplazamiento horizontal |
| `fuerzaSalto` | `× 0.55` px/fotograma | Velocidad vertical inicial del salto |

---

## 4. Los niveles

### Nivel 1 — Los primeros pasos por Córdoba
Ribera del Guadalquivir, bajo el Puente Romano, hasta la Torre de la Calahorra.

1. Moverse y saltar pequeños huecos.
2. **El río es demasiado ancho**: abrir la consola, bajar la **gravedad** a 5.0 o menos y cerrar con ENTER.
3. **Muralla de 6 bloques**: con poca gravedad también se salta más alto.
4. Una pasarela bajo el puente se derrumba 1,25 s después de entrar → subir la **velocidad** a 6.5 o más.
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
│   │   ├── Game.java               Bucle a 60 FPS fijos, máquina de estados, cámara, escalado a la ventana
│   │   ├── GameState.java          MENU, JUGANDO, EDITOR, PAUSA, NIVEL_COMPLETADO, JUEGO_COMPLETADO, RANKING
│   │   └── Input.java              Teclado y ratón (pulsada / mantenida)
│   ├── fisica/                     Física y colisiones
│   │   ├── PhysicsConfig.java      ★ Variables físicas centralizadas y su conversión a píxeles
│   │   ├── VariableFisica.java     Una variable editable: valor, límites, paso, bloqueo
│   │   └── CollisionManager.java   Colisiones por ejes separados + detección de peligros
│   ├── entidades/
│   │   ├── Player.java             Coco: movimiento, salto, gravedad, animaciones
│   │   └── Fantasma.java           Graba, guarda y reproduce el fantasma del récord del nivel 2
│   ├── niveles/                    Definición de niveles
│   │   ├── Level.java              Clase base: mapa de tiles, objetos, farolas, decoración
│   │   ├── Level1.java             Nivel 1
│   │   ├── Level2.java             Nivel 2 (Mezquita)
│   │   ├── plataformas/
│   │   │   ├── PlataformaDinamica.java Base de plataformas no fijas
│   │   │   ├── PuenteDerrumbe.java     Suelo que se derrumba tras una cuenta atrás
│   │   │   ├── PlataformaTemporal.java Azulejos que aparecen y desaparecen
│   │   │   └── PlataformaMovil.java    Plataforma que va y viene (arrastra al jugador)
│   │   └── elementos/
│   │       ├── ObstaculoMovil.java Bola de pinchos con movimiento suave
│   │       ├── Farola.java         Punto de control
│   │       └── Decoracion.java     Macetas, palmeras, naranjos, cañas
│   ├── graficos/                   Todo lo que se dibuja
│   │   ├── GameRenderer.java       Dibuja escenario, objetos, jugador y HUD
│   │   ├── Sprites.java            Pixel art del cocodrilo (en texto), tiles y objetos
│   │   ├── Fondos.java             Fondos con parallax: Puente Romano, Mezquita, casas encaladas…
│   │   ├── Particulas.java         Efectos visuales
│   │   ├── ambiente/               Animaciones del escenario (solo visual)
│   │   │   ├── Ambiente.java       Punto de entrada: qué se dibuja tras cada capa del fondo
│   │   │   ├── Nubes.java          Nubes en 3 capas que avanzan con el viento y el parallax
│   │   │   └── Agua.java           Olas, espuma, cáusticas y destellos del río del fondo
│   │   └── luz/                    Iluminación y profundidad (solo visual)
│   │       ├── EstiloVisual.java   ★ Panel de control del arte: colores e intensidades por tema
│   │       ├── Iluminacion.java    Penumbra, luces de farolas/meta/Coco, sol, lámparas y viñeta
│   │       ├── Profundidad.java    Neblina del fondo, sombra del terreno, volumen y sombras de contacto
│   │       ├── SalaProfunda.java   Interior de la Mezquita: columnas y arcos en 3 planos de profundidad
│   │       └── SpritesLuz.java     Degradados de luz pixelados (en caché)
│   ├── interfaz/                   Menús y consola de código
│   │   ├── CodeEditor.java         ★ La consola de código (IDE simulado, en vivo)
│   │   ├── GameMenu.java           Pantallas de inicio, pausa y victoria
│   │   ├── PantallaRanking.java    Final de partida (escribir nombre) y TOP 10 (general / nivel 1 / nivel 2)
│   │   ├── Andalusi.java           Motivos andalusíes: estrellas, dovelas, arquería, alicatado y botones
│   │   └── Ui.java / Boton.java    Utilidades de interfaz
│   ├── ranking/
│   │   ├── Ranking.java            Carga, ordena y guarda las partidas en ranking.txt
│   │   └── Puntuacion.java         Una partida: nombre, tiempos (total y por nivel), caídas, compilaciones
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
      ├─ Level.actualizar()         plataformas, obstáculos, farolas
      ├─ Player.actualizar()        lee PhysicsConfig → vx, vy
      │    └─ CollisionManager.moverJugador()
      ├─ CollisionManager.tocaPeligro() → morir / reaparecer en la farola
      └─ ¿meta? → celebración → pantalla de victoria
 └─ EDITOR → CodeEditor.actualizar() → ENTER guarda los valores en PhysicsConfig y cierra
            └─ Coco y el nivel quietos, el cronómetro sigue
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

### Iluminación y profundidad
La carpeta `graficos/luz/` añade luz y sensación de profundidad **sin tocar ningún sprite ni la lógica
del juego** (solo lee el estado y pinta encima):

* **Profundidad:** neblina en las capas lejanas del fondo, sombra que proyecta el terreno, interior de
  los bloques grandes más oscuro (volumen) y sombras de contacto bajo macetas, farolas y Coco.
* **Iluminación:** resplandor del sol, halo de las farolas encendidas y de la meta, penumbra del
  atardecer y del interior de la Mezquita (con luz alrededor de Coco y lámparas colgantes) y viñeta.

* **Interior de la Mezquita:** la sala de columnas se dibuja en tres planos (`SalaProfunda`). Los
  lejanos tienen arcos más pequeños que convergen hacia el horizonte, más niebla y menos parallax; al
  fondo, una arquería en silueta contra una luz cálida. Sustituye a la capa plana `mezquita_capa3.png`.

Para cambiar el aspecto basta con tocar los valores de `EstiloVisual.java` (uno por tema).

### Animaciones del ambiente
La carpeta `graficos/ambiente/` da vida al escenario:

* **Nubes** en tres capas (lejos, medio y cerca) que avanzan con el viento a distinta velocidad.
  Las nubes que venían pintadas en el cielo del río se borran al cargar para que no queden quietas.
* **Agua**: olas que avanzan con espuma en la cresta, más oscura en lo hondo, reflejos de luz que se
  mueven por dentro y destellos en el río del fondo.

### Estética andalusí de la interfaz
Los botones de los menús (`Boton.andalusi()`) usan esquinas escalonadas como las almenas de la Mezquita,
filete de oro, dovelas rojas y blancas y estrellas de ocho puntas. El menú de inicio tiene el título en una
placa con arquería, letras que ondulan, rayos de sol, pétalos de azahar, un panel de código que se edita
solo y se maneja también con **↑/↓ + ENTER**.

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
   `Level` y usando `suelo()`, `agua()`, `pinchos()`, `farola()`, `decoracion()`…
3. ~~**Escribir el valor** con el teclado en la consola en lugar de usar [+]/[-].~~ ✅ Hecho.
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

### Tipografía de la interfaz: Pixel Operator

La interfaz utiliza **Pixel Operator** para lectura, **Pixel Operator Bold** para títulos,
estados y cabeceras, y **Pixel Operator Mono** para relojes, contadores y valores numéricos.
Los tres TTF se incorporan sin modificar desde el [paquete publicado por su autor,
Jayvee Enaguas (HarvettFox96)](https://www.dafont.com/pixel-operator.font).
La licencia **CC0 1.0 Universal** original está en `assets/fonts/PixelOperator-LICENSE.txt`.
`assets/fonts/PixelOperator-source.json` registra procedencia, versión y hashes SHA-256.
Los archivos Pixelify anteriores permanecen para facilitar la reversión, pero no se cargan.

`src/cordoba/interfaz/Ui.java` centraliza las fuentes, tamaño nativo de 16 píxeles, títulos
de 32, interlineado, colores, contornos y sombras. Las fuentes se cargan una única vez,
sin instalar nada en el sistema y sin conexión durante la partida. Al empaquetar un JAR,
incluir `assets/fonts/` en su raíz; los lanzadores existentes los leen desde la carpeta
del proyecto. Las flechas y ordinales º/ª, ausentes en esta familia, se dibujan como
símbolos pixelados; las tildes, ñ, ü y signos ¿/¡ son glifos de Pixel Operator.

**Relojes fijos:** anteriormente la consola recalculaba su posición con el ancho del texto
y el HUD recalculaba la caja según la lectura. Ahora el estado y los relojes tienen
coordenadas y contenedores fijos. `Ui.tiempo` dibuja caracteres de Pixel Operator Mono en
celdas enteras de 8 píxeles. Reserva seis celdas para minutos alineados a la derecha, dos
para segundos, dos para centésimas y dos separadores inamovibles; cubre todo el rango de
fotogramas int. Los espacios iniciales no se dibujan; la lectura sigue siendo `0:49.26`.
Esto estabiliza también los pasos a 10, 100 y más minutos, sin truncar el tiempo.
El cálculo del tiempo y el formato de los récords no se han modificado.

El nombre del nivel ocupa una fila propia; la segunda reúne reloj e indicadores. Cuando
hay fantasma, su comparación ocupa otra fila. La consola tiene mayor interlineado y salida
más alta. Se han ajustado las columnas del ranking para los nombres de doce caracteres.

Se conserva el lienzo de 640×360, sin antialiasing ni métricas fraccionarias. La presentación
usa nearest-neighbor y la escala más grande que quepa manteniendo 16:9 (no tiene por qué ser
entera), así que al maximizar o con **F11** (pantalla completa) el juego llena la pantalla.
La transformación del ratón usa esa misma escala y esos mismos márgenes.

**Comprobaciones reproducibles** (PowerShell desde la raíz, JDK 17+):

```powershell
javac -encoding UTF-8 --release 17 -d out -sourcepath src src/cordoba/Main.java
javac -encoding UTF-8 --release 17 -cp out -d .typography-check .typography-check/VerificarTipografia.java .typography-check/VerificarTemporizador.java .typography-check/VerificarVentana.java
java '-Djava.awt.headless=true' -cp 'out;.typography-check' VerificarTipografia
java '-Djava.awt.headless=true' -cp 'out;.typography-check' VerificarTemporizador
java -cp 'out;.typography-check' VerificarVentana
```

Las pruebas comprueban familia, caracteres españoles, anchuras y transformación del ratón
en seis resoluciones. Generan ocho pantallas, dos tablas completas con nombres de doce
caracteres y una muestra de `a e g m ñ 0 1 6 8`.
El test del reloj compara píxeles de 7.200 fotogramas y trece transiciones, incluyendo
9→10 segundos, 59→60, 9→10 minutos, 99→100 y el máximo int. Comprueba que los separadores,
el estado EN EJECUCIÓN y los límites de la caja no cambian de posición.
La prueba con ventana ejecuta el bucle real en menú, partida y consola a 640×360, 960×540,
1280×720 y 1000×600. Guarda capturas en `.typography-check/` y cierra la ventana al terminar.
No guarda partidas ni cambia el ranking o el fantasma existentes.

**Revertir esta segunda mejora:** los archivos previos a Pixel Operator están conservados
en `.pixel-operator-backup-20261117-104631/`; la primera copia `.typography-backup/` sigue
intacta. Estos comandos restauran los cinco archivos Java, pruebas y README de esta copia
y recompilan; también reemplazan cualquier edición posterior en esos mismos archivos:

```powershell
Copy-Item -Path '.pixel-operator-backup-20261117-104631\src\cordoba' -Destination '.\src' -Recurse -Force
Copy-Item -Path '.pixel-operator-backup-20261117-104631\.typography-check\*' -Destination '.\.typography-check' -Force
Copy-Item -LiteralPath '.pixel-operator-backup-20261117-104631\README.md' -Destination '.\README.md' -Force
.\ejecutar.bat
```

Los archivos nuevos de Pixel Operator pueden conservarse: el código anterior no los carga.
