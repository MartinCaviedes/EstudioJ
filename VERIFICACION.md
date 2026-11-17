# Resultado de validación

Validación realizada el 17 de noviembre de 2026 en Windows, con JDK 21.

Comando ejecutado desde la carpeta del proyecto:

```powershell
.\gradlew.bat --no-daemon check run --console=plain
```

Resultado: **BUILD SUCCESSFUL**. Kotlin compiló los archivos principales y los de verificación. La tarea `verificar`, incluida en `check`, terminó con **64 comprobaciones superadas**. La tarea `test` se omite intencionalmente porque las comprobaciones son un programa ejecutable y no casos JUnit.

La demostración `run` confirmó:

- Los cinco tickets sugeridos, incluyendo el ticket gratuito de MO22TO.
- Recaudación total de $13.238,75 y promedio de $2.647,75.
- Recaudación de $7.140,00 en particulares, $0,00 en motos y $6.098,75 en camionetas.
- Mayor permanencia: XY99ZA con 180 minutos.
- Diez espacios disponibles tras las cinco salidas y la rehabilitación del espacio 10.
- Consulta de estados Procesando durante las esperas del sensor.
- Mensajes controlados por patente inválida, cliente inválido, vehículo inexistente, tiempo inválido y estacionamiento sin capacidad.
- Continuidad hasta el mensaje final de la demostración.

Se compiló y ejecutó mediante Gradle en la terminal. No se realizó una prueba manual de la interfaz de IntelliJ IDEA. El proyecto incluye los archivos de Gradle necesarios para importarlo y ejecutarlo allí.

La primera prueba sin conexión no pudo resolver el plugin con la caché disponible. La validación final utilizó resolución normal de dependencias. Una máquina nueva necesita JDK 21 e internet para la primera sincronización.
