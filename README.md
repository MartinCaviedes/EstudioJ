# UrbanPark: solución de estudio en Kotlin

Aplicación de consola basada en los archivos de `Caso a resolver` y en los ejemplos de `Ejemplos`. La solución conserva el nivel de las guías: clases, herencia, `override`, `sealed class`, listas, funciones de orden superior, excepciones y corrutinas. La demostración utiliza datos predefinidos, como el ejemplo del compañero; el enunciado no exige un menú interactivo.

## Abrir y ejecutar

1. Abrir **esta carpeta UrbanPark** como proyecto en IntelliJ IDEA.
2. Seleccionar **JDK 21** como SDK del proyecto y JVM de Gradle.
3. Esperar la sincronización de Gradle. La primera vez puede necesitar internet para descargar las dependencias.
4. Ejecutar `main()` de `src/main/kotlin/Main.kt`, o la tarea Gradle `run`.

Desde PowerShell, ubicado en esta carpeta:

```powershell
.\gradlew.bat run
.\gradlew.bat verificar
.\gradlew.bat check
```

`run` muestra las operaciones, los errores controlados y el reporte. `verificar` ejecuta comprobaciones que fallan si una regla no se cumple. `check` incluye esas verificaciones. No hace falta instalar Kotlin o Gradle por separado: se incluye el wrapper de Gradle.

Las comprobaciones son un programa con `check`, no pruebas JUnit; por eso la tarea estándar `test` está deshabilitada. Para validarlas se utiliza `verificar` o `check`.

Versiones declaradas: Kotlin 2.2.10, kotlinx-coroutines-core 1.10.2, Gradle 9.6.0 y JDK 21. Se eligieron Kotlin y corrutinas disponibles en el entorno; Kotlin cumple el mínimo 1.9 del caso. El wrapper proviene del ejemplo adjunto.

## Archivos

| Archivo | Responsabilidad |
| --- | --- |
| `src/main/kotlin/Vehiculos.kt` | Datos comunes, validación de patente y tarifas de cada subtipo. |
| `src/main/kotlin/EstadosYTicket.kt` | Cuatro estados, espacio y comprobante de salida. |
| `src/main/kotlin/SistemaUrbanPark.kt` | Reservas, entradas/salidas, historial, consultas y reporte. |
| `src/main/kotlin/Consola.kt` | Formato de montos, conversión de cliente y mensajes de errores. |
| `src/main/kotlin/Main.kt` | Escenario demostrativo con los datos sugeridos y corrutinas hijas. |
| `src/test/kotlin/Verificacion.kt` | Reglas y casos límite reproducibles sin framework adicional. |
| `GUIA_DE_ESTUDIO.md` | Análisis, decisiones, fórmulas, pauta y ejercicios para practicar. |

## Resultado esperado de los cinco datos sugeridos

| Patente | Minutos | Monto |
| --- | ---: | ---: |
| AB12CD | 75 | $1.785,00 |
| XY99ZA | 180 | $5.355,00 |
| MO22TO | 25 | $0,00 |
| CA44RG | 120 | $3.867,50 |
| CA77RG | 45 | $2.231,25 |

Total **$13.238,75**, promedio **$2.647,75**, cinco vehículos atendidos y diez espacios disponibles después de rehabilitar el espacio 10. Particulares: $7.140,00; camionetas: $6.098,75; motos: $0,00. Mayor permanencia: XY99ZA, 180 minutos. Mayor recaudación por tipo: Particular. Abonados del historial: AB12CD.

Los tickets se numeran en el orden en que se completan las salidas; con concurrencia, no conviene depender de una patente fija para cada número. La demostración de capacidad usa otro sistema para conservar intacto el reporte anterior.

## Decisiones del enunciado

- La moto con tiempo positivo menor de 30 minutos puede pagar cero. Es la excepción de R1 y de los datos sugeridos a la prohibición general de R3. Se rechaza cero para los demás casos.
- El 20% de abonado se aplica a particulares, siguiendo la regla específica R1. El glosario lo presenta de forma general: esta ambigüedad se explica en la guía.
- Se cobra proporcionalmente por minuto: no se redondea a una hora completa. Se conservan decimales y solo se formatea la salida; el caso no fija una regla de redondeo a pesos enteros.
- Las corrutinas comparten el hilo de `runBlocking`. Este ejercicio no implementa acceso desde varios hilos; no se deben agregar `Dispatchers.Default` o `Dispatchers.IO` a las operaciones sobre el mismo sistema sin sincronización adicional.

Consulta `GUIA_DE_ESTUDIO.md` para entender las decisiones y reconstruir el ejercicio por etapas.
