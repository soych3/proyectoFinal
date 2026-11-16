package com.techlab.api_techlab.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.techlab.api_techlab.dto.PedidoRequest;
import com.techlab.api_techlab.dto.PedidoResponse;
import com.techlab.api_techlab.service.PedidoService;

@RestController
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // POST /api/pedidos -> 201 con el pedido en estado PENDIENTE; 400 si falta stock
    @PostMapping("/api/pedidos")
    public ResponseEntity<PedidoResponse> crear(@RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crear(request));
    }

    // GET /api/usuarios/{id}/pedidos -> historial del usuario (404 si el usuario no existe)
    @GetMapping("/api/usuarios/{id}/pedidos")
    public ResponseEntity<List<PedidoResponse>> historial(@PathVariable int id) {
        return ResponseEntity.ok(pedidoService.listarPorUsuario(id));
    }
}
