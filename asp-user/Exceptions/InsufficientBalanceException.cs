namespace asp_user.Exceptions;

public class InsufficientBalanceException(decimal currentBalance, decimal amountToReduce) : Exception(
	$"Insufficient balance. Current balance: {currentBalance}, Amount to reduce: {amountToReduce}"
);
