package org.example

// R2: cada variante guarda únicamente los datos que corresponden a ese estado.
sealed class EstadoEspacio {
    data object Libre : EstadoEspacio()
    data class Ocupado(val vehiculo: Vehiculo) : EstadoEspacio()
    data class Procesando(val motivo: String) : EstadoEspacio()
    data class FueraDeServicio(val causa: String) : EstadoEspacio()
}

data class Espacio(val numero: Int, var estado: EstadoEspacio = EstadoEspacio.Libre)

// El ticket conserva el vehículo aunque su espacio vuelva a estar libre.
data class Ticket(
    val numero: Int,
    val vehiculo: Vehiculo,
    val minutos: Int,
    val total: Double
)
