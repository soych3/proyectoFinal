package com.techlab.api_techlab.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.techlab.api_techlab.dto.LineaPedidoRequest;
import com.techlab.api_techlab.dto.PedidoRequest;
import com.techlab.api_techlab.dto.PedidoResponse;
import com.techlab.api_techlab.exception.StockInsuficienteException;
import com.techlab.api_techlab.exception.UsuarioNoEncontradoException;
import com.techlab.api_techlab.model.Pedido;
import com.techlab.api_techlab.model.Producto;
import com.techlab.api_techlab.model.Usuario;
import com.techlab.api_techlab.repository.PedidoRepository;
import com.techlab.api_techlab.repository.ProductoRepository;
import com.techlab.api_techlab.repository.UsuarioRepository;

class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private ProductoRepository productoRepository;
    private UsuarioRepository usuarioRepository;
    private PedidoService service;
    private Producto cafe;

    @BeforeEach
    void preparar() {
        pedidoRepository = mock(PedidoRepository.class);
        productoRepository = mock(ProductoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new PedidoService(pedidoRepository, productoRepository, usuarioRepository);

        cafe = new Producto();
        cafe.setId(1);
        cafe.setNombre("Café Premium");
        cafe.setPrecio(new BigDecimal("12.50"));
        cafe.setStock(10);

        Usuario usuario = new Usuario();
        usuario.setId(5);
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario));
        when(productoRepository.buscarParaActualizar(1)).thenReturn(Optional.of(cafe));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void creaPedidoPendienteDescuentaStockYCalculaTotal() {
        PedidoRequest request = new PedidoRequest(5, List.of(new LineaPedidoRequest(1, 4)));

        PedidoResponse respuesta = service.crear(request);

        assertEquals("PENDIENTE", respuesta.estado());
        assertEquals(new BigDecimal("50.00"), respuesta.total());
        assertEquals(6, cafe.getStock());
    }

    @Test
    void sumaCantidadesDeUnProductoRepetido() {
        PedidoRequest request = new PedidoRequest(5,
                List.of(new LineaPedidoRequest(1, 2), new LineaPedidoRequest(1, 3)));

        PedidoResponse respuesta = service.crear(request);

        assertEquals(1, respuesta.lineas().size());
        assertEquals(5, respuesta.lineas().get(0).cantidad());
        assertEquals(5, cafe.getStock());
    }

    @Test
    void sinStockSuficienteLanzaExcepcionYNoDescuenta() {
        PedidoRequest request = new PedidoRequest(5, List.of(new LineaPedidoRequest(1, 11)));

        assertThrows(StockInsuficienteException.class, () -> service.crear(request));
        assertEquals(10, cafe.getStock());
    }

    @Test
    void usuarioInexistenteLanzaExcepcion() {
        PedidoRequest request = new PedidoRequest(99, List.of(new LineaPedidoRequest(1, 1)));

        assertThrows(UsuarioNoEncontradoException.class, () -> service.crear(request));
    }

    @Test
    void cantidadCeroEsInvalida() {
        PedidoRequest request = new PedidoRequest(5, List.of(new LineaPedidoRequest(1, 0)));

        assertThrows(IllegalArgumentException.class, () -> service.crear(request));
    }
}
