# Guía de estudio: resolver UrbanPark desde los requisitos

## 1. Qué pide realmente el caso

Se debe construir el núcleo de una aplicación de estacionamientos en **consola Kotlin/JVM**. No se pide una pantalla Android, Compose, base de datos, API ni arquitectura con repositorios. Los datos del turno permanecen en memoria.

Las entradas y salidas esperan a sensores simulados. Mientras una operación espera, el espacio debe aparecer como `Procesando` y otras corrutinas deben poder trabajar. Al salir, el vehículo deja un ticket en el historial para que el reporte no pierda sus datos.

### Material revisado y nivel utilizado

- `Ejemplos/Guia 3 Herencia y polimorfismo(1).txt`: `open class`, parámetros al padre, `override`, `super`, condiciones y funciones.
- `Ejemplos/Conceptos de Kotlin Coroutines_ operaciones suspendidas y concurrencia estructurada.pdf`: suspensión y coordinación de corrutinas.
- `Ejemplos/Guia_POO_Kotlin_Corrutinas_y_Simulaciones.docx.pdf`: estados sellados, consultas sobre historial y simulaciones integradas. Sus ejercicios ArenaByte y EcoCharge tienen responsabilidades similares a UrbanPark.
- Proyecto UrbanPark del compañero: tres archivos con dominio, sistema y `main`; enums, `require`, listas y `delay`.
- `Caso a resolver/DSY1105_EP1_Instrucciones_Alumno (1).pdf`: caso específico R1-R7, tiempos y datos sugeridos.
- `Caso a resolver/Evaluación Parcial 1_Estudiante_DSY1105 (1).pdf`: requisitos técnicos y pauta de indicadores.

La solución usa el mismo conjunto de herramientas. Se separan estados y presentación para leerlos mejor; no se incorporan frameworks de aplicación.

### Diferencias importantes respecto del ejemplo del compañero

1. Se restituye la gratuidad de motos menores de 30 minutos que ese código omite explícitamente.
2. Los `when` contemplan los cuatro estados sin un `else` que esconda variantes.
3. El reporte muestra todas las consultas y la recaudación por cada tipo.
4. Se evita el ingreso duplicado de una patente, también durante el procesamiento.
5. Una salida fallida devuelve el espacio a `Ocupado`, para poder corregir el dato y reintentar.
6. Se usan `launch` y consultas durante `delay` para hacer visible la concurrencia. Llamar funciones suspendidas una después de otra es válido, pero las ejecuta secuencialmente.
7. La clase base es `open class`, como solicita literalmente la pauta, con métodos sobrescribibles. El ejemplo usa una base abstracta, que permite heredar, pero aquí se evita esa diferencia textual con el indicador.

## 2. Decisiones que conviene poder explicar

### Moto gratuita frente a tarifa positiva

R1 exige que una moto con menos de 30 minutos pague cero, mientras R3 dice que ninguna tarifa puede ser cero. R6 matiza que es error cuando ocurre en un caso donde no debería, y los datos sugeridos incluyen una moto de 25 minutos a $0.

Se adopta la interpretación específica: `Moto` con `0 < minutos < 30` admite cero. Cualquier otro total debe ser finito y positivo. No se elimina la validación general. A los 30 minutos exactos la moto paga por 30 minutos.

### Abonado: glosario frente a R1

El glosario menciona un descuento de abonado sin limitar el tipo. R1 especifica ese 20% para particulares. Se sigue R1: **solo Particular aplica el 20%**. Motos y camionetas abonadas conservan su tarifa, salvo gratuidad de moto. Si el docente confirma que el beneficio es global, se debe mover el descuento al método común y retirarlo de `Particular` para evitar aplicarlo dos veces.

### Tiempo, fecha e importes

- La fecha de ingreso se captura al construir el vehículo, como en el ejemplo; en la demostración se construye inmediatamente antes de ingresarlo. El campo es `val` y no se modifica al confirmar el sensor. No representa una marca de tiempo enviada por hardware real.
- Los minutos se reciben como argumento para poder reproducir 75, 180 y los demás tiempos sugeridos sin esperar horas reales.
- Se cobra una fracción de hora: `minutos / 60.0`. No hay una regla que indique cobrar por horas iniciadas.
- Se utiliza `Double`, como en clases. La salida muestra dos decimales. No se trunca con `toInt()`, porque perdería los $0,50 y $0,25 de los ejemplos.
- El enum hace imposible construir normalmente un vehículo con un cliente desconocido. `leerTipoCliente()` valida un texto externo y demuestra el rechazo de `VIP`.
- Se exige el formato de patente simplificado del propio caso: dos letras ASCII mayúsculas, dos dígitos ASCII y dos letras ASCII. No se intenta reemplazarlo por otros formatos reales.

