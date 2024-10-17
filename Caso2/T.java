import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class T extends Thread {

    private int clase; 
    private ConcurrentHashMap<Integer, Integer> bitR;
    private CopyOnWriteArrayList<Integer> marco;
    private String ruta;

    private int hits = 0;
    private int fallas = 0;

    public T(int clase, ConcurrentHashMap<Integer, Integer> bitR, CopyOnWriteArrayList<Integer> marco, String ruta) {
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
    public synchronized void gestionarReferencias() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ruta)))) {
            for (int i = 0; i < 5; i++) {
                reader.readLine(); 
            }
            String linea;
            while ((linea = reader.readLine()) != null) {
                Thread.sleep(1);
                String[] datos = linea.split(",");
                int pagina = Integer.parseInt(datos[1]);
                if (marco.contains(pagina)) {

                    actualizarR(pagina);
                    hits++;
                } else {

                    fallas++;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para envejecimiento del bit R
    public synchronized void envejecimientoBitR() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(2);
                for (Integer pagina : bitR.keySet()) {
                    bitR.put(pagina, 0);
                }
            } catch (InterruptedException e) {
                System.out.println("Thread de envejecimiento interrumpido, terminando.");
                break;
            }
        }
    }

    // Actualiza el bit R a 1 cuando una página es referenciada
    public synchronized void actualizarR(int pagina) {
        bitR.put(pagina, 1); 
    }

    // Métodos para obtener los contadores
    public synchronized int getHits() {
        return hits;
    }

    public synchronized int getFallas() {
        return fallas;
    }
}
