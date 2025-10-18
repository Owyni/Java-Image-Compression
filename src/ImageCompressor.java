import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class ImageCompressor {

    private BufferedImage originalImage;
    private int width;
    private int height;
    // Lista de píxeles: [R, G, B]
    private ArrayList<int[]> pixelPoints; 

    // Constructor para cargar la imagen y aplanarla
    public ImageCompressor(String filePath) throws IOException {
        originalImage = ImageIO.read(new File(filePath));
        if (originalImage == null) {
            throw new IOException("Error al leer la imagen. Archivo no encontrado o formato inválido.");
        }
        width = originalImage.getWidth();
        height = originalImage.getHeight();
        pixelPoints = new ArrayList<>();
        
        // Simulación de 'read_image' y aplanado (parte de 'initialize_means' en Python)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = originalImage.getRGB(x, y);
                // Extrae los componentes R, G, B (0-255)
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                // Almacena el píxel [R, G, B]. No escalamos a [0, 1] para simplicidad.
                pixelPoints.add(new int[]{red, green, blue}); 
            }
        }
    }
    
    // Simula 'initialize_means'
    public double[][] initializeMeans(int clusters) {
        Random random = new Random();
        double[][] means = new double[clusters][3]; // [clusters] filas, 3 columnas (R, G, B)

        // Inicialización aleatoria (seleccionando puntos de la imagen)
        for (int i = 0; i < clusters; i++) {
            int randomIndex = random.nextInt(pixelPoints.size());
            int[] pixel = pixelPoints.get(randomIndex);
            
            // Asigna los valores RGB del píxel aleatorio como el centroide inicial
            means[i][0] = pixel[0]; // R
            means[i][1] = pixel[1]; // G
            means[i][2] = pixel[2]; // B
        }
        return means;
    }

    // Simula la función 'distance' (Distancia euclidiana en 3D para R, G, B)
    private double calculateDistance(int[] pixel, double[] centroid) {
        double distSq = 0;
        for (int i = 0; i < 3; i++) {
            // El código Python original usa solo x e y (dos dimensiones),
            // aquí usamos las 3 dimensiones (R, G, B) que es lo correcto para el color.
            distSq += Math.pow(pixel[i] - centroid[i], 2);
        }
        return Math.sqrt(distSq);
    }

    // Simula la función 'k_means'
    public int[] runKMeans(double[][] initialMeans, int clusters, int iterations) {
        double[][] means = initialMeans;
        int[] index = new int[pixelPoints.size()]; // Índice de clúster para cada píxel

        for (int iter = 0; iter < iterations; iter++) {
            // PASO 1: ASIGNACIÓN (Asigna píxeles al centroide más cercano)
            for (int j = 0; j < pixelPoints.size(); j++) {
                int[] pixel = pixelPoints.get(j);
                double minDistance = Double.MAX_VALUE;
                int closestCluster = -1;

                for (int k = 0; k < clusters; k++) {
                    double dist = calculateDistance(pixel, means[k]);
                    if (dist < minDistance) {
                        minDistance = dist;
                        closestCluster = k;
                    }
                }
                index[j] = closestCluster;
            }

            // PASO 2: ACTUALIZACIÓN (Recalcula los centroides)
            double[][] newMeansSum = new double[clusters][3];
            int[] counts = new int[clusters];

            for (int j = 0; j < pixelPoints.size(); j++) {
                int clusterId = index[j];
                int[] pixel = pixelPoints.get(j);
                
                newMeansSum[clusterId][0] += pixel[0]; 
                newMeansSum[clusterId][1] += pixel[1]; 
                newMeansSum[clusterId][2] += pixel[2]; 
                counts[clusterId]++;
            }

            // Calcula el promedio
            for (int k = 0; k < clusters; k++) {
                if (counts[k] > 0) {
                    means[k][0] = newMeansSum[k][0] / counts[k];
                    means[k][1] = newMeansSum[k][1] / counts[k];
                    means[k][2] = newMeansSum[k][2] / counts[k];
                } 
            }
        }
        return index;
    }

    // Simula la función 'compress_image'
    public void compressAndSaveImage(int[] index, double[][] finalMeans, int clusters, String outputFileName) throws IOException {
        
        // Prepara los colores finales redondeados
        int[][] finalColors = new int[clusters][3];
        for (int i = 0; i < clusters; i++) {
            finalColors[i][0] = (int) Math.round(finalMeans[i][0]);
            finalColors[i][1] = (int) Math.round(finalMeans[i][1]);
            finalColors[i][2] = (int) Math.round(finalMeans[i][2]);
        }
        
        // Reconstruye la imagen
        BufferedImage compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int flatIndex = y * width + x; 
                int clusterId = index[flatIndex];
                
                int r = finalColors[clusterId][0];
                int g = finalColors[clusterId][1];
                int b = finalColors[clusterId][2];
                
                // Combina R, G, B en el valor RGB de Java: (R << 16) | (G << 8) | B
                int rgb = (r << 16) | (g << 8) | b;
                compressedImage.setRGB(x, y, rgb);
            }
        }

        // Guarda la imagen
        ImageIO.write(compressedImage, "png", new File(outputFileName));
        System.out.println("Imagen comprimida guardada como: " + outputFileName);
    }

    public static void main(String[] args) {
        

        //RUTA DE LA IMAGEN A COMPRIMIR
        String inputPath = "Imagen.png";



        int clusters = 16;
        int iterations = 10; 
        
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Ingrese el número de colores en la imagen comprimida. Por defecto = 16");
            if (scanner.hasNextInt()) {
                clusters = scanner.nextInt();
            }

            // 1. Cargar y Aplanar (read_image)
            ImageCompressor compressor = new ImageCompressor(inputPath);
            
            // 2. Inicializar Centroides (initialize_means)
            double[][] initialMeans = compressor.initializeMeans(clusters);
            
            // 3. Ejecutar K-means (k_means)
            int[] finalIndex = compressor.runKMeans(initialMeans, clusters, iterations);
            
            // 4. Comprimir y Guardar (compress_image)
            String outputPath = "compressed_" + clusters + "_colors.png";
            compressor.compressAndSaveImage(finalIndex, initialMeans, clusters, outputPath);
            
        } catch (IOException e) {
            System.err.println("Error de I/O: Asegúrate de que '" + inputPath + "' exista en el directorio del proyecto.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ocurrió un error general.");
            e.printStackTrace();
        }
    }
}