package Hilos;

import Otros.SaveConsoleOutput;
import RecursosCompartidos.Hora;
import RecursosCompartidos.Terminal;

public class EmpleadoEmbarque implements Runnable {

    private Terminal terminal;
    private Hora hora;

    public EmpleadoEmbarque(Terminal terminal,Hora hora){
        this.terminal = terminal;
        this.hora = hora;
    }

    @Override
    public void run() {

        boolean nuevaRonda;

        terminal.empezarDiaTrabajo();
        SaveConsoleOutput.imprimir(Thread.currentThread().getName()+" empezo su dia de trabajo");

        //Representa su jornada laboral
        nuevaRonda = terminal.empezarNuevaRondaEmbarque();
        while(nuevaRonda){
            //Espera a que suene una alarma de nuevo embarque para hacer el llamado a embarcar
            terminal.avisarNuevoEmbarque(terminal);

            nuevaRonda = terminal.empezarNuevaRondaEmbarque();
        }
        SaveConsoleOutput.imprimir(Thread.currentThread().getName()+" termino su dia de trabajo");
    }
    
}
