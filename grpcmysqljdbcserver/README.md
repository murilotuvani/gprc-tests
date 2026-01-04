# Servidor MySQL 8.


```bash
docker exec -it mysql84 /bin/bash
mysql -u root -proot
```

```sql
create user 'grpc'@'127.0.0.1' identified by 'grpc';
create user 'grpc' identified by 'grpc';
       
       
CREATE DATABASE IF NOT EXISTS grpc;
USE grpc;
    
grant all on grpc.* to 'grpc';
grant all on grpc.* to 'grpc'@'127.0.0.1';

CREATE TABLE skus (
sku_id BIGINT UNSIGNED PRIMARY KEY,
warehouse_id BIGINT UNSIGNED DEFAULT NULL,
item_id BIGINT UNSIGNED NOT NULL,
amount INT UNSIGNED NOT NULL DEFAULT 0,
country_code VARCHAR(3) NOT NULL,
availability_type ENUM('READY_TO_SHIP','MADE_TO_ORDER','OPEN_BOX', 'USED','REFURBISHED') NOT NULL DEFAULT 'READY_TO_SHIP',
price_amount DECIMAL(19, 4) NOT NULL,
currency_code CHAR(3) NOT NULL DEFAULT 'USD',
last_updated TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);
```