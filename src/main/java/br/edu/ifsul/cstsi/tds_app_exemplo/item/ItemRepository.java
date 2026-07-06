package br.edu.ifsul.cstsi.tds_app_exemplo.item;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}