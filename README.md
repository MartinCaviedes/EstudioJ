# UrbanPark: práctica de corrección de errores

Esta rama contiene **seis defectos intencionales** de programación y lógica. El proyecto compila y se puede ejecutar, pero algunos cobros y consultas son incorrectos.

| Versión | Rama | Uso |
| --- | --- | --- |
| Solución de referencia | [main](https://github.com/MartinCaviedes/EstudioJ/tree/main) | Consultar la implementación correcta y su guía. |
| Ejercicio con defectos | [practica-errores](https://github.com/MartinCaviedes/EstudioJ/tree/practica-errores) | Encontrar, explicar y corregir errores. |

La copia local de esta práctica está en una carpeta distinta de la solución original. Los cambios de esta rama no alteran `main`.

## Empezar sin ver las respuestas

1. Abre esta carpeta en IntelliJ con **JDK 21** y espera la sincronización de Gradle.
2. Ejecuta `Main.kt` o `./gradlew.bat run`. Anota los resultados que no coincidan con el caso.
3. Ejecuta `./gradlew.bat diagnosticar`: muestra todos los escenarios, aunque algunos fallen.
4. Corrige el código de `src/main/kotlin`, un problema a la vez. No cambies los valores esperados de las verificaciones.
5. Repite `diagnosticar` hasta obtener **8 escenarios correctos y 0 pendientes**.
6. Ejecuta `./gradlew.bat check`: deben pasar las **64 comprobaciones originales**.

En PowerShell:

```powershell
.\gradlew.bat run
.\gradlew.bat diagnosticar
.\gradlew.bat check
```

**Es normal que `diagnosticar` y `check` terminen con error al comenzar.** La compilación debe funcionar; el fallo procede de las comprobaciones que detectan los defectos. Una corrección completa debe hacer que ambas tareas finalicen correctamente.

Las verificaciones originales no se han alterado. Se añadió un diagnóstico que ejecuta seis escenarios independientes y dos controles del comportamiento que debe conservarse.

## Ayudas opcionales

- [Cuaderno de práctica](practica/CUADERNO.md): procedimiento y registro de hallazgos sin revelar las correcciones.
- [Pistas graduadas](practica/PISTAS.md): consulta solo cuando te atasques.
- [Soluciones y explicación](practica/SOLUCIONES.md): contiene las respuestas; déjalo para el final.
- [Estado de verificación de esta rama](VERIFICACION.md).

`GUIA_DE_ESTUDIO.md` pertenece a la solución de referencia y conserva explicaciones y resultados correctos. Puedes usarla al finalizar; leerla antes puede revelar pistas. Los resultados que describe son el objetivo, no los resultados actuales de esta rama.

## Dificultad y nota orientativa

El objetivo es una práctica de nivel intermedio, orientada a una solución parcialmente correcta del rango solicitado **5,0–5,5**. Se conservan estructura, herencia, corrutinas y manejo de errores; se deterioran cálculos, condiciones y consultas de negocio.

**No es posible asegurar ese rango como nota real.** La pauta evalúa niveles por indicador y no asigna una rebaja fija por cada bug. El profesor puede valorar de otra forma el impacto de cada defecto y no se adjuntó una fórmula de conversión de porcentaje a nota. La comprobación objetiva aquí es que existen seis fallos reproducibles, no una calificación certificada.

No se introdujeron errores de sintaxis: el caso indica que una solución que no compile no permite evidenciar los indicadores, lo que haría poco útil intentar ajustar una nota intermedia.

## Tecnologías

Kotlin 2.2.10, kotlinx-coroutines-core 1.10.2, Gradle 9.6.0 y JDK 21. La primera sincronización puede requerir internet. La tarea `test` está deshabilitada porque las comprobaciones son programas ejecutables, no casos JUnit; utiliza `diagnosticar`, `verificar` o `check`.
