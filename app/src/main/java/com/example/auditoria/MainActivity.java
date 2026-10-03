package com.example.auditoria;

import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tarea: cliente IPC. Esta app es un proyecto independiente (simula una auditoría externa)
 * y lee el historial de sensores de la app com.example.crud únicamente a través del
 * ContentResolver, sin abrir el archivo de base de datos de la otra app.
 */
public class MainActivity extends AppCompatActivity {

    private static final int CODIGO_SOLICITUD_PERMISO = 1001;

    private TextView tvEstado;
    private AuditoriaAdapter adapter;

    /** El ContentResolver bloquea, así que la consulta nunca se hace en el hilo principal. */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handlerPrincipal = new Handler(Looper.getMainLooper());

    private ContentObserver observador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Desde Android 15 las apps dibujan de borde a borde: hay que separar el contenido
        // de la barra de estado y de la de navegación.
        View rootLayout = findViewById(R.id.main_auditoria);
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top + 16,
                    v.getPaddingRight(), systemBars.bottom + 16);
            return insets;
        });

        tvEstado = findViewById(R.id.tv_estado);
        Button btnConsultar = findViewById(R.id.btn_consultar);
        RecyclerView recyclerView = findViewById(R.id.recycler_auditoria);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AuditoriaAdapter();
        recyclerView.setAdapter(adapter);

        btnConsultar.setOnClickListener(v -> consultarHistorial());

        // Cuando la app propietaria inserta una lectura notifica la URI: aquí se refresca solo.
        observador = new ContentObserver(handlerPrincipal) {
            @Override
            public void onChange(boolean selfChange, @Nullable Uri uri) {
                consultarHistorial();
            }
        };

        asegurarPermiso();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (tienePermiso()) {
            getContentResolver().registerContentObserver(
                    HistorialContract.CONTENT_URI, true, observador);
            consultarHistorial();
        }
    }

    @Override
    protected void onStop() {
        getContentResolver().unregisterContentObserver(observador);
        super.onStop();
    }

    private boolean tienePermiso() {
        return ContextCompat.checkSelfPermission(this, HistorialContract.PERMISO_LECTURA)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * El permiso es de nivel "normal", así que el sistema lo concede al instalar por el
     * <uses-permission> del manifiesto. Se solicita de todos modos para dejar explícito el
     * flujo y detectar el caso en que la app propietaria no esté instalada.
     */
    private void asegurarPermiso() {
        if (!tienePermiso()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{HistorialContract.PERMISO_LECTURA}, CODIGO_SOLICITUD_PERMISO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_SOLICITUD_PERMISO) {
            consultarHistorial();
        }
    }

    /** Consulta la URI expuesta por la app CRUD y vuelca el Cursor en la lista. */
    private void consultarHistorial() {
        mostrarEstado("Consultando " + HistorialContract.CONTENT_URI + " ...");

        executor.execute(() -> {
            String[] projection = {
                    HistorialContract.COLUMNA_ID,
                    HistorialContract.COLUMNA_SENSOR,
                    HistorialContract.COLUMNA_VALOR,
                    HistorialContract.COLUMNA_UNIDAD,
                    HistorialContract.COLUMNA_TIMESTAMP,
                    HistorialContract.COLUMNA_DISPOSITIVO
            };

            List<LecturaAuditada> lecturas = new ArrayList<>();
            try (Cursor cursor = getContentResolver().query(
                    HistorialContract.CONTENT_URI, projection, null, null,
                    HistorialContract.ORDEN_POR_DEFECTO)) {

                if (cursor == null) {
                    publicar(null, "No se encontró el provider. ¿Está instalada la app CRUD?");
                    return;
                }

                int idxId = cursor.getColumnIndexOrThrow(HistorialContract.COLUMNA_ID);
                int idxSensor = cursor.getColumnIndexOrThrow(HistorialContract.COLUMNA_SENSOR);
                int idxValor = cursor.getColumnIndexOrThrow(HistorialContract.COLUMNA_VALOR);
                int idxUnidad = cursor.getColumnIndexOrThrow(HistorialContract.COLUMNA_UNIDAD);
                int idxTimestamp = cursor.getColumnIndexOrThrow(HistorialContract.COLUMNA_TIMESTAMP);
                int idxDispositivo = cursor.getColumnIndexOrThrow(HistorialContract.COLUMNA_DISPOSITIVO);

                while (cursor.moveToNext()) {
                    lecturas.add(new LecturaAuditada(
                            cursor.getLong(idxId),
                            cursor.getString(idxSensor),
                            cursor.getDouble(idxValor),
                            cursor.getString(idxUnidad),
                            cursor.getLong(idxTimestamp),
                            cursor.getString(idxDispositivo)));
                }

                String mensaje = lecturas.isEmpty()
                        ? "El provider respondió, pero el historial está vacío."
                        : String.format(Locale.getDefault(),
                        "%d registros leídos por IPC desde la app CRUD.", lecturas.size());
                publicar(lecturas, mensaje);

            } catch (SecurityException e) {
                publicar(null, "Permiso denegado: falta " + HistorialContract.PERMISO_LECTURA);
            } catch (IllegalArgumentException e) {
                publicar(null, "URI o columna no válida: " + e.getMessage());
            } catch (Exception e) {
                publicar(null, "Error al consultar el provider: " + e.getMessage());
            }
        });
    }

    private void publicar(List<LecturaAuditada> lecturas, String mensaje) {
        handlerPrincipal.post(() -> {
            if (lecturas != null) {
                adapter.actualizarLista(lecturas);
            }
            tvEstado.setText(mensaje);
        });
    }

    private void mostrarEstado(String mensaje) {
        tvEstado.setText(mensaje);
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
