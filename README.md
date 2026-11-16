# MYF Perfumes — Sistema final de ventas, inventario, tienda y decants

Sistema web de **MYF Perfumes** basado en microservicios con Java 21, Spring Boot, Spring Cloud API Gateway, SQL Server, RabbitMQ, JWT, HTML/CSS/JavaScript y Docker.

La versión final incluye dos interfaces separadas:

- **Tienda cliente:** `http://localhost:5500/index.html`
- **Panel administrativo:** `http://localhost:5500/admin.html`

## Funcionalidades principales

### Tienda cliente
- Catálogo real obtenido desde Product Service.
- Búsqueda por perfume o marca.
- Filtros por marca, género y tipo de presentación.
- Botellas completas y **decants de 5 ml, 10 ml y 30 ml** como presentaciones del mismo perfume.
- Stock de botellas y volumen disponible para decants consultado desde Inventory Service.
- Carrito persistente en el navegador.
- Validación de stock en frontend y **revalidación obligatoria en backend**.
- El checkout público recalcula precios en backend y no acepta descuentos enviados por el cliente.
- Checkout y creación de pedido real.
- Registro de cliente, método de pago, dirección y notas.

### Panel administrativo
- Login con JWT.
- Dashboard.
- CRUD de productos.
- Configuración de botella y decants por perfume.
- Inventario de botellas cerradas.
- Inventario en mililitros para botellas abiertas destinadas a decants.
- Acción **Abrir botella para decants**: descuenta una botella cerrada y suma automáticamente su volumen al stock en ml.
- Registro de venta directa.
- Pedidos y cambio de estado.
- Cancelación con reintegro de inventario.
- Clientes.
- Alertas de stock y de volumen bajo.
- Reportes de ventas, botellas y decants.

## Arquitectura

```text
Tienda cliente ─┐
                ├──> API Gateway :8080 ──> Auth Service      :8081
Panel admin ────┘                         Product Service    :8082
                                          Inventory Service  :8083
                                          Sales Service      :8084
                                          Alert Service      :8085
                                          Report Service     :8086
                                                  │
                                    ┌─────────────┴─────────────┐
                                    │                           │
                                SQL Server                  RabbitMQ
                                 :1433                    :5672 / :15672
```

Cada microservicio conserva su propia base de datos:

| Servicio | Puerto | Base de datos |
|---|---:|---|
| Gateway | 8080 | — |
| Auth | 8081 | MYF_Auth_DB |
| Product | 8082 | MYF_Producto_DB |
| Inventory | 8083 | MYF_Inventario_DB |
| Sales | 8084 | MYF_Venta_DB |
| Alert | 8085 | MYF_Alerta_DB |
| Report | 8086 | MYF_Reporte_DB |

## RabbitMQ como BUS de eventos

Exchange principal: `myf.events`

Eventos utilizados:

- `stock.low`
- `stock.recovered`
- `sale.created`
- `order.created`
- `order.cancelled`

Ejemplo del flujo:

```text
Compra → Sales Service → Inventory Service
                         │
                         ├─ descuenta botella o ml
                         │
                         └─ stock.low → RabbitMQ → Alert Service

Sales Service ── sale.created → RabbitMQ → Report Service
```

## Modelo de decants

Un perfume puede tener varias presentaciones sin duplicarse visualmente:

```text
Khamrah Dukhan
├── Botella 100 ml
├── Decant 5 ml
├── Decant 10 ml
└── Decant 30 ml
```

El inventario diferencia:

- `stockActual`: botellas cerradas.
- `volumenDecantMl`: mililitros disponibles de una botella abierta para decants.

Ejemplo: si hay 15 ml disponibles, se puede vender 5 ml o 10 ml, pero no 30 ml.

Cuando una botella cerrada se destina a decants, el panel puede abrirla: el sistema resta 1 botella y suma los ml correspondientes al volumen disponible para decantar, dejando trazabilidad en movimientos.

