' Abre el juego sin la ventana negra de la consola (doble clic en este archivo).
' Compila como ejecutar.bat, pero escondido; si algo falla lo dice en una ventanita.
Option Explicit
Dim sh, fso, carpeta, titulo, codigo
Set sh = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
carpeta = fso.GetParentFolderName(WScript.ScriptFullName)
sh.CurrentDirectory = carpeta
titulo = "C" & ChrW(243) & "rdoba: C" & ChrW(243) & "digo Salvaje"

If Not fso.FolderExists(carpeta & "\out") Then fso.CreateFolder carpeta & "\out"

' compilar (0 = sin ventana, True = esperar a que acabe)
codigo = sh.Run("cmd /c javac -encoding UTF-8 -d out -sourcepath src src\cordoba\Main.java > compilar.log 2>&1", 0, True)
If codigo <> 0 Then
    MsgBox "No se ha podido compilar el juego." & vbCrLf & vbCrLf & _
           "Comprueba que tienes el JDK 17 o superior." & vbCrLf & _
           "Los detalles est" & ChrW(225) & "n en compilar.log", vbExclamation, titulo
    WScript.Quit 1
End If
If fso.FileExists(carpeta & "\compilar.log") Then fso.DeleteFile carpeta & "\compilar.log"

' javaw = Java sin consola
On Error Resume Next
sh.Run "javaw -cp out cordoba.Main", 0, False
If Err.Number <> 0 Then
    MsgBox "No se encuentra Java (javaw). Instala el JDK 17 o superior.", vbExclamation, titulo
End If
