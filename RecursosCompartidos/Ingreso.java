package RecursosCompartidos;

import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;

import Otros.Pasaje;
import Otros.SaveConsoleOutput;

public class Ingreso {
    
    private int cantAerolineas;

    //Permite que el empleado de informes atiendo a un pasajero a la vez
    Semaphore mutexInforme = new Semaphore(0, true);

    //Indica cuando se empieza a trabajar en el ingreso
    Semaphore diaTrabajo = new Semaphore(0);

    //Flag que indica si habra otra ronda de trabajo para el empleado de informe.
    boolean flagRondaInforme = true;
    Semaphore mutexFlagRondaInforme = new Semaphore(1);
    //Permite que el empleado de informe acceda al flag unicamente cuando haya informacion valida en el mismo.
    Semaphore accesoAFlagInforme = new Semaphore(0);

    //Funciona parecido al flag de arriba ,pero para los empleados de atencion
    boolean[] flagRondaAtencion;
    Semaphore[] mutexFlagRondaAtencion;
    Semaphore[] accesoFlagAtencion;

    //Representa las filas externas en el hall central.Es decir, la espera en el hall en caso de que el puesto de atencion este completo.
    Semaphore[] filasHallCentral;
    
    //Filas de cada puesto de atencion.
    Semaphore[] filasPuestoAtencion;

    //Puesto de envio-recibimiento entre el pasajero y el empleado de informe.
    SynchronousQueue queueEmpleadoInformes = new SynchronousQueue<>(true);

    //Puestos de envio-recibimiento entre el pasajero y el respectivo empleado de atencion.
    SynchronousQueue[] queueEmpleadoAtencion;

