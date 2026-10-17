package com.myf.inventory.event;
public record StockLowEvent(Long productoId, int stockActual, int stockMinimo, String tipo, String unidad, String mensaje) {}
