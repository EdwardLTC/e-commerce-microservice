using asp_user.Attributes;
using Com.Ecommerce.Aspnet.User;
using FluentValidation;

namespace asp_user.Services.Validators;

[RegisterValidator]
public class CreateUserRequestValidator : AbstractValidator<CreateUserRequest>
{
    public CreateUserRequestValidator()
    {
        RuleFor(x => x.Name).NotEmpty();
        RuleFor(x => x.Email).NotEmpty().EmailAddress();
        RuleFor(x => x.Password).NotEmpty().MinimumLength(6);
    }
}