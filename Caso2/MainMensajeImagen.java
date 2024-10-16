import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainMensajeImagen {
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Seleccione una opción:");
            System.out.println("1. Esconder mensajes en imágenes");
            System.out.println("2. Recuperar mensajes de una imagen");
            int opcion = Integer.parseInt(br.readLine());

            if (opcion == 1) {
                // Opción 1: Esconder mensajes
                System.out.println("Ingrese el nombre del archivo de imagen BMP de entrada (incluyendo extensión .bmp):");
                String archivoEntrada = br.readLine();
                Imagen imagen = new Imagen(archivoEntrada);

                // Crear un array para los mensajes
                String[] mensajes = new String[5];
                char[][] mensajesCharArray = new char[5][];
                int[] longitudesMensajes = new int[5];

                // Ingresar 5 mensajes
                for (int i = 0; i < 5; i++) {
                    System.out.println("Ingrese el mensaje " + (i + 1) + ":");
                    mensajes[i] = br.readLine();
                    mensajesCharArray[i] = mensajes[i].toCharArray();
                    longitudesMensajes[i] = mensajesCharArray[i].length;

                    // Esconder cada mensaje en la imagen
                    imagen.esconder(mensajesCharArray[i], longitudesMensajes[i]);
                    System.out.println("Mensaje " + (i + 1) + " escondido en la imagen.");
                    
                    // Guardar la imagen modificada
                    System.out.println("Ingrese el nombre del archivo de salida para el mensaje " + (i + 1) + " (incluyendo extensión .bmp):");
                    String archivoSalida = br.readLine();
                    imagen.escribirImagen(archivoSalida);
                    System.out.println("Imagen modificada guardada en: " + archivoSalida);
                }
                
            } else if (opcion == 2) {
                // Opción 2: Recuperar un mensaje
                System.out.println("Ingrese el nombre del archivo de imagen BMP de entrada (incluyendo extensión .bmp):");
                String archivoEntrada = br.readLine();
                Imagen imagen = new Imagen(archivoEntrada);

                for (int i = 0; i < 5; i++) {
                    int longitudMensaje = imagen.leerLongitud();
                    char[] mensajeRecuperado = new char[longitudMensaje];
                    imagen.recuperar(mensajeRecuperado, longitudMensaje);
                    System.out.println("Mensaje " + (i + 1) + " recuperado: " + new String(mensajeRecuperado));
                }

            } else {
                System.out.println("Opción no válida. Por favor seleccione 1 o 2.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
