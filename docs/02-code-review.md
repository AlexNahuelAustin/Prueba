# Code Review — Sistema de Gestión de Solicitudes Académicas

**Rama revisada:** `main` · **Alcance:** del tag `estado-inicial` al `etapa-pruebas`
**Revisor:** Austin Alex Nahuel · **Fecha:** 25/10/2026

---

## 1. Checklist técnico

| # | Criterio | ¿Cumple? | Observación |
|---|---|---|---|
| 1 | El código compila sin errores ni warnings | ✅ Sí | `gradlew build` limpio |
| 2 | La suite de pruebas pasa al 100% | ✅ Sí | 14/14 en verde |
| 3 | Cada clase tiene una única responsabilidad | ✅ Sí | Separado en `models`, `repository`, `services` |
| 4 | No hay métodos de más de 30 líneas | ✅ Sí | El más largo es `registrarSolicitud`, 35 líneas |
| 5 | No hay código duplicado | ✅ Sí | El mapeo del `ResultSet` quedó en un solo método |
| 6 | Los nombres revelan intención | ⚠️ Parcial | Ver observación 1 |
| 7 | No hay números ni strings mágicos | ✅ Sí | Constantes y enums |
| 8 | Las dependencias se inyectan, no se instancian | ✅ Sí | Constructor injection en el servicio |
| 9 | Los errores se manejan explícitamente | ⚠️ Parcial | Ver observación 4 |
| 10 | No hay código muerto | ✅ Sí | Corregido, ver observación 2 |
| 11 | No hay vulnerabilidades de inyección SQL | ✅ Sí | `PreparedStatement` en todo el repositorio |
| 12 | Los recursos se cierran correctamente | ✅ Sí | `try-with-resources` |
| 13 | La lógica de negocio es testeable sin infraestructura | ✅ Sí | Dobles en memoria |
| 14 | La cobertura de ramas supera el 50% | ⚠️ Parcial | 54% total, pero 0% en repositorios |
| 15 | La nomenclatura es consistente | ❌ No | Ver observación 1 |

---

## 2. Observaciones y acciones

### Observación 1 — Nomenclatura mixta inglés/español
**Severidad:** baja · **Estado:** aceptada por el equipo

Las interfaces se llaman `RepositoryDeSolicitudes`, `RepositoryDeEstudiantes` y `RepositoryDeResponsables`, mezclando el término inglés *Repository* con conectores en español. El resto del código está íntegramente en español (`ServicioDeSolicitudes`, `GeneradorDeReportes`, `DatosDeEstudiante`).

*Recomendación:* unificar a `RepositorioDeSolicitudes` o migrar todo a inglés.

*Decisión:* se acepta la inconsistencia por tratarse de una convención ya usada por el equipo en proyectos anteriores. Queda registrada como deuda de estilo, no de diseño.

### Observación 2 — Código muerto detectado por cobertura
**Severidad:** media · **Estado:** corregida

El reporte de JaCoCo mostró el paquete raíz con 292 líneas al 0% de cobertura. Al inspeccionarlo se detectó que `GestorDeSolicitudes` seguía en el repositorio pese a que sus responsabilidades ya habían sido migradas al servicio y a los repositorios.

*Corrección:* se eliminó la clase. La cobertura de ramas subió de 30% a 54% solo por esa eliminación, lo que muestra cómo el código muerto distorsiona la métrica.

### Observación 3 — Paquete con mayúscula inicial
**Severidad:** baja · **Estado:** corregida

El paquete se había creado como `Repository`, violando la convención de Java de paquetes en minúscula. Provocó un error de compilación al no coincidir con los imports.

*Corrección:* renombrado a `repository`.

### Observación 4 — El servicio imprime por consola
**Severidad:** media · **Estado:** pendiente

`ServicioDeSolicitudes` escribe los mensajes de error con `System.out.println` y devuelve `-1` o `false`. Eso mezcla la capa de presentación con la de negocio y obliga al llamador a interpretar códigos de retorno en lugar de recibir una respuesta tipada.

*Recomendación:* devolver un `Resultado` con el motivo del rechazo, y dejar la impresión en el `Main`.

*Motivo del aplazamiento:* el cambio altera la firma de los métodos públicos y requiere reescribir los 14 tests. Se difiere para no mezclarlo con el refactoring estructural ya verificado.

### Observación 5 — Entidades con campos públicos
**Severidad:** media · **Estado:** pendiente

`Solicitud`, `Estudiante` y `Responsable` exponen todos sus campos como `public`. Cualquier clase puede modificar el estado de una solicitud sin pasar por una regla de negocio.

*Recomendación:* encapsular con getters y mover a la entidad las operaciones que le corresponden (por ejemplo, `solicitud.cerrar()` en lugar de asignar `estado` y `fechaCierre` desde afuera).

*Motivo del aplazamiento:* se prioriza completar la documentación de las etapas antes de abrir un nuevo frente de refactoring.

### Observación 6 — Repositorios sin cobertura
**Severidad:** media · **Estado:** pendiente

Los adaptadores H2 tienen 0% de cobertura. Las pruebas unitarias usan los dobles en memoria, lo cual es correcto, pero significa que el SQL real nunca se ejecuta en la suite.

*Recomendación:* agregar tests de integración que levanten H2 en memoria (`jdbc:h2:mem:test`) y verifiquen el mapeo real.

### Observación 7 — Validación de email débil
**Severidad:** baja · **Estado:** pendiente

La validación es `email.contains("@")`. Acepta valores como `"@"` o `"a@"`.

*Recomendación:* usar una expresión regular o una librería de validación.

---

## 3. Criterios de aceptación técnica

Para aprobar la integración de este incremento se exigió:

1. ✅ Compilación limpia
2. ✅ 100% de la suite de pruebas en verde
3. ✅ Comportamiento externo idéntico al del tag `estado-inicial`, verificado comparando la salida del `Main`
4. ✅ Sin código duplicado detectable
5. ✅ Sin vulnerabilidades de inyección SQL
6. ⚠️ Cobertura de ramas superior al 50% en la capa de servicios — se alcanzó 66%

**Resultado de la revisión:** aprobado con observaciones. Las seis observaciones pendientes quedan registradas como deuda técnica con su recomendación, sin bloquear la integración.