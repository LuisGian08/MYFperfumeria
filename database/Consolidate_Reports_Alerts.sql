:ON ERROR EXIT
SET NOCOUNT ON;

IF DB_ID(N'MYF_Venta_DB') IS NULL
    THROW 51000, 'MYF_Venta_DB does not exist.', 1;
IF DB_ID(N'MYF_Inventario_DB') IS NULL
    THROW 51001, 'MYF_Inventario_DB does not exist.', 1;
IF DB_ID(N'MYF_Reporte_DB') IS NULL
    THROW 51002, 'MYF_Reporte_DB does not exist.', 1;
IF DB_ID(N'MYF_Alerta_DB') IS NULL
    THROW 51003, 'MYF_Alerta_DB does not exist.', 1;
IF OBJECT_ID(N'MYF_Reporte_DB.dbo.ReporteVentaDiaria', N'U') IS NULL
    THROW 51004, 'Source table ReporteVentaDiaria does not exist.', 1;
IF OBJECT_ID(N'MYF_Reporte_DB.dbo.ReporteProductoVendido', N'U') IS NULL
    THROW 51005, 'Source table ReporteProductoVendido does not exist.', 1;
IF OBJECT_ID(N'MYF_Reporte_DB.dbo.EventoVentaProcesado', N'U') IS NULL
    THROW 51006, 'Source table EventoVentaProcesado does not exist.', 1;
IF OBJECT_ID(N'MYF_Alerta_DB.dbo.AlertaStock', N'U') IS NULL
    THROW 51007, 'Source table AlertaStock does not exist.', 1;
GO

USE [MYF_Venta_DB];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.ReporteVentaDiaria', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.ReporteVentaDiaria (
            Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            Fecha DATE NOT NULL UNIQUE,
            CantidadVentas INT NOT NULL,
            UnidadesVendidas INT NOT NULL,
            BotellasVendidas INT NOT NULL,
            DecantsVendidos INT NOT NULL,
            TotalVentas NUMERIC(38,2) NOT NULL,
            FechaActualizacion DATETIME2 NOT NULL
        );
    END;

    IF OBJECT_ID(N'dbo.ReporteProductoVendido', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.ReporteProductoVendido (
            Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            Fecha DATE NOT NULL,
            ProductoId BIGINT NOT NULL,
            PresentacionId BIGINT NULL,
            NombreProducto NVARCHAR(255) NOT NULL,
            TipoPresentacion NVARCHAR(255) NOT NULL,
            VolumenMl NUMERIC(10,2) NULL,
            CantidadVendida INT NOT NULL,
            TotalVendido NUMERIC(38,2) NOT NULL
        );
    END;

    IF OBJECT_ID(N'dbo.EventoVentaProcesado', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.EventoVentaProcesado (
            Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            VentaId BIGINT NOT NULL UNIQUE,
            FechaProcesado DATETIME2 NOT NULL
        );
    END;

    SET IDENTITY_INSERT dbo.ReporteVentaDiaria ON;
    INSERT INTO dbo.ReporteVentaDiaria (
        Id, Fecha, CantidadVentas, UnidadesVendidas, BotellasVendidas,
        DecantsVendidos, TotalVentas, FechaActualizacion
    )
    SELECT source.Id, source.Fecha, source.CantidadVentas, source.UnidadesVendidas,
           source.BotellasVendidas, source.DecantsVendidos, source.TotalVentas,
           source.FechaActualizacion
    FROM MYF_Reporte_DB.dbo.ReporteVentaDiaria AS source
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.ReporteVentaDiaria AS target WHERE target.Id = source.Id
    );
    SET IDENTITY_INSERT dbo.ReporteVentaDiaria OFF;

    SET IDENTITY_INSERT dbo.ReporteProductoVendido ON;
    INSERT INTO dbo.ReporteProductoVendido (
        Id, Fecha, ProductoId, PresentacionId, NombreProducto, TipoPresentacion,
        VolumenMl, CantidadVendida, TotalVendido
    )
    SELECT source.Id, source.Fecha, source.ProductoId, source.PresentacionId,
           source.NombreProducto, source.TipoPresentacion, source.VolumenMl,
           source.CantidadVendida, source.TotalVendido
    FROM MYF_Reporte_DB.dbo.ReporteProductoVendido AS source
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.ReporteProductoVendido AS target WHERE target.Id = source.Id
    );
    SET IDENTITY_INSERT dbo.ReporteProductoVendido OFF;

    SET IDENTITY_INSERT dbo.EventoVentaProcesado ON;
    INSERT INTO dbo.EventoVentaProcesado (Id, VentaId, FechaProcesado)
    SELECT source.Id, source.VentaId, source.FechaProcesado
    FROM MYF_Reporte_DB.dbo.EventoVentaProcesado AS source
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.EventoVentaProcesado AS target WHERE target.Id = source.Id
    );
    SET IDENTITY_INSERT dbo.EventoVentaProcesado OFF;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

USE [MYF_Inventario_DB];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.AlertaStock', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.AlertaStock (
            Id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
            ProductoId BIGINT NOT NULL,
            Tipo NVARCHAR(255) NOT NULL,
            StockActual INT NOT NULL,
            StockMinimo INT NOT NULL,
            Unidad NVARCHAR(255) NOT NULL,
            Mensaje NVARCHAR(255) NOT NULL,
            Estado NVARCHAR(255) NOT NULL,
            FechaAlerta DATETIME2 NOT NULL,
            FechaAtendida DATETIME2 NULL
        );
    END;

    SET IDENTITY_INSERT dbo.AlertaStock ON;
    INSERT INTO dbo.AlertaStock (
        Id, ProductoId, Tipo, StockActual, StockMinimo, Unidad, Mensaje,
        Estado, FechaAlerta, FechaAtendida
    )
    SELECT source.Id, source.ProductoId, source.Tipo, source.StockActual,
           source.StockMinimo, source.Unidad, source.Mensaje, source.Estado,
           source.FechaAlerta, source.FechaAtendida
    FROM MYF_Alerta_DB.dbo.AlertaStock AS source
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.AlertaStock AS target WHERE target.Id = source.Id
    );
    SET IDENTITY_INSERT dbo.AlertaStock OFF;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

USE [MYF_Venta_DB];
SELECT N'ReporteVentaDiaria' AS TableName,
       (SELECT COUNT_BIG(*) FROM MYF_Reporte_DB.dbo.ReporteVentaDiaria) AS SourceRows,
       (SELECT COUNT_BIG(*) FROM MYF_Venta_DB.dbo.ReporteVentaDiaria) AS TargetRows
UNION ALL
SELECT N'ReporteProductoVendido',
       (SELECT COUNT_BIG(*) FROM MYF_Reporte_DB.dbo.ReporteProductoVendido),
       (SELECT COUNT_BIG(*) FROM MYF_Venta_DB.dbo.ReporteProductoVendido)
UNION ALL
SELECT N'EventoVentaProcesado',
       (SELECT COUNT_BIG(*) FROM MYF_Reporte_DB.dbo.EventoVentaProcesado),
       (SELECT COUNT_BIG(*) FROM MYF_Venta_DB.dbo.EventoVentaProcesado);
GO

USE [MYF_Inventario_DB];
SELECT N'AlertaStock' AS TableName,
       (SELECT COUNT_BIG(*) FROM MYF_Alerta_DB.dbo.AlertaStock) AS SourceRows,
       (SELECT COUNT_BIG(*) FROM MYF_Inventario_DB.dbo.AlertaStock) AS TargetRows;
GO
