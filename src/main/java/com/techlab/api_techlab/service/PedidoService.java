package com.techlab.api_techlab.service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techlab.api_techlab.dto.LineaPedidoRequest;
import com.techlab.api_techlab.dto.PedidoRequest;
import com.techlab.api_techlab.dto.PedidoResponse;
import com.techlab.api_techlab.exception.ProductoNoEncontradoException;
import com.techlab.api_techlab.exception.StockInsuficienteException;
import com.techlab.api_techlab.exception.UsuarioNoEncontradoException;
import com.techlab.api_techlab.model.Pedido;
import com.techlab.api_techlab.model.Producto;
import com.techlab.api_techlab.model.Usuario;
import com.techlab.api_techlab.repository.PedidoRepository;
import com.techlab.api_techlab.repository.ProductoRepository;
import com.techlab.api_techlab.repository.UsuarioRepository;

@Service
public class PedidoService {

    private static final int CANTIDAD_MAXIMA = 10_000;

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // POST /api/pedidos
    // Todo ocurre en una transacción: si algún producto no tiene stock suficiente se lanza
    // la excepción y se deshacen los descuentos de stock ya hechos (no queda un pedido a medias).
    @Transactional
    public PedidoResponse crear(PedidoRequest request) {
        if (request == null || request.usuarioId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (request.lineas() == null || request.lineas().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto.");
        }

        Usuario usuario = obtenerUsuario(request.usuarioId());

        // Map id de producto -> cantidad total (si un producto viene repetido se suman las cantidades)
        // TreeMap: se bloquean los productos siempre en orden de id, así dos pedidos simultáneos
        // no pueden quedar trabados entre sí (deadlock).
        Map<Integer, Integer> cantidades = new TreeMap<>();
        for (LineaPedidoRequest l : request.lineas()) {
            if (l == null || l.productoId() == null) {
                throw new IllegalArgumentException("Cada línea debe indicar el productoId.");
            }
            if (l.cantidad() == null || l.cantidad() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad del producto " + l.productoId() + " debe ser mayor a cero.");
            }
            if (l.cantidad() > CANTIDAD_MAXIMA) {
                throw new IllegalArgumentException(
                        "La cantidad máxima por producto es " + CANTIDAD_MAXIMA + ".");
            }
            // Math.addExact falla en vez de desbordar si se repite un producto muchas veces
            try {
                cantidades.merge(l.productoId(), l.cantidad(), Math::addExact);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Cantidad total inválida para el producto " + l.productoId() + ".");
            }
        }

        Pedido pedido = new Pedido(usuario);
        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
            int productoId = entrada.getKey();
            int cantidad = entrada.getValue();
            Producto producto = productoRepository.buscarParaActualizar(productoId)
                    .orElseThrow(() -> new ProductoNoEncontradoException(
                            "No se encontro un producto con el id " + productoId));

            if (producto.getStock() < cantidad) {
                throw new StockInsuficienteException("Stock insuficiente para " + producto.getNombre()
                        + ": disponible " + producto.getStock() + ", solicitado " + cantidad + ".");
            }
            producto.setStock(producto.getStock() - cantidad);
            pedido.agregarLinea(producto, cantidad);
        }

        return PedidoResponse.desde(pedidoRepository.save(pedido));
    }

    // GET /api/usuarios/{id}/pedidos
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorUsuario(int usuarioId) {
        obtenerUsuario(usuarioId);
        return pedidoRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    private Usuario obtenerUsuario(int id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontro un usuario con el id " + id));
    }
}
