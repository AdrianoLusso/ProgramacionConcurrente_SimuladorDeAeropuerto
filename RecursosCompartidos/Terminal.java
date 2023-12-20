package RecursosCompartidos;

import java.time.LocalTime;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Otros.SaveConsoleOutput;

public class Terminal {
    
    //Flags que marcas si ya es posible embarcar para cada puerta de la terminal.
    private Dictionary<Integer,Boolean> puedenEmbarcar =  new Hashtable<>();
    private ReentrantLock salaEmbarque = new ReentrantLock();
    //Las condiciones son las esperas de cada embarque correspondiente a cada puerta de la terminal.
    private Dictionary<Integer,Condition> esperarEmbarque = new Hashtable<Integer,Condition>();

    //Puestos de envio-recibimiento entre el pasajero y el respectivo empleado de atencion.
    private LinkedBlockingQueue queuePuestoEmbarque = new LinkedBlockingQueue<>(10);

    //Marca cuando empieza el dia de trabajo.
    private boolean empezarDiaTrabajo = false;
    private ReentrantLock diaTrabajo = new ReentrantLock();
    private Condition aeropuertoAbierto = diaTrabajo.newCondition();

    //Flag que marca cuando el empleado de embarque puede evaluar si debe hacer una ronda mas de trabajo.
    //Sirve para que no tome un dato desactualizado respecto a si debe seguir o no trabajando.
    private boolean puedeVerElFlag = false;
    private ReentrantLock accesoAFlagEmbarque = new ReentrantLock();
    private Condition esperarFlag = accesoAFlagEmbarque.newCondition();

    //Flag que marca cuando el empleado de embarque debe hacer una ronda mas de trabajo
    private boolean flagRondaEmbarque = true;
    private ReentrantLock mutexFlagRondaEmbarque = new ReentrantLock();

    public Terminal(int[] embarques){        
        //Por cada puesto de embarque, e asigna la condition correspondiente.
        //Se inicia cada flag de que se puede embarcar como false.
        for(int i = 0;i<embarques.length;i++){
            esperarEmbarque.put(embarques[i],salaEmbarque.newCondition());
            puedenEmbarcar.put(embarques[i],false);
        }
        
    }

    //Reloj tasks
    public void abrirElEmbarque(){
        diaTrabajo.lock();
        //Marca el aueropuerto como abierto y le avisa al empleado de embarque
        empezarDiaTrabajo = true;
        aeropuertoAbierto.signalAll();
        diaTrabajo.unlock();
    }
    public void sonarAlarmaNuevoEmbarque(int puestoEmbarque){
        //Empieza a sonar la la alarma de un nuevo embarque en la respectiva puerto
        //Aun asi, el reloj sigue funcionando.No espera a que alguien reciba la alarma.
        SaveConsoleOutput.imprimir("El reloj hace sonar alarma para embarque de puerta "+puestoEmbarque);
        queuePuestoEmbarque.add(puestoEmbarque);
        
        accesoAFlagEmbarque.lock();
        puedeVerElFlag = true;
        esperarFlag.signalAll();
        accesoAFlagEmbarque.unlock();
    }
    public void terminarDiaTrabajo(){
        //Indica al empleado de embarque que ya no tiene mas rondas de trabajo que hacer
        mutexFlagRondaEmbarque.lock();
        flagRondaEmbarque = false;
        mutexFlagRondaEmbarque.unlock();

        accesoAFlagEmbarque.lock();
        puedeVerElFlag = true;
        esperarFlag.signalAll();
        accesoAFlagEmbarque.unlock();
    }

    //Pasajero tasks
    public void esperarAEmbarcar(int puestoEmbarque){
        salaEmbarque.lock();
        try {
            SaveConsoleOutput.imprimir("Pasajero "+Thread.currentThread().getName()+" llega a la sala de embarque.");
            //Tendran que esperar a embarcar hasta que se les avise que haya un vuelo en su puerta de embarque, y que coincida con la hora de su vuelo
            while(!puedenEmbarcar.get(puestoEmbarque) ){
                SaveConsoleOutput.imprimir("Pasajero "+Thread.currentThread().getName()+" debe esperar a que lo llamen a embarcar por puerta "+puestoEmbarque);
                esperarEmbarque.get(puestoEmbarque).await();
            }
            SaveConsoleOutput.imprimir("Pasajero "+Thread.currentThread().getName()+" embarca a su vuelo. Buen viaje!!!");
        }
        catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        }
        salaEmbarque.unlock();
    }    

    //Empleado de embarque Tasks
    public void empezarDiaTrabajo(){
        try {
            diaTrabajo.lock();
            //Mientras no haya empezado el dia de trabajo, espera.
            while(!empezarDiaTrabajo){
            aeropuertoAbierto.await();
            }
            diaTrabajo.unlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public boolean empezarNuevaRondaEmbarque(){
        boolean nuevaRonda = false;

        try {
            accesoAFlagEmbarque.lock();
            //Mientras no tenga permitido ver el flag que le dice si debe hacer otra ronda de trabajo, espera.
            while(!puedeVerElFlag){
            esperarFlag.await();
            }
            //Vuelve a setear a false para que la siguiente vez tambien deba esperar que le permitan el paso.
            puedeVerElFlag = false;
            accesoAFlagEmbarque.unlock();

            //Ve si tiene que seguir anunciado embarques
            mutexFlagRondaEmbarque.lock();
            nuevaRonda = flagRondaEmbarque;
            mutexFlagRondaEmbarque.unlock();
        } catch (Exception e) {
        }
        return nuevaRonda;
    }
    public void avisarNuevoEmbarque(Terminal terminal){
       
        int puertaDeEmbarque;
        try {
             //El empleado de embarque espera a que suene una alarma para avisar a la respectiva puerta de embarque
            SaveConsoleOutput.imprimir(Thread.currentThread().getName()+" espera a que suene una nueva alarma.");
            puertaDeEmbarque = (int)queuePuestoEmbarque.take();
            SaveConsoleOutput.imprimir(Thread.currentThread().getName()+" recibio una nueva alarma para la puerta "+puertaDeEmbarque);

            //Setea el flag de que se puede embarcar por la respectiva puerta
            puedenEmbarcar.put(puertaDeEmbarque,true);

            //Le avisa a todos los pasajeros esperando para la respectiva puerta de embarque, que el embarque esta abierto.
            SaveConsoleOutput.imprimir(Thread.currentThread().getName()+" avisa por altavoz que empieza embarque en puerta "+puertaDeEmbarque);
            salaEmbarque.lock();
            esperarEmbarque.get(puertaDeEmbarque).signalAll();
            salaEmbarque.unlock();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