## 3. Traducir requisitos a responsabilidades

| Requisito | Implementación | Evidencia |
| --- | --- | --- |
| R1: tipos y datos comunes | `Vehiculo`, `Particular`, `Moto`, `Camioneta`, `TipoCliente` | Propiedades `val`, tarifas propias y detalle de carga pesada sí/no. |
| R2: estados | `EstadoEspacio` y `Espacio` | Libre, Ocupado con vehículo, Procesando con motivo y FueraDeServicio con causa. |
| R3: cobros y validaciones | `init`, `calcularTotal`, consultas de recaudación | Patente válida, minutos positivos, IVA, descuentos y excepción de gratuidad. |
| R4: gestión y reportes | Listas privadas y funciones de consulta | Disponibles, abonados, promedio, patentes, mayor tiempo y reporte de tickets. |
| R5: sensores | `registrarEntrada` y `registrarSalida` | `suspend`, `delay(3000)`, `delay(6500)` y estado temporal. |
| R6: errores | `require`, `error`, `ejecutarSeguro` y recuperación del estado | Mensaje sin traza, operación rechazada y continuidad del programa. |
| R7: organización | Archivos de dominio, estados, sistema, consola y main | Funciones y archivos con responsabilidades reconocibles. |

## 4. Leer las clases y las propiedades

`Vehiculo` almacena lo compartido. Las hijas entregan sus parámetros al constructor del padre:

```kotlin
class Moto(patente: String, marca: String, tipoCliente: TipoCliente) :
    Vehiculo(patente, marca, tipoCliente)
```

No se repiten `val patente`, `val marca` ni `val tipoCliente` en cada hija: esas propiedades ya existen en la clase base. `Camioneta` agrega `val cargaPesada`, que es propio de su tipo.

- `val`: la referencia o valor de la propiedad no se reasigna.
- `var`: permite cambiar `Espacio.estado`.
- `open`: permite heredar una clase o sobrescribir un miembro.
- `override`: reemplaza el comportamiento heredado.
- `protected`: las hijas acceden al método de tarifa, pero la consola no lo llama directamente.
- `private`: las listas se modifican mediante operaciones del sistema, no desde `main`.

`val historial = mutableListOf<Ticket>()` **sí permite agregar tickets**: `val` impide reemplazar la lista, no cambiar su contenido. Por eso la lista es privada y `ticketsDelTurno()` devuelve una copia de lectura.

La base `Vehiculo` no ofrece una tarifa genérica inventada. Si se intenta cobrar una instancia genérica, produce un error descriptivo. Las operaciones normales utilizan las tres hijas.

## 5. Cálculo y polimorfismo

El método público común sigue este orden:

1. Validar que los minutos sean positivos.
2. Llamar al cálculo de tarifa del subtipo.
3. Multiplicar por `1.19` para agregar IVA.
4. Multiplicar por `0.50` si el cliente es discapacitado.
5. Validar el resultado final.

`calcularTotal()` no necesita preguntar si el vehículo es particular o camioneta para calcular su tarifa: Kotlin ejecuta el `override` del objeto real. La consulta por `Moto` solo se utiliza para verificar la excepción explícita del total cero.

Un `List<Vehiculo>` puede contener instancias de las tres hijas. Al llamar `vehiculo.calcularTotal(...)` o `vehiculo.detalle()`, cada instancia aporta su comportamiento. Ese es el polimorfismo que debe poder demostrarse.

### Cálculos manuales para comprobar el programa

| Vehículo | Operación en orden | Total |
| --- | --- | ---: |
| Particular AB12CD, abonado, 75 min | `1500 × 75 / 60 × 0.80 × 1.19` | $1.785,00 |
| Particular XY99ZA, regular, 180 min | `1500 × 180 / 60 × 1.19` | $5.355,00 |
| Moto MO22TO, 25 min | Gratuidad porque `25 < 30` | $0,00 |
| Camioneta CA44RG, pesada y discapacitado, 120 min | `2500 × 120 / 60 × 1.30 × 1.19 × 0.50` | $3.867,50 |
| Camioneta CA77RG, sin carga pesada, 45 min | `2500 × 45 / 60 × 1.19` | $2.231,25 |

Una moto regular de 30 minutos paga `800 × 30 / 60 × 1.19 = $476,00`. Una moto de 0 minutos debe rechazarse: no es una estadía positiva gratuita.

## 6. Estados: por qué se usa sealed class

Un enum sirve para `TipoCliente`, donde solo interesa uno de tres valores. Los estados necesitan datos diferentes: `Ocupado` lleva un vehículo; `Procesando`, un motivo; `FueraDeServicio`, una causa.

