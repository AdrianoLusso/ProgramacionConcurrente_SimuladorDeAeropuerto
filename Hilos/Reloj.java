package Hilos;

import java.time.LocalTime;
import java.util.Dictionary;
import java.util.Hashtable;

import RecursosCompartidos.EntradaFreeshop;
import RecursosCompartidos.Hora;
import RecursosCompartidos.Ingreso;
import RecursosCompartidos.Mover;
import RecursosCompartidos.Terminal;
import Otros.InfoVuelo;
import Otros.SaveConsoleOutput;

public class Reloj implements Runnable {
    
    private Hora hora;
    private Ingreso ingreso;
    private Mover mover;
    private EntradaFreeshop[] freeshops;
    private Dictionary<Character,Terminal> terminales = new Hashtable<>();

    private InfoVuelo[] infoVuelos;

    public Reloj(Hora hora,Ingreso ingreso,Mover mover,EntradaFreeshop[] freeshops,Terminal[] terminales,InfoVuelo[] infoVuelos){
        this.hora = hora;
        this.ingreso = ingreso;
        this.mover = mover;
        this.freeshops = freeshops;
        this.terminales.put('A',terminales[0]);
        this.terminales.put('B',terminales[1]);
        this.terminales.put('C',terminales[2]);
        //De la informacion de los vuelos ,tomo el dato del horarios de cada vuelo.Esto es porque el reloj de aeropuerto
        //Avisara a los empleados de embarque cuando es hora de un vuelo, y estos deben ver si es un vuelo que les corresponde
        //avisar
        this.infoVuelos = infoVuelos;
    }

    @Override
    public void run()
    {
        
        LocalTime inicioTrabajo = LocalTime.of(6, 0, 0);
        LocalTime finTrabajo = LocalTime.of(22, 0, 0);
        LocalTime finDia = LocalTime.of(0,0,0);

        
        SaveConsoleOutput.imprimir("HORA: "+hora.get());

        //00:00 - 06:00
        while(!hora.get().equals(inicioTrabajo))
        {
            try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                hora.pasarMinuto();
                //SaveConsoleOutput.imprimir("HORA: "+hora.get());
        }
        
        //A las 6AM abre todo el aeropuerto
        mover.abrirElMover();
        ingreso.abrirElIngreso();
        freeshops[0].abrirFreeshop('A');
        freeshops[1].abrirFreeshop('B');
        freeshops[2].abrirFreeshop('C');
        terminales.get('A').abrirElEmbarque();
        terminales.get('B').abrirElEmbarque();
        terminales.get('C').abrirElEmbarque();
        //06:00 - 22:00
        while(!hora.get().equals(finTrabajo)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hora.pasarMinuto();
           SaveConsoleOutput.imprimir("HORA: "+hora.get());
             
            //A las 10PM es el ultimo vuelo. Un pasajero no deberia entrar al freeshop con menos de 30 minutos antes del embarque.
            //Dadas estas dos condiciones, los freeshops cierran a las 9:30PM
            if(hora.get().equals(LocalTime.of(21, 30, 0))){
                freeshops[0].cerrarFreeshop('A');
                freeshops[1].cerrarFreeshop('B');
                freeshops[2].cerrarFreeshop('C');
            }
             //A las 8PM los emps de informey atencion deja de trabajar.La idea es que, desde las 8PM, el checkin no funcione.
            //Esto es porque el ultimo vuelo posible es a las 10PM,y los pasajeros deberian haber hecho el checkin, minimo, 2 horas antes.
            if(hora.get().equals(LocalTime.of(20, 0, 0))){
                ingreso.cerrarElIngreso();
            }
            
            //Cuando sea la hora de embarque de algun vuelo, el reloj dara la alerta a los empleados de embarque.
            for(int i=0;i<infoVuelos.length;i++){
                if(hora.get().equals(infoVuelos[i].getPasaje().getHorario())){
                    terminales.get(infoVuelos[i].getTerminal()).sonarAlarmaNuevoEmbarque(infoVuelos[i].getPuestoEmbarque());
                }
            }
        }
        //A las 10PM el mover deja de viajar y cierran las terminales.
        SaveConsoleOutput.imprimir("FLAG");
        mover.cerrarElMover();
        terminales.get('A').terminarDiaTrabajo();
        terminales.get('B').terminarDiaTrabajo();
        terminales.get('C').terminarDiaTrabajo();

        //22:00 - 00:00
        while(!hora.get().equals(finDia)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            hora.pasarMinuto();
           //SaveConsoleOutput.imprimir("HORA: "+hora.get());
        }
    }
}

    



