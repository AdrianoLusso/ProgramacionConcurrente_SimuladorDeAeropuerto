package Otros;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveConsoleOutput {

    private static File file = new File("output.txt") ;
    private static FileWriter writer;

    public static void openFileWriter()
    {
         // Create a FileWriter object to write to the file.
        try {
            writer = new FileWriter(file);
        } catch (IOException e) {
            System.out.println("ERROR ABRIENDO DRIVER");
        }
    }

    public static void closeFileWriter()
    {
        // Close the FileWriter object.
        try {
            writer.close();
        } catch (IOException e) {
            System.out.println("ERROR CERRANDO DRIVER");
        }
    }

    public static void imprimir(String texto)
    {
        try {
            System.out.println(texto);
            writer.write(texto+"\n");
        } catch (IOException e) {
            System.out.println("ERROR DE ESCRITURA");
        }


    }
}