## Requisitos

- Docker Desktop con Docker Compose v2 para ejecutar el sistema completo.
- Java 21 y Maven 3.9+ solo para ejecutar pruebas o depurar servicios fuera de Docker.
- PowerShell en Windows para los scripts incluidos.

El flujo soportado para el sistema completo es Docker Compose desde esta carpeta exterior. No requiere instalar SQL Server Express ni iniciar servicios Java manualmente.

## IMPORTANTE si ya tiene la versión anterior funcionando

**No ejecute de nuevo el script antiguo que elimina tablas.**

Abra SQL Server Management Studio y ejecute una sola vez:

```text
database/MYF_Perfumes_Upgrade_Final.sql
```

El script agrega las columnas/tablas necesarias para decants, clientes, pedidos y reportes sin borrar deliberadamente los datos existentes.

Después inicie los servicios. Hibernate completará cualquier columna nueva pendiente mediante `ddl-auto: update`.

## Inicialización en Windows

1. Inicie Docker Desktop.
2. Si todavía no existe `.env`, copie `.env.example` a `.env` y configure credenciales locales. No sobrescriba un `.env` existente.
3. Desde PowerShell, en la raíz exterior del repositorio, ejecute:

```powershell
.\run-all.ps1
```

Si la política de PowerShell bloquea los scripts, usa el comando equivalente:

```powershell
docker compose --project-name myf_perfumes_sistema_final_2026 --file .\docker-compose.yml --env-file .\.env up -d --build
```

El script valida la configuración y construye/inicia el stack de esta carpeta. `db-init` crea las seis bases solo cuando faltan. Los contenedores usan los nombres internos de servicio (`product`, `inventory`, etc.) para comunicarse.

Direcciones locales:

- Tienda y administración: `http://localhost:5500`
- API y Swagger consolidado: `http://localhost:8080/swagger-ui.html`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001` (usuario `admin`; contraseña configurada en `.env`)
- RabbitMQ: `http://localhost:15672`

Para consultar estado y registros:

```powershell
docker compose --project-name myf_perfumes_sistema_final_2026 ps
docker compose --project-name myf_perfumes_sistema_final_2026 logs -f gateway
```

Para detener los contenedores sin borrar datos:

```powershell
.\stop-all.ps1
```

El SQL Server, Prometheus y Grafana usan volúmenes persistentes. No ejecute `docker compose down -v`, porque borraría los datos guardados.

## APIs públicas de la tienda

Sin JWT:

```text
GET  /api/productos
GET  /api/productos/{id}
GET  /api/categorias
GET  /api/marcas
GET  /api/inventario
GET  /api/inventario/{productoId}
POST /api/ventas/checkout
```

Las operaciones administrativas `POST`, `PUT` y `DELETE` continúan protegidas por JWT, salvo el checkout público.

## Ejemplo de producto con presentaciones

```json
{
  "categoriaId": 1,
  "marcaId": 1,
  "codigo": "PERF-KHAMRAH-DUKHAN",
  "nombre": "Khamrah Dukhan",
  "descripcion": "Fragancia intensa y especiada",
  "tipo": "PERFUME",
  "volumenMl": 100,
  "genero": "UNISEX",
  "precio": 170,
  "costo": 110,
  "imagenUrl": null,
  "presentaciones": [
    {"tipo":"BOTELLA","volumenMl":100,"precio":170,"costo":110,"activo":true},
    {"tipo":"DECANT","volumenMl":5,"precio":15,"costo":0,"activo":true},
    {"tipo":"DECANT","volumenMl":10,"precio":28,"costo":0,"activo":true},
    {"tipo":"DECANT","volumenMl":30,"precio":70,"costo":0,"activo":true}
  ]
}
```

## Ejemplo de inventario

```json
POST /api/inventario/1/configurar
{
  "stockActual": 3,
  "stockMinimo": 2,
  "volumenDecantMl": 85,
  "volumenDecantMinimoMl": 20
}
```

