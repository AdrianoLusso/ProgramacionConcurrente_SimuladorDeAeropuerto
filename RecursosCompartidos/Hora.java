package RecursosCompartidos;

import java.time.LocalTime;
import java.util.concurrent.locks.ReentrantLock;

public class Hora {
    

    LocalTime hora;
    ReentrantLock mutexHora = new ReentrantLock();

    public Hora(){
        hora = LocalTime.of(0, 0, 0);
    }

    //Reloj tasks
    public void pasarMinuto()
    {
        mutexHora.lock();
        hora = hora.plusMinutes(1);
        mutexHora.unlock();
    }

    //Aux
    public LocalTime get()
    {
        mutexHora.lock();
        LocalTime res = hora;
        mutexHora.unlock();
        return res;
    }
}
