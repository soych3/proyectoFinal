package com.techlab.api_techlab.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.techlab.api_techlab.model.Categoria;
import com.techlab.api_techlab.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    // GET /api/categorias (alimenta el desplegable del formulario)
    public List<Categoria> listarTodas() {
        return repository.findAll();
    }
}
