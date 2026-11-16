package com.techlab.api_techlab.util;

import java.math.BigDecimal;

import com.techlab.api_techlab.exception.StockInsuficienteException;

public class Validador {

    public static void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
    }

    public static void validarPrecio(BigDecimal precio) {
        if (precio == null) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }
        // compareTo devuelve un número negativo si precio es menor que cero
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
    }

    public static void validarStock(Integer stock) {
        if (stock == null) {
            throw new IllegalArgumentException("El stock es obligatorio.");
        }
        if (stock < 0) {
            throw new StockInsuficienteException("El stock no puede ser negativo.");
        }
    }
}
