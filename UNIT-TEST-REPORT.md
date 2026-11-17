# Informe de pruebas unitarias JUnit

## Arquitectura comprobada

El proyecto activo tiene cinco módulos Maven: Gateway, Auth, Product, Inventory y Sales. Las alertas se prueban dentro de Inventory; los reportes, dentro de Sales.

## Estado de validación de esta modificación

Se ejecutó la suite del reactor con Maven 3.9.9 y JDK 21 en un contenedor temporal, porque `mvn` no está instalado localmente y el repositorio no incluye Maven Wrapper:

```text
BUILD SUCCESS
17 pruebas ejecutadas; 0 fallidas, 0 errores, 0 omitidas
```

| Módulo | Pruebas | Fallos | Errores |
|---|---:|---:|---:|
| Gateway | 1 | 0 | 0 |
| Auth | 1 | 0 | 0 |
| Product | 1 | 0 | 0 |
| Inventory, incluye Alertas | 6 | 0 | 0 |
| Sales, incluye Reportes | 8 | 0 | 0 |
| **Total** | **17** | **0** | **0** |

El comando ejecutado fue:

```powershell
docker run --rm --volume "${PWD}:/workspace" --volume "$env:USERPROFILE\.m2:/root/.m2" --workdir /workspace maven:3.9.9-eclipse-temurin-21 mvn --batch-mode --no-transfer-progress test
```

Con Maven 3.9+ instalado localmente, se puede reproducir desde la raíz con:

```powershell
mvn test
```

No se necesita iniciar SQL Server ni RabbitMQ para estas pruebas unitarias; las dependencias externas se simulan. Surefire genera sus resultados en `target/surefire-reports/` dentro de cada módulo.

## Casos relevantes

- **Inventario:** apertura de botellas, control de stock, consumo de decants y eventos de recuperación.
- **Alertas (Inventory):** evento de stock bajo crea una alerta y evento de recuperación actualiza la alerta correspondiente.
- **Ventas:** checkout, validación de precios/stock, cancelaciones, estados de pago y comprobantes.
- **Reportes (Sales):** `sale.created` actualiza agregados y registra el evento para evitar procesarlo dos veces.
- **Gateway, Auth y Product:** pruebas existentes de JWT, autenticación y operaciones de productos.

Una prueba unitaria no sustituye la validación de SQL Server, RabbitMQ ni un flujo E2E. Mockito/JDK emitieron advertencias sobre carga dinámica del agente; no afectaron el resultado.
