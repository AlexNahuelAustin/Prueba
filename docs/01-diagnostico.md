# Diagnóstico de Calidad — Sistema de Gestión de Solicitudes Académicas

## 1. Calidad externa vs. calidad interna

### Calidad externa: qué funciona

Desde la mirada del usuario, el sistema cumple con lo que promete:

- Registra solicitudes y devuelve un identificador.
- Rechaza datos inválidos (legajo vacío, email sin arroba, tipo inexistente) con un mensaje claro.
- Impide transiciones de estado inválidas: no se puede poner en proceso una solicitud sin responsable, ni reabrir una ya cerrada.
- Asigna responsables respetando el tope de carga.
- Persiste los datos en H2, de modo que sobreviven al reinicio.
- Genera el reporte con totales, demora promedio y vencidas.

La ejecución del `Main` lo demuestra: todos los casos, tanto los exitosos como los de error, se comportan como se espera.

### Calidad interna: qué está mal por dentro

Nada de lo anterior se percibe desde el código. La clase `GestorDeSolicitudes` concentra **toda** la aplicación: validaciones, reglas de negocio, acceso a la base de datos y presentación por consola, en 300 líneas sin separación alguna.

Consecuencias concretas:

| Dimensión | Estado actual |
|---|---|
| **Mantenibilidad** | Cambiar el tope de solicitudes abiertas exige buscar un `5` suelto en medio de un método de 80 líneas. |
| **Testabilidad** | No se puede probar ninguna regla de negocio sin levantar la base de datos. No hay un solo punto de inyección. |
| **Legibilidad** | Los nombres (`p`, `ep`, `res`, `c`, `lst`) obligan a leer el cuerpo entero para entender qué hace cada variable. |
| **Evolución** | Agregar un tipo de solicitud o un estado nuevo obliga a tocar varios métodos distintos, con riesgo de olvidar alguno. |
| **Seguridad** | El SQL se arma concatenando strings: un legajo con comilla rompe la consulta o permite inyección. |

### La conclusión

El sistema tiene **calidad externa aceptable y calidad interna pobre**. Esa combinación es exactamente la definición de deuda técnica: funciona hoy, pero cada cambio futuro cuesta más de lo que debería. El riesgo no es que falle ahora, sino que el costo del cambio crezca hasta volver el módulo intocable.

---

## 2. Deuda técnica y code smells

| # | Code smell | Evidencia | Ubicación | Impacto | Riesgo | Prioridad |
|---|---|---|---|---|---|---|
| 1 | **God Object** | Una sola clase valida, aplica reglas de negocio, accede a la base e imprime por consola. 300 líneas, 9 métodos públicos. | `GestorDeSolicitudes` (clase completa) | Cuatro motivos de cambio distintos conviven. Viola SRP. | Alto: cualquier cambio puede romper algo no relacionado. | **Alta** |
| 2 | **Método largo** | `registrarSolicitud` tiene 70 líneas y hace 6 cosas: valida, busca el estudiante, lo da de alta, cuenta abiertas, calcula prioridad e inserta. | `registrarSolicitud`, líneas 45-115 | Imposible de leer de una pasada; los comentarios internos marcan los métodos que faltan. | Alto: alta probabilidad de introducir bugs al modificarlo. | **Alta** |
| 3 | **Código duplicado** | `buscarPorEstado`, `buscarPorTipo` y `buscarPorResponsable` son idénticos salvo la cláusula `WHERE`. El mapeo del `ResultSet` a `Solicitud` (11 líneas) está copiado tres veces. | `buscarPorEstado`, `buscarPorTipo`, `buscarPorResponsable` | Agregar un campo a `Solicitud` obliga a modificar tres lugares. | Alto: corregir dos de tres copias deja un bug silencioso. | **Alta** |
| 4 | **Long parameter list / Data clump** | `registrarSolicitud` recibe 8 parámetros, 6 de ellos `String` consecutivos. Los datos del estudiante (nombre, apellido, email, teléfono, carrera) siempre viajan juntos. | `registrarSolicitud`, firma | El cliente debe recordar el orden exacto; invertir dos `String` compila igual y falla en producción. | Alto: error difícil de detectar, no lo atrapa el compilador. | **Alta** |
| 5 | **Números y strings mágicos** | `if (cant >= 5)`, `if (asignadas >= 10)`, `> 30`, y los literales `"pendiente"`, `"en_proceso"`, `"certificado"` repetidos por toda la clase. | Métodos `registrarSolicitud`, `asignarResponsable`, `cambiarEstado`, `generarReporte` | El significado está en comentarios, no en el código. Un typo como `"pendinte"` compila. | Medio: reglas de negocio invisibles y dispersas. | **Media** |
| 6 | **Nombres poco claros** | Variables `p`, `ep`, `res`, `rech`, `c`, `sum`, `lst`, `st`, `rs`, `prio`. Cada una necesita un comentario para entenderse. | `generarReporte`, los tres `buscarPor...` | El comentario hace el trabajo que debería hacer el nombre. | Medio: aumenta el tiempo de comprensión del código. | **Media** |
| 7 | **Condicional complejo** | `cambiarEstado` tiene `if/else` anidados de dos niveles con la máquina de estados completa escrita a mano, y la lógica de transición mezclada con el armado del SQL. | `cambiarEstado`, líneas 120-180 | Agregar un estado obliga a tocar el método entero. Viola OCP. | Alto: las transiciones válidas no son evidentes y es fácil romperlas. | **Alta** |
| 8 | **Excepciones tragadas** | Nueve bloques `catch (Exception ex)` que imprimen el mensaje y devuelven `-1`, `false` o una lista vacía. | Todos los métodos de la clase | El llamador no distingue "no hay resultados" de "falló la base de datos". | Alto: errores de infraestructura pasan inadvertidos. | **Alta** |

### Resumen de prioridades

- **Alta (6):** God Object, método largo, código duplicado, long parameter list, condicional complejo, excepciones tragadas.
- **Media (2):** números mágicos, nombres poco claros.

El orden de ataque propuesto es resolver primero el God Object —separando responsabilidades— porque eso habilita naturalmente la corrección de los demás.

### Observaciones adicionales (no priorizadas)

Fuera de los ocho smells documentados, se detectaron tres problemas vinculados al acceso a datos que se registran como deuda pendiente: SQL armado por concatenación de strings (riesgo de inyección), apertura y cierre manual de la conexión repetido en cada método, y ausencia de `try-with-resources`.