La jerarquía sellada permite que `when` revise todas las alternativas. Si se agrega otro estado, el compilador obliga a actualizar las expresiones exhaustivas.

Transiciones normales:

```text
Entrada: Libre -> Procesando(registrando entrada) -> Ocupado(vehículo)
Salida:  Ocupado -> Procesando(calculando tarifa) -> Libre
Falla de salida: Procesando -> Ocupado original
Mantenimiento: Libre -> FueraDeServicio(causa) -> Libre
```

No se debe inhabilitar un espacio ocupado o que tiene una operación en curso. Tampoco se cuenta `Procesando` como disponible, aunque todavía no esté `Ocupado`.

## 7. Corrutinas sin confundir suspensión y concurrencia

- `suspend fun` permite suspender una función, pero no inicia por sí misma una tarea paralela.
- `delay` suspende la corrutina y permite que otras se ejecuten. No se usa `Thread.sleep`.
- `runBlocking` mantiene vivo el punto de entrada de consola mientras terminan sus corrutinas. Bloquea el hilo llamador hasta el final, pero su bucle puede ejecutar las corrutinas hijas durante las esperas suspendidas.
- `launch` inicia una corrutina hija. Aquí no hace falta devolver un valor mediante `async`.
- `coroutineScope` espera a todos sus hijos: primero terminan las entradas y después comienzan las salidas.

El código reserva el espacio **antes de `delay`**. Si se buscara libre, se esperara y recién entonces se marcara ocupado, dos corrutinas podrían elegir el mismo espacio.

También se reserva la patente antes de esperar. De lo contrario, una segunda entrada de la misma patente podría ocupar otro espacio mientras la primera aún no termina.

Esta solución funciona con las corrutinas confinadas al mismo hilo de `runBlocking`, como en `main` y en las verificaciones. No pretende ser un servidor con acceso desde varios hilos. Un cambio a varios dispatchers necesitaría proteger de forma coordinada las reservas, los estados, las consultas y el historial, por ejemplo con un mutex; no basta con proteger solo `historial.add`.

## 8. Manejo de errores y consistencia

`require` produce `IllegalArgumentException` cuando un dato es incorrecto. `error` produce `IllegalStateException` cuando la operación no se puede hacer en el estado actual.

`ejecutarSeguro` captura esos dos errores y muestra un mensaje. Cada operación tiene su propio bloque: si se pusiera un único `try` alrededor de toda la demostración, el primer error saltaría el resto de las instrucciones de ese bloque.

La construcción de un vehículo inválido debe ocurrir **dentro** de `ejecutarSeguro`, porque su `init` puede fallar antes de entrar al estacionamiento.

El sistema además recupera el estado anterior: si falla un cobro, el espacio vuelve a `Ocupado`, sin ticket ni recaudación nueva. Así se puede volver a pedir la salida con minutos válidos. Una entrada cancelada libera espacio y patente. Una salida cancelada conserva el vehículo.

`CancellationException` se vuelve a lanzar en la capa de consola: la cancelación pertenece al control de corrutinas y no se debe esconder como un dato incorrecto.

## 9. Colecciones y consultas

El historial es la fuente de consultas sobre vehículos **atendidos**, incluyendo los que pagaron cero. Un vehículo que sigue estacionado no pertenece aún al historial de salidas.

| Pregunta | Herramienta utilizada |
| --- | --- |
| ¿Cuántos espacios libres? | `count` y comprobación exhaustiva del estado. |
| ¿Qué vehículos atendidos son abonados? | `filter` por cliente y `map` para obtener vehículos. |
| ¿Ingreso promedio? | `map` de totales y `average`, con caso vacío en 0. |
| ¿Patentes que salieron? | `map` del historial. |
| ¿Mayor permanencia? | `maxByOrNull`, que puede devolver `null`. |
| ¿Recaudación total? | `sumOf`. |
| ¿Recaudación por tipo? | `associateWith`, `filter` y `sumOf`. |
| ¿Cómo mostrar los tickets? | `forEach` en consola. |

No se almacenan totales duplicados que puedan desactualizarse: se calculan a partir del historial vigente. Al agregar un ticket, las consultas ya reflejan la recaudación nueva. Con diez espacios y un turno pequeño, este enfoque es suficiente y fácil de revisar.

Si hay empate en mayor permanencia o recaudación, se toma la primera coincidencia según el orden de la colección; el enunciado no fija desempate. La moto gratuita cuenta como vehículo atendido al calcular el promedio.

## 10. Relación con los indicadores de la pauta

