package org.example

import kotlinx.coroutines.delay

class SistemaUrbanPark(
    val nombre: String = "UrbanPark Centro",
    capacidad: Int = 10,
    private val esperaEntrada: Long = 3000,
    private val esperaSalida: Long = 6500
) {
    init {
        require(capacidad > 0) { "La capacidad debe ser positiva." }
        require(esperaEntrada >= 0 && esperaSalida >= 0) { "Las esperas no pueden ser negativas." }
    }

    private val espacios = MutableList(capacidad) { Espacio(it + 1) }
    private val historial = mutableListOf<Ticket>()
    // Incluye patentes en proceso: evita duplicados mientras un sensor responde.
    private val patentesPresentes = mutableSetOf<String>()

    private fun estaLibre(espacio: Espacio): Boolean = when (espacio.estado) {
        EstadoEspacio.Libre -> true
        is EstadoEspacio.Ocupado -> false
        is EstadoEspacio.Procesando -> false
        is EstadoEspacio.FueraDeServicio -> false
    }

    private fun contiene(espacio: Espacio, patente: String): Boolean = when (val estado = espacio.estado) {
        EstadoEspacio.Libre -> false
        is EstadoEspacio.Ocupado -> estado.vehiculo.patente == patente
        is EstadoEspacio.Procesando -> false
        is EstadoEspacio.FueraDeServicio -> false
    }

    // Se usa desde el hilo de runBlocking y sus launch, sin Dispatchers adicionales.
    // Reservar ANTES de delay impide que otra corrutina elija el mismo espacio.
    suspend fun registrarEntrada(vehiculo: Vehiculo) {
        require(vehiculo.patente !in patentesPresentes) { "La patente ${vehiculo.patente} ya está registrada o procesándose." }
        val espacio = espacios.firstOrNull { estaLibre(it) }
            ?: error("Estacionamiento sin capacidad: no hay espacios libres.")
        patentesPresentes.add(vehiculo.patente)
        espacio.estado = EstadoEspacio.Procesando("Registrando entrada de ${vehiculo.patente}")
        try {
            delay(esperaEntrada)
            espacio.estado = EstadoEspacio.Ocupado(vehiculo)
        } catch (error: Exception) {
            // Una espera cancelada no debe dejar una reserva fantasma.
            espacio.estado = EstadoEspacio.Libre
            patentesPresentes.remove(vehiculo.patente)
            throw error
        }
        println("[Entrada] Espacio ${espacio.numero}: ${vehiculo.detalle()}")
    }

    suspend fun registrarSalida(patente: String, minutos: Int): Ticket {
        val espacio = espacios.firstOrNull { contiene(it, patente) }
            ?: error(if (patente in patentesPresentes) {
                "La patente $patente tiene una operación en proceso. Espere su confirmación."
            } else {
                "Vehículo no encontrado: $patente."
            })
        val ocupado = espacio.estado as EstadoEspacio.Ocupado
        espacio.estado = EstadoEspacio.Procesando("Calculando tarifa de $patente")
        val ticket: Ticket
        try {
            delay(esperaSalida)
            val total = ocupado.vehiculo.calcularTotal(minutos)
            ticket = Ticket(historial.size + 1, ocupado.vehiculo, minutos, total)
            historial.add(ticket)
            patentesPresentes.remove(patente)
            espacio.estado = EstadoEspacio.Libre
        } catch (error: Exception) {
            // Si el cobro falla, el vehículo sigue dentro y se puede reintentar.
            espacio.estado = ocupado
            throw error
        }
        println("[Salida] Ticket #${ticket.numero} | $patente | $minutos min | ${dinero(ticket.total)}")
        return ticket
    }

    fun inhabilitarEspacio(numero: Int, causa: String) {
        require(causa.isNotBlank()) { "Debe indicar la causa de la inhabilitación." }
        val espacio = buscarEspacio(numero)
        when (espacio.estado) {
            EstadoEspacio.Libre -> espacio.estado = EstadoEspacio.FueraDeServicio(causa)
            is EstadoEspacio.Ocupado -> error("El espacio $numero está ocupado.")
            is EstadoEspacio.Procesando -> error("El espacio $numero está procesando una operación.")
            is EstadoEspacio.FueraDeServicio -> error("El espacio $numero ya está fuera de servicio.")
        }
    }

    fun habilitarEspacio(numero: Int) {
        val espacio = buscarEspacio(numero)
        when (espacio.estado) {
            EstadoEspacio.Libre -> error("El espacio $numero ya está libre.")
            is EstadoEspacio.Ocupado -> error("El espacio $numero está ocupado.")
            is EstadoEspacio.Procesando -> error("El espacio $numero está procesando una operación.")
            is EstadoEspacio.FueraDeServicio -> espacio.estado = EstadoEspacio.Libre
        }
    }

    private fun buscarEspacio(numero: Int): Espacio {
        return espacios.firstOrNull { it.numero == numero }
            ?: throw IllegalArgumentException("No existe el espacio $numero.")
    }

    fun estadoDeEspacio(numero: Int): EstadoEspacio = buscarEspacio(numero).estado
    fun ticketsDelTurno(): List<Ticket> = historial.toList()
    fun espaciosDisponibles(): Int = espacios.count { estaLibre(it) }
    fun vehiculosAbonados(): List<Vehiculo> = historial
        .filter { it.vehiculo.tipoCliente == TipoCliente.ABONADO }
        .map { it.vehiculo }
    fun ingresoPromedio(): Double = if (historial.isEmpty()) 0.0 else historial.map { it.total }.average()
    fun patentesQueHanSalido(): List<String> = historial.map { it.vehiculo.patente }
    fun vehiculoMasTiempo(): Ticket? = historial.maxByOrNull { it.minutos }
    fun totalRecaudado(): Double = historial.sumOf { it.total }

    fun recaudacionPorTipo(): Map<String, Double> {
        // Incluye los tipos sin tickets con recaudación cero.
        return listOf("Particular", "Moto", "Camioneta").associateWith { tipo ->
            historial.filter { it.vehiculo.tipoVehiculo == tipo }.sumOf { it.total }
        }
    }

    fun tipoConMasIngresos(): String {
        return if (historial.isEmpty()) "Sin atenciones" else
            recaudacionPorTipo().maxByOrNull { it.value }!!.key
    }

    fun mostrarEstados() {
        espacios.forEach { espacio ->
            val detalle = when (val estado = espacio.estado) {
                EstadoEspacio.Libre -> "Libre"
                is EstadoEspacio.Ocupado -> "Ocupado: ${estado.vehiculo.detalle()}"
                is EstadoEspacio.Procesando -> "Procesando: ${estado.motivo}"
                is EstadoEspacio.FueraDeServicio -> "Fuera de servicio: ${estado.causa}"
            }
            println("Espacio ${espacio.numero}: $detalle")
        }
    }

    fun reporteCierre() {
        println("\n--- CIERRE DE TURNO: $nombre ---")
        if (historial.isEmpty()) println("Sin vehículos atendidos.")
        historial.forEach { ticket ->
            println("Ticket #${ticket.numero} | ${ticket.vehiculo.detalle()} | ${ticket.minutos} min | ${dinero(ticket.total)}")
        }
        println("Total recaudado: ${dinero(totalRecaudado())}")
        recaudacionPorTipo().forEach { (tipo, total) -> println("Recaudación $tipo: ${dinero(total)}") }
        println("Vehículos atendidos: ${historial.size}")
        println("Ingreso promedio: ${dinero(ingresoPromedio())}")
        println("Tipo con más ingresos: ${tipoConMasIngresos()}")
        println("Espacios disponibles al cierre: ${espaciosDisponibles()}")
        println("Patentes que han salido: ${patentesQueHanSalido()}")
        println("Abonados del historial: ${vehiculosAbonados().map { it.patente }}")
        val mayor = vehiculoMasTiempo()
        println(if (mayor == null) "Mayor permanencia: sin atenciones." else
            "Mayor permanencia: ${mayor.vehiculo.patente}, ${mayor.minutos} minutos.")
    }
}
