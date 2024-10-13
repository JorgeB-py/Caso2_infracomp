import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        while (true) {  // Mantener el programa corriendo hasta que el usuario decida salir
            try {
                System.out.println("Nombre del archivo: ");
                String ruta = br.readLine();
                Imagen imagen = new Imagen(ruta);  // Asumiendo que la clase Imagen está bien definida
                System.out.println("Seleccione una opción: ");
                System.out.println("1. Generar archivo de referencias");
                System.out.println("2. Simulación de reemplazo de páginas");
                System.out.println("3. Salir");
                int opcion = Integer.parseInt(br.readLine());
                if (opcion == 3) {
                    System.out.println("Saliendo del programa...");
                    break;  // Salir del bucle
                }

                switch (opcion) {
                    case 1:
                        System.out.println("Tamaño de página en bytes: ");
                        int tamanoPagina = Integer.parseInt(br.readLine());
                        imagen.opcion1(tamanoPagina, imagen); 
                        break;
                    case 2:
                        imagen.opcion2();
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, intente de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Entrada no válida. Por favor, ingrese un número.");
            } catch (Exception e) {
                System.out.println("Ocurrió un error: " + e.getMessage());
            }
        }
    }
}
