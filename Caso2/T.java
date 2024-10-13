import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class T extends Thread {

    private int clase;  // 0 = gestión de referencias, 1 = envejecimiento del bit R
    private HashMap<Integer, Integer> bitR;  // Mapa que almacena los bits R de las páginas
    private List<Integer> marco;  // Lista que representa los marcos de página
    private String ruta;
    
    // Contadores para hits y fallas
    private int hits = 0;
    private int fallas = 0;

    public T(int clase, HashMap<Integer, Integer> bitR, List<Integer> marco, String ruta) {
        this.clase = clase;
        this.bitR = bitR;
        this.marco = marco;
        this.ruta = ruta;
    }

    public void run() {
        if (clase == 0) {
            gestionarReferencias();
        } else {
            envejecimientoBitR();
        }
    }

    // Método para gestionar las referencias de páginas y actualizar bit R
    public void gestionarReferencias() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ruta)))) {
            for (int i = 0; i < 5; i++) {
                reader.readLine();  // Saltar las primeras 5 líneas
            }
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                int pagina = Integer.parseInt(datos[1]);  // Página referenciada
                synchronized (this) {  // Sincroniza el acceso a marco y bitR
                    if (marco.contains(pagina)) {
                        // Si la página ya está en los marcos, es un hit
                        actualizarR(pagina);
                        hits++;
                        System.out.println("Hit: " + hits);
                    } else {
                        // Fallo de página
                        fallas++;
                        System.out.println("Falla: " + fallas);
    
                        // Reemplazo: encuentra el primer marco vacío o la primera página con bit R = 0
                        boolean reemplazada = false;
                        for (int i = 0; i < marco.size(); i++) {
                            if (marco.get(i) == -1 || bitR.get(marco.get(i)) == 0) {
                                // Reemplaza la página en el marco con la nueva página
                                marco.set(i, pagina);
                                reemplazada = true;
                                break;
                            }
                        }
    
                        if (!reemplazada) {
                            marco.set(0, pagina);
                        }
    
                        actualizarR(pagina); 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    // Método para envejecimiento del bit R
    public void envejecimientoBitR() {
        while (!isInterrupted()) {  // Verifica si el thread ha sido interrumpido
            try {
                Thread.sleep(2);  // Simula que cada 2 ms se envejece el bit R
                synchronized (this) {  // Sincroniza el acceso a marco y bitR
                    for (Integer pagina : bitR.keySet()) {
                        bitR.put(pagina, 0);  // Envejece el bit R
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Thread de envejecimiento interrumpido, terminando.");
                break;  // Sale del ciclo cuando es interrumpido
            }
        }
    }

    // Actualiza el bit R a 1 cuando una página es referenciada
    public synchronized void actualizarR(int pagina) {
        bitR.put(pagina, 1);  // Se pone el bit R a 1 cuando la página es referenciada
    }

    // Métodos para obtener los contadores
    public synchronized int getHits() {
        return hits;
    }

    public synchronized int getFallas() {
        return fallas;
    }
}
