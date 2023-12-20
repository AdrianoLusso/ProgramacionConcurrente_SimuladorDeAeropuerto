import java.io.Console;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import Hilos.ControlMover;
import Hilos.EmpleadoAtencion;
import Hilos.EmpleadoEmbarque;
import Hilos.EmpleadoInforme;
import Hilos.Pasajero;
import Hilos.Reloj;
import Otros.InfoVuelo;
import Otros.Pasaje;
import RecursosCompartidos.CajasFreeshop;
import RecursosCompartidos.EntradaFreeshop;
import RecursosCompartidos.Hora;
import RecursosCompartidos.Ingreso;
import RecursosCompartidos.Mover;
import RecursosCompartidos.Terminal;
import Otros.SaveConsoleOutput;

public class main {

    public static void main(String[] args) {

        SaveConsoleOutput.openFileWriter();
        Scanner in = new Scanner(System.in);
        Random r = new Random();

        //Declaracion de recursos compartidos
        Ingreso ingreso;
        Mover mover;
        EntradaFreeshop[] entradaFreeshops;
        CajasFreeshop[] cajasFreeshops;
        Hora hora;
        Terminal[] terminales;
        
        // Declaracion de hilos
        Thread[] pasajero;
        Thread empInforme;
        Thread[] empsAtencion;
        Thread controlMover;
        Thread reloj;
        Thread[] empsEmbarque;

        // Declaracion de otros
        int cantAerolineas = 4, cantPasajeros = 17, maxPersXFila = 2, cantDestinos = 7,
        cantVuelos = 4,capacidadMover=7;
        int[] capacidadFreeshop = {6,4,3};
        int[] embarquesA = {1,2,3,4,5,6,7};
        int[] embarquesB = {8,9,10,11,12,13,14,15};
        int[] embarquesC = {16,17,18,19,20};
        Pasaje[] pasaje;
        InfoVuelo[] infoVuelo;

        
        
        /* if (false) {

            // Consultas al usuario
            SaveConsoleOutput.imprimir("Cuantas aerolineas existen?:");
            cantAerolineas = in.nextInt();

            SaveConsoleOutput.imprimir("Cuantos pasajeros existen?:");
            cantPasajeros = in.nextInt();

            SaveConsoleOutput.imprimir("Cual es el maximo de personas que puede haber en "
                    + "las filas de los centros de atencion?:");
            maxPersXFila = in.nextInt();

            SaveConsoleOutput.imprimir("Cual es la cantidad existente de destinos?:");
            cantDestinos = in.nextInt();

            SaveConsoleOutput.imprimir("Cual es la cantidad existente de vuelos a suceder?:");
            cantVuelos = in.nextInt();

            SaveConsoleOutput.imprimir("Cual es la capacidad maxima del mover?:");
            capacidadMover = in.nextInt();
        } */

        //Creacion de recursos compartidos
        ingreso = new Ingreso(cantAerolineas,maxPersXFila);
        mover = new Mover(capacidadMover);
        entradaFreeshops = new EntradaFreeshop[3];
        cajasFreeshops = new CajasFreeshop[3];
        for (int i =0;i<3;i++){
            entradaFreeshops[i] = new EntradaFreeshop(capacidadFreeshop[i]);
            cajasFreeshops[i] = new CajasFreeshop();
        }
        hora = new Hora();
        terminales = new Terminal[3];
        terminales[0] = new Terminal(embarquesA);
        terminales[1] = new Terminal(embarquesB);
        terminales[2] = new Terminal(embarquesC);


        // Creacion de vuelos
        pasaje = new Pasaje[cantVuelos];
        infoVuelo = new InfoVuelo[cantVuelos];

        LinkedList embarquesOcupados = new LinkedList();
        for (int i = 0; i < cantVuelos; i++) {
            pasaje[i] = new Pasaje(r.nextInt(cantDestinos), r.nextInt(cantAerolineas),
                    r.nextInt(8,22), r.nextInt(60));

            int puestoEmbarque;
            do{
                puestoEmbarque = r.nextInt(20) + 1;
            }while(embarquesOcupados.contains(puestoEmbarque));
            embarquesOcupados.add(puestoEmbarque);

            char terminal;

            // En funcion del puesto de embarque, decido la terminal
            if (puestoEmbarque < 8) {
                terminal = 'A';
            } else if (puestoEmbarque > 15) {
                terminal = 'C';
            } else {
                terminal = 'B';
            }
            infoVuelo[i] = new InfoVuelo(terminal, puestoEmbarque, pasaje[i]);
        }

        SaveConsoleOutput.imprimir("PASAJES:");
        for (int i = 0; i < cantVuelos; i++) {
            SaveConsoleOutput.imprimir(pasaje[i].toString());
        }
        SaveConsoleOutput.imprimir("INFOVUELOS:");
        for (int i = 0; i < cantVuelos; i++) {
            SaveConsoleOutput.imprimir(infoVuelo[i].toString());
        }
        SaveConsoleOutput.imprimir("----------------------------");
        // Creacion de pasajeros
        pasajero = new Thread[cantPasajeros];
        for (int i = 0; i < cantPasajeros; i++) {
            pasajero[i] = new Thread(new Pasajero(pasaje,ingreso,mover,entradaFreeshops,cajasFreeshops,hora,terminales),""+i);
        }

        // Creacion de empleado de informe 
        empInforme = new Thread(new EmpleadoInforme(ingreso,hora));

        //Creacion de empleados de atencion
        empsAtencion = new Thread[cantAerolineas];
        for (int i =0;i<cantAerolineas;i++)
        {
            empsAtencion[i] = new Thread(new EmpleadoAtencion(ingreso, i, infoVuelo));
        }
        
        //Creacion de controlMover
        controlMover = new Thread(new ControlMover(mover,hora));

        //Creacion de reloj
        reloj = new Thread(new Reloj(hora,ingreso,mover,entradaFreeshops,terminales,infoVuelo));

        //Creacion de empleados de embarque
        empsEmbarque = new Thread[3];
        empsEmbarque[0] = new Thread(new EmpleadoEmbarque(terminales[0], hora),"Empleado de embarque A");
        empsEmbarque[1] = new Thread(new EmpleadoEmbarque(terminales[1], hora),"Empleado de embarque B");
        empsEmbarque[2] = new Thread(new EmpleadoEmbarque(terminales[2], hora),"Empleado de embarque C");

        //Iniciar hilos
        reloj.start();;
        empInforme.start();
        controlMover.start();
        for (int i = 0; i < cantAerolineas; i++) {
            empsAtencion[i].start();
        }
        for (int i = 0; i < cantPasajeros; i++) {
            pasajero[i].start();
        }
        empsEmbarque[0].start();
        empsEmbarque[1].start();
        empsEmbarque[2].start();

        in.close();
        //Esperar el fin de todos los hilos para dar por finalizada la simulacion
        try {
            reloj.join();
            empInforme.join();
            controlMover.join();
            for (int i = 0; i < cantAerolineas; i++) {
                empsAtencion[i].join();;
            } 
            for (int i = 0; i < cantPasajeros; i++) {
                pasajero[i].join();
            }
            empsEmbarque[0].join();
            empsEmbarque[1].join();
            empsEmbarque[2].join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        SaveConsoleOutput.imprimir("SIMULACION TERMINADA :D");
        SaveConsoleOutput.closeFileWriter();
    }
}
