package com.gprcclientdemo;

import com.example.sku.Sku;
import com.example.sku.SkuRequest;
import com.example.sku.SkuResponse;
import com.example.sku.SkuServiceGrpc;
import com.google.protobuf.Timestamp;
import com.google.type.Money;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {

    record ProdutoEstoque(
            int skuId,
            int warehouseId,
            int itemId,
            double amount,
            String countryCode,
            String availabilityType,
            double basePrice,
            String currencyCode,
            java.sql.Timestamp alterado
    ) {}

    static void main() {
        String url = "jdbc:mysql://127.0.0.1:3380/autogeral";
        String user = "root";
        String password = "root";
        String query = """
                select a.codigo sku_id
                     , a.loja warehouse_id
                     , a.produto_codigo item_id
                     , a.disponivel amount
                     , 'BR' contry_code
                     , 'READY_TO_SHIP' availability_type
                     , b.preco_prazo base_price
                     , 'BRL' currency_code
                     , a.alterado
                  from produto_estoque a join produtos_dbf b on a.PRODUTO_CODIGO=b.CODIGO
                 where b.FINALIDADE_CODIGO=1
                   and b.PRECO_PRAZO>0
                """;

        String target = "127.0.0.1:9090";
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        SkuServiceGrpc.SkuServiceBlockingStub stub = SkuServiceGrpc.newBlockingStub(channel);

        List<ProdutoEstoque> batch = new ArrayList<>();
        int batchSize = 1000;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            IO.println("Erro: Driver JDBC do MySQL não encontrado.");
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();

        LocalDateTime beforProcess = LocalDateTime.now();
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ProdutoEstoque record = new ProdutoEstoque(
                        rs.getInt("sku_id"),
                        rs.getInt("warehouse_id"),
                        rs.getInt("item_id"),
                        rs.getDouble("amount"),
                        rs.getString("contry_code"),
                        rs.getString("availability_type"),
                        rs.getDouble("base_price"),
                        rs.getString("currency_code"),
                        rs.getTimestamp("alterado")
                );
                batch.add(record);

                if (batch.size() >= batchSize) {
                    List<ProdutoEstoque> batchToSend = batch;
                    executor.submit(() -> {
                        LocalDateTime befor = LocalDateTime.now();
                        sendBatch(stub, batchToSend);
                        LocalDateTime after = LocalDateTime.now();
                        IO.println("Time for batch to be sent: " + java.time.Duration.between(befor, after).toMillis() + " ms");
                    });
                    batch = new ArrayList<>();
                }
            }

            // Enviar o restante
            if (!batch.isEmpty()) {
                List<ProdutoEstoque> batchToSend = batch;
                executor.submit(() -> sendBatch(stub, batchToSend));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            channel.shutdownNow();
        }
        LocalDateTime afterProcess = LocalDateTime.now();
        IO.println("Total time for the process: " + java.time.Duration.between(beforProcess, afterProcess).toMillis() + " ms");
    }

    private static void sendBatch(SkuServiceGrpc.SkuServiceBlockingStub stub, List<ProdutoEstoque> lista) {
        SkuRequest.Builder requestBuilder = SkuRequest.newBuilder();

        for (ProdutoEstoque p : lista) {
            Sku.Builder skuBuilder = Sku.newBuilder()
                    .setSkuId(p.skuId())
                    .setWarehouseId(p.warehouseId())
                    .setItemId(p.itemId())
                    .setAmount((int) p.amount())
                    .setCountryCode(p.countryCode())
                    .setAvailabilityType(Sku.AvailabilityType.valueOf(p.availabilityType()));

            Money money = Money.newBuilder()
                    .setCurrencyCode(p.currencyCode())
                    .setUnits((long) p.basePrice())
                    .setNanos((int) ((p.basePrice() - (long) p.basePrice()) * 1_000_000_000))
                    .build();
            skuBuilder.setBasePrice(money);

            if (p.alterado() != null) {
                Timestamp timestamp = Timestamp.newBuilder()
                        .setSeconds(p.alterado().getTime() / 1000)
                        .setNanos(p.alterado().getNanos())
                        .build();
                skuBuilder.setLastUpdated(timestamp);
            }

            requestBuilder.addSkus(skuBuilder.build());
        }

        SkuResponse response = stub.importSkus(requestBuilder.build());
        IO.println("Lote enviado. Resposta: " + response.getMessage());
    }
}
