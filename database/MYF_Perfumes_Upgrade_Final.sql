
USE MYF_Producto_DB;
GO
IF OBJECT_ID('dbo.PresentacionProducto','U') IS NULL
CREATE TABLE dbo.PresentacionProducto(
 Id BIGINT IDENTITY PRIMARY KEY, ProductoId BIGINT NOT NULL, Codigo NVARCHAR(80) NOT NULL UNIQUE, Tipo NVARCHAR(20) NOT NULL,
 VolumenMl DECIMAL(10,2) NOT NULL, Precio DECIMAL(12,2) NOT NULL, Costo DECIMAL(12,2) NULL DEFAULT 0, Estado NVARCHAR(20) NOT NULL DEFAULT N'ACTIVO',
 FechaCreacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(), FechaActualizacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
 CONSTRAINT FK_PresentacionProducto_Producto FOREIGN KEY(ProductoId) REFERENCES dbo.Producto(Id) ON DELETE CASCADE
);
GO
/* Upgrade no destructivo para una base MYF Perfumes existente.
   Ejecutar una sola vez antes de iniciar la versión final. */
USE MYF_Inventario_DB;
GO
IF COL_LENGTH('dbo.Inventario','VolumenDecantMl') IS NULL ALTER TABLE dbo.Inventario ADD VolumenDecantMl INT NOT NULL CONSTRAINT DF_Inv_VolDec DEFAULT 0;
IF COL_LENGTH('dbo.Inventario','VolumenDecantMinimoMl') IS NULL ALTER TABLE dbo.Inventario ADD VolumenDecantMinimoMl INT NOT NULL CONSTRAINT DF_Inv_VolDecMin DEFAULT 20;
IF COL_LENGTH('dbo.Inventario','Version') IS NULL ALTER TABLE dbo.Inventario ADD Version BIGINT NOT NULL CONSTRAINT DF_Inv_Version DEFAULT 0;
IF COL_LENGTH('dbo.MovimientoInventario','Unidad') IS NULL ALTER TABLE dbo.MovimientoInventario ADD Unidad NVARCHAR(20) NOT NULL CONSTRAINT DF_Mov_Unidad DEFAULT N'UNIDAD';
DECLARE @c1 sysname=(SELECT name FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID('dbo.MovimientoInventario') AND name='CK_MovimientoInventario_Tipo');
IF @c1 IS NOT NULL EXEC('ALTER TABLE dbo.MovimientoInventario DROP CONSTRAINT '+QUOTENAME(@c1));
GO

USE MYF_Alerta_DB;
GO
IF COL_LENGTH('dbo.AlertaStock','Unidad') IS NULL ALTER TABLE dbo.AlertaStock ADD Unidad NVARCHAR(20) NOT NULL CONSTRAINT DF_Alerta_Unidad DEFAULT N'UNIDAD';
DECLARE @c2 sysname=(SELECT name FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID('dbo.AlertaStock') AND name='CK_AlertaStock_Tipo');
IF @c2 IS NOT NULL EXEC('ALTER TABLE dbo.AlertaStock DROP CONSTRAINT '+QUOTENAME(@c2));
GO

USE MYF_Venta_DB;
GO
IF OBJECT_ID('dbo.Cliente','U') IS NULL
CREATE TABLE dbo.Cliente(Id BIGINT IDENTITY PRIMARY KEY,Nombre NVARCHAR(180) NOT NULL,Telefono NVARCHAR(50),Email NVARCHAR(180),Direccion NVARCHAR(500),FechaCreacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),FechaActualizacion DATETIME2 NOT NULL DEFAULT SYSDATETIME());
IF COL_LENGTH('dbo.Venta','ClienteId') IS NULL ALTER TABLE dbo.Venta ADD ClienteId BIGINT NULL;
IF COL_LENGTH('dbo.Venta','Canal') IS NULL ALTER TABLE dbo.Venta ADD Canal NVARCHAR(20) NOT NULL CONSTRAINT DF_Venta_Canal DEFAULT N'ADMIN';
IF COL_LENGTH('dbo.Venta','StockReintegrado') IS NULL ALTER TABLE dbo.Venta ADD StockReintegrado BIT NOT NULL CONSTRAINT DF_Venta_Reintegro DEFAULT 0;
IF COL_LENGTH('dbo.DetalleVenta','PresentacionId') IS NULL ALTER TABLE dbo.DetalleVenta ADD PresentacionId BIGINT NULL;
IF COL_LENGTH('dbo.DetalleVenta','VolumenMl') IS NULL ALTER TABLE dbo.DetalleVenta ADD VolumenMl DECIMAL(10,2) NULL;
ALTER TABLE dbo.Venta ALTER COLUMN UsuarioId BIGINT NULL;
DECLARE @cv1 sysname=(SELECT name FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID('dbo.Venta') AND name='CK_Venta_Estado');
IF @cv1 IS NOT NULL EXEC('ALTER TABLE dbo.Venta DROP CONSTRAINT '+QUOTENAME(@cv1));
DECLARE @cv2 sysname=(SELECT name FROM sys.check_constraints WHERE parent_object_id=OBJECT_ID('dbo.DetalleVenta') AND name='CK_DetalleVenta_TipoProducto');
IF @cv2 IS NOT NULL EXEC('ALTER TABLE dbo.DetalleVenta DROP CONSTRAINT '+QUOTENAME(@cv2));
GO

USE MYF_Reporte_DB;
GO
IF OBJECT_ID('dbo.EventoVentaProcesado','U') IS NULL
CREATE TABLE dbo.EventoVentaProcesado(Id BIGINT IDENTITY PRIMARY KEY, VentaId BIGINT NOT NULL UNIQUE, FechaProcesado DATETIME2 NOT NULL DEFAULT SYSDATETIME());
IF COL_LENGTH('dbo.ReporteVentaDiaria','BotellasVendidas') IS NULL ALTER TABLE dbo.ReporteVentaDiaria ADD BotellasVendidas INT NOT NULL CONSTRAINT DF_Rep_Bot DEFAULT 0;
IF COL_LENGTH('dbo.ReporteVentaDiaria','DecantsVendidos') IS NULL ALTER TABLE dbo.ReporteVentaDiaria ADD DecantsVendidos INT NOT NULL CONSTRAINT DF_Rep_Dec DEFAULT 0;
IF COL_LENGTH('dbo.ReporteProductoVendido','PresentacionId') IS NULL ALTER TABLE dbo.ReporteProductoVendido ADD PresentacionId BIGINT NULL;
IF COL_LENGTH('dbo.ReporteProductoVendido','TipoPresentacion') IS NULL ALTER TABLE dbo.ReporteProductoVendido ADD TipoPresentacion NVARCHAR(20) NOT NULL CONSTRAINT DF_Rep_Tipo DEFAULT N'BOTELLA';
IF COL_LENGTH('dbo.ReporteProductoVendido','VolumenMl') IS NULL ALTER TABLE dbo.ReporteProductoVendido ADD VolumenMl DECIMAL(10,2) NULL;
DECLARE @ur sysname=(SELECT name FROM sys.key_constraints WHERE parent_object_id=OBJECT_ID('dbo.ReporteProductoVendido') AND name='UQ_ReporteProducto_FechaProducto');
IF @ur IS NOT NULL EXEC('ALTER TABLE dbo.ReporteProductoVendido DROP CONSTRAINT '+QUOTENAME(@ur));
GO
