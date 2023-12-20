package RecursosCompartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.midi.Soundbank;

import Otros.SaveConsoleOutput;

public class Mover {
    
    private int capacidad;

    //Este cyclic barrier se rompe cuando se completa la capacidad del mover.
    //Al romperse, los pasajeros y el mover entran en condicion de viaje.
    private CyclicBarrier barrierEsperaViaje;

    //Este lock protege el valor de capacidad restante del mover, y tiene la condicion de que los pasajeros podran entrar al mover unicamente cuando tenga capacidad disponible
    private int capacidadRestante;
    private ReentrantLock entradaMover = new ReentrantLock();
    private Condition puedeEntrarAlMover = entradaMover.newCondition();

    //mutex para proteger el flag que marca el inicio del viaje del mover.
    private boolean inicioViajeMoverFlag = false; 
    private ReentrantLock mutexInicioViajeMoverFlag = new ReentrantLock();
    private Condition puedeIniciarViaje = mutexInicioViajeMoverFlag.newCondition();

    private ReentrantLock pararEnTerminal = new ReentrantLock(false);
    //permite al mover avisar cuando los pasajeros deben bajar en cada una de las terminales
    private Condition[] bajarEnTerminal = new Condition[3];
    //Permite a los pasajeros avisar cuando bajaron todos los que debian bajar en una determinada terminal, de forma que el mover pueda seguir el viaje.
    private Condition seguirViaje;

    //Marca cuanta gente que debe bajar en cada una de las tres terminales SIGUE DENTRO del mover. El contador para cada terminal sube cuando hay un pasajero de esa
    //terminar dentro del mover, y baja cuando ese pasajero se va.Tiene su respectivo mutex.
    private int[] ocupacionMover = {0,0,0};
    private ReentrantLock[] mutexOcupacionMover = new ReentrantLock[3];

    //Marca cuando empieza el dia de trabajo.
    private boolean diaTrabajoFlag = false;
    private ReentrantLock mutexDiaTrabajoFlag = new ReentrantLock();
    private Condition empezarElDiaDeTrabajo = mutexDiaTrabajoFlag.newCondition();

    //Este flag no es un booleano. Esto se debe a que debe imitar el comportamiento de un samaforo genera que puede
    //llegar a tener N permisos acumulados, siendo N la cantidad de pasajeros que esten esperando en la fila al mover.
    private int verificarRondaFlag = 0;
    private boolean verificarRondaFlag2 = true;
    private ReentrantLock mutexVerificarRondaFlag = new ReentrantLock();
    private Condition podesVerificarElFlag = mutexVerificarRondaFlag.newCondition();
    
    //Marca una nueva roda de viaje para el mover.
    private boolean rondaFlag = true;
    private ReentrantLock mutexRondaFlag = new ReentrantLock();

    public Mover(int capacidadMover)
    {
        capacidad = capacidadMover;
        capacidadRestante = capacidadMover;

        //el +1 representa al mover
        barrierEsperaViaje = new CyclicBarrier(capacidadMover);

        bajarEnTerminal[0] = pararEnTerminal.newCondition();
        bajarEnTerminal[1] = pararEnTerminal.newCondition();
        bajarEnTerminal[2] = pararEnTerminal.newCondition();
        seguirViaje = pararEnTerminal.newCondition();

        mutexOcupacionMover[0] = new ReentrantLock();
        mutexOcupacionMover[1] = new ReentrantLock();
        mutexOcupacionMover[2] = new ReentrantLock();
    }

    //Reloj tasks
        public void abrirElMover(){
        //Se le avisa al control del mover que debe empezar a accionar su movimiento.
        mutexDiaTrabajoFlag.lock();
        diaTrabajoFlag = true;
        empezarElDiaDeTrabajo.signalAll();
        mutexDiaTrabajoFlag.unlock();
    }
        public void cerrarElMover(){
            //Se marca que el mover no debe hacer mas viajes
            mutexRondaFlag.lock();
            rondaFlag = false;
            mutexRondaFlag.unlock();

            //Le permite al control del mover ver la info de que no hara mas viajes hoy
            mutexVerificarRondaFlag.lock();
            verificarRondaFlag2 = true;
            //SaveConsoleOutput.imprimir("FLAG LUEGO DE CERRAR:"+verificarRondaFlag+verificarRondaFlag2);
            podesVerificarElFlag.signalAll();
            mutexVerificarRondaFlag.unlock();
        }

