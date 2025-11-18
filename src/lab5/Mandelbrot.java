package lab5;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mandelbrot extends JFrame {

    private final int MAX_ITER;
    private final double ZOOM = 150;
    private BufferedImage I;

    public Mandelbrot() {
        this(570);
    }

    public Mandelbrot(int maxIter) {
        super("Mandelbrot Set");
        this.MAX_ITER = maxIter;
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                computeAndSetPixel(x, y);
            }
        }
    }

    public Mandelbrot(int numThreads, int numTasks, int maxIter) {
        super("Mandelbrot Set");
        this.MAX_ITER = maxIter;
        setBounds(100, 100, 800, 600);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        I = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int width = getWidth();
        int height = getHeight();
        int totalPixels = width * height;

        if (numTasks == totalPixels) {
            // Each task is one pixel
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int my_x = x;
                    int my_y = y;
                    executor.execute(() -> {computeAndSetPixel(my_x, my_y);});
                }
            }
        } else {
            // Divide work into numTasks chunks
            int pixelsPerTask = totalPixels / numTasks;
            int remainingPixels = totalPixels % numTasks;

            int currentPixel = 0;
            for (int task = 0; task < numTasks; task++) {
                int startPixel = currentPixel;
                int endPixel = startPixel + pixelsPerTask + (task < remainingPixels ? 1 : 0);
                currentPixel = endPixel;

                executor.execute(() -> {
                    for (int pixel = startPixel; pixel < endPixel; pixel++) {
                        int x = pixel % width;
                        int y = pixel / width;
                        computeAndSetPixel(x, y);
                    }
                });
            }
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        I.flush();
    }

    private void computeAndSetPixel(int x, int y) {
        double zx = 0, zy = 0;
        double cX = (x - 400) / ZOOM;
        double cY = (y - 300) / ZOOM;
        int iter = MAX_ITER;

        while (zx * zx + zy * zy < 4 && iter > 0) {
            double tmp = zx * zx - zy * zy + cX;
            zy = 2.0 * zx * zy + cY;
            zx = tmp;
            iter--;
        }

        I.setRGB(x, y, iter | (iter << 8));
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(I, 0, 0, this);
    }
}