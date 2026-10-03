package com.example.auditoria;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuditoriaAdapter extends RecyclerView.Adapter<AuditoriaAdapter.LecturaViewHolder> {

    private List<LecturaAuditada> lecturas = new ArrayList<>();
    private final SimpleDateFormat formatoFechaHora =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    public void actualizarLista(List<LecturaAuditada> nuevaLista) {
        this.lecturas = nuevaLista != null ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LecturaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lectura, parent, false);
        return new LecturaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull LecturaViewHolder holder, int position) {
        LecturaAuditada lectura = lecturas.get(position);
        holder.tvSensor.setText(String.format(Locale.getDefault(), "#%d  %s (%s)",
                lectura.id, lectura.sensor, lectura.dispositivo));
        holder.tvValor.setText(String.format(Locale.getDefault(), "%.1f %s",
                lectura.valor, lectura.unidad));
        holder.tvFechaHora.setText(formatoFechaHora.format(new Date(lectura.timestamp)));
    }

    @Override
    public int getItemCount() {
        return lecturas.size();
    }

    static class LecturaViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSensor;
        final TextView tvValor;
        final TextView tvFechaHora;

        LecturaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSensor = itemView.findViewById(R.id.tv_item_sensor);
            tvValor = itemView.findViewById(R.id.tv_item_valor);
            tvFechaHora = itemView.findViewById(R.id.tv_item_fecha_hora);
        }
    }
}
