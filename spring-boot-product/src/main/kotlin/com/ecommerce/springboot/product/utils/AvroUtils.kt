package com.ecommerce.springboot.product.utils

import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.avro.specific.SpecificRecord
import java.io.ByteArrayOutputStream

object AvroUtils {
    fun <T : SpecificRecord> serialize(record: T): ByteArray {
        val writer = SpecificDatumWriter<T>(record.schema)
        val out = ByteArrayOutputStream()
        val encoder = EncoderFactory.get().binaryEncoder(out, null)
        writer.write(record, encoder)
        encoder.flush()
        return out.toByteArray()
    }

    fun <T : SpecificRecord> deserialize(data: ByteArray, schema: org.apache.avro.Schema, clazz: Class<T>): T {
        val reader = SpecificDatumReader<T>(schema)
        val decoder = DecoderFactory.get().binaryDecoder(data, null)
        return reader.read(null, decoder)
    }
}
