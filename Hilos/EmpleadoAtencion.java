package Hilos;

import Otros.InfoVuelo;
import Otros.Pasaje;
import RecursosCompartidos.Ingreso;
import Otros.SaveConsoleOutput;

public class EmpleadoAtencion implements Runnable {

    private Ingreso ingreso;
    private InfoVuelo[] infosVuelos;
    private int puestoAtencion;

    public EmpleadoAtencion(Ingreso ingreso, int puestoAtencion, InfoVuelo[] infosVuelos) {
        this.ingreso = ingreso;
        this.infosVuelos = infosVuelos;
        this.puestoAtencion = puestoAtencion;
    }

    @Override
    public void run() {
        
        int i;
        Object[] publicInfoVuelo;
        Pasaje pasaje;
        boolean nuevaRonda;

        ingreso.empezarDiaTrabajo();
        SaveConsoleOutput.imprimir("El empleado de atencion "+puestoAtencion+" empieza el dia de trabajo.");        

        //El while representa su jornada laboral
        nuevaRonda = ingreso.empezarNuevaRondaAtencion(puestoAtencion);
        SaveConsoleOutput.imprimir("puesto "+puestoAtencion+": "+nuevaRonda);
        while(nuevaRonda)
        {
            //Recibe el pasaje de pasajero
            pasaje = ingreso.empezarAtencionAPasajero(puestoAtencion);

             i = 0;
            publicInfoVuelo = null;
            try {
                //SaveConsoleOutput.imprimir("El empleado de atencion "+puestoAtencion+" esta analizando el pasaje.");
                Thread.sleep(2500);

                do{
                    //SaveConsoleOutput.imprimir("num"+puestoAtencion+": "+infosVuelos[i].getPasaje().equals(pasaje));
                    if(infosVuelos[i].getPasaje().equals(pasaje))
                    {
                        publicInfoVuelo = infosVuelos[i].toPublicInfoVuelo();
                    }
                    i++;
                }while(i < infosVuelos.length && publicInfoVuelo == null);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //SaveConsoleOutput.imprimir("ATENCION "+puestoAtencion+" encontro "+publicInfoVuelo.toString());
            //Entrega embarque y terminal a pasajero
            System.out.println("TIPO:"+publicInfoVuelo.toString());
            ingreso.terminarAtencionAPasajero(puestoAtencion, publicInfoVuelo);

            nuevaRonda = ingreso.empezarNuevaRondaAtencion(puestoAtencion);
        }

        SaveConsoleOutput.imprimir("El empleado de atencion "+puestoAtencion+" termino su dia de trabajo.");
    }

}
