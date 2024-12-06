import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImagePixelPainter extends JPanel {
    private BufferedImage image;

    public BufferedImagePixelPainter() {
        // Crear un BufferedImage de 200x200 píxeles
        image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        // Llenar la imagen con un color de fondo (opcional)
        fillBackground(Color.WHITE);

        // Dibujar píxeles individuales
        drawPixel(50, 50, Color.RED);   // Píxel rojo en (50, 50)
        drawPixel(100, 100, Color.BLUE); // Píxel azul en (100, 100)

        // Crear un patrón o línea (ejemplo adicional)
        for (int x = 0; x < 200; x++) {
            drawPixel(x, x, Color.GREEN); // Línea diagonal verde
        }
    }

    private void fillBackground(Color color) {
        int rgb = color.getRGB();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, rgb);
            }
        }
    }

    private void drawPixel(int x, int y, Color color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, color.getRGB());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dibujar el BufferedImage en el panel
        g.drawImage(image, 0, 0, null);
    }

    public static void main(String[] args) {
        // Crear el marco de la ventana
        JFrame frame = new JFrame("BufferedImage Pixel Painter");
        BufferedImagePixelPainter panel = new BufferedImagePixelPainter();

        // Configuración del marco
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(220, 240); // Tamaño ligeramente mayor para incluir los bordes
        frame.add(panel); // Añadir el panel al marco
        frame.setVisible(true); // Mostrar la ventana
    }
}
