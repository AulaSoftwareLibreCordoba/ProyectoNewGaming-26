package cordoba;

import cordoba.graficos.Sprites;
import cordoba.nucleo.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// Arranca el juego. Con --exportar-assets guarda los gráficos en assets/.
public class Main {

    public static void main(String[] args) throws Exception {
        // que Windows/macOS no reescalen la ventana y emborronen los píxeles
        System.setProperty("sun.java2d.uiScale", "1");
        // el audio tarda ~1 s en despertar: se abre ya, mientras se crea la ventana
        cordoba.audio.Sonido.precalentar();

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
            ventana.setIconImages(iconos());
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

    // icono de la ventana y de la barra de tareas: la cara de Coco (assets/interface/icono.png) en varios
    // tamaños para que Windows elija el que mejor quede; si no está, Coco en pixel art
    private static List<Image> iconos() {
        List<Image> lista = new ArrayList<>();
        BufferedImage grande = Sprites.leerPng("interface/icono");
        BufferedImage pequeno = Sprites.leerPng("interface/icono_pequeno");   // más de cerca y con marco fino
        if (grande == null) {
            lista.add(Sprites.COCO_QUIETO);
            return lista;
        }
        if (pequeno == null) pequeno = grande;
        for (int lado : new int[] { 16, 20, 24, 32, 40, 48, 64, 128, 256 }) {
            BufferedImage portada = lado <= 32 ? pequeno : grande;
            BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            // reducir a la mitad varias veces queda mucho más nítido que de golpe
            Image paso = portada;
            int w = portada.getWidth();
            while (w / 2 >= lado * 2) {
                w /= 2;
                BufferedImage mitad = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gm = mitad.createGraphics();
                gm.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gm.drawImage(paso, 0, 0, w, w, null);
                gm.dispose();
                paso = mitad;
            }
            g.drawImage(paso, 0, 0, lado, lado, null);
            g.dispose();
            lista.add(img);
        }
        return lista;
    }
}
