# Auditoría Sensores (cliente IPC)

Segundo proyecto de Android Studio que simula una **aplicación de auditoría externa**.
No comparte código ni base de datos con la app principal `com.example.crud`: lee el
historial de lecturas de la ESP32 únicamente a través del `ContentProvider` que esa app
expone.

## Cómo funciona

1. Declara el permiso de la app propietaria en el manifiesto:
   `com.example.crud.permission.LEER_HISTORIAL` (protectionLevel `normal`, se concede al
   instalar).
2. Declara `<queries><provider android:authorities="com.example.crud.provider" /></queries>`
   porque desde Android 11 los providers de otras apps están ocultos por el filtrado de
   visibilidad de paquetes.
3. `MainActivity` consulta con `ContentResolver.query()` la URI
   `content://com.example.crud.provider/historial_sensores` en un hilo de fondo y vuelca
   el `Cursor` en un `RecyclerView`.
4. Registra un `ContentObserver` sobre esa URI: cuando la app CRUD guarda una lectura nueva
   llama a `notifyChange()` y esta lista se refresca sola.

## Probarlo

```bash
# 1. Instalar primero la app propietaria (proyecto CRUD)
cd ../CRUD && ./gradlew installDebug

# 2. Instalar el cliente de auditoría
cd ../AuditoriaSensores && ./gradlew installDebug
```

Para comprobar el provider desde consola, sin pasar por la app:

```bash
adb shell content query --uri content://com.example.crud.provider/historial_sensores
```

Si la lista aparece vacía, abre la app CRUD para que reciba lecturas de Firebase (cada
lectura de LDR, temperatura y humedad se guarda en Room) y vuelve a consultar.
