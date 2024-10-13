import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

public class Imagen {

    byte[] header = new byte[54];
    byte[][][] imagen;
    int alto, ancho; // en pixeles
    int padding;
    String nombre;

    String[] registros;

    /***
     * Método para crear una matriz imagen a partir de un archivo.
     * 
     * @param input: nombre del archivo. El formato debe ser BMP de 24 bits de bit
     *               depth
     * @pos la matriz imagen tiene los valores correspondientes a la imagen
     *      almacenada en el archivo.
     */
    public Imagen(String input) {

        nombre = new String(input);
        try {
            FileInputStream fis = new FileInputStream(nombre);
            fis.read(header);

            // Extraer el ancho y alto de la imagen desde la cabecera
            // Almacenados en little endian
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16) |
                    ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) |
                    ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);

            System.out.println("Ancho: " + ancho + " px, Alto: " + alto + " px");
            imagen = new byte[alto][ancho][3];

            int rowSizeSinPadding = ancho * 3;
            // El tamaño de la fila debe ser múltiplo de 4 bytes
            padding = (4 - (rowSizeSinPadding % 4)) % 4;

            // Leer y modificar los datos de los píxeles
            // (en formato RGB, pero almacenados en orden BGR)
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    // Leer los 3 bytes del píxel (B, G, R)
                    fis.read(pixel);
                    // imagen[i][j] = new Color();
                    imagen[i][j][0] = pixel[0];
                    imagen[i][j][1] = pixel[1];
                    imagen[i][j][2] = pixel[2];
                }
                fis.skip(padding);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para esconder un valor en una matriz imagen.
     * 
     * @param contador: contador de bytes escritos en la matriz
     * 
     * @param valor:    valor que se quiere esconder
     * @param numbits:  longitud (en bits) del valor
     * @pre la matriz imagen debe haber sido inicializada con una imagen
     * @pos los bits recibidos como parámetro (en valor) están escondido en la
     *      imagen.
     * 
     */
    private void escribirBits(int contador, int valor, int numbits) {
        // la matriz tiene ancho pixel de ancho (num pixeles por fila)
        // Cada pixel de la matriz corresponde a 3 Bytes.
        // En cada pixel es posible esconder 3 bits (en cada byte de los componentes se
        // esconde un bit)
        int bytesPorFila = ancho * 3;// ancho de la imagen en bytes
        int mascara;
        for (int i = 0; i < numbits; i++) {
            // i: i-ésimo bit del valor que se va a esconder
            // 8*pos: por cada byte debemos esconder 8 bits
            // 8*pos + i indica el byte que se debe modificar

            int fila = (8 * contador + i) / bytesPorFila;
            // Cada posición de la matriz agrupa 3 bytes (RGB).
            int col = ((8 * contador + i) % bytesPorFila) / 3;
            int color = ((8 * contador + i) % bytesPorFila) % 3;

            mascara = valor >> i;
            mascara = mascara & 1;
            imagen[fila][col][color] = (byte) ((imagen[fila][col][color] & 0xFE) | mascara);
        }
    }

    /**
     * Método para esconder un mensaje en una matriz imagen.
     * 
     * @param mensaje:  Mensaje a esconder
     * @param longitud: longitud del mensaje
     * @pre la matriz imagen debe haber sido inicializada con una imagen
     * @pre la longitud del mensaje en bits debe ser menor que el numero de pixels
     *      de la imagen * 3
     * @pos la longitud del mensaje y el mensaje completo están escondidos en la
     *      imagen
     */
    public void esconder(char[] mensaje, int longitud) {
        int contador = 0;
        byte elByte;
        escribirBits(contador, longitud, 16);
        // La longitud del mensaje se esconderá en los primeros 16 bytes.
        // Eso es el equivalente a 2 caracteres (en necesidad de almacenamiento).
        // El primer byte del mensaje se almacena después de la longitud (a partir del
        // byte 17)
        contador = 2;

        for (int i = 0; i < longitud; i++) {
            elByte = (byte) mensaje[i];
            escribirBits(contador, elByte, 8);
            contador++;
            if (i % 1000 == 0)
                System.out.println("Van " + i + " caracteres de " + longitud);
        }
    }

    /**
     * Método para escribir una imagen a un archivo en formato BMP
     * 
     * @param output: nombre del archivo donde se almacenará la imagen.
     *                Se espera que se invoque para almacenar la imagen modificada.
     * @pre la matriz imagen debe haber sido inicializada con una imagen
     * @pos se creó el archivo en formato bmp con la información de la matriz imagen
     */
    public void escribirImagen(String output) {
        byte pad = 0;
        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(header);
            byte[] pixel = new byte[3];

            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    // Leer los 3 bytes del píxel (B, G, R)
                    pixel[0] = imagen[i][j][0];
                    pixel[1] = imagen[i][j][1];
                    pixel[2] = imagen[i][j][2];
                    fos.write(pixel);
                }
                for (int k = 0; k < padding; k++)
                    fos.write(pad);
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método para recuperar la longitud del mensaje escondido en una imagen.
     * 
     * @pre la matriz imagen debe haber sido inicializada con una imagen.
     *      la imagen debe esconder la longitud de un mensaje escondido,
     *      y el mensaje.
     * @return La longitud del mensaje que se esconde en la imagen.
     */
    public int leerLongitud() {
        int longitud = 0;
        // Usamos 16 bits para almacenar la longitud del mensaje.
        // Esos 16 bits se esconden en los primeros 16 bytes de la imagen.
        // Como cada posición de la matriz tiene 3 bytes y se esconde un bit en cada
        // byte.
        // Debemos leer 5 pixeles completos y 1 byte del siguiente pixel.
        for (int i = 0; i < 16; i++) {
            // ancho es el número de pixeles en una fila
            // ancho*3 es el número de bytes en una fila
            int col = (i % (ancho * 3)) / 3;
            longitud = longitud | (imagen[0][col][((i % (ancho * 3)) % 3)] & 1) << i;

        }
        return longitud;
    }

    /**
     * Método para recuperar la longitud del mensaje escondido en una imagen.
     *
     * @pre La matriz `imagen` debe haber sido inicializada con una imagen que
     *      contiene un mensaje escondido.
     * @return La longitud del mensaje escondido en la imagen.
     */
    public int leerLongitud2(FileWriter fw, int tamanoPagina) throws IOException {
        int longitud = 0;

        // La longitud del mensaje está almacenada en los primeros 16 bits de la imagen.
        // La información de estos 16 bits está dispersa en los primeros 16 bytes de la
        // imagen.
        // Cada píxel contiene 3 bytes (canales RGB), y se oculta 1 bit en cada byte.

        for (int i = 0; i < 16; i++) {
            // Calcula la columna de la imagen correspondiente para el bit actual.
            // `ancho * 3` es el número total de bytes por fila (3 bytes por píxel).

            int col = (i % (ancho * 3)) / 3; // Columna
            int canal = (i % (ancho * 3)) % 3; // Canal (R, G, B)

            // Extrae el bit menos significativo del byte específico que contiene el bit.
            // `(i % (ancho * 3)) % 3` selecciona el canal (R, G, B) en el que se encuentra
            // el bit.
            // `& 1` obtiene el bit menos significativo.
            // `<< i` desplaza el bit a la posición adecuada dentro del número `longitud`.

            longitud = longitud | ((imagen[0][col][canal] & 1) << i);

            // Registro de referencia de acceso a la imagen
            int paginaVirtual = i / tamanoPagina; // Contamos con que nunca el tamano de pagina será menor a 8 bytes
            int desplazamiento = (i) % tamanoPagina;
            fw.write("Imagen[0][" + col + "]." + (canal == 0 ? "R" : (canal == 1 ? "G" : "B")) + "," + paginaVirtual
                    + "," + desplazamiento + ",R\n");
        }

        // Retorna el valor de longitud, que ahora contiene los primeros 16 bits
        // decodificados.
        return longitud;
    }

    /**
     * Método para recuperar un mensaje escondido en una imagen
     * 
     * @param cadena:   vector de char, con espacio ya asignado
     * @param longitud: tamaño del mensaje escondido
     *                  y tamaño del vector (espacio disponible para almacenar
     *                  información)
     * @pre la matriz imagen debe haber sido inicializada con una imagen.
     *      la imagen debe esconder la longitud de un mensaje escondido,
     *      y el mensaje.
     * @pos cadena contiene el mensaje escondido en la imagen
     */

    public void recuperar(char[] cadena, int longitud) {
        // (imagen.ancho*3) ancho de cada fila de la matriz en bytes
        int bytesFila = ancho * 3;
        System.out.println(longitud);
        for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
            cadena[posCaracter] = 0;
            for (int i = 0; i < 8; i++) {
                // los dos primeros caracteres son la longitud. Hay que saltarlos
                int numBytes = 16 + (posCaracter * 8) + i;
                int fila = numBytes / bytesFila;
                int col = numBytes % (bytesFila) / 3;
                cadena[posCaracter] = (char) (cadena[posCaracter]
                        | (imagen[fila][col][((numBytes % bytesFila) % 3)] & 1) << i);
            }
        }
    }

    public void recuperar2(char[] cadena, int longitud, FileWriter fw, int tamanoPagina, int numeroFilas,
            int numeroColumnas) throws IOException {
        // El número de bytes en cada fila de la imagen, considerando tres canales
        // (RGB).
        int bytesFila = ancho * 3;
        System.out.println("Longitud de mensaje: " + longitud);

        // Recorre cada carácter del mensaje oculto.
        for (int posCaracter = 0; posCaracter < longitud; posCaracter++) {
            // Inicializa el carácter actual en la posición `posCaracter` de `cadena` a 0.
            cadena[posCaracter] = 0;
            int paginaVirtualMensaje = (posCaracter + 3 * numeroFilas * numeroColumnas) / tamanoPagina;
            int desplazamientoMensaje = (posCaracter) % tamanoPagina;
            fw.write("Mensaje[" + posCaracter + "]," + paginaVirtualMensaje + "," + desplazamientoMensaje + ",W\n");

            // Cada carácter se compone de 8 bits, así que repetimos el proceso 8 veces.
            for (int i = 0; i < 8; i++) {
                // Calcula la posición del bit en el que se encuentra la información relevante.
                // Se inicia en el byte 16 para omitir los primeros 2 caracteres, que almacenan
                // la longitud del mensaje.
                int numBytes = 16 + (posCaracter * 8) + i;

                // Calcula la fila de la imagen en función del número total de bytes.
                int fila = numBytes / bytesFila;

                // Calcula la columna de la imagen, teniendo en cuenta los tres canales de color
                // por píxel.
                int col = (numBytes % bytesFila) / 3;
                int canal = (numBytes % bytesFila) % 3;

                // Extrae el bit menos significativo del canal específico del píxel.
                // Esto se hace mediante una operación AND con 1.
                // Luego, se desplaza el bit a la posición adecuada con `<< i` y se combina con
                // el carácter en construcción.
                cadena[posCaracter] = (char) (cadena[posCaracter] |
                        ((imagen[fila][col][canal] & 1) << i));

                // Registro de referencia de acceso a la imagen
                int paginaVirtual = (numBytes) / tamanoPagina;
                int desplazamiento = (numBytes) % tamanoPagina;
                fw.write("Imagen[" + fila + "][" + col + "]." + (canal == 0 ? "R" : (canal == 1 ? "G" : "B")) + ","
                        + paginaVirtual + "," + desplazamiento + ",R\n");

                // Registro de referencia de acceso al mensaje

                fw.write("Mensaje[" + posCaracter + "]," + paginaVirtualMensaje + "," + desplazamientoMensaje + ",W\n");
            }

        }
    }

    public void opcion1(int tamanoPagina, Imagen imagen) {

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        try {

            // Variables de interés para la parte inicial del archivo de texto a retornar
            int numeroFilas = imagen.alto;
            int numeroColumnas = imagen.ancho;
            int longitudTemp = imagen.leerLongitud();
            int longitud;

            // Generar archivo de referencias
            try (FileWriter fw = new FileWriter("referencias.txt")) {
                fw.write("TP=" + tamanoPagina + "\n");
                fw.write("NF=" + numeroFilas + "\n");
                fw.write("NC=" + numeroColumnas + "\n");
                int numeroPaginas = (3 * numeroFilas * numeroColumnas + longitudTemp) / tamanoPagina;
                // int numeroReferencias = (numeroFilas * numeroColumnas * 3) + longitudTemp;
                int numeroReferencias = 8 * longitudTemp * 2 + 16;
                fw.write("NR=" + numeroReferencias + "\n");
                fw.write("NP=" + numeroPaginas + "\n");

                // Leer la longitud del mensaje y registrar las referencias de acceso
                longitud = imagen.leerLongitud2(fw, tamanoPagina);

                // Añadir las referencias generadas para el mensaje durante la recuperación
                char[] mensaje = new char[longitud];
                recuperar2(mensaje, longitud, fw, tamanoPagina, numeroFilas, numeroColumnas);
            }
            System.out.println("Archivo de referencias generado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void opcion2() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        try {
            //System.out.println("Nombre del archivo de referencias: ");
            String ruta = "C:/Users/jorgi/Desktop/Uniandes/infracomp/Caso2_infracomp/referencias.txt";
            System.out.println("Número de marcos de página: ");
            int num_marcospag = Integer.parseInt(br.readLine());
            List<Integer> marco = new ArrayList<>(Collections.nCopies(num_marcospag, -1)); // Inicializa marcos con -1

            
            HashMap<Integer, Integer> bitR = new HashMap<Integer, Integer>();

            // Crear threads
            T t1 = new T(0, bitR, marco, ruta);
            T t2 = new T(1, bitR, marco, ruta);

            t1.start();
            t2.start();

            // Esperar a que los threads terminen
            t1.join();
            t2.interrupt();

            // Imprimir resultados
            System.out.println("Hits: " + t1.getHits());
            System.out.println("Fallas: " + t1.getFallas());

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
