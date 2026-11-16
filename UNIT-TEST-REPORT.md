# Evidencia de pruebas unitarias JUnit

**Fecha de última ejecución:** 2026-11-16<br>
**Proyecto:** MYF Perfumes — siete módulos Spring Boot<br>
**Ejecución:** `mvn clean test` desde la raíz exterior<br>
**Resultado:** `BUILD SUCCESS` — código de salida 0

## Resumen final

| Servicio | Clase de prueba | Ejecutadas | Fallidas | Errores | Omitidas |
|---|---|---:|---:|---:|---:|
| Gateway | `JwtGatewayFilterTest` | 1 | 0 | 0 | 0 |
| Autenticación | `AuthServiceTest` | 1 | 0 | 0 | 0 |
| Productos | `ProductoServiceTest` | 1 | 0 | 0 | 0 |
| Inventario | `InventarioServiceTest` | 4 | 0 | 0 | 0 |
| Ventas | `VentaServiceTest` | 6 | 0 | 0 | 0 |
| Alertas | `AlertListenerTest` | 1 | 0 | 0 | 0 |
| Reportes | `ReportListenerTest` | 1 | 0 | 0 | 0 |
| **Total** | **7 servicios** | **15** | **0** | **0** | **0** |

## Cómo reproducir cada servicio por separado

Abre PowerShell en la raíz exterior del repositorio y ejecuta **solo el comando del servicio que quieras verificar**:

```powershell
mvn -pl gateway-service test
```

```powershell
mvn -pl auth-service test
```

```powershell
mvn -pl product-service test
```

```powershell
mvn -pl inventory-service test
```

```powershell
mvn -pl sales-service test
```

```powershell
mvn -pl alert-service test
```

```powershell
mvn -pl report-service test
```

Cada ejecución muestra el resultado solo de ese módulo y escribe su propio reporte Surefire. No hace falta levantar la aplicación, SQL Server ni RabbitMQ para estas pruebas unitarias.

## Evidencia por ejecución

La última ejecución de `mvn clean test` mostró `BUILD SUCCESS` y devolvió código de salida 0:

| Comando Maven | Resultado | Reporte Surefire |
|---|---|---|
| `-pl gateway-service test` | 1 ejecutada, 0 fallidas | `gateway-service/target/surefire-reports/TEST-com.myf.gateway.JwtGatewayFilterTest.xml` |
| `-pl auth-service test` | 1 ejecutada, 0 fallidas | `auth-service/target/surefire-reports/TEST-com.myf.auth.service.AuthServiceTest.xml` |
| `-pl product-service test` | 1 ejecutada, 0 fallidas | `product-service/target/surefire-reports/TEST-com.myf.product.service.ProductoServiceTest.xml` |
| `-pl inventory-service test` | 4 ejecutadas, 0 fallidas | `inventory-service/target/surefire-reports/TEST-com.myf.inventory.service.InventarioServiceTest.xml` |
| `-pl sales-service test` | 6 ejecutadas, 0 fallidas | `sales-service/target/surefire-reports/TEST-com.myf.sales.service.VentaServiceTest.xml` |
| `-pl alert-service test` | 1 ejecutada, 0 fallidas | `alert-service/target/surefire-reports/TEST-com.myf.alert.service.AlertListenerTest.xml` |
| `-pl report-service test` | 1 ejecutada, 0 fallidas | `report-service/target/surefire-reports/TEST-com.myf.report.service.ReportListenerTest.xml` |

Cada XML va acompañado de su reporte de texto `.txt` con el mismo nombre base. En VS Code, abre un XML o TXT de la columna Reporte para consultar el resultado de ese servicio sin mezclarlo con los demás.

## Cobertura positiva por servicio

- **Gateway:** un JWT válido permite avanzar por una ruta protegida y propaga los encabezados de identidad.
- **Autenticación:** registrar un usuario guarda una contraseña codificada y devuelve su token y datos.
- **Productos:** crear un producto persiste una presentación activa y devuelve el resumen del catálogo.
- **Inventario:** abrir botella, validar stock insuficiente, descontar decants y comprobar que una entrada con stock normal no publique eventos de recuperación falsos.
- **Ventas:** checkout recalcula importes y valida stock/descuentos; también se comprueban cancelaciones, estados de pago y persistencia de comprobantes.
- **Alertas:** recibir un evento de stock bajo crea una alerta pendiente con sus datos.
- **Reportes:** recibir un evento de venta actualiza los totales diarios y de presentación y registra el evento procesado.

Las clases están en `src/test/java` bajo el paquete de cada servicio. Surefire genera un XML y un TXT en `target/surefire-reports/` dentro del módulo que acabas de ejecutar, por ejemplo:

- `inventory-service/target/surefire-reports/TEST-com.myf.inventory.service.InventarioServiceTest.xml`
- `sales-service/target/surefire-reports/TEST-com.myf.sales.service.VentaServiceTest.xml`
- `gateway-service/target/surefire-reports/TEST-com.myf.gateway.JwtGatewayFilterTest.xml`

Los reportes locales de `target/` se regeneran al ejecutar Maven y no están versionados. Este documento conserva el resumen de la última ejecución.

## Defectos reproducidos y correcciones

Antes de los cambios, las pruebas de regresión registraron un fallo en cada servicio:

| Servicio | Defecto reproducido | Corrección aplicada | Estado |
|---|---|---|---|
| Inventario | Se enviaban eventos `stock.recovered` en actualizaciones normales con el inventario ya sobre el mínimo. | Solo se envían eventos de recuperación cuando un inventario previamente persistido pasa de estar bajo/sobre el umbral a quedar por encima de él. | Corregido; prueba de regresión aprobada. |
| Ventas | Si un reintegro parcial durante una cancelación fallaba, reintentar podía duplicar los reintegros que ya habían concluido. | Ante un fallo, se compensan en orden inverso los reintegros realizados antes de devolver el error; si la compensación también falla, se registra el error y se informa que requiere conciliación manual. | Corregido para compensaciones exitosas; prueba de reintento aprobada. |

La corrección de Ventas reduce el riesgo de duplicación en este flujo, pero sigue siendo una operación distribuida entre servicios: una caída que impida también la compensación puede requerir conciliación manual. Esta prueba simula las respuestas HTTP y no sustituye una prueba de integración con Inventario y SQL Server reales.

## Alcance

Estas son pruebas unitarias: repositorios, RabbitMQ y llamadas HTTP externas se simulan o interceptan. El resultado demuestra que los casos probados pasan con sus dependencias controladas; no acredita el arranque de los siete servicios, la conexión real a la base, la integración con RabbitMQ ni un flujo E2E de navegador.

La JVM mostró advertencias de carga dinámica del agente de Mockito durante algunas ejecuciones. No afectaron los resultados.
