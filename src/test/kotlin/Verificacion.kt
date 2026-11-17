package org.example

import kotlin.math.abs
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

private var comprobaciones = 0

private fun verificar(condicion: Boolean, mensaje: String) {
    check(condicion) { mensaje }
    comprobaciones++
}

private fun verificarMonto(actual: Double, esperado: Double) {
    verificar(abs(actual - esperado) < 0.00001, "Monto esperado $esperado, obtenido $actual")
}

private suspend fun esperarError(operacion: suspend () -> Unit) {
    var rechazado = false
    try {
        operacion()
    } catch (error: IllegalArgumentException) {
        rechazado = true
    } catch (error: IllegalStateException) {
        rechazado = true
    }
    verificar(rechazado, "La operación debía rechazarse")
}

fun main() = runBlocking {
    val vehiculos: List<Vehiculo> = listOf(
        Particular("AB12CD", "Toyota Corolla", TipoCliente.ABONADO),
        Particular("XY99ZA", "Hyundai i10", TipoCliente.REGULAR),
        Moto("MO22TO", "Yamaha MT-07", TipoCliente.REGULAR),
        Camioneta("CA44RG", "Ford Ranger", TipoCliente.DISCAPACITADO, true),
        Camioneta("CA77RG", "Chevrolet D-Max", TipoCliente.REGULAR, false)
    )
    val minutos = listOf(75, 180, 25, 120, 45)
    val montos = listOf(1785.0, 5355.0, 0.0, 3867.5, 2231.25)
    vehiculos.forEachIndexed { indice, vehiculo ->
        verificarMonto(vehiculo.calcularTotal(minutos[indice]), montos[indice])
    }

    // Límite estricto: 29 es gratuito; 30 ya paga. La gratuidad vale para todo cliente.
    TipoCliente.entries.forEach { cliente ->
        verificarMonto(Moto("MO11TO", "Moto", cliente).calcularTotal(29), 0.0)
    }
    verificarMonto(vehiculos[2].calcularTotal(30), 476.0)
    verificarMonto(Moto("MO11TO", "Moto", TipoCliente.DISCAPACITADO).calcularTotal(60), 476.0)
    // Interpretación de R1: el beneficio de abonado del 20% es para particulares.
    verificarMonto(Camioneta("CA11RG", "Camioneta", TipoCliente.ABONADO, false).calcularTotal(60), 2975.0)
    verificar(vehiculos[3].detalle().contains("Carga pesada: sí"), "Falta detalle de carga")
    verificar(vehiculos[4].detalle().contains("Carga pesada: no"), "Falta detalle de no carga")

    esperarError { Particular("123ABC", "Error", TipoCliente.REGULAR) }
    esperarError { Particular("AB١٢CD", "Error", TipoCliente.REGULAR) }
    esperarError { Particular("AB12CD", " ", TipoCliente.REGULAR) }
    esperarError { leerTipoCliente("VIP") }
    verificar(leerTipoCliente(" abonado ") == TipoCliente.ABONADO, "Conversión de cliente")
    esperarError { vehiculos[2].calcularTotal(0) }
    esperarError { vehiculos[0].calcularTotal(-1) }
    val ceroInvalido = object : Vehiculo("ER00OR", "Prueba", TipoCliente.REGULAR) {
        override fun calcularTarifaBase(minutos: Int) = 0.0
    }
    val negativo = object : Vehiculo("ER01OR", "Prueba", TipoCliente.REGULAR) {
        override fun calcularTarifaBase(minutos: Int) = -10.0
    }
    esperarError { ceroInvalido.calcularTotal(60) }
    esperarError { negativo.calcularTotal(60) }

    val sistema = SistemaUrbanPark(esperaEntrada = 0, esperaSalida = 0)
    verificarMonto(sistema.ingresoPromedio(), 0.0)
    verificar(sistema.vehiculoMasTiempo() == null, "Historial vacío")
    verificar(sistema.tipoConMasIngresos() == "Sin atenciones", "Mayor ingreso sin tickets")
    verificar(sistema.espaciosDisponibles() == 10, "Capacidad inicial")
    sistema.inhabilitarEspacio(1, "Sensor en reparación")
    vehiculos.forEach { sistema.registrarEntrada(it) }
    verificar(sistema.estadoDeEspacio(1) is EstadoEspacio.FueraDeServicio, "Se asignó un espacio inhabilitado")
    verificar(sistema.estadoDeEspacio(2) is EstadoEspacio.Ocupado, "No se eligió el primer espacio libre")
    esperarError { sistema.inhabilitarEspacio(2, "Reparación") }
    esperarError { sistema.habilitarEspacio(2) }
    esperarError { sistema.habilitarEspacio(99) }
    esperarError { sistema.registrarEntrada(vehiculos[0]) }
    esperarError { sistema.registrarSalida("ZZ99ZZ", 30) }
    esperarError { sistema.registrarSalida("AB12CD", 0) }
    verificar(sistema.estadoDeEspacio(2) is EstadoEspacio.Ocupado, "Cobro inválido perdió el vehículo")
    verificar(sistema.ticketsDelTurno().isEmpty(), "Cobro fallido produjo un ticket")
    vehiculos.forEachIndexed { indice, vehiculo -> sistema.registrarSalida(vehiculo.patente, minutos[indice]) }
    verificarMonto(sistema.totalRecaudado(), 13238.75)
    verificarMonto(sistema.ingresoPromedio(), 2647.75)
    verificarMonto(sistema.recaudacionPorTipo().getValue("Particular"), 7140.0)
    verificarMonto(sistema.recaudacionPorTipo().getValue("Camioneta"), 6098.75)
    verificarMonto(sistema.recaudacionPorTipo().getValue("Moto"), 0.0)
    verificar(sistema.tipoConMasIngresos() == "Particular", "Tipo de mayor ingreso")
    verificar(sistema.vehiculoMasTiempo()?.vehiculo?.patente == "XY99ZA", "Mayor permanencia")
    verificar(sistema.vehiculosAbonados().map { it.patente } == listOf("AB12CD"), "Consulta de abonados")
    verificar(sistema.patentesQueHanSalido() == vehiculos.map { it.patente }, "Consulta de patentes")
    verificar(sistema.ticketsDelTurno().map { it.numero } == (1..5).toList(), "Numeración de tickets")
    verificar(sistema.espaciosDisponibles() == 9, "Fuera de servicio debe seguir inhabilitado")
    sistema.habilitarEspacio(1)
    verificar(sistema.espaciosDisponibles() == 10, "Rehabilitación")
    // Un vehículo puede regresar tras una salida confirmada.
    sistema.registrarEntrada(vehiculos[0])
    verificar(sistema.espaciosDisponibles() == 9, "Reingreso")

    val lleno = SistemaUrbanPark(esperaEntrada = 0, esperaSalida = 0)
    repeat(10) { indice ->
        lleno.registrarEntrada(Particular("LL${indice.toString().padStart(2, '0')}AA", "Prueba", TipoCliente.REGULAR))
    }
    esperarError { lleno.registrarEntrada(vehiculos[0]) }
    verificar(lleno.espaciosDisponibles() == 0, "Capacidad excedida")

    // yield deja iniciar al hijo; delay lo mantiene suspendido mientras se consulta.
    val concurrente = SistemaUrbanPark(capacidad = 2, esperaEntrada = 1000, esperaSalida = 1000)
    coroutineScope {
        val entrada = launch { concurrente.registrarEntrada(vehiculos[0]) }
        yield()
        verificar(concurrente.estadoDeEspacio(1) is EstadoEspacio.Procesando, "Falta estado Procesando")
        verificar(concurrente.espaciosDisponibles() == 1, "Reserva no descontada")
        esperarError { concurrente.registrarEntrada(vehiculos[0]) }
        esperarError { concurrente.registrarSalida("AB12CD", 60) }
        esperarError { concurrente.inhabilitarEspacio(1, "Mantenimiento") }
        entrada.cancelAndJoin()
        verificar(concurrente.espaciosDisponibles() == 2, "Cancelación dejó reserva fantasma")
    }
    coroutineScope {
        val entrada1 = launch { concurrente.registrarEntrada(vehiculos[0]) }
        val entrada2 = launch { concurrente.registrarEntrada(vehiculos[1]) }
        yield()
        verificar(concurrente.espaciosDisponibles() == 0, "Entradas simultáneas reutilizaron un espacio")
        entrada1.join()
        entrada2.join()
        verificar(concurrente.estadoDeEspacio(1) is EstadoEspacio.Ocupado, "Entrada 1")
        verificar(concurrente.estadoDeEspacio(2) is EstadoEspacio.Ocupado, "Entrada 2")
        val salida = launch { concurrente.registrarSalida("AB12CD", 75) }
        yield()
        esperarError { concurrente.registrarSalida("AB12CD", 75) }
        salida.cancelAndJoin()
        verificar(concurrente.estadoDeEspacio(1) is EstadoEspacio.Ocupado, "Cancelación de salida perdió el vehículo")
        verificar(concurrente.ticketsDelTurno().isEmpty(), "Salida cancelada generó ticket")
    }
    // El contenedor de consola debe capturar errores y ejecutar la operación siguiente.
    var continuo = false
    ejecutarSeguro { throw IllegalArgumentException("Dato de prueba") }
    ejecutarSeguro { continuo = true }
    verificar(continuo, "El programa no continuó")

    println("VERIFICACIÓN CORRECTA: $comprobaciones comprobaciones superadas.")
}