    //Pasajero tasks
    public void entrarAlMover()
    {
        int numeroArribo=-1;

        //Le permite al control mover ver el flag que le dice si debe, o no, hacer otro viaje.
        mutexVerificarRondaFlag.lock();
        verificarRondaFlag++;
                    //SaveConsoleOutput.imprimir("FLAG LUEGO DE PASAJERO EN LA FILA:"+verificarRondaFlag+verificarRondaFlag2);
        podesVerificarElFlag.signalAll();
        mutexVerificarRondaFlag.unlock();

        try {
            SaveConsoleOutput.imprimir("El pasajero "+Thread.currentThread().getName()+" espera para entrar al mover.");
            entradaMover.lock();
            //Espera hasta que quede capacidad para subir al mover.
            while(capacidadRestante==0){
                puedeEntrarAlMover.await();
            }
            //Como logro entrar,disminuye la capacidad en 1
            capacidadRestante--;

            //Elimina un flag de verificar ronda flag, porque este pasajero ya esta en el mover.
            mutexVerificarRondaFlag.lock();
            verificarRondaFlag--;
                        //SaveConsoleOutput.imprimir("FLAG LUEGO DE PASAJERO ENTRO BIEN:"+verificarRondaFlag+verificarRondaFlag2);
            mutexVerificarRondaFlag.unlock();

            entradaMover.unlock();
            SaveConsoleOutput.imprimir("El pasajero "+Thread.currentThread().getName()+" entro al mover");

            //En el momento que haya un pasajero en mover, si pasa demasiado tiempo, el mover no espera a que se llene y arranca.
            numeroArribo = barrierEsperaViaje.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
        } catch (TimeoutException e) { 

            //Drena toda la capacidad restante. Es decir, se considera lleno para que ningun pasajero intente entrar al mover estando este de viaje.
            entradaMover.lock();
            capacidadRestante = 0;
            entradaMover.unlock();

            SaveConsoleOutput.imprimir("Se acabo el tiempo de espera del mover!!!"); 
            numeroArribo = 0;
        }
        SaveConsoleOutput.imprimir("numeroArribo: "+numeroArribo);
        if(numeroArribo == 0)
        {
            mutexInicioViajeMoverFlag.lock();
            inicioViajeMoverFlag = true;
            puedeIniciarViaje.signal();
            mutexInicioViajeMoverFlag.unlock();
            
            //inicioViajeMover.release();
        }

        SaveConsoleOutput.imprimir("El pasajero "+Thread.currentThread().getName()+" inicio su viaje en el mover.");
    }
    public void bajarEnTerminal(char terminal)
    {
        //f: char --> int que se aplica a cada terminal para facilitar la legibilidad del modulo bajarEnTerminal().
        int numTerminal = toNumTerminal(terminal);

            try {
                //Contador para saber cantidad de gente que baja en cada terminal.
                mutexOcupacionMover[numTerminal].lock();;
                ocupacionMover[numTerminal]++;
                mutexOcupacionMover[numTerminal].unlock();
                SaveConsoleOutput.imprimir("Pasajero "+Thread.currentThread().getName()+" debe bajar en la terminal "+terminal);
        
                pararEnTerminal.lock();

                bajarEnTerminal[numTerminal].await();
                SaveConsoleOutput.imprimir("Pasajero "+Thread.currentThread().getName()+" bajo en la terminal "+terminal);

                //Diminuye la ocupacion del mover en 1. Si es el ultimo pasajero en bajar, le avisa al mover que siga el viaje.
                mutexOcupacionMover[numTerminal].lock();
                ocupacionMover[numTerminal]--;
                if(ocupacionMover[numTerminal]==0)
                {
                    seguirViaje.signal();
                }
                mutexOcupacionMover[numTerminal].unlock();
            } catch (InterruptedException e) {
                e.printStackTrace();
        }
        pararEnTerminal.unlock();
    }

