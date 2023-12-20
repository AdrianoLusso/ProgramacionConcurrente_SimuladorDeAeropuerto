package Otros;

import java.lang.reflect.Array;

public class InfoVuelo {
    
    private char terminal;
    private int puestoEmbarque;
    private Pasaje pasaje;

    public InfoVuelo(char terminal, int puestoEmbarque, Pasaje pasaje)
    {
        this.terminal = terminal;
        this.puestoEmbarque = puestoEmbarque;
        this.pasaje = pasaje;
    }
    
    public Pasaje getPasaje()
    {
        return pasaje;
    }
    public char getTerminal(){
        return terminal;
    }
    public int getPuestoEmbarque(){
        return puestoEmbarque;
    }

    public String toString(){
        String string = "terminal:"+terminal+", puestoEmbarque:"+puestoEmbarque+", pasaje:("+pasaje.toString()+")"; 
        return string;}

    //Transforma la informacion de un vuelo en la version publica que se le entrega a un pasajero luego de ser atendido en el puesto de atencion.
    public Object[] toPublicInfoVuelo()
    {
        Object[] res = {terminal,puestoEmbarque};
        return res;
    }
}
