package org.example

import java.util.Locale
import kotlinx.coroutines.CancellationException

fun dinero(monto: Double): String = "$" + String.format(Locale.forLanguageTag("es-CL"), "%,.2f", monto)

fun leerTipoCliente(texto: String): TipoCliente {
    return when (texto.trim().uppercase()) {
        "REGULAR" -> TipoCliente.REGULAR
        "ABONADO" -> TipoCliente.ABONADO
        "DISCAPACITADO" -> TipoCliente.DISCAPACITADO
        else -> throw IllegalArgumentException("Tipo de cliente inválido: $texto.")
    }
}

// Rodea CADA operación, incluida la construcción del vehículo, para poder continuar.
suspend fun ejecutarSeguro(operacion: suspend () -> Unit) {
    try {
        operacion()
    } catch (error: CancellationException) {
        throw error // La cancelación de corrutinas no es un error de datos.
    } catch (error: IllegalArgumentException) {
        println("Dato inválido: ${error.message}")
    } catch (error: IllegalStateException) {
        println("Operación rechazada: ${error.message}")
    }
}
