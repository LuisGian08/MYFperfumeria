# MYF Perfumes — Cambios de la versión final

Esta versión parte del proyecto MYF Perfumes existente y lo amplía sin reemplazar la arquitectura de microservicios.

## Tienda cliente
- Home y catálogo responsive con apariencia de ecommerce.
- Productos cargados desde Product Service.
- Buscador y filtros por marca, género y presentación.
- Vista de producto con botella completa y decants de 5, 10 y 30 ml.
- Disponibilidad calculada desde Inventory Service.
- Carrito persistente y checkout.
- Registro de pedido/venta y cliente.
- Revalidación de stock y precios en backend.
- El checkout público no puede asignarse descuentos.

## Administración
- Login JWT.
- Dashboard.
- CRUD de productos.
- Configuración de presentaciones por perfume.
- Botellas cerradas y volumen en ml para decants.
- Acción para abrir una botella y convertirla en volumen disponible para decants.
- Venta directa, pedidos, clientes, alertas y reportes.
- Cancelación de pedido con reintegro de inventario y protección contra reactivación inconsistente.

## Backend
- Presentaciones de producto `BOTELLA` y `DECANT`.
- Inventario separado en unidades y ml.
- Optimistic locking en inventario para reducir ventas concurrentes sobre el mismo stock.
- Movimientos de inventario con unidad `UNIDAD` o `ML`.
- Clientes y estados de pedido.
- Snapshot del precio/presentación en el detalle de venta.
- Reportes separados de botellas y decants.
- Idempotencia básica de `sale.created` en Report Service mediante registro de ventas procesadas.

## RabbitMQ / BUS
Exchange: `myf.events`.

Eventos principales:
- `stock.low`
- `stock.recovered`
- `sale.created`
- `order.created`
- `order.cancelled`

## Docker
- Dockerfile para cada microservicio.
- Frontend en NGINX.
- Docker Compose con SQL Server, inicializador de bases, RabbitMQ, los siete servicios Java y frontend.
- `.env.example` para configuración.

## Base de datos
Para una instalación existente ejecutar una sola vez:

`database/MYF_Perfumes_Upgrade_Final.sql`

El script agrega la estructura nueva sin ejecutar los `DROP TABLE` del script original.

## Validaciones realizadas en el entorno de generación
- JavaScript: validación sintáctica con `node --check`.
- YAML: parseo correcto de `docker-compose.yml` y todos los `application.yml`.
- Revisión estática de rutas entre frontend, Gateway y controllers.
- Revisión sintáctica parcial de Java con JDK 21; el entorno no dispone de Maven ni Docker para ejecutar la compilación integral o levantar los contenedores.

La verificación final de ejecución debe hacerse en el equipo de desarrollo con Maven/IntelliJ y la infraestructura local.
