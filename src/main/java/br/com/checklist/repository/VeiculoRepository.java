package br.com.checklist.repository;

import br.com.checklist.domain.entity.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {
    Optional<Veiculo> findByPlacaIgnoreCase(String placa);
    boolean existsByPlacaIgnoreCase(String placa);
    List<Veiculo> findByAtivoTrueOrderByPlacaAsc();
    List<Veiculo> findAllByOrderByPlacaAsc();
}
