use futures::stream::TryStreamExt;
use mongodb::{
    bson::{doc, Document},
    options::{ClientOptions, FindOptions},
    Client, Database,
};
use std::error::Error;
use tonic::{transport::Server, Request, Response, Status};

// Estrutura de módulos simplificada
pub mod com {
    pub mod example {
        pub mod sku {
            tonic::include_proto!("com.example.sku");
        }
    }
}

// Simplifica o acesso aos tipos usando 'use'
use com::example::sku::sku_service_server::{SkuService, SkuServiceServer};
use com::example::sku::{
    Sku, SkuByItemRequest, SkuByWarehouseRequest, SkuGetByIdRequest, SkuListResponse, SkuRequest,
    SkuResponse,
};

// Define a estrutura do serviço
#[derive(Debug)]
pub struct MySkuService {
    db: Database,
}

// Funções auxiliares para conversão entre Protobuf e BSON
impl MySkuService {
    fn sku_to_doc(&self, sku: &Sku) -> Document {
        let mut doc = doc! {
            "item_id": sku.item_id as i64,
            "amount": sku.amount as i32,
            "country_code": &sku.country_code,
            "availability_type": sku.availability_type,
        };

        if let Some(sku_id) = sku.sku_id {
            doc.insert("sku_id", sku_id as i64);
        }
        if let Some(warehouse_id) = sku.warehouse_id {
            doc.insert("warehouse_id", warehouse_id as i64);
        }

        // Conversão simplificada de Money e Timestamp se necessário
        // Para simplificar, estamos salvando apenas campos básicos.
        // Em produção, você converteria Money e Timestamp para estruturas BSON adequadas.

        doc
    }

    fn doc_to_sku(&self, doc: Document) -> Sku {
        Sku {
            sku_id: doc.get_i64("sku_id").ok().map(|v| v as u64),
            warehouse_id: doc.get_i64("warehouse_id").ok().map(|v| v as u64),
            item_id: doc.get_i64("item_id").unwrap_or_default() as u64,
            amount: doc.get_i32("amount").unwrap_or_default() as u32,
            country_code: doc.get_str("country_code").unwrap_or_default().to_string(),
            availability_type: doc.get_i32("availability_type").unwrap_or_default(),
            base_price: None, // Simplificação
            last_updated: None, // Simplificação
        }
    }
}

// Implementa a trait gerada pelo tonic para o serviço
#[tonic::async_trait]
impl SkuService for MySkuService {
    async fn import_skus(
        &self,
        request: Request<SkuRequest>,
    ) -> Result<Response<SkuResponse>, Status> {
        let skus = request.into_inner().skus;
        let collection = self.db.collection::<Document>("skus");

        let mut docs = Vec::new();
        for sku in skus {
            docs.push(self.sku_to_doc(&sku));
        }

        if !docs.is_empty() {
            match collection.insert_many(docs).await {
                Ok(_) => {
                    let reply = SkuResponse {
                        success: true,
                        message: "Skus importados com sucesso".to_string(),
                    };
                    Ok(Response::new(reply))
                }
                Err(e) => {
                    eprintln!("Erro ao inserir no MongoDB: {}", e);
                    Err(Status::internal("Erro ao inserir dados no banco"))
                }
            }
        } else {
             Ok(Response::new(SkuResponse {
                success: true,
                message: "Nenhum Sku para importar".to_string(),
            }))
        }
    }

    async fn get_by_id(
        &self,
        request: Request<SkuGetByIdRequest>,
    ) -> Result<Response<Sku>, Status> {
        let sku_id = request.into_inner().sku_id;
        let collection = self.db.collection::<Document>("skus");

        let filter = doc! { "sku_id": sku_id as i64 };

        match collection.find_one(filter).await {
            Ok(Some(doc)) => Ok(Response::new(self.doc_to_sku(doc))),
            Ok(None) => Err(Status::not_found("Sku não encontrado")),
            Err(e) => {
                eprintln!("Erro ao buscar no MongoDB: {}", e);
                Err(Status::internal("Erro ao buscar dados"))
            }
        }
    }

    async fn get_by_warehouse(
        &self,
        request: Request<SkuByWarehouseRequest>,
    ) -> Result<Response<SkuListResponse>, Status> {
        let warehouse_id = request.into_inner().warehouse_id;
        let collection = self.db.collection::<Document>("skus");

        let filter = doc! { "warehouse_id": warehouse_id as i64 };
        let mut cursor = collection.find(filter).await.map_err(|e| Status::internal(e.to_string()))?;

        let mut skus = Vec::new();
        while let Some(doc) = cursor.try_next().await.map_err(|e| Status::internal(e.to_string()))? {
            skus.push(self.doc_to_sku(doc));
        }

        Ok(Response::new(SkuListResponse { skus }))
    }

    async fn get_by_item(
        &self,
        request: Request<SkuByItemRequest>,
    ) -> Result<Response<SkuListResponse>, Status> {
        let item_id = request.into_inner().item_id;
        let collection = self.db.collection::<Document>("skus");

        let filter = doc! { "item_id": item_id as i64 };
        let mut cursor = collection.find(filter).await.map_err(|e| Status::internal(e.to_string()))?;

        let mut skus = Vec::new();
        while let Some(doc) = cursor.try_next().await.map_err(|e| Status::internal(e.to_string()))? {
            skus.push(self.doc_to_sku(doc));
        }

        Ok(Response::new(SkuListResponse { skus }))
    }
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    // Configuração da conexão com o MongoDB
    let client_uri = "mongodb://grpc:grpc@127.0.0.1:27017/grpc";
    let client_options = ClientOptions::parse(client_uri).await?;
    let client = Client::with_options(client_options)?;

    println!("Conectado ao MongoDB com sucesso!");

    let db = client.database("grpc");

    // Configuração do servidor gRPC
    let addr = "0.0.0.0:50051".parse()?;
    let sku_service = MySkuService { db };

    println!("Servidor gRPC ouvindo em {}", addr);

    Server::builder()
        .add_service(SkuServiceServer::new(sku_service))
        .serve(addr)
        .await?;

    Ok(())
}
