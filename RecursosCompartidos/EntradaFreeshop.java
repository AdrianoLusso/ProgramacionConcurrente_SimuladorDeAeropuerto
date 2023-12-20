package RecursosCompartidos;

import java.util.Random;

import Otros.SaveConsoleOutput;

public class EntradaFreeshop {
    
    private Random r = new Random();
    //Cuanta gente entra en el freeshop, y cuanta gente hay actualmente.
    private int capacidadFreeshop;
    
    private boolean freeshopAbierto = false;
    
    public EntradaFreeshop(int capacidadFreeshop)
    {
        this.capacidadFreeshop = capacidadFreeshop;
        
        
    }
        
    //Reloj tasks
    public synchronized void abrirFreeshop(char terminal){
        freeshopAbierto = true;
        SaveConsoleOutput.imprimir("El freeshop "+terminal+" empieza a atender gente.");
    }
    public synchronized void cerrarFreeshop(char terminal){
        freeshopAbierto = false;
        SaveConsoleOutput.imprimir("El freeshop "+terminal+" no deja entrar a mas gente.");
    } 

    //Pasajero tasks

    public synchronized boolean entrarAlFreeshop(char terminal)
    {
        boolean entra = freeshopAbierto && (r.nextInt(3) >= 1) && capacidadFreeshop>0;

        //Entra al freeshop
        if(entra){
            SaveConsoleOutput.imprimir("Pasajero "+Thread.currentThread().getName()+" entro al freeshop "+terminal);
            SaveConsoleOutput.imprimir("El freeshop "+terminal+" le queda capacidad "+(--capacidadFreeshop));
        }
        else{
            SaveConsoleOutput.imprimir("Pasajero "+Thread.currentThread().getName()+" no entro al freeshop.");
        }
        return entra;       
    }
    public synchronized void salirDeFreeshop(char terminal)
    {
        SaveConsoleOutput.imprimir("El pasajero "+Thread.currentThread().getName()+" salio del freeshop "+terminal);
        SaveConsoleOutput.imprimir("El freeshop "+terminal+" le queda capacidad "+(++capacidadFreeshop));
    }

}
