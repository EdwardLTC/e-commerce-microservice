using asp_user.Kafka;
using Microsoft.AspNetCore.Mvc;

namespace asp_user.Users;

[Route("users")]
[ApiController]
public class UsersController(KafkaProducerService kafkaProducerService) : Controller
{
    [HttpGet]
    public async Task<ActionResult> Test()
    {
        var ack = await kafkaProducerService.ProduceAsync("user.get", "1", new { id = 1 });
        Console.WriteLine(ack);
        return Ok(ack);
    }
}