using asp_user.Attributes;
using Com.Ecommerce.Aspnet.User;
using FluentValidation;

namespace asp_user.Services.Validators;

[RegisterValidator]
public class GetUserByIdRequestValidator : AbstractValidator<GetUserByIdRequest>
{
    public GetUserByIdRequestValidator()
    {
        RuleFor(x => x.Id).NotEmpty().Must(id => Guid.TryParse(id, out _)).WithMessage("Id must be a valid UUID");
    }
}