    public Ingreso(int cantAerolineas,int maxPersXFila)
    {
        this.cantAerolineas = cantAerolineas;

        filasHallCentral = new Semaphore[cantAerolineas];
        filasPuestoAtencion = new Semaphore[cantAerolineas];
        queueEmpleadoAtencion = new SynchronousQueue[cantAerolineas];

        flagRondaAtencion = new boolean[cantAerolineas];
        mutexFlagRondaAtencion = new Semaphore[cantAerolineas];
        accesoFlagAtencion = new Semaphore[cantAerolineas];


        for (int i=0;i<cantAerolineas;i++)
        {
            filasHallCentral[i] = new Semaphore(maxPersXFila, true);
            filasPuestoAtencion[i] = new Semaphore(0,true);
            queueEmpleadoAtencion[i] = new SynchronousQueue<>(true);
        
            flagRondaAtencion[i] = true;
            mutexFlagRondaAtencion[i] = new Semaphore(1);
            accesoFlagAtencion[i] = new Semaphore(0);
        }

    }
    public void empezarDiaTrabajo()
    {
        try {
            diaTrabajo.acquire();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Pasajero Tasks
    public int irACentroDeInformes(Pasaje pasaje)
    {
        int puestoAtencion = -1;
        try {
            //Le permite al empleado de informe ver el flag que le dice si debe,o no, hacer otra ronda de trabajo.
            accesoAFlagInforme.release();

            //El pasajero empieza a ser atendido.
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" esta esperando para entrar al puesto de informes");
            mutexInforme.acquire();
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" entro al puesto de informes");


            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" le da el pasaje al emp de informe");
            //El pasajero le da el pasaje.
            queueEmpleadoInformes.put(pasaje);

            //El pasajero escucha que puesto de atencion el corresponde
            puestoAtencion = (int) queueEmpleadoInformes.take();
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" tomo exitosamente el dato del emp de informe");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" dejo al puesto de informes");
        //El pasajero deja de ser atendido.
        mutexInforme.release();

        return puestoAtencion;
    }
    public void pasarPorHallCentral(int puestoAtencion)
    {
        try {
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" esta en el hall central.");
            if(!filasHallCentral[puestoAtencion].tryAcquire())
            {
                SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+"debe esperar en el hall a que haya lugar para hacer fila en su puesto de atencion.");
                filasHallCentral[puestoAtencion].acquire();
                SaveConsoleOutput.imprimir("El guardia le dice al pasajero "+Thread.currentThread().getName()+" que pase al puesto de atencion");
            }
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" entro al puesto de atencion.");

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void hacerFilaEnPuestoDeAtencion(int puestoAtencion)
    {
        try {
            //Le dice al empleado de atencion que debe atender a este pasajero porque ya esta en fila.No puede hacerlo esperar en fila para nada.
            accesoFlagAtencion[puestoAtencion].release();

            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" hace fila en el puesto de atencion "+puestoAtencion);
            filasPuestoAtencion[puestoAtencion].acquire();
            filasHallCentral[puestoAtencion].release();
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" sera atendido en el puesto de atencion "+puestoAtencion);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public Object[] serAtendido(int puestoAtencion,Pasaje pasaje)
    {
        Object[] infoVuelo = null;
        try {
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" le entrega el pasaje al emp de atencion.");
            queueEmpleadoAtencion[puestoAtencion].put(pasaje);

            infoVuelo = (Object[]) queueEmpleadoAtencion[puestoAtencion].take();
            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" recibio la terminal y embarque.");

            SaveConsoleOutput.imprimir("El pasajero "+ Thread.currentThread().getName()+" deja el puesto de atencion.");
            filasPuestoAtencion[puestoAtencion].release();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return infoVuelo;
    }

    //Emp de informe Tasks
    public boolean empezarNuevaRondaInforme()
    {
        boolean nuevaRonda = false;
        try {
            //Espera a que el reloj o un nuevo pasajero por atender le indique que debe ver si le toca trabajar.
            accesoAFlagInforme.acquire();

            //Ve si le toca atender a un cliente o terminar su dia de trabajo.
            mutexFlagRondaInforme.acquire();
            nuevaRonda = flagRondaInforme;
            mutexFlagRondaInforme.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return nuevaRonda;
    }
    public Pasaje empezarInformeAPasajero()
    {
        Pasaje pasaje = null;
        
        //Recibe el pasaje del pasajero.
        try {
            SaveConsoleOutput.imprimir("El emp de informe espera a recibir un pasaje");
            pasaje = (Pasaje) queueEmpleadoInformes.take();
            SaveConsoleOutput.imprimir("El emp de informe recibe un pasaje");

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return pasaje;
    }
    public void terminarInformeAPasajero(int puestoEmbarque)
    {
        try {
            SaveConsoleOutput.imprimir("El emp de informe le quiere dar el dato al pasajero");
            queueEmpleadoInformes.put(puestoEmbarque);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Emp atencion Tasks
    public boolean empezarNuevaRondaAtencion(int puestoAtencion)
    {
        boolean nuevaRonda = false;
        try {
            //Espera a que el reloj o un nuevo pasajero por atender le indique que debe ver si le toca trabajar.
            accesoFlagAtencion[puestoAtencion].acquire();

            //Ve si le toca atender a un cliente o terminar su dia de trabajo.
            mutexFlagRondaAtencion[puestoAtencion].acquire();
            nuevaRonda = flagRondaAtencion[puestoAtencion];
            mutexFlagRondaAtencion[puestoAtencion].release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return nuevaRonda;
    }
    public Pasaje empezarAtencionAPasajero(int puestoAtencion)
    {
        Pasaje pasaje = null;
         try {
            pasaje = (Pasaje) queueEmpleadoAtencion[puestoAtencion].take();
            SaveConsoleOutput.imprimir("El empleado de atencion "+puestoAtencion+" recibe el pasaje.");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return pasaje;
    }
    public void terminarAtencionAPasajero(int puestoAtencion, Object[] publicInfoVuelo)
    {
        try {
            SaveConsoleOutput.imprimir("El empleado de atencion "+puestoAtencion+" entrega terminal y embarque.");
            queueEmpleadoAtencion[puestoAtencion].put(publicInfoVuelo);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Reloj tasks
    public void abrirElIngreso()
    {
        //Permite a todos los empleados de atencion y al empleado de informe empezar su dia de trabajo.
        //Un permiso por cada empleado.
        diaTrabajo.release(cantAerolineas+1);

        //Permite a los pasajeros entrar,efectivamente, al puesto de informes.Antes esperaban en la fila.
        mutexInforme.release();

        //Permite a los pasajeros entrar,efectivamente, a los puestos de atencion.Antes esperaban en la fila.
        for(int i=0;i<cantAerolineas;i++)
        {
            filasPuestoAtencion[i].release();
        }
    }
    public void cerrarElIngreso()
    {
        try {
            //Se marca que el empleado de informes no debe hacer mas rondas de trabajo
            mutexFlagRondaInforme.acquire();
            flagRondaInforme = false;
            mutexFlagRondaInforme.release();

            //Se marca a los empleados de atencion que no deben hacer mas rondas de trabajo
            for(int i=0;i<cantAerolineas;i++)
            {
                mutexFlagRondaAtencion[i].acquire();
                flagRondaAtencion[i] = false;
                mutexFlagRondaAtencion[i].release();

                //Permite a los empleados de atencion ver la info de que ya no trabajan mas por hoy
                accesoFlagAtencion[i].release();
            }

            //Le permite al empleado de informes ver la info de que ya no trabaja mas por hoy.
            accesoAFlagInforme.release();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
