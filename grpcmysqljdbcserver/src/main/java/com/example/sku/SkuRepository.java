package com.example.sku;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Repository
public class SkuRepository {

    private final JdbcTemplate jdbcTemplate;

    public SkuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Sku> skuRowMapper = new RowMapper<Sku>() {
        @Override
        public Sku mapRow(ResultSet rs, int rowNum) throws SQLException {
            Sku sku = new Sku();
            sku.setId(rs.getLong("sku_id"));
            sku.setWarehouseId(rs.getObject("warehouse_id") != null ? rs.getLong("warehouse_id") : null);
            sku.setItemId(rs.getLong("item_id"));
            sku.setAmount(rs.getInt("amount"));
            sku.setCountryCode(rs.getString("country_code"));
            sku.setAvailabilityType(AvailabilityType.valueOf(rs.getString("availability_type")));
            sku.setPriceAmount(rs.getBigDecimal("price_amount"));
            sku.setCurrencyCode(rs.getString("currency_code"));
            
            Timestamp lastUpdated = rs.getTimestamp("last_updated");
            if (lastUpdated != null) {
                sku.setLastUpdated(lastUpdated.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());
            }
            return sku;
        }
    };

    public List<Sku> findAll() {
        String sql = "SELECT * FROM skus";
        return jdbcTemplate.query(sql, skuRowMapper);
    }

    public Optional<Sku> findById(Long id) {
        String sql = "SELECT * FROM skus WHERE sku_id = ?";
        List<Sku> skus = jdbcTemplate.query(sql, skuRowMapper, id);
        return skus.stream().findFirst();
    }

    public List<Sku> findByWarehouseId(Long warehouseId) {
        String sql = "SELECT * FROM skus WHERE warehouse_id = ?";
        return jdbcTemplate.query(sql, skuRowMapper, warehouseId);
    }

    public List<Sku> findByItemId(Long itemId) {
        String sql = "SELECT * FROM skus WHERE item_id = ?";
        return jdbcTemplate.query(sql, skuRowMapper, itemId);
    }

    public int save(Sku sku) {
        String sql = """
                INSERT INTO skus (sku_id, warehouse_id, item_id, amount, country_code, availability_type, price_amount, currency_code, last_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                        warehouse_id      = VALUES(warehouse_id),
                        amount            = VALUES(amount),
                        availability_type = VALUES(availability_type),
                        price_amount      = VALUES(price_amount),
                        currency_code     = VALUES(currency_code),
                        last_updated      = VALUES(last_updated);
                """;
        return jdbcTemplate.update(sql,
                sku.getId(),
                sku.getWarehouseId(),
                sku.getItemId(),
                sku.getAmount(),
                sku.getCountryCode(),
                sku.getAvailabilityType().name(),
                sku.getPriceAmount(),
                sku.getCurrencyCode(),
                sku.getLastUpdated() != null ? Timestamp.from(sku.getLastUpdated().toInstant()) : null
        );
    }

    public int update(Sku sku) {
        String sql = "UPDATE skus SET warehouse_id = ?, item_id = ?, amount = ?, country_code = ?, availability_type = ?, price_amount = ?, currency_code = ?, last_updated = ? " +
                "WHERE sku_id = ?";
        return jdbcTemplate.update(sql,
                sku.getWarehouseId(),
                sku.getItemId(),
                sku.getAmount(),
                sku.getCountryCode(),
                sku.getAvailabilityType().name(),
                sku.getPriceAmount(),
                sku.getCurrencyCode(),
                sku.getLastUpdated() != null ? Timestamp.from(sku.getLastUpdated().toInstant()) : null,
                sku.getId()
        );
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM skus WHERE sku_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
