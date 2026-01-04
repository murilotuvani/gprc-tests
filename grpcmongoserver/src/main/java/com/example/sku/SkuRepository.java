package com.example.sku;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkuRepository extends MongoRepository<Sku, Long> {

    List<Sku> findByWarehouseId(Long warehouseId);

    List<Sku> findByItemId(Long itemId);
}
