# Sistema de Gestión de Solicitudes Académicas

Trabajo Práctico Integrador — Metodología de Sistemas II (UTN)

Módulo para registrar solicitudes de estudiantes, clasificarlas por tipo, prioridad y estado, asignar responsables, consultar y generar reportes.

El objetivo del trabajo no es la funcionalidad en sí, sino **documentar la mejora de su calidad interna**: diagnóstico de code smells, aplicación de SOLID y Clean Code, refactorings verificados y revisión técnica.

## Tecnologías

- Java 21
- Gradle
- H2 (base de datos embebida)
- JUnit 5 + JaCoCo

## Instalación y uso

```bash
cd Sistema-Academico
./gradlew run        # ejecuta la demostración
./gradlew test       # corre la suite de pruebas
```

Reporte de cobertura:

```bash
./gradlew test jacocoTestReport
# build/reports/jacoco/test/html/index.html
```

## Funcionalidades

- Registrar solicitud (crea el estudiante si no existe)
- Cambiar estado respetando las transiciones válidas
- Asignar responsable con control de carga máxima
- Buscar por estado, tipo o responsable
- Generar reporte con totales, demora promedio y vencidas

## Estructura

```
Sistema-Academico/src/main/java/ar/utn/solicitudes/
├── models/        entidades, enums y objetos de valor
├── repository/    puertos de persistencia y adaptadores H2
├── services/      reglas de negocio y generación de reportes
└── Main.java      ensamblado de dependencias
```

## Las dos versiones

El repositorio contiene el antes y el después identificados por tags:

| Tag | Qué contiene |
|---|---|
| `estado-inicial` | Sistema base con los problemas de diseño a diagnosticar |
| `etapa-diagnostico` | Documento de calidad interna/externa y 8 code smells |
| `etapa-refactoring` | Los cinco refactorings aplicados |
| `etapa-pruebas` | Suite de 14 pruebas y reporte de cobertura |
| `etapa-docs` | Checklist de code review y documentación |
| `estado-refactorizado` | Estado final |

Comparación completa del antes y después:

```
https://github.com/AlexNahuelAustin/Prueba/compare/estado-inicial...estado-refactorizado
```

## Refactorings aplicados

| # | Refactoring | Smell que resuelve | Principio |
|---|---|---|---|
| 1 | Introduce Parameter Object | 8 parámetros en `registrarSolicitud` | — |
| 2 | Replace Magic Number with Constant | Números y strings mágicos | — |
| 3 | Extract Class | God Object, duplicación, excepciones tragadas | SRP + DIP |
| 4 | Extract Method | Métodos de 70 líneas | SRP |
| 5 | Replace Conditional with Polymorphism | Condicionales anidados de estado | OCP |

## Documentación

- [`docs/01-diagnostico.md`](docs/01-diagnostico.md) — calidad interna vs externa y code smells
- [`docs/02-code-review.md`](docs/02-code-review.md) — checklist técnico y observaciones

## Autor

Austin Alex Nahuel