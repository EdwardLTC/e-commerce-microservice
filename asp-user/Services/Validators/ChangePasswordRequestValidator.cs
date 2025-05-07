using asp_user.Attributes;
using Com.Ecommerce.Aspnet.User;
using FluentValidation;

namespace asp_user.Services.Validators;

[RegisterValidator]
public class ChangePasswordRequestValidator : AbstractValidator<ChangePasswordRequest>
{
    public ChangePasswordRequestValidator()
    {
        RuleFor(x => x.Id).NotEmpty().Must(id => Guid.TryParse(id, out _)).WithMessage("Id must be a valid UUID");
        RuleFor(x => x.OldPassword).MinimumLength(6);
        RuleFor(x => x.NewPassword).MinimumLength(6);
    }
}