package Hilos;

import RecursosCompartidos.Hora;
import RecursosCompartidos.Mover;
import Otros.SaveConsoleOutput;

public class ControlMover implements Runnable{

            
    private Mover mover;
    private Hora hora;

    public ControlMover(Mover mover,Hora hora)
    {
        this.mover = mover;
        this.hora = hora;
    } 

    @Override
    public void run() {

        char[] terminales = {'A','B','C'};
        boolean nuevaRonda;

        mover.empezarDiaTrabajo();
        SaveConsoleOutput.imprimir("El mover arranco el motor.");

        //El while representa horario de funcionamiento
        nuevaRonda = mover.empezarNuevaRondaViaje();
        while(nuevaRonda)
        {
            //Espera en el ingreso a que llegue gente
            mover.esperarQueSeLleneMover();

            //Recorrido de las 3 terminales
            for (int i = 0;i<3;i++)
            {
                SaveConsoleOutput.imprimir("El mover viaja a la terminal "+terminales[i]);
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mover.pararEnTerminal(terminales[i]);
            }
        
            try {
                SaveConsoleOutput.imprimir("El mover vuelve al ingreso...");
                //Cuando ya debugee, devolver a 9000
                Thread.sleep(700);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //Preparamos todo para repetir el viaje.
            mover.pararEnIngreso();

            nuevaRonda = mover.empezarNuevaRondaViaje();
        }
        SaveConsoleOutput.imprimir("El mover paro el motor.");
    }
    
}
