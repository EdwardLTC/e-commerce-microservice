using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace asp_user.Migrations
{
    /// <inheritdoc />
    public partial class AddWalletToUser : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<decimal>(
                name: "wallet",
                schema: "users",
                table: "users",
                type: "numeric",
                nullable: false,
                defaultValue: 0m);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "wallet",
                schema: "users",
                table: "users");
        }
    }
}
