# Cuaderno de práctica

## Objetivo

Corregir seis defectos sin reemplazar el proyecto por la solución de `main`. Debes poder explicar qué requisito incumple cada defecto, qué lo provoca y por qué el cambio lo corrige.

El código tiene errores intencionales, pero los valores esperados del diagnóstico y de las comprobaciones originales son correctos. No se trata de hacer pasar el ejercicio eliminando verificaciones.

## Primera pasada: observación

1. Ejecuta la demostración completa.
2. Calcula manualmente los montos del enunciado.
3. Anota cuáles coinciden y cuáles no.
4. Crea casos que la demostración no cubre: tiempos límite, tipos con tarifas distintas y espacios inhabilitados.
5. Ejecuta el diagnóstico para comparar tus hallazgos con los escenarios A-F.

La salida de Gradle puede mostrar `BUILD FAILED` al diagnosticar; eso es esperado mientras queden defectos. Un fallo de compilación sería un problema distinto, que deberías resolver antes de comparar resultados.

## Registro

| Escenario | Resultado observado | Resultado requerido | Hipótesis | Corrección y explicación |
| --- | --- | --- | --- | --- |
| A | | | | |
| B | | | | |
| C | | | | |
| D | | | | |
| E | | | | |
| F | | | | |

## Segunda pasada: corregir

Para cada hallazgo:

1. Localiza la función responsable, sin cambiar otras partes por intuición.
2. Identifica los tipos y valores intermedios.
3. Formula el requisito en una oración.
4. Haz el cambio mínimo que lo cumpla.
5. Ejecuta el diagnóstico y comprueba que el escenario ahora pasa.
6. Prueba un caso vecino para evitar una corrección demasiado específica.

Las seis pruebas son independientes: sus datos se eligieron para que arreglar una no sea un requisito para detectar las demás. Los dos controles deben pasar desde el inicio y ayudan a reconocer regresiones.

## Cierre

Debes obtener:

```text
Diagnóstico: 8 escenarios correctos; 0 pendientes.
VERIFICACIÓN CORRECTA: 64 comprobaciones superadas.
```

Después ejecuta `run` y contrasta nuevamente los cinco tickets, sus totales y las consultas. El diagnóstico no sustituye las comprobaciones originales.

Preguntas para defender tus correcciones:

- ¿Qué condición hacía visible el fallo y por qué otros datos lo ocultaban?
- ¿El defecto estaba en la regla de negocio, el tipo de dato o el criterio de consulta?
- ¿La corrección funciona con otros vehículos y tiempos?
- ¿Se mantienen los cuatro estados y las esperas asíncronas?
- ¿Qué indicador de la pauta se relaciona con el error?

Si usas Git, puedes registrar un commit por corrección. Para conservar el ejercicio inicial, crea tu propia rama desde `practica-errores` antes de comenzar.
