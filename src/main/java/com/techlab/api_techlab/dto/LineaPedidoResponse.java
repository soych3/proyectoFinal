package com.techlab.api_techlab.dto;

import java.math.BigDecimal;

import com.techlab.api_techlab.model.LineaPedido;

public record LineaPedidoResponse(
        int productoId,
        String nombre,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {

    public static LineaPedidoResponse desde(LineaPedido l) {
        return new LineaPedidoResponse(
                l.getProducto().getId(),
                l.getProducto().getNombre(),
                l.getCantidad(),
                l.getPrecioUnitario(),
                l.getSubtotal());
    }
}
