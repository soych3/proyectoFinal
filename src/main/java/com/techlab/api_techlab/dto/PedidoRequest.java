package com.techlab.api_techlab.dto;

import java.util.List;

// Cuerpo JSON de POST /api/pedidos
public record PedidoRequest(Integer usuarioId, List<LineaPedidoRequest> lineas) {
}
