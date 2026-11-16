package com.techlab.api_techlab.dto;

import java.math.BigDecimal;

// Cuerpo JSON de POST /api/productos y PUT /api/productos/{id}
public record ProductoRequest(
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer categoriaId,
        String imagenUrl,
        Integer stock) {
}