## Ejemplo de checkout

```json
POST /api/ventas/checkout
{
  "descuento": 0,
  "metodoPago": "YAPE",
  "observacion": "Entrega por la tarde",
  "canal": "WEB",
  "cliente": {
    "nombre": "Cliente Demo",
    "telefono": "999999999",
    "email": "cliente@ejemplo.com",
    "direccion": "Dirección de prueba"
  },
  "items": [
    {"productoId": 1, "presentacionId": 2, "cantidad": 2}
  ]
}
```

Si la presentación 2 corresponde a un decant de 10 ml, la compra de cantidad 2 descuenta **20 ml** del inventario de decants.

## Estados de pedido

- PENDIENTE
- CONFIRMADO
- PREPARANDO
- LISTO
- ENTREGADO
- CANCELADO

Una cancelación reintegra al inventario las botellas o mililitros descontados, una sola vez.

## Swagger

La documentación consolidada de los seis servicios está en:

- `http://localhost:8080/swagger-ui.html`

También se puede abrir cada servicio directamente:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`
- `http://localhost:8085/swagger-ui.html`
- `http://localhost:8086/swagger-ui.html`

Cada servicio documenta sus operaciones, parámetros, validaciones y ejemplos de solicitudes. Las operaciones protegidas aparecen con el esquema `bearerAuth` (HTTP Bearer, JWT).

Para probar operaciones administrativas:

1. Ejecuta `POST /api/auth/login` desde Swagger con las credenciales de desarrollo configuradas para el entorno.
2. Copia el valor `token` de la respuesta.
3. Pulsa **Authorize**, pega el token y confirma; Swagger adjuntará la autorización a las operaciones protegidas.
4. Las rutas públicas, como las consultas del catálogo y `POST /api/ventas/checkout`, no requieren autorización.

El botón **Authorize** solo envía el token al probar las operaciones; la autorización real sigue aplicándose en el API Gateway.

## Prometheus y Grafana

`run-all.ps1` inicia Prometheus y Grafana junto con RabbitMQ. Prometheus recolecta las métricas Actuator de los seis servicios y del Gateway.

- Grafana: `http://localhost:3001` por defecto (usuario `admin`; contraseña de `GRAFANA_PASSWORD` en `.env`). Cambia `GRAFANA_PORT` en `.env` para usar otro puerto del equipo anfitrión.
- Prometheus: `http://localhost:9090`
- Métricas de cada servicio: `http://localhost:<puerto>/actuator/prometheus`

El datasource de Prometheus y el dashboard `MYF Perfumes - Servicios` se cargan automáticamente en Grafana. Los volúmenes Docker conservan historial de métricas y configuración del dashboard.

## Publicar la tienda en Netlify con Cloudflare Tunnel

Netlify publica únicamente los archivos estáticos del frontend. Cloudflare Tunnel conecta un hostname HTTPS con el Nginx del contenedor `frontend`; ese Nginx entrega imágenes/comprobantes y reenvía `/api/` al Gateway. No abras ni reenvíes al router los puertos de SQL Server, RabbitMQ ni los microservicios.

1. En Cloudflare Zero Trust, crea un túnel administrado y agrega una ruta pública como `api.tudominio.com` hacia `http://frontend:80`. Usa el token del túnel en `CLOUDFLARE_TUNNEL_TOKEN` dentro del `.env` local; no lo subas a Git.
2. Despliega este repositorio en Netlify con la configuración de [netlify.toml](./netlify.toml). En las variables de entorno del sitio configura `MYF_API=https://api.tudominio.com` (reemplaza el hostname por el de tu ruta Cloudflare) y vuelve a desplegar. El build genera `frontend/runtime-config.js`; la variable es una URL pública, no un secreto.
3. En `.env`, agrega el origen exacto del sitio Netlify a `CORS_ALLOWED_ORIGINS`, separado por comas y sin rutas, por ejemplo `http://localhost:5500,https://tu-sitio.netlify.app`.
4. Desde PowerShell, inicia/actualiza el Gateway y el túnel:

   ```powershell
   docker compose --project-name myf_perfumes_sistema_final_2026 --file .\docker-compose.yml --env-file .\.env --profile cloudflare up -d --build gateway cloudflared
   ```

