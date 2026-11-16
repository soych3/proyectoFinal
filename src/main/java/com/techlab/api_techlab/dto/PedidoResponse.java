package com.techlab.api_techlab.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.techlab.api_techlab.model.Pedido;

public record PedidoResponse(
        int id,
        int usuarioId,
        LocalDateTime fecha,
        String estado,
        BigDecimal total,
        List<LineaPedidoResponse> lineas) {

    public static PedidoResponse desde(Pedido p) {
        return new PedidoResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getFecha(),
                p.getEstado().name(),
                p.getTotal(),
                p.getLineas().stream().map(LineaPedidoResponse::desde).toList());
    }
}
