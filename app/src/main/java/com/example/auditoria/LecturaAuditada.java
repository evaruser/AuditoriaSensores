package com.example.auditoria;

/** Registro del historial tal como llega por IPC desde el ContentProvider de la app CRUD. */
public class LecturaAuditada {

    public final long id;
    public final String sensor;
    public final double valor;
    public final String unidad;
    public final long timestamp;
    public final String dispositivo;

    public LecturaAuditada(long id, String sensor, double valor, String unidad,
                           long timestamp, String dispositivo) {
        this.id = id;
        this.sensor = sensor;
        this.valor = valor;
        this.unidad = unidad;
        this.timestamp = timestamp;
        this.dispositivo = dispositivo;
    }
}
