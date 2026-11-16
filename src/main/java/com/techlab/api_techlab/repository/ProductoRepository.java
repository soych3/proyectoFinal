package com.techlab.api_techlab.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techlab.api_techlab.model.Producto;

import jakarta.persistence.LockModeType;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Filtra en la base (reemplaza el recorrido en memoria) y no falla si hay productos sin categoría
    List<Producto> findByCategoriaNombreIgnoreCase(String nombre);

    // Bloquea la fila mientras dura la transacción: dos pedidos simultáneos no pueden
    // descontar el mismo stock a la vez.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> buscarParaActualizar(@Param("id") int id);
}
