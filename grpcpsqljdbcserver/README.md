# Servidor MySQL 8.


```bash
docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres
docker exec -it postgres /bin/bash
psql -U postgres
```

```sql
create user grpc with password 'grpc';
       
       
CREATE DATABASE grpc WITH OWNER grpc;
\c grpc


-- 2. Definição do Tipo ENUM (Equivalente ao gRPC enum)
CREATE TYPE availability_type AS ENUM (
    'READY_TO_SHIP', 
    'MADE_TO_ORDER', 
    'OPEN_BOX', 
    'USED', 
    'REFURBISHED'
);

-- 3. Criação da Tabela Sku
CREATE TABLE skus (
sku_id BIGINT PRIMARY KEY,
warehouse_id BIGINT,
item_id BIGINT NOT NULL,
amount INTEGER NOT NULL DEFAULT 0,
country_code CHAR(3) NOT NULL,
availability availability_type NOT NULL DEFAULT 'READY_TO_SHIP',
price_amount NUMERIC(19, 4) NOT NULL, -- Precisão para cálculos financeiros
currency_code VARCHAR(3) NOT NULL DEFAULT 'USD',
last_updated TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Índices para performance (Recomendado para gRPC Services)
CREATE INDEX idx_skus_warehouse ON skus(warehouse_id);
CREATE INDEX idx_skus_item ON skus(item_id);
```