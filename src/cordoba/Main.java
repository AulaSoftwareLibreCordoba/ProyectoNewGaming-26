package cordoba;

import cordoba.graficos.Sprites;
import cordoba.nucleo.Game;

import javax.swing.*;
import java.awt.*;

// Arranca el juego. Con --exportar-assets guarda los gráficos como PNG en assets/ para poder editarlos.
public class Main {

    public static void main(String[] args) throws Exception {
        // que Windows/macOS no reescalen la ventana y emborronen los píxeles
        System.setProperty("sun.java2d.uiScale", "1");

        if (args.length > 0 && args[0].equals("--exportar-assets")) {
            Sprites.exportarAssets();
            System.out.println("Assets exportados en la carpeta assets/");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("Córdoba: Código Salvaje");
            Game juego = new Game();

            // la ventana más grande que quepa siendo múltiplo exacto de 640x360
            Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
            int escala = Math.max(1, Math.min((int) (pantalla.width * 0.9 / Game.ANCHO),
                                              (int) (pantalla.height * 0.85 / Game.ALTO)));
            juego.setPreferredSize(new Dimension(Game.ANCHO * escala, Game.ALTO * escala));

            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.add(juego);
            ventana.pack();
            ventana.setMinimumSize(new Dimension(660, 400));
            ventana.setLocationRelativeTo(null);
            ventana.setIconImage(Sprites.COCO_QUIETO);
            ventana.setVisible(true);

            // F11: pantalla completa de verdad (sin bordes ni barra de Windows)
            juego.setAlPantallaCompleta(() -> {
                Rectangle monitor = ventana.getGraphicsConfiguration().getBounds();
                boolean completa = !ventana.isUndecorated();
                ventana.dispose();   // hay que cerrarla para poder quitarle el borde
                ventana.setUndecorated(completa);
                if (completa) {
                    ventana.setBounds(monitor);   // sin bordes y del tamaño del monitor
                } else {
                    ventana.pack();
                    ventana.setLocationRelativeTo(null);
                }
                ventana.setVisible(true);
                juego.requestFocus();
            });
            juego.setAlSalir(() -> {
                ventana.dispose();
                System.exit(0);
            });
            juego.iniciar();
        });
    }
}
