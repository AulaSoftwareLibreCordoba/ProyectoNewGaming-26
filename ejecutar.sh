#!/bin/sh
# Compila y ejecuta Córdoba: Código Salvaje (requiere JDK 17+)
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -encoding UTF-8 -d out -sourcepath src src/cordoba/Main.java || { echo "Error de compilación. ¿Tienes el JDK 17+?"; exit 1; }
java -cp out cordoba.Main
