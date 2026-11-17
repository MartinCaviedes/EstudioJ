package org.example

import kotlin.math.abs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking

private var correctos = 0
private var pendientes = 0

private suspend fun escenario(nombre: String, prueba: suspend () -> Unit) {
    try {
        prueba()
        correctos++
        println("[OK] $nombre")
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        pendientes++
        println("[REVISAR] $nombre: ${error.message}")
    }
}

private fun monto(actual: Double, esperado: Double) {
    check(abs(actual - esperado) < 0.00001) { "Se esperaba $esperado; se obtuvo $actual." }
}

fun main() = runBlocking {
    // Se comprueba comportamiento esperado; aquí no se incluyen las correcciones.
    escenario("A. Cobro proporcional de un particular abonado") {
        monto(Particular("AB12CD", "Toyota", TipoCliente.ABONADO).calcularTotal(75), 1785.0)
    }
    escenario("B. Camioneta pesada con beneficio de discapacidad") {
        monto(Camioneta("CA44RG", "Ford", TipoCliente.DISCAPACITADO, true).calcularTotal(120), 3867.5)
    }
    escenario("C. Moto a los 30 minutos exactos") {
        monto(Moto("MO22TO", "Yamaha", TipoCliente.REGULAR).calcularTotal(30), 476.0)
    }
    escenario("D. Abonados del historial") {
        val sistema = SistemaUrbanPark(esperaEntrada = 0, esperaSalida = 0)
        val abonado = Particular("AB12CD", "Toyota", TipoCliente.ABONADO)
        val regular = Particular("XY99ZA", "Hyundai", TipoCliente.REGULAR)
        sistema.registrarEntrada(abonado)
        sistema.registrarEntrada(regular)
        sistema.registrarSalida(abonado.patente, 60)
        sistema.registrarSalida(regular.patente, 60)
        val patentes = sistema.vehiculosAbonados().map { it.patente }
        check(patentes == listOf("AB12CD")) { "Se esperaba [AB12CD]; se obtuvo $patentes." }
    }
    escenario("E. Mayor permanencia con tarifas de tipos distintos") {
        val sistema = SistemaUrbanPark(esperaEntrada = 0, esperaSalida = 0)
        sistema.registrarEntrada(Moto("MO22TO", "Yamaha", TipoCliente.REGULAR))
        sistema.registrarEntrada(Camioneta("CA77RG", "Chevrolet", TipoCliente.REGULAR, false))
        sistema.registrarSalida("MO22TO", 120)
        sistema.registrarSalida("CA77RG", 60)
        val patente = sistema.vehiculoMasTiempo()?.vehiculo?.patente
        check(patente == "MO22TO") { "Se esperaba MO22TO (120 minutos); se obtuvo $patente." }
    }
    escenario("F. Disponibilidad con un espacio fuera de servicio") {
        val sistema = SistemaUrbanPark(esperaEntrada = 0, esperaSalida = 0)
        sistema.inhabilitarEspacio(10, "Mantenimiento")
        val disponibles = sistema.espaciosDisponibles()
        check(disponibles == 9) { "Se esperaban 9 espacios disponibles; se obtuvieron $disponibles." }
    }

    // Controles que deben seguir funcionando desde el inicio de la práctica.
    escenario("Control 1. Moto gratuita y particular de una hora") {
        monto(Moto("MO22TO", "Yamaha", TipoCliente.REGULAR).calcularTotal(25), 0.0)
        monto(Particular("XY99ZA", "Hyundai", TipoCliente.REGULAR).calcularTotal(60), 1785.0)
    }
    escenario("Control 2. Recuperación después de un tiempo inválido") {
        val sistema = SistemaUrbanPark(esperaEntrada = 0, esperaSalida = 0)
        sistema.registrarEntrada(Particular("AB12CD", "Toyota", TipoCliente.ABONADO))
        var rechazado = false
        try {
            sistema.registrarSalida("AB12CD", 0)
        } catch (error: IllegalArgumentException) {
            rechazado = true
        }
        check(rechazado) { "El tiempo cero debía rechazarse." }
        check(sistema.estadoDeEspacio(1) is EstadoEspacio.Ocupado) { "Se perdió el vehículo." }
        check(sistema.ticketsDelTurno().isEmpty()) { "Se emitió un ticket sin cobro válido." }
    }

    println("\nDiagnóstico: $correctos escenarios correctos; $pendientes pendientes.")
    check(pendientes == 0) { "La práctica tiene $pendientes escenarios por corregir." }
}
