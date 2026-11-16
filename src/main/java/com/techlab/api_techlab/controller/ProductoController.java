package com.techlab.api_techlab.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techlab.api_techlab.dto.ProductoRequest;
import com.techlab.api_techlab.model.Producto;
import com.techlab.api_techlab.service.ProductoService;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /api/productos -> 200 con la lista (vacía si no hay productos)
    @GetMapping
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    // GET /api/productos/categoria/{categoria} -> 200 con los productos de esa categoría
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(productoService.listarPorCategoria(categoria));
    }

    // GET /api/productos/{id} -> 200, o 404 si no existe (lo resuelve GlobalExceptionHandler)
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtener(@PathVariable int id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // POST /api/productos -> 201 Created
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody ProductoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.guardar(datos));
    }

    // PUT /api/productos/{id} -> 200
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable int id, @RequestBody ProductoRequest datos) {
        return ResponseEntity.ok(productoService.actualizar(id, datos));
    }

    // DELETE /api/productos/{id} -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
