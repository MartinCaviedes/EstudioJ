# Respuestas: abrir después de practicar

Las siguientes son las seis correcciones. No es necesario modificar las verificaciones ni desactivar validaciones.

## A. División entera en la tarifa de particulares

Archivo: `src/main/kotlin/Vehiculos.kt`, método `Particular.calcularTarifaBase`.

```kotlin
var subtotal = 1500.0 * minutos / 60.0
```

La versión defectuosa calcula `minutos / 60` entre enteros: 75 minutos se convierten en una hora, perdiendo los quince minutos restantes. Al usar aritmética decimal se conserva 1,25 horas. Con el descuento e IVA, el abonado debe pagar $1.785,00; la versión defectuosa cobra $1.428,00. Un particular de menos de una hora llega a generar cero y activa el rechazo de tarifa.

Indicadores relacionados: tipos, operadores aritméticos y validación de resultados.

## B. Porcentaje incorrecto de carga pesada

Archivo: `src/main/kotlin/Vehiculos.kt`, método `Camioneta.calcularTarifaBase`.

```kotlin
return if (cargaPesada) subtotal * 1.30 else subtotal
```

La versión defectuosa aplica `1.03`, equivalente a un 3% adicional. R1 exige 30%. La camioneta pesada y con beneficio de discapacidad de 120 minutos debe pagar $3.867,50; la versión defectuosa calcula $3.064,25.

Indicador relacionado: operadores aritméticos y reglas diferenciadas por subtipo.

## C. Límite inclusivo en la gratuidad de motos

Archivo: `src/main/kotlin/Vehiculos.kt`, método `Moto.calcularTarifaBase`.

```kotlin
return if (minutos < 30) 0.0 else 800.0 * minutos / 60.0
```

La versión defectuosa usa `<= 30`, devolviendo cero a los 30 minutos exactos. La validación común correctamente admite gratuidad solo por debajo de 30, por lo que ese cobro se rechaza. Corregir la tarifa, sin relajar la validación, hace que los 30 minutos cuesten $476,00.

Indicadores relacionados: condicionales y casos límite.

## D. Filtro de clientes equivocado

Archivo: `src/main/kotlin/SistemaUrbanPark.kt`, función `vehiculosAbonados`.

```kotlin
.filter { it.vehiculo.tipoCliente == TipoCliente.ABONADO }
```

La versión defectuosa filtra `REGULAR`. La forma del código es válida y devuelve objetos, pero responde otra pregunta. El diagnóstico compara explícitamente las patentes para que una lista no vacía no se confunda con una lista correcta.

Indicador relacionado: consultas funcionales sobre colecciones.

## E. Máximo según el monto en lugar del tiempo

Archivo: `src/main/kotlin/SistemaUrbanPark.kt`, función `vehiculoMasTiempo`.

```kotlin
fun vehiculoMasTiempo(): Ticket? = historial.maxByOrNull { it.minutos }
```

La versión defectuosa compara `it.total`. Una moto de 120 minutos paga menos que una camioneta de 60: el mayor pago no identifica la mayor permanencia. Los datos del diagnóstico aíslan este fallo y no dependen del recargo de carga pesada.

Indicador relacionado: transformación y análisis de colecciones.

## F. Contar como disponible todo lo que no está ocupado

Archivo: `src/main/kotlin/SistemaUrbanPark.kt`, función `espaciosDisponibles`.

```kotlin
fun espaciosDisponibles(): Int = espacios.count { estaLibre(it) }
```

La versión defectuosa usa `!is EstadoEspacio.Ocupado`, incluyendo Procesando y FueraDeServicio. Eso informa cupos falsos aunque la asignación de entradas siga protegiendo esos espacios correctamente. Se reutiliza `estaLibre`, que distingue las cuatro alternativas.

Indicadores relacionados: condiciones, estados y consultas de negocio.

## Comprobar la reparación

Después de las seis correcciones:

```powershell
.\gradlew.bat diagnosticar
.\gradlew.bat check
.\gradlew.bat run
```

El diagnóstico debe mostrar ocho escenarios correctos. Las comprobaciones originales deben seguir siendo 64 y pasar sin cambios. Los cinco datos sugeridos deben volver a recaudar $13.238,75, con promedio de $2.647,75.

La rama `main` contiene la implementación original para comparar. Esta clave describe cambios mínimos; otras soluciones son válidas si cumplen los requisitos y conservan los casos límite.
