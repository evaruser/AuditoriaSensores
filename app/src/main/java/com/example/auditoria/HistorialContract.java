package com.example.auditoria;

import android.net.Uri;

/**
 * Copia del contrato publicado por la app propietaria (com.example.crud).
 *
 * Una app cliente no comparte código con ella, así que replica solo lo público: autoridad,
 * URI y nombres de columna. Nunca se toca el archivo .db de la otra app.
 */
public final class HistorialContract {

    public static final String AUTHORITY = "com.example.crud.provider";

    public static final String PERMISO_LECTURA = "com.example.crud.permission.LEER_HISTORIAL";

    /** content://com.example.crud.provider/historial_sensores */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/historial_sensores");

    public static final String COLUMNA_ID = "id";
    public static final String COLUMNA_SENSOR = "sensor";
    public static final String COLUMNA_VALOR = "valor";
    public static final String COLUMNA_UNIDAD = "unidad";
    public static final String COLUMNA_TIMESTAMP = "timestamp";
    public static final String COLUMNA_DISPOSITIVO = "dispositivo";

    public static final String ORDEN_POR_DEFECTO = COLUMNA_TIMESTAMP + " DESC";

    private HistorialContract() {
    }
}
