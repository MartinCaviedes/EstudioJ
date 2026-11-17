package org.example

import java.time.LocalDateTime

enum class TipoCliente { REGULAR, ABONADO, DISCAPACITADO }

// R1: open permite heredar; val mantiene los datos comunes inmutables.
open class Vehiculo(
    val patente: String,
    val marca: String,
    val tipoCliente: TipoCliente
) {
    val fechaIngreso: LocalDateTime = LocalDateTime.now()
    open val tipoVehiculo: String = "Genérico"

    init {
        require(Regex("[A-Z]{2}[0-9]{2}[A-Z]{2}").matches(patente)) {
            "Patente inválida: $patente. Debe tener el formato AB12CD."
        }
        require(marca.isNotBlank()) { "La marca no puede estar vacía." }
    }

    // Cada hija modifica esta parte; el orden de IVA y beneficio queda en la base.
    protected open fun calcularTarifaBase(minutos: Int): Double {
        error("El vehículo genérico no tiene una tarifa definida.")
    }

    fun calcularTotal(minutos: Int): Double {
        require(minutos > 0) { "El tiempo estacionado debe ser mayor que cero." }
        val subtotal = calcularTarifaBase(minutos)
        var total = subtotal * 1.19
        if (tipoCliente == TipoCliente.DISCAPACITADO) total *= 0.50

        // R1 y los datos sugeridos permiten $0 SOLO para motos de menos de 30 min.
        // Esta excepción explícita resuelve la contradicción con la regla general R3.
        val gratuidadValida = this is Moto && minutos < 30 && total == 0.0
        require(total.isFinite() && (total > 0.0 || gratuidadValida)) {
            "Resultado de tarifa inválido: $total. Debe ser positivo, salvo moto gratuita."
        }
        return total
    }

    open fun detalle(): String = "$tipoVehiculo | $patente | $marca | $tipoCliente"
}

class Particular(patente: String, marca: String, tipoCliente: TipoCliente) :
    Vehiculo(patente, marca, tipoCliente) {
    override val tipoVehiculo = "Particular"

    override fun calcularTarifaBase(minutos: Int): Double {
        var subtotal = 1500.0 * minutos / 60.0
        if (tipoCliente == TipoCliente.ABONADO) subtotal *= 0.80
        return subtotal
    }
}

class Moto(patente: String, marca: String, tipoCliente: TipoCliente) :
    Vehiculo(patente, marca, tipoCliente) {
    override val tipoVehiculo = "Moto"

    override fun calcularTarifaBase(minutos: Int): Double {
        return if (minutos < 30) 0.0 else 800.0 * minutos / 60.0
    }
}

class Camioneta(
    patente: String,
    marca: String,
    tipoCliente: TipoCliente,
    val cargaPesada: Boolean
) : Vehiculo(patente, marca, tipoCliente) {
    override val tipoVehiculo = "Camioneta"

    override fun calcularTarifaBase(minutos: Int): Double {
        val subtotal = 2500.0 * minutos / 60.0
        return if (cargaPesada) subtotal * 1.30 else subtotal
    }

    override fun detalle(): String {
        return "${super.detalle()} | Carga pesada: ${if (cargaPesada) "sí" else "no"}"
    }
}
