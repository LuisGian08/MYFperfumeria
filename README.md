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
                                                  │
                                    ┌─────────────┴─────────────┐
                                    │                           │
                 4 SQL Server independientes             RabbitMQ
                 Auth :14331, Product :14332          :5672 / :15672
                 Inventory :14333, Sales :14334
```

Alertas forman parte de Inventory Service y reportes de Sales Service. Las API públicas `/api/alertas/**` y `/api/reportes/**` se mantienen, por lo que el frontend conserva sus rutas.

Cada dominio de datos tiene su propia instancia SQL Server y volumen:

| Servicio | Puerto | Base de datos |
|---|---:|---|
| Auth Service | 8081 / SQL 14331 | MYF_Auth_DB |
| Product Service | 8082 / SQL 14332 | MYF_Producto_DB |
| Inventory Service | 8083 / SQL 14333 | MYF_Inventario_DB; alertas |
| Sales Service | 8084 / SQL 14334 | MYF_Venta_DB; reportes |
| Gateway | 8080 | — |

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
                         └─ stock.low → RabbitMQ → Inventory Service (alertas)

Sales Service ── sale.created → RabbitMQ → Sales Service (reportes)
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

## Si ya tiene la versión anterior funcionando

**No ejecute de nuevo el script antiguo que elimina tablas.**

Si todavía usa una instancia compartida con las seis bases anteriores, haga primero backups. Antes de consolidar, abra SQL Server Management Studio y, si aún no lo aplicó, ejecute una sola vez:

```text
database/MYF_Perfumes_Upgrade_Final.sql
```

El script agrega columnas/tablas necesarias para decants, clientes, pedidos, reportes y alertas sin borrar deliberadamente los datos existentes. Después siga la sección **Migración a las cuatro bases independientes**; no arranque la nueva aplicación hasta restaurar y verificar los cuatro backups consolidados.

En una instalación limpia, `run-all.ps1` crea las cuatro bases y Hibernate prepara sus tablas al iniciar los servicios.

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

El script valida la configuración y construye/inicia el stack de esta carpeta. Los cuatro trabajos `db-init-*` crean cada base solo cuando falta. Los contenedores usan nombres internos de servicio (`product-db`, `inventory-db`, etc.) para comunicarse.

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

## Migración a las cuatro bases independientes

El Compose principal ya define cuatro instancias SQL Server, pero **no copia datos automáticamente**. No ejecutes `run-all.ps1` con datos de producción hasta completar y verificar la migración. El procedimiento conserva las seis bases originales como respaldo y no las elimina.

### 1. Respaldar y detener escrituras

En SSMS conéctate a la instancia SQL Server que actualmente contiene `MYF_Auth_DB`, `MYF_Producto_DB`, `MYF_Inventario_DB`, `MYF_Venta_DB`, `MYF_Alerta_DB` y `MYF_Reporte_DB`. Haz un backup Full de las seis bases desde **Tasks > Back Up** y verifica que los archivos `.bak` se puedan leer. Mantén SQL Server disponible mientras realizas la consolidación.

Detén los servicios de aplicación antiguos para evitar escrituras concurrentes y que los consumidores anteriores de RabbitMQ compitan con los nuevos. En Docker Desktop, identifica y detén Gateway, Auth, Product, Inventory, Sales, Alert, Report y Frontend; deja activo el SQL Server antiguo hasta terminar la consolidación y crear los cuatro backups finales. No uses `docker compose down -v` ni elimines contenedores/volúmenes. Si aún no aplicaste la actualización histórica `database/MYF_Perfumes_Upgrade_Final.sql`, verifica sus cambios antes de migrar; la tabla de alertas debe incluir `Unidad` y Reportes debe incluir las columnas de presentación e idempotencia.

### 2. Consolidar tablas en la instancia original

En SSMS, conéctate a la instancia original y abre `database/Consolidate_Reports_Alerts.sql`. Activa **Query > SQLCMD Mode** porque el script usa `:ON ERROR EXIT`, y ejecútalo una sola vez mientras la aplicación sigue detenida. El script copia sin borrar:

- `ReporteVentaDiaria`, `ReporteProductoVendido` y `EventoVentaProcesado` a `MYF_Venta_DB`.
- `AlertaStock` a `MYF_Inventario_DB`.

El resultado final del script muestra filas de origen y destino. Confirma que coincidan las cantidades de cada tabla; si el script devuelve error o no coinciden, detente y conserva las bases originales. El script no elimina ni modifica las tablas de origen.

### 3. Crear backups consolidados

En SSMS, vuelve a generar backups Full de estas cuatro bases ya consolidadas:

```text
MYF_Auth_DB.bak
MYF_Producto_DB.bak
MYF_Inventario_DB.bak
MYF_Venta_DB.bak
```

Crea `.\database\backups` en la raíz del proyecto y copia allí esos cuatro archivos. La carpeta está excluida de Git porque puede contener información sensible.

Después de confirmar que los cuatro archivos consolidados se pueden leer, detén el SQL Server antiguo para liberar memoria, pero conserva su contenedor/servicio y volumen como rollback. No lo vuelvas a iniciar junto con el nuevo stack durante la prueba.

### 4. Iniciar solo los motores nuevos

Desde PowerShell en la raíz del repositorio, con Docker Desktop activo y `.env` configurado:

```powershell
$compose = @("compose", "--project-name", "myf_perfumes_sistema_final_2026", "--file", ".\docker-compose.yml", "--env-file", ".\.env")
docker @compose up -d auth-db product-db inventory-db sales-db
docker @compose run --rm db-init-auth
docker @compose run --rm db-init-product
docker @compose run --rm db-init-inventory
docker @compose run --rm db-init-sales
```

Los backups del host quedan visibles dentro de cada motor como `/var/opt/mssql/backup/`. Aún no inicies los servicios Java.

### 5. Restaurar los backups en las instancias nuevas

En SSMS, conéctate a cada puerto local y restaura el backup correspondiente:

| Instancia en SSMS | Base |
|---|---|
| `localhost,14331` | `MYF_Auth_DB` |
| `localhost,14332` | `MYF_Producto_DB` |
| `localhost,14333` | `MYF_Inventario_DB` |
| `localhost,14334` | `MYF_Venta_DB` |

Para cada backup, ejecuta primero `RESTORE FILELISTONLY FROM DISK = N'/var/opt/mssql/backup/<archivo>.bak';` en la instancia destino. Usa los valores `LogicalName` del resultado en la restauración y asigna los archivos de datos/log a rutas únicas bajo `/var/opt/mssql/data/`. Ejemplo de forma (reemplaza los nombres lógicos por los obtenidos):

```sql
RESTORE DATABASE [MYF_Auth_DB]
FROM DISK = N'/var/opt/mssql/backup/MYF_Auth_DB.bak'
WITH MOVE N'<logical-data-name>' TO N'/var/opt/mssql/data/MYF_Auth_DB.mdf',
     MOVE N'<logical-log-name>' TO N'/var/opt/mssql/data/MYF_Auth_DB_log.ldf',
     REPLACE, RECOVERY, STATS = 5;
```

Si un backup contiene más archivos de datos, agrega un `MOVE` por cada archivo lógico. Repite el proceso con el nombre de archivo/base correspondiente. No sobrescribas los backups de origen ni elimines la instancia/volumen anterior.

### 6. Verificar y arrancar la aplicación

En cada destino, confirma que la base esté `ONLINE`; compara los recuentos de tablas con los de la instancia original y revisa que las tablas de reportes estén en `MYF_Venta_DB` y `AlertaStock` en `MYF_Inventario_DB`. Solo cuando las cuatro restauraciones estén verificadas, ejecuta:

```powershell
.\run-all.ps1
```

Prueba inicio de sesión, catálogo, checkout, inventario, alertas y reportes. Conserva los backups y volúmenes originales hasta completar la validación. Para rollback, detén esta aplicación y vuelve a conectar la versión/instancia anterior; nunca borres el volumen antiguo como parte de esta prueba.

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

La documentación consolidada de los cuatro servicios está en:

- `http://localhost:8080/swagger-ui.html`

También se puede abrir cada servicio directamente:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`

Alertas aparecen en la documentación de Inventory Service y reportes en la de Sales Service; se acceden mediante las rutas públicas preservadas `/api/alertas/**` y `/api/reportes/**`. Las operaciones protegidas aparecen con el esquema `bearerAuth` (HTTP Bearer, JWT).

Para probar operaciones administrativas:

1. Ejecuta `POST /api/auth/login` desde Swagger con las credenciales de desarrollo configuradas para el entorno.
2. Copia el valor `token` de la respuesta.
3. Pulsa **Authorize**, pega el token y confirma; Swagger adjuntará la autorización a las operaciones protegidas.
4. Las rutas públicas, como las consultas del catálogo y `POST /api/ventas/checkout`, no requieren autorización.

El botón **Authorize** solo envía el token al probar las operaciones; la autorización real sigue aplicándose en el API Gateway.

## Prometheus y Grafana

`run-all.ps1` inicia Prometheus y Grafana junto con RabbitMQ. Prometheus recolecta las métricas Actuator de los cuatro servicios y del Gateway.

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

Netlify aplica los encabezados HTTP definidos en `frontend/_headers`, incluida una CSP obligatoria. Los scripts quedan limitados al mismo origen; los estilos inline siguen permitidos por el HTML existente. Se usan fuentes del sistema para evitar depender de Google Fonts. Mientras se utilice Quick Tunnel, las conexiones e imágenes de la API se permiten desde `*.trycloudflare.com`; si cambia el mecanismo/hostname del túnel, ajusta esos orígenes y vuelve a desplegar. Estas reglas aplican a los archivos estáticos de Netlify, no a las respuestas del API que pasan por Cloudflare Tunnel.

## Pruebas unitarias JUnit

Las pruebas Java están en `src/test/java` de los cinco módulos:

- Gateway: `gateway-service/src/test/java/com/myf/gateway/JwtGatewayFilterTest.java`
- Autenticación: `auth-service/src/test/java/com/myf/auth/service/AuthServiceTest.java`
- Productos: `product-service/src/test/java/com/myf/product/service/ProductoServiceTest.java`
- Inventario: `inventory-service/src/test/java/com/myf/inventory/service/InventarioServiceTest.java`
- Alertas: `inventory-service/src/test/java/com/myf/inventory/service/AlertListenerTest.java`
- Ventas: `sales-service/src/test/java/com/myf/sales/service/VentaServiceTest.java`
- Reportes: `sales-service/src/test/java/com/myf/sales/service/ReportListenerTest.java`

Desde PowerShell, situado en la raíz exterior del repositorio, ejecuta el comando del servicio que quieres probar. Por ejemplo:

```powershell
mvn -pl inventory-service test
```

Para ejecutar otro servicio, reemplaza `inventory-service` por `gateway-service`, `auth-service`, `product-service` o `sales-service`. Alertas y reportes se prueban desde sus módulos integrados. Cada comando ejecuta pruebas del módulo seleccionado y genera evidencia independiente. No requiere iniciar SQL Server ni RabbitMQ porque las dependencias externas se simulan. Surefire escribe archivos XML y TXT en `target/surefire-reports/` del módulo; se regeneran en cada ejecución y no se conservan en Git. El [informe de pruebas](UNIT-TEST-REPORT.md) resume los módulos y comandos.

## Entorno desechable para integración

Para iniciar SQL Server y RabbitMQ aislados de la base principal:

```powershell
.\integration-env-start.ps1
```

Este entorno usa SQL Server en `localhost:14330` con las cuatro bases de negocio y RabbitMQ en `localhost:5673`. Para retirarlo, ejecuta `.\integration-env-stop.ps1`; no afecta los contenedores ni los volúmenes de la aplicación principal.

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
