fn main() -> Result<(), Box<dyn std::error::Error>> {
    // Configura o protoc vendored para evitar dependência externa
    unsafe {
        std::env::set_var("PROTOC", protoc_bin_vendored::protoc_bin_path().unwrap());
    }

    tonic_build::configure()
        .compile(
            &["proto/sku.proto"],
            &["proto"],
        )?;
    Ok(())
}
