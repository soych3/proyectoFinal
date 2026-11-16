package com.techlab.api_techlab.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techlab.api_techlab.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}
