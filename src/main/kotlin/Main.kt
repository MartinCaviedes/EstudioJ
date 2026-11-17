package org.example

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val sistema = SistemaUrbanPark()
    // List<Vehiculo> evidencia polimorfismo: contiene tres clases concretas.
    val vehiculos: List<Vehiculo> = listOf(
        Particular("AB12CD", "Toyota Corolla", TipoCliente.ABONADO),
        Particular("XY99ZA", "Hyundai i10", TipoCliente.REGULAR),
        Moto("MO22TO", "Yamaha MT-07", TipoCliente.REGULAR),
        Camioneta("CA44RG", "Ford Ranger", TipoCliente.DISCAPACITADO, true),
        Camioneta("CA77RG", "Chevrolet D-Max", TipoCliente.REGULAR, false)
    )

    println("--- ERRORES DE DATOS: la sesión continúa ---")
    ejecutarSeguro { sistema.registrarEntrada(Particular("123ABC", "Error", TipoCliente.REGULAR)) }
    ejecutarSeguro { leerTipoCliente("VIP") }
    ejecutarSeguro { sistema.registrarSalida("ZZ99ZZ", 30) }

    println("\n--- ENTRADAS ASÍNCRONAS ---")
    sistema.inhabilitarEspacio(10, "Mantenimiento de sensor")
    coroutineScope {
        vehiculos.forEach { vehiculo ->
            launch { ejecutarSeguro { sistema.registrarEntrada(vehiculo) } }
        }
        delay(100)
        println("La consola consulta estados mientras los sensores responden:")
        sistema.mostrarEstados()
    } // Espera a todos los hijos antes de permitir las salidas.

    println("\n--- TARIFA INVÁLIDA Y REINTENTO ---")
    ejecutarSeguro { sistema.registrarSalida("AB12CD", 0) }
    println("Estados tras el error: el vehículo permanece ocupado y permite reintentar.")
    sistema.mostrarEstados()

    println("\n--- SALIDAS ASÍNCRONAS ---")
    val minutos = listOf(75, 180, 25, 120, 45)
    coroutineScope {
        vehiculos.forEachIndexed { indice, vehiculo ->
            launch { ejecutarSeguro { sistema.registrarSalida(vehiculo.patente, minutos[indice]) } }
        }
        delay(100)
        println("Disponibles durante las salidas: ${sistema.espaciosDisponibles()}")
        sistema.mostrarEstados()
    }
    sistema.habilitarEspacio(10)
    sistema.reporteCierre()

    // Un segundo sistema evita contaminar el historial de los cinco datos sugeridos.
    println("\n--- PRUEBA DE CAPACIDAD (10 espacios) ---")
    val lleno = SistemaUrbanPark()
    coroutineScope {
        repeat(10) { indice ->
            launch {
                ejecutarSeguro {
                    lleno.registrarEntrada(Particular("LL${indice.toString().padStart(2, '0')}AA", "Prueba", TipoCliente.REGULAR))
                }
            }
        }
    }
    ejecutarSeguro { lleno.registrarEntrada(Moto("EX11TR", "Extra", TipoCliente.REGULAR)) }
    println("Fin de la demostración: el programa sigue operativo.")
}
