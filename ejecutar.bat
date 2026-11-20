@echo off
REM Compila y ejecuta Cordoba: Codigo Salvaje (requiere JDK 17+)
REM Para abrirlo sin ninguna consola, haz doble clic en jugar.vbs
cd /d "%~dp0"
if not exist out mkdir out
javac -encoding UTF-8 -d out -sourcepath src src\cordoba\Main.java
if errorlevel 1 (
  echo Error de compilacion. Comprueba que tienes el JDK 17 o superior.
  pause
  exit /b 1
)
REM javaw no usa consola; start la suelta para que esta ventana se cierre ya
start "" javaw -cp out cordoba.Main
