package Otros;

import java.time.LocalTime;

public class Pasaje {

    private int destino;
    private int aerolineas;
    private LocalTime horario;

    public int getDestino() {
        return this.destino;
    }

    public void setDestino(int destino) {
        this.destino = destino;
    }

    public int getAerolineas() {
        return this.aerolineas;
    }

    public void setAerolineas(int aerolineas) {
        this.aerolineas = aerolineas;
    }

    public LocalTime getHorario(){
        return horario;
    }
    public Pasaje(int des, int aero, int hora, int minutos) {
        destino = des;
        aerolineas = aero;
        horario = LocalTime.of(hora, minutos, 0);
    }

    public String toString(){
        String string = "destino:"+destino+", aerolineas:"+aerolineas+", horario:"+horario.toString(); 
        return string;}
}
