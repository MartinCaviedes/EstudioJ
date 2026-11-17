# Validación de la rama de práctica

Fecha: 17 de noviembre de 2026. Entorno: Windows, JDK 21, Gradle 9.6.0 y Kotlin 2.2.10.

## Estado inicial: defectos conservados

Se ejecutó:

```powershell
.\gradlew.bat --no-daemon classes run diagnosticar --console=plain
```

Resultados observados:

- Los archivos principales y de diagnóstico compilan.
- `run` termina la demostración, incluido el control de errores y capacidad.
- `diagnosticar` ejecuta todos los escenarios y termina con **2 correctos y 6 pendientes**.
- La salida no exitosa de Gradle corresponde al diagnóstico que detecta los seis defectos; no a una falla de compilación.
- Las 64 comprobaciones originales de `src/test/kotlin/Verificacion.kt` se mantienen sin cambios respecto de `main`.

El total mostrado por esta rama en los cinco datos sugeridos es incorrecto por diseño. Usa los requisitos y las verificaciones como referencia; no adaptes los valores esperados a ese total.

## Control de las soluciones

Se preparó una copia temporal fuera de esta rama y se repusieron únicamente las seis líneas de producción que difieren de la solución de referencia. Los diagnósticos y verificaciones se mantuvieron iguales.

Se ejecutó en esa copia:

```powershell
.\gradlew.bat --no-daemon diagnosticar check --console=plain
```

Resultado: **BUILD SUCCESSFUL**.

```text
Diagnóstico: 8 escenarios correctos; 0 pendientes.
VERIFICACIÓN CORRECTA: 64 comprobaciones superadas.
```

La copia de práctica que se publica conserva los seis defectos. La solución correcta continúa disponible en `main`. La validación comprueba el comportamiento del ejercicio y su reparación; no certifica una nota académica determinada.
