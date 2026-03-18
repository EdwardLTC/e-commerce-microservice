using Avro.IO;
using Avro.Specific;

namespace asp_user.Kafka;

public static class AvroMessageSerializer
{
    public static byte[] Serialize<T>(T record) where T : ISpecificRecord
    {
        using var ms = new MemoryStream();
        var encoder = new BinaryEncoder(ms);
        var writer = new SpecificDatumWriter<T>(record.Schema);
        writer.Write(record, encoder);
        return ms.ToArray();
    }
}
