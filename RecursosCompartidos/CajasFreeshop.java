package RecursosCompartidos;

import java.util.concurrent.SynchronousQueue;

import Otros.SaveConsoleOutput;

public class CajasFreeshop {

    private int proximoNumeroCaja = 1;
    private int ultimoNumeroCajaAtendido = 0;

    private boolean caja1Libre = true;

    public CajasFreeshop() {
    }

    public synchronized int entrarACaja() {
        // Por defecto, cuando le toque pagar ira a la caja 2
        int cajaAsignada = 2;

        // El numero de caja que le corresponde es el marcado como el proximo numero de
        // caja a ser tomado.
        int numeroCajaTomado = proximoNumeroCaja++;

        SaveConsoleOutput.imprimir(
                "El pasajero " + Thread.currentThread().getName() + " tomo el numero de caja " + numeroCajaTomado);
        SaveConsoleOutput
                .imprimir("El pasajero " + Thread.currentThread().getName() + " debe esperar su turno para pagar.");
        // Pasara a pagar solo cuando su numero numero de caja cumpla la siguiente
        // condicion.
        while (ultimoNumeroCajaAtendido + 1 != numeroCajaTomado && ultimoNumeroCajaAtendido + 2 != numeroCajaTomado) {
            try {
                this.wait();
                SaveConsoleOutput.imprimir("Pasajero " + Thread.currentThread().getName() + " miran su numero tomado.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SaveConsoleOutput.imprimir("El pasajero " + Thread.currentThread().getName() + " le toca pagar.");

        // Si la caja 1 esta libre, entonces su numero de caja asignada cambia a la caja
        // 1.
        if (caja1Libre) {
            cajaAsignada = 1;
            caja1Libre = false;
        }

        // retorna el numero de caja que le toco al pasajero.
        return cajaAsignada;
    }
    public synchronized void salirDeCaja(int caja) {
        SaveConsoleOutput.imprimir("Pasajero " + Thread.currentThread().getName() + " deja la caja " + caja);

        // Si su caja era la 1, la marca como libre.
        if (caja == 1) {
            caja1Libre = true;
        }

        // Se aumenta el contador que marca cual fue el ultimo numero atendido.
        ultimoNumeroCajaAtendido++;

        this.notifyAll();
    }
}
