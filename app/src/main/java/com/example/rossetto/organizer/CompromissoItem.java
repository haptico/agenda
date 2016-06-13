package com.example.rossetto.organizer;

/**
 * Created by admin on 13/06/2016.
 */
public class CompromissoItem {
    private String compromisso;
    private String data;
    private String hora;
    private String local;

    public CompromissoItem(String compromisso, String data, String hora, String local) {
        this.compromisso = compromisso;
        this.data = data;
        this.hora = hora;
        this.local = local;
    }

    public String getCompromisso() {
        return compromisso;
    }

    public String getData() {
        return data;
    }

    public String getHora() {
        return hora;
    }

    public String getLocal() {
        return local;
    }
}