| Indicador | Peso | Dónde se evidencia |
| --- | ---: | --- |
| IE 1.1.1: tipos y variables | 10% | `String`, `Int`, `Double`, `Boolean`, `LocalDateTime`, enum; `val` y `var estado`. |
| IE 1.1.2: aritmética | 10% | Fracción de hora, 20%, 30%, IVA, 50%, promedio y recaudación. |
| IE 1.1.3: condiciones | 10% | `if` en tarifas, validaciones y `when` exhaustivo de estados. |
| IE 1.2.1: funciones modulares | 10% | Métodos de tarifa, registro, consultas, presentación y captura de errores. |
| IE 1.2.2: colecciones | 10% | `MutableList<Espacio>`, `MutableList<Ticket>` y `List<Vehiculo>`. |
| IE 1.2.3: orden superior | 15% | `filter`, `map`, `sumOf`, `count`, `maxByOrNull`, `associateWith`. |
| IE 1.2.4: iteración y visualización | 5% | `forEach`, `forEachIndexed`, estados y reporte legible. |
| IE 1.3.1: herencia | 10% | Base `open class Vehiculo`, tres hijas y argumentos al padre. |
| IE 1.3.2: polimorfismo | 5% | `override calcularTarifaBase`, `override detalle`, lista de tipo base. |
| IE 1.3.3: corrutinas | 10% | `suspend`, `delay`, `runBlocking`, `launch`, consultas durante esperas. |
| IE 1.3.4: excepciones | 5% | `try-catch`, errores de datos, recuperación y continuidad. |

Los pesos suman 100%. Esta matriz indica evidencia en el proyecto; no sustituye el criterio del docente, especialmente ante las ambigüedades indicadas.

## 11. Verificaciones reproducibles

Ejecuta `./gradlew.bat verificar` desde PowerShell. Si una condición falla, la tarea termina con error; si todas pasan, imprime la cantidad de comprobaciones superadas.

Se comprueban:

- Los cinco montos sugeridos y las sumas del reporte.
- Motos de 29 y 30 minutos, gratuidad para cada cliente y descuento de discapacidad.
- Patente inválida, marca vacía, cliente desconocido y tiempo cero o negativo.
- Total cero indebido y total negativo usando vehículos de prueba.
- Historial vacío, abonados, patentes, mayor permanencia y tickets consecutivos.
- Capacidad diez y exclusión de espacios fuera de servicio.
- Recuperación tras una tarifa inválida y posibilidad de reingreso.
- Patentes duplicadas, operaciones simultáneas y reservas mientras se espera.
- Cancelación de entradas y salidas sin perder consistencia.
- Continuidad de la consola tras un error controlado.

Las verificaciones utilizan esperas cero o cortas para terminar rápido. `main` conserva los 3 y 6,5 segundos reales exigidos por el caso.

## 12. Ruta para reconstruirlo sin mirar la solución

1. Escribe los datos comunes y justifica el uso de `val`.
2. Crea una base `open` y calcula solo un particular regular de una hora.
3. Agrega los tres `override` y comprueba los cinco cálculos a mano.
4. Introduce los cuatro estados, con sus datos asociados.
5. Crea diez espacios y escribe una entrada síncrona que elija el primero libre.
6. Implementa una salida que genere un ticket y libere el espacio.
7. Agrega el historial y escribe cada consulta con funciones de colecciones.
8. Introduce validaciones y rodea cada operación con un control de errores.
9. Convierte entrada y salida a `suspend`, pasando a Procesando antes de `delay`.
10. Desde `runBlocking`, utiliza `launch` y comprueba que se puede consultar mientras se espera.
11. Prueba un cobro inválido y verifica que puedas reintentarlo.
12. Genera el cierre y compáralo con los cálculos manuales.

Preguntas para autoevaluarte:

- ¿Por qué `minutos / 60` puede producir un resultado incorrecto si ambos son enteros?
- ¿Por qué multiplicar por `0.80` aplica un 20% de descuento?
- ¿Qué diferencia hay entre `List<Vehiculo>` y `List<Particular>`?
- ¿Por qué un enum basta para cliente, pero una sealed class ayuda con estados?
- ¿Qué sucede si se registra una salida cuando la entrada sigue Procesando?
- ¿Por qué no se puede contar como libre un espacio que espera al sensor?
- ¿Qué sucede si se borra el vehículo antes de validar su tarifa?
- ¿Por qué el promedio incluye el ticket gratuito de la moto?
- ¿Qué cambia si quitas `launch` y llamas cada registro en secuencia?
- ¿Qué operaciones adicionales necesitarían sincronización si hubiera varios hilos?

Para practicar cambios pequeños, agrega un filtro de tickets de camionetas, una consulta del ticket de mayor monto o una impresión de las fechas de ingreso. Comprueba que el reporte original siga coincidiendo antes de intentar un caso nuevo.
