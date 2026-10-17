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

## Requisitos locales

- Java 21.
- Maven 3.9+.
- IntelliJ IDEA.
- SQL Server 2022/Express.
- RabbitMQ o Docker Desktop.
- Python 3 para servir el frontend local, si no usa Docker.

## IMPORTANTE si ya tiene la versión anterior funcionando

**No ejecute de nuevo el script antiguo que elimina tablas.**

Abra SQL Server Management Studio y ejecute una sola vez:

```text
database/MYF_Perfumes_Upgrade_Final.sql
```

El script agrega las columnas/tablas necesarias para decants, clientes, pedidos y reportes sin borrar deliberadamente los datos existentes.

Después inicie los servicios. Hibernate completará cualquier columna nueva pendiente mediante `ddl-auto: update`.

## Instalación desde cero

### 1. Crear las seis bases

Ejecute en SQL Server:

```text
database/Create_Databases.sql
```

Las tablas se crean/actualizan al iniciar cada microservicio.

### 2. Credenciales de desarrollo

La configuración local conserva valores de desarrollo para facilitar la ejecución académica. Antes de producción cambie todos los secretos.

Administrador inicial:

```text
Correo: admin@myf.local
Contraseña: Admin123*
```

### 3. RabbitMQ

RabbitMQ debe estar disponible en:

```text
localhost:5672
```

Panel web:

```text
http://localhost:15672
```

### 4. Ejecutar desde IntelliJ

Abra la carpeta raíz del proyecto como proyecto Maven y ejecute:

1. `AuthApplication`
2. `ProductApplication`
3. `InventoryApplication`
4. `SalesApplication`
5. `AlertApplication`
6. `ReportApplication`
7. `GatewayApplication`

Luego, dentro de `frontend`:

```powershell
python -m http.server 5500
```

Abra:

```text
Tienda: http://localhost:5500/index.html
Admin:  http://localhost:5500/admin.html
```

También puede ejecutar:

```powershell
.\run-all.ps1
```

El script evita iniciar una segunda copia cuando uno de los puertos ya está ocupado.

Para detener los procesos locales del proyecto:

```powershell
.\stop-all.ps1
```

## Docker

1. Copie `.env.example` como `.env`.
2. Cambie las contraseñas/secretos del archivo `.env`.
3. El servicio `db-init` espera a SQL Server y crea automáticamente las seis bases si todavía no existen.
4. Ejecute:

```bash
docker compose up --build -d
```

Docker Compose contiene:

- SQL Server
- inicializador automático de bases (`db-init`)
- RabbitMQ
- 7 servicios Java
- frontend con NGINX

Los contenedores usan los nombres de servicio internos (`product`, `inventory`, etc.) para comunicarse entre ellos.

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

Durante desarrollo:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`
- `http://localhost:8085/swagger-ui.html`
- `http://localhost:8086/swagger-ui.html`

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
