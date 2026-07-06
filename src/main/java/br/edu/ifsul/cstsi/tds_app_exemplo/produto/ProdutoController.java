package br.edu.ifsul.cstsi.tds_app_exemplo.produto;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Pelo Princípio da Responsabilidade Única (SRP - Single-responsibility Principle) os controller manipulam
    apenas DTOs.
    Porém, se a meta for entregar o produto no Curto Prazo, é aceitável não utilizar DTO no controller.
    Mas, se a meta for de Longo Prazo, a entrega de um produto em produção, é importante aplicar o SRP, e fazer
    com que a controller trabalhe apenas com DTOs, isso facilitará a manutenção no futuro e ajuda na segurança do
    aplicativo (não expõe todos os dados aos clientes do aplicativo).
 */
@RestController//indica que esta classe é um controller REST
@RequestMapping("api/v1/produtos") //Endpoint para toda classe
public class ProdutoController {

    @GetMapping
    public ResponseEntity<String> findAll(){
        return ResponseEntity.ok("findAll");
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> findById(@PathVariable Long id){
        return ResponseEntity.ok("findById" + " " + id);
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<String> findByNome(@PathVariable String nome){
        return ResponseEntity.ok("findByNome" + nome);
    }

    @PostMapping
    public ResponseEntity<String> insert(@RequestBody Produto produto){
        return ResponseEntity.ok("insert" + " " + produto);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody Produto produto){
        return ResponseEntity.ok("update" + " " + id + " " + produto);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        return ResponseEntity.ok("delete" + " " + id);
    }
}
