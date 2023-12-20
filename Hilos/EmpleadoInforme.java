package Hilos;

import Otros.Pasaje;
import RecursosCompartidos.Hora;
import RecursosCompartidos.Ingreso;
import Otros.SaveConsoleOutput;

public class EmpleadoInforme implements Runnable {

    private Ingreso ingreso;
    private Hora hora;

    public EmpleadoInforme(Ingreso ingreso,Hora hora)
    {
        this.ingreso = ingreso;
        this.hora = hora;
    }

    @Override
    public void run() {

        Pasaje pasaje;
        boolean nuevaRonda;

        ingreso.empezarDiaTrabajo();
        SaveConsoleOutput.imprimir("El empleado de informe empezo su dia de trabajo.");

        //El while representa su jornada laboral
        nuevaRonda = ingreso.empezarNuevaRondaInforme();
        while(nuevaRonda)
        {
            //Esperar a atender a un pasajero
            pasaje = ingreso.empezarInformeAPasajero();

            try {
                SaveConsoleOutput.imprimir("El emp de informe esta leyendo el pasaje ("+pasaje+")");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //Terminar de atender pasajero
            ingreso.terminarInformeAPasajero(pasaje.getAerolineas());

            nuevaRonda = ingreso.empezarNuevaRondaInforme();
        }
        SaveConsoleOutput.imprimir("El empleado de informe termino su dia de trabajo");
        
    }
    
    
}
