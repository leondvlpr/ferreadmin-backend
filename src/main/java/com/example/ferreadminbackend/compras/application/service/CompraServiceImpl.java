package com.example.ferreadminbackend.compras.application.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ferreadminbackend.compras.application.dto.CompraDTO;
import com.example.ferreadminbackend.compras.domain.entity.Compra;
import com.example.ferreadminbackend.compras.domain.repository.CompraRepository;
import com.example.ferreadminbackend.insumo.application.dto.ResponseDTO;
import com.example.ferreadminbackend.insumo.application.service.InsumoServiceImpl;
import com.example.ferreadminbackend.insumo.domain.entity.Insumo;

@Service
public class CompraServiceImpl implements CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private InsumoServiceImpl insumoServiceImpl;

    @Override
    public List<Compra> obtenerCompras() {
        return compraRepository.findAll();
    }

    @Override
    public Optional<Compra> obtenerCompra(Long idCompra) {
        return compraRepository.findById(idCompra);
    }

    @Override
    public ResponseDTO guardarCompraPersonalizada(CompraDTO compra) {
        try {

            Compra compraEntity = new Compra();

            List<Insumo> insumosCompra = compra
                    .getInsumos()
                    .stream()
                    .map(insumoCompra -> {

                        Insumo insumo = insumoServiceImpl.obtenerInsumoPorId(insumoCompra.getIdInsumo())
                                .orElseThrow(() -> new IllegalArgumentException("El insumo no existe"));

                        // REASIGNAR CANTIDAD DEL INSUMO
                        insumo.setCantidadDisponible(insumo.getCantidadDisponible() + insumoCompra.getCantidad());

                        return insumo;
                    }).collect(Collectors.toList());

            Long totalCompra = insumosCompra
                    .stream()
                    .mapToLong(Insumo::getPrecioUnitario).sum();

            compraEntity.setFechaCompra(LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            compraEntity.setInsumos(insumosCompra);
            compraEntity.setDescripcionCompra(compra.getDescripcionCompra());
            compraEntity.setTotalCompra(totalCompra);
            compraEntity.setNombreCliente(compra.getNombreCliente());
            compraRepository.save(compraEntity);

            return new ResponseDTO(200, "La compra se guardó correctamente", null);
        } catch (Exception e) {
            System.out.println(e);
            return new ResponseDTO(500, "Error interno al intentar guardar la compra", null);
        }
    }

    @Override
    public ResponseDTO generarOrdenCompra() {

        return new ResponseDTO(200, "La orden de compra se generó correctamente", null);
    }
}
