package Hilos;

import java.time.temporal.ChronoUnit;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

import Otros.Pasaje;
import RecursosCompartidos.Ingreso;
import RecursosCompartidos.Mover;
import RecursosCompartidos.Terminal;
import RecursosCompartidos.CajasFreeshop;
import RecursosCompartidos.EntradaFreeshop;
import RecursosCompartidos.Hora;
import Otros.SaveConsoleOutput;

public class Pasajero implements Runnable {

    private Pasaje[] pasajes;
    private Pasaje pasaje;

    private Ingreso ingreso;
    private Mover mover;
    private Dictionary<Character, EntradaFreeshop> entradasFreeshops = new Hashtable<>();
    private Dictionary<Character, CajasFreeshop> cajasFreeshops = new Hashtable<>();
    private Hora hora;
    private Dictionary<Character, Terminal> terminales = new Hashtable<>();

    public Pasajero(Pasaje[] pasajes, Ingreso ingreso, Mover mover, EntradaFreeshop[] entradasFreeshops,
            CajasFreeshop[] cajasFreeshops, Hora hora, Terminal[] terminales) {
        this.pasajes = pasajes;
        this.ingreso = ingreso;
        this.mover = mover;

        // f: char --> Terminal
        this.entradasFreeshops.put('A', entradasFreeshops[0]);
        this.entradasFreeshops.put('B', entradasFreeshops[1]);
        this.entradasFreeshops.put('C', entradasFreeshops[2]);
        this.cajasFreeshops.put('A', cajasFreeshops[0]);
        this.cajasFreeshops.put('B', cajasFreeshops[1]);
        this.cajasFreeshops.put('C', cajasFreeshops[2]);

        this.hora = hora;

        this.terminales.put('A', terminales[0]);
        this.terminales.put('B', terminales[1]);
        this.terminales.put('C', terminales[2]);
    }

    @Override
    public void run() {

        boolean entroFreeshop, comproFreeshop;

        // Entra al aeropuerto
        entrarAlAeropuerto();

        // Ir al centro de informes
        int puestoAtencion = ingreso.irACentroDeInformes(pasaje);
        SaveConsoleOutput.imprimir("El pasajero " + Thread.currentThread().getName() + " con pasaje(" + pasaje
                + ") obtuvo el puesto de atencion " + puestoAtencion);

        // Pasar por hall central
        ingreso.pasarPorHallCentral(puestoAtencion);

        // Esperar a ser atendido en su puesto de atencion
        //ingreso.hacerFilaEnPuestoDeAtencion(puestoAtencion);

        // Es atendido en el puesto de atencion
        Object[] infoVuelo = ingreso.serAtendido(puestoAtencion, pasaje);
        // SaveConsoleOutput.imprimir("FLAG:El pasajero "+
        // Thread.currentThread().getName()+" con pasaje("+pasaje+") obtuvo terminal
        // "+infoVuelo[0]+", embarque "+infoVuelo[1]);

        // Intenta entrar al mover, o lo espera en su defecto.
        mover.entrarAlMover();

        // Viaja en el mover hasta que le toque bajar en la terminal
        mover.bajarEnTerminal((char) infoVuelo[0]);

        // Ver si puede entrar al freeshop. El booleano indica si lo logro
        entroFreeshop = tieneTiempoAntesDeEmbarque()
                && entradasFreeshops.get((Character) infoVuelo[0]).entrarAlFreeshop((char) infoVuelo[0]);
        if (entroFreeshop) {

            comproFreeshop = comprarEnFreeshop();
            if (comproFreeshop) {
                int caja = cajasFreeshops.get((Character) infoVuelo[0]).entrarACaja();

                SaveConsoleOutput.imprimir(
                        "El pasajero " + Thread.currentThread().getName() + " esta pagando en la caja " + caja);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                cajasFreeshops.get((Character) infoVuelo[0]).salirDeCaja(caja);
            }
            // Sale del freeshop.
            entradasFreeshops.get((Character) infoVuelo[0]).salirDeFreeshop((char) infoVuelo[0]);
        }

        //Si luego de pasar por el freeshop(indiferentemente de si entro), ya no le queda tiempo para su vuelo,
        //se va del aeropuerto
        if(tieneTiempoAntesDelDespegue()){
        // Espera en la sala de embarques a que le avisen que debe embarcar
        terminales.get((char) infoVuelo[0]).esperarAEmbarcar((int) infoVuelo[1]);
        }

    }

    // aux
    private void entrarAlAeropuerto() {
        Random r = new Random();
        pasaje = pasajes[r.nextInt(pasajes.length)];
        // TODO:QUE NO SE LE ASIGNE UN PASAJE QUE YA SE LE PASO EL TIEMPO
        SaveConsoleOutput
                .imprimir("El pasajero " + Thread.currentThread().getName() + " obtuvo el pasaje " + pasaje.toString());
        SaveConsoleOutput
                .imprimir("El pasajero " + Thread.currentThread().getName() + " obtuvo el pasaje " + pasaje.toString());
    }

    public boolean comprarEnFreeshop() {
        Random r = new Random();
        boolean compro = r.nextInt(4) >= 1;

        if (compro) {
            SaveConsoleOutput.imprimir("Pasajero " + Thread.currentThread().getName() + " comprara en el freeshop.");
            try {
                Thread.sleep(3500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            SaveConsoleOutput.imprimir(
                    "Pasajero " + Thread.currentThread().getName() + " solo mirara productos en el freeshop.");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return compro;
    }

    public boolean tieneTiempoAntesDeEmbarque() {
        long tiempoAntesDeEmbarque = hora.get().until(pasaje.getHorario(), ChronoUnit.MINUTES);
        SaveConsoleOutput.imprimir(
                "MINUTOS ANTES DE EMBARQUE DE " + Thread.currentThread().getName() + ": " + tiempoAntesDeEmbarque);
        if (tiempoAntesDeEmbarque >= 30) {
            SaveConsoleOutput.imprimir(
                    "El pasajero " + Thread.currentThread().getName() + " si tiene tiempo antes del embarque");
        } else {
            SaveConsoleOutput.imprimir(
                    "El pasajero " + Thread.currentThread().getName() + " no tiene tiempo antes del embarque");
        }

        return tiempoAntesDeEmbarque >= 30;
    }

    public boolean tieneTiempoAntesDelDespegue() {
        // Averigua cuanto tiempo le queda antes del despegue.
        // El despegue es 5 minutos despues del horario de embarque.
        long tiempoAntesDeDespegue = hora.get().until(pasaje.getHorario().plusMinutes(5), ChronoUnit.MINUTES);

        // Si le queda mas de 1 minuto antes del despegue, le da el tiempo a subir(no es
        // algo seguro, es un ultimo intento
        // del pasajero de correr y llegar al avion)
        if (tiempoAntesDeDespegue >= 1) {
            SaveConsoleOutput
                    .imprimir("El pasajero " + Thread.currentThread().getName() + " si tiene tiempo antes del desgue");
        } else {
            SaveConsoleOutput
                    .imprimir("El pasajero " + Thread.currentThread().getName() + " sno tiene tiempo antes del desgue, asi que se va del aeropuerto");

        }

        return tiempoAntesDeDespegue >=1;
    }
}
