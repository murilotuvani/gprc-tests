package com.example.sku;

import com.example.sku.grpc.Sku;
import com.example.sku.grpc.SkuByItemRequest;
import com.example.sku.grpc.SkuByWarehouseRequest;
import com.example.sku.grpc.SkuGetByIdRequest;
import com.example.sku.grpc.SkuListResponse;
import com.example.sku.grpc.SkuRequest;
import com.example.sku.grpc.SkuResponse;
import com.example.sku.grpc.SkuServiceGrpc;
import com.google.protobuf.Timestamp;
import com.google.type.Money;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GrpcSkuService extends SkuServiceGrpc.SkuServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcSkuService.class);
    private final SkuRepository skuRepository;

    public GrpcSkuService(SkuRepository skuRepository) {
        this.skuRepository = skuRepository;
    }

    @Override
    public void importSkus(SkuRequest request, StreamObserver<SkuResponse> responseObserver) {
        try {
            List<com.example.sku.Sku> skus = new ArrayList<>();
            for (com.example.sku.grpc.Sku skuProto : request.getSkusList()) {
                com.example.sku.Sku skuEntity = mapProtoToEntity(skuProto);
                skus.add(skuEntity);
            }

            String responseMassage = "";
            if(skus.isEmpty()){
                responseMassage = "No skus to import.";
            } else {
                List<com.example.sku.Sku> savedSkus = skuRepository.saveAll(skus);
                responseMassage = "Imported " + savedSkus.size() + " skus successfully.";

            }

            SkuResponse response = SkuResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage(responseMassage)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.info("ImportSkus completed: {}", responseMassage);
        } catch (Exception e) {
            String message = "Error importing skus: " + e.getMessage();
            SkuResponse response = SkuResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(message)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            logger.error("ImportSkus failed: {}", message, e);
        }
    }

    @Override
    public void getById(SkuGetByIdRequest request, StreamObserver<Sku> responseObserver) {
        Optional<com.example.sku.Sku> skuEntity = skuRepository.findById(request.getSkuId());
        if (skuEntity.isPresent()) {
            responseObserver.onNext(mapEntityToProto(skuEntity.get()));
        } else {
            responseObserver.onNext(Sku.getDefaultInstance());
        }
        responseObserver.onCompleted();
        logger.info("GetById completed for skuId: {}", request.getSkuId());
    }

    @Override
    public void getByWarehouse(SkuByWarehouseRequest request, StreamObserver<SkuListResponse> responseObserver) {
        List<com.example.sku.Sku> allSkus = skuRepository.findByWarehouseId(request.getWarehouseId());
        List<Sku> protoSkus = allSkus.stream()
                .map(this::mapEntityToProto)
                .collect(Collectors.toList());

        SkuListResponse response = SkuListResponse.newBuilder()
                .addAllSkus(protoSkus)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        logger.info("GetByWarehouse completed for warehouseId: {}", request.getWarehouseId());
    }

    @Override
    public void getByItem(SkuByItemRequest request, StreamObserver<SkuListResponse> responseObserver) {
        List<com.example.sku.Sku> allSkus = skuRepository.findByItemId(request.getItemId());
        List<Sku> protoSkus = allSkus.stream()
                .map(this::mapEntityToProto)
                .collect(Collectors.toList());

        SkuListResponse response = SkuListResponse.newBuilder()
                .addAllSkus(protoSkus)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        logger.info("GetByItem completed for itemId: {}", request.getItemId());
    }

    private com.example.sku.Sku mapProtoToEntity(Sku proto) {
        com.example.sku.Sku entity = new com.example.sku.Sku();
        if (proto.hasSkuId()) {
            entity.setId(proto.getSkuId());
        }
        if (proto.hasWarehouseId()) {
            entity.setWarehouseId(proto.getWarehouseId());
        }
        entity.setItemId(proto.getItemId());
        entity.setAmount(proto.getAmount());
        entity.setCountryCode(proto.getCountryCode());
        
        switch (proto.getAvailabilityType()) {
            case READY_TO_SHIP:
                entity.setAvailabilityType(AvailabilityType.READY_TO_SHIP);
                break;
            case MADE_TO_ORDER:
                entity.setAvailabilityType(AvailabilityType.MADE_TO_ORDER);
                break;
            case OPEN_BOX:
                entity.setAvailabilityType(AvailabilityType.OPEN_BOX);
                break;
            case USED:
                entity.setAvailabilityType(AvailabilityType.USED);
                break;
            case REFURBISHED:
                entity.setAvailabilityType(AvailabilityType.REFURBISHED);
                break;
            default:
                entity.setAvailabilityType(AvailabilityType.READY_TO_SHIP);
        }

        if (proto.hasBasePrice()) {
            Money money = proto.getBasePrice();
            long units = money.getUnits();
            int nanos = money.getNanos();
            BigDecimal amount = BigDecimal.valueOf(units).add(BigDecimal.valueOf(nanos, 9));
            entity.setPriceAmount(amount);
            entity.setCurrencyCode(money.getCurrencyCode());
        }

        if (proto.hasLastUpdated()) {
            Timestamp ts = proto.getLastUpdated();
            entity.setLastUpdated(Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos())
                    .atZone(ZoneId.systemDefault()).toOffsetDateTime());
        }

        return entity;
    }

    private Sku mapEntityToProto(com.example.sku.Sku entity) {
        Sku.Builder builder = Sku.newBuilder();
        
        if (entity.getId() != null) {
            builder.setSkuId(entity.getId());
        }
        if (entity.getWarehouseId() != null) {
            builder.setWarehouseId(entity.getWarehouseId());
        }
        if (entity.getItemId() != null) {
            builder.setItemId(entity.getItemId());
        }
        if (entity.getAmount() != null) {
            builder.setAmount(entity.getAmount());
        }
        if (entity.getCountryCode() != null) {
            builder.setCountryCode(entity.getCountryCode());
        }

        if (entity.getAvailabilityType() != null) {
            switch (entity.getAvailabilityType()) {
                case READY_TO_SHIP:
                    builder.setAvailabilityType(Sku.AvailabilityType.READY_TO_SHIP);
                    break;
                case MADE_TO_ORDER:
                    builder.setAvailabilityType(Sku.AvailabilityType.MADE_TO_ORDER);
                    break;
                case OPEN_BOX:
                    builder.setAvailabilityType(Sku.AvailabilityType.OPEN_BOX);
                    break;
                case USED:
                    builder.setAvailabilityType(Sku.AvailabilityType.USED);
                    break;
                case REFURBISHED:
                    builder.setAvailabilityType(Sku.AvailabilityType.REFURBISHED);
                    break;
            }
        }

        if (entity.getPriceAmount() != null) {
            BigDecimal price = entity.getPriceAmount();
            long units = price.longValue();
            int nanos = price.remainder(BigDecimal.ONE).movePointRight(9).intValue();
            
            Money money = Money.newBuilder()
                    .setCurrencyCode(entity.getCurrencyCode() != null ? entity.getCurrencyCode() : "USD")
                    .setUnits(units)
                    .setNanos(nanos)
                    .build();
            builder.setBasePrice(money);
        }

        if (entity.getLastUpdated() != null) {
            Instant instant = entity.getLastUpdated().toInstant();
            Timestamp timestamp = Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();
            builder.setLastUpdated(timestamp);
        }

        return builder.build();
    }
}
