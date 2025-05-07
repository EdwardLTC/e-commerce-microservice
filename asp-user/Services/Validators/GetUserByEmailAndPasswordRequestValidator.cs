using asp_user.Attributes;
using Com.Ecommerce.Aspnet.User;
using FluentValidation;

namespace asp_user.Services.Validators;

[RegisterValidator]
public class GetUserByEmailAndPasswordRequestValidator : AbstractValidator<GetUserByEmailAndPasswordRequest>
{
    public GetUserByEmailAndPasswordRequestValidator()
    {
        RuleFor(x => x.Email).EmailAddress();
        RuleFor(x => x.Password).MinimumLength(6);
    }
}