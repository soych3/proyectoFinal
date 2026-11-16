package com.techlab.api_techlab.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techlab.api_techlab.dto.ProductoRequest;
import com.techlab.api_techlab.exception.CategoriaNoEncontradaException;
import com.techlab.api_techlab.exception.ProductoNoEncontradoException;
import com.techlab.api_techlab.model.Categoria;
import com.techlab.api_techlab.model.Producto;
import com.techlab.api_techlab.repository.CategoriaRepository;
import com.techlab.api_techlab.repository.ProductoRepository;
import com.techlab.api_techlab.util.Validador;

@Service
public class ProductoService {

    private final ProductoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    // GET /api/productos
    public List<Producto> listarTodos() {
        return repository.findAll();
    }

    // GET /api/productos/{id}
    public Producto obtenerPorId(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("No se encontro un producto con el id " + id));
    }

    // POST /api/productos
    public Producto guardar(ProductoRequest datos) {
        Producto p = new Producto();
        aplicarDatos(p, datos);
        return repository.save(p);
    }

    // PUT /api/productos/{id}
    @Transactional
    public Producto actualizar(int id, ProductoRequest datos) {
        // Se bloquea la fila: un pedido en curso no puede pisar (ni ser pisado por) este cambio de stock.
        // Si el id no existe se lanza la excepción y no se modifica nada.
        Producto p = repository.buscarParaActualizar(id)
                .orElseThrow(() -> new ProductoNoEncontradoException("No se encontro un producto con el id " + id));
        aplicarDatos(p, datos);
        return repository.save(p);
    }

    // DELETE /api/productos/{id}
    public void eliminar(int id) {
        repository.delete(obtenerPorId(id));
    }

    // GET /api/productos/categoria/{categoria}
    public List<Producto> listarPorCategoria(String categoria) {
        return repository.findByCategoriaNombreIgnoreCase(categoria);
    }

    // Valida el cuerpo recibido y lo copia al producto
    private void aplicarDatos(Producto p, ProductoRequest datos) {
        Validador.validarNombre(datos.nombre());
        Validador.validarPrecio(datos.precio());
        Validador.validarStock(datos.stock());

        p.setNombre(datos.nombre().trim());
        p.setDescripcion(datos.descripcion());
        p.setPrecio(datos.precio());
        p.setImagenUrl(datos.imagenUrl());
        p.setStock(datos.stock());
        p.setCategoria(obtenerCategoria(datos.categoriaId()));
    }

    private Categoria obtenerCategoria(Integer categoriaId) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new CategoriaNoEncontradaException("No existe la categoría con id " + categoriaId));
    }
}
