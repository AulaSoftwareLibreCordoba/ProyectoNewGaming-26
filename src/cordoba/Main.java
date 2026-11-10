package cordoba;

import cordoba.graficos.Sprites;
import cordoba.nucleo.Game;

import javax.swing.*;
import java.awt.*;

/**
 * Punto de entrada de «Córdoba: Código Salvaje».
 *
 * Ejecutar sin argumentos para jugar.
 * Con el argumento --exportar-assets se guardan todos los gráficos como PNG en
 * la carpeta assets/ (después el juego los cargará desde ahí y podréis editarlos).
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Evita que Windows/macOS reescalen la ventana y emborronen los píxeles
        System.setProperty("sun.java2d.uiScale", "1");

        if (args.length > 0 && args[0].equals("--exportar-assets")) {
            Sprites.exportarAssets();
            System.out.println("Assets exportados en la carpeta assets/");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame ventana = new JFrame("Córdoba: Código Salvaje");
            Game juego = new Game();

            // Tamaño de ventana: el mayor múltiplo entero de 640x360 que quepa en pantalla
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

            juego.setAlSalir(() -> {
                ventana.dispose();
                System.exit(0);
            });
            juego.iniciar();
        });
    }
}
