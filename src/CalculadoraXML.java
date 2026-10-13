import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalculadoraXML {

    public static void main(String[] args) {
        // Validación de argumentos de línea de comandos
        if (args.length < 2 || args.length > 3) {
            System.out.println("Uso correcto: java CalculadoraXML <documento.xml> <variable1> [variable2]");
            return;
        }

        String archivoXml = args[0];
        String var1 = args[1];
        String var2 = (args.length == 3) ? args[2] : null;

        try {
            // Manejo: No se puede abrir el documento XML
            File inputFile = new File(archivoXml);
            if (!inputFile.exists() || !inputFile.isFile()) {
                System.err.println("Error: No se pudo abrir el documento XML. El archivo '" + archivoXml + "' no existe.");
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Extracción de datos usando Java Collections API (ArrayList)
            List<Double> datosVar1 = extraerValores(doc, var1);

            // Manejo: La variable no existe en el documento
            if (datosVar1.isEmpty()) {
                System.err.println("Error: La variable '" + var1 + "' no existe en el documento o no contiene datos.");
                return;
            }

            System.out.println("=== Estadísticas para la variable: " + var1 + " ===");
            imprimirEstadisticas(datosVar1);

            // Si el usuario proporcionó una segunda variable
            if (var2 != null) {
                List<Double> datosVar2 = extraerValores(doc, var2);

                if (datosVar2.isEmpty()) {
                    System.err.println("\nError: La variable '" + var2 + "' no existe en el documento o no contiene datos.");
                    return;
                }

                System.out.println("\n=== Estadísticas para la variable: " + var2 + " ===");
                imprimirEstadisticas(datosVar2);

                // Calcular coeficiente de correlación
                if (datosVar1.size() == datosVar2.size()) {
                    double correlacion = calcularCorrelacion(datosVar1, datosVar2);
                    System.out.println("\n=== Análisis Bivariable ===");
                    System.out.printf("Coeficiente de correlación de Pearson (r): %.4f\n", correlacion);
                } else {
                    System.err.println("\nAdvertencia: Las variables tienen un número distinto de elementos. No se puede calcular la correlación.");
                }
            }

        } catch (Exception e) {
            System.err.println("Excepción durante la ejecución: " + e.getMessage());
        }
    }

    // Método para buscar etiquetas en el XML y guardar sus valores numéricos en una Lista
    private static List<Double> extraerValores(Document doc, String nombreVariable) throws Exception {
        List<Double> listaValores = new ArrayList<>(); // Uso del Collections API
        NodeList nodeList = doc.getElementsByTagName(nombreVariable);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String texto = node.getTextContent().trim();
                try {
                    // Manejo: La variable no es numérica
                    double valor = Double.parseDouble(texto);
                    listaValores.add(valor);
                } catch (NumberFormatException e) {
                    throw new Exception("La variable '" + nombreVariable + "' contiene un valor no numérico: '" + texto + "'");
                }
            }
        }
        return listaValores;
    }

    // Método para calcular Media, Varianza, Desviación, Max y Min
    private static void imprimirEstadisticas(List<Double> datos) {
        int n = datos.size();
        double suma = 0;

        for (double num : datos) {
            suma += num;
        }

        double media = suma / n;

        double sumaDiferenciasCuadrado = 0;
        for (double num : datos) {
            sumaDiferenciasCuadrado += Math.pow(num - media, 2);
        }

        // Varianza muestral (n-1). Si deseas poblacional, divide solo entre n.
        double varianza = (n > 1) ? sumaDiferenciasCuadrado / (n - 1) : 0;
        double desviacionEstandar = Math.sqrt(varianza);

        // Uso de Collections de Java para Max y Min
        double max = Collections.max(datos);
        double min = Collections.min(datos);

        System.out.printf("Elementos contados (n): %d\n", n);
        System.out.printf("Media: %.4f\n", media);
        System.out.printf("Varianza: %.4f\n", varianza);
        System.out.printf("Desviación estándar: %.4f\n", desviacionEstandar);
        System.out.printf("Valor Máximo: %.4f\n", max);
        System.out.printf("Valor Mínimo: %.4f\n", min);
    }

    // Método para calcular la Correlación de Pearson
    private static double calcularCorrelacion(List<Double> x, List<Double> y) {
        int n = x.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0, sumY2 = 0;

        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += Math.pow(x.get(i), 2);
            sumY2 += Math.pow(y.get(i), 2);
        }

        double numerador = (n * sumXY) - (sumX * sumY);
        double denominador = Math.sqrt(((n * sumX2) - Math.pow(sumX, 2)) * ((n * sumY2) - Math.pow(sumY, 2)));

        if (denominador == 0) return 0;
        return numerador / denominador;
    }
}