El backend y el túnel deben permanecer activos en la PC para que la tienda pública pueda consultar el catálogo. Las imágenes y comprobantes siguen dependiendo del volumen local `uploads`. `runtime-config.js` conserva el comportamiento local para Docker; no pongas el token de Cloudflare en Netlify ni en archivos versionados.

Netlify aplica los encabezados HTTP definidos en `frontend/_headers`. La CSP se publica inicialmente como `Content-Security-Policy-Report-Only` para identificar recursos que la política podría bloquear sin interrumpir la tienda. La web actualmente utiliza fuentes de Google Fonts, estilos inline y un hostname HTTPS dinámico para el API; revisa la consola del navegador y el siguiente escaneo antes de convertirla en una política obligatoria y restringir sus orígenes. Estas reglas aplican a los archivos estáticos de Netlify, no a las respuestas del API que pasan por Cloudflare Tunnel.

## Pruebas unitarias JUnit

Las pruebas Java están en `src/test/java` de los siete módulos:

- Gateway: `gateway-service/src/test/java/com/myf/gateway/JwtGatewayFilterTest.java`
- Autenticación: `auth-service/src/test/java/com/myf/auth/service/AuthServiceTest.java`
- Productos: `product-service/src/test/java/com/myf/product/service/ProductoServiceTest.java`
- Inventario: `inventory-service/src/test/java/com/myf/inventory/service/InventarioServiceTest.java`
- Ventas: `sales-service/src/test/java/com/myf/sales/service/VentaServiceTest.java`
- Alertas: `alert-service/src/test/java/com/myf/alert/service/AlertListenerTest.java`
- Reportes: `report-service/src/test/java/com/myf/report/service/ReportListenerTest.java`

Desde PowerShell, situado en la raíz exterior del repositorio, ejecuta el comando del servicio que quieres probar. Por ejemplo:

```powershell
mvn -pl inventory-service test
```

Para ejecutar otro servicio, reemplaza `inventory-service` por `gateway-service`, `auth-service`, `product-service`, `sales-service`, `alert-service` o `report-service`. Cada comando ejecuta solo las pruebas del módulo seleccionado y genera su evidencia independiente. No requiere iniciar SQL Server ni RabbitMQ porque las dependencias externas se simulan. Surefire escribe archivos XML y TXT en `target/surefire-reports/` del módulo; se regeneran en cada ejecución y no se conservan en Git. El [informe de pruebas](UNIT-TEST-REPORT.md) contiene los siete comandos, el resultado individual y la ruta de cada reporte.

## Entorno desechable para integración

Para iniciar SQL Server y RabbitMQ aislados de la base principal:

```powershell
.\integration-env-start.ps1
```

Este entorno usa SQL Server en `localhost:14330` y RabbitMQ en `localhost:5673`. Para retirarlo, ejecuta `.\integration-env-stop.ps1`; no afecta los contenedores ni el volumen de la aplicación principal.

## Prueba rápida

Con todos los servicios encendidos:

```powershell
.\smoke-test.ps1
```

El script comprueba login, catálogo público, inventario y dashboard.

## Seguridad

Para producción:

- Cambiar `JWT_SECRET`.
- Cambiar contraseña SQL Server.
- Cambiar credenciales RabbitMQ.
- Cambiar contraseña del usuario administrador.
- Usar HTTPS.
- Restringir CORS a los dominios reales.
- No subir `.env` a Git.

`.env` ya está contemplado en `.gitignore`.