    //ControlMover tasks
    public boolean empezarNuevaRondaViaje(){
        boolean nuevaRonda = false;
        try {
            //Espera a que la flag a que el reloj o un pasajero le indique que debe ser si le toca trabajar.
            mutexVerificarRondaFlag.lock();
            while(verificarRondaFlag == 0 && !verificarRondaFlag2)
            {
                //SaveConsoleOutput.imprimir("ESPERA EL CONTROL DE FLAG:"+verificarRondaFlag+verificarRondaFlag2);
                podesVerificarElFlag.await();
            }
            //SaveConsoleOutput.imprimir("PASO EL CONTROL DE FLAG"+verificarRondaFlag+verificarRondaFlag2);
            if(verificarRondaFlag2){
                verificarRondaFlag2=false;
            }
            mutexVerificarRondaFlag.unlock(); 

            //Ve si le toca hacer otro viaje o terminar dia de trabajo.
            mutexRondaFlag.lock();
            nuevaRonda = rondaFlag;
            mutexRondaFlag.unlock();
        } catch (Exception e) {
        }
        return nuevaRonda;
    }
    //Me tome la libertad de extender el resultado y agregar un tiempo limite a la espera del mover en el ingreso.
    public void empezarDiaTrabajo(){
        
        try {
            mutexDiaTrabajoFlag.lock();
            //Hasta que el flag de empezar dia de trabajo no sea true, el mover no arranca.
            while(!diaTrabajoFlag){
                empezarElDiaDeTrabajo.await();
            }
            //No hace falta, pero ayuda a la escalabilidad en caso de que se quieran simular X cantidad de dias
            diaTrabajoFlag = false;
        mutexDiaTrabajoFlag.unlock();
        } catch (Exception e) {
            // TODO: handle exception
        }
        
    }
    public void esperarQueSeLleneMover()
    {
        try {
            SaveConsoleOutput.imprimir("El mover esta esperando que se llene de gente, o pase el tiempo de espera.");
            //Si el flag de que el mover debe iniciar viaje es false, el mover espera.
            mutexInicioViajeMoverFlag.lock();
            while(!inicioViajeMoverFlag){
                puedeIniciarViaje.await();
            }
            //El mover logro inciar viaje. Setea el flag a false de nuevo para que, al volver, deba esperar de nuevo.
            inicioViajeMoverFlag = false;
            mutexInicioViajeMoverFlag.unlock();

            SaveConsoleOutput.imprimir("El mover empezo su viaje.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void pararEnTerminal(char terminal)
    {
        int numTerminal = toNumTerminal(terminal);

        //No parara en esta terminal.
        if(ocupacionMover[numTerminal] == 0)
        {
            SaveConsoleOutput.imprimir("El mover no para en la terminal "+terminal);
        }
        //Si hay gente que deba parar en la terminal.
        else
        {
            pararEnTerminal.lock();  

            bajarEnTerminal[numTerminal].signalAll();
            SaveConsoleOutput.imprimir("El mover paro en la terminal "+terminal);
        
            //Hasta que no baje el ultimo pasajero, se queda esperando.
            try {

                seguirViaje.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        pararEnTerminal.unlock();
        }

        
    }
    public void pararEnIngreso()
    {

        //Reseteamos la barrera para esperar a que se llene el mover o se cumpla el tiempo limite.
        barrierEsperaViaje.reset();

        entradaMover.lock();
        //Habilitamos a que entren 'capacidad' numero de personas al mover
        capacidadRestante = capacidad;
        //Se le avisa a todos los pasajeros en fila que se habilitaron nuevos espacios en el mover.
        puedeEntrarAlMover.signalAll();
        entradaMover.unlock();
    }

    //Aux
    private int toNumTerminal(char terminal)
    {
        int n;
        switch(terminal)
        {
            case 'A': n = 0;break;
            case 'B': n = 1;break;
            default : n = 2;break;
        }
        return n;
    }

}
