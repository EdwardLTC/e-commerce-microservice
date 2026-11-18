use std::env;
use std::fs;
use std::path::PathBuf;

fn main() {
    // Resolve the proto directory relative to the project root
    let manifest_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    let proto_dir = PathBuf::from(&manifest_dir)
        .parent()
        .expect("Failed to find project root")
        .join(".proto");

    let user_proto = proto_dir.join("User.proto");
    let product_proto = proto_dir.join("Product.proto");
    let order_proto = proto_dir.join("Order.proto");

    let proto_path = proto_dir.to_str().unwrap();

    // Create output directories
    fs::create_dir_all("src/proto/user").unwrap();
    fs::create_dir_all("src/proto/product").unwrap();
    fs::create_dir_all("src/proto/order").unwrap();

    // Generate User service stubs
    tonic_prost_build::configure()
        .build_client(true)
        .build_server(false)
        .out_dir("src/proto/user")
        .compile_protos(&[user_proto.to_str().unwrap()], &[proto_path])
        .expect("Failed to compile User proto");

    // Generate Product service stubs
    tonic_prost_build::configure()
        .build_client(true)
        .build_server(false)
        .out_dir("src/proto/product")
        .compile_protos(&[product_proto.to_str().unwrap()], &[proto_path])
        .expect("Failed to compile Product proto");

    // Generate Order service stubs
    tonic_prost_build::configure()
        .build_client(true)
        .build_server(false)
        .out_dir("src/proto/order")
        .compile_protos(&[order_proto.to_str().unwrap()], &[proto_path])
        .expect("Failed to compile Order proto");

    println!("cargo:rerun-if-changed=build.rs");
    println!("cargo:rerun-if-changed={}", user_proto.display());
    println!("cargo:rerun-if-changed={}", product_proto.display());
    println!("cargo:rerun-if-changed={}", order_proto.display());
}
