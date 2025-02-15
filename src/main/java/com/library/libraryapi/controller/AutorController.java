package com.library.libraryapi.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.library.libraryapi.controller.mapper.AutorMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.library.libraryapi.controller.dto.AutorDTO;
import com.library.libraryapi.controller.dto.ErroResposta;

import com.library.libraryapi.exception.RegistroDuplicadoException;

import com.library.libraryapi.model.Autor;
import com.library.libraryapi.service.AutorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/autores")
@RequiredArgsConstructor
// http://localhost:8080/autores
public class AutorController {

    private final AutorService service;


    @Qualifier("autorMapper")
    private final AutorMapper mapper;

    @PostMapping
    public ResponseEntity<Object> salvar(@RequestBody @Valid AutorDTO dto) {
        try {
             Autor autor = mapper.toEntity(dto);
             service.salvar(autor);
//            Autor autorEntidade = dto.mapearParaAutor();
//            service.salvar(autorEntidade);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(autor.getId())
                    .toUri();

            return ResponseEntity.created(location).build();
        } catch (RegistroDuplicadoException e) {
            var erroDto = ErroResposta.conflito(e.getMessage());
            return ResponseEntity.status(erroDto.status()).body(erroDto);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<AutorDTO> obterDetalhes(@PathVariable String id) {
        var idAutor = UUID.fromString(id);
        // return service.obterPorId(idAutor)
        // .map(autor -> ResponseEntity.ok(mapper.toDTO(autor)))
        // .orElseGet(() -> ResponseEntity.notFound().build());

        // Refatoracao usando o mapper
        Optional<Autor> autor = service.obterPorId(idAutor);
        if (autor.isPresent()) {
            Autor autorEncontrado = autor.get();
            AutorDTO autorDTO = new AutorDTO(autorEncontrado.getId(), autorEncontrado.getNome(),
                    autorEncontrado.getDataNascimento(), autorEncontrado.getNacionalidade());
            return ResponseEntity.ok(autorDTO);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") String id) {
        var idAutor = UUID.fromString(id);
        Optional<Autor> autor = service.deletarPorId(idAutor);
        if (autor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deletarPorId(autor.get().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<AutorDTO>> pesquisar(@RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "nacionalidade", required = false) String nacionalidade) {
        List<Autor> lista = service.pesquisaByExample(nome, nacionalidade);
        return ResponseEntity
                .ok(lista.stream().map(autor -> new AutorDTO(autor.getId(), autor.getNome(), autor.getDataNascimento(),
                        autor.getNacionalidade())).toList());
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> atualizar(@PathVariable("id") String id, @RequestBody AutorDTO entity) {
        try {
            var idAutor = UUID.fromString(id);
            Optional<Autor> autor = service.obterPorId(idAutor);
            if (autor.isEmpty()) {
                return ResponseEntity.notFound().build();

            }

            Autor autorEncontrado = autor.get();
            autorEncontrado.setNome(entity.nome());
            autorEncontrado.setDataNascimento(entity.dataNascimento());
            autorEncontrado.setNacionalidade(entity.nacionalidade());
            service.atualizar(autorEncontrado);

            return ResponseEntity.noContent().build();
        } catch (RegistroDuplicadoException e) {
            var erroDto = ErroResposta.conflito(e.getMessage());
            return ResponseEntity.status(erroDto.status()).body(erroDto);
        }
    }

}
