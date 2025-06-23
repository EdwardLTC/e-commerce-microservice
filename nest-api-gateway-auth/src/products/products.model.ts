import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber, IsOptional, IsPositive, IsString, Min } from 'class-validator';

export class GetProductsRequest {
  @ApiProperty({
    example: 0,
    description: 'Number of products to skip for pagination',
  })
  @IsPositive()
  @Min(0)
  skip: number;

  @ApiProperty({
    example: 10,
    description: 'Number of products to take for pagination',
  })
  @IsPositive()
  @Min(1)
  take: number;
}

export class CreateProductRequest {
  @ApiProperty({
    example: 'High Quality Product',
    description: 'Name of the product',
  })
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiProperty({
    example: 100.0,
    description: 'Price of the product in USD',
  })
  @IsNumber()
  @IsPositive()
  price: number;

  @ApiProperty({
    example: 'A high-quality product that meets all your needs.',
    description: 'Description of the product',
  })
  @IsString()
  @IsNotEmpty()
  description: string;

  @ApiProperty({
    example: 'BrandName',
    description: 'Brand of the product',
  })
  @IsString()
  @IsNotEmpty()
  brand: string;

  @ApiProperty({
    example: 100,
    description: 'Stock quantity of the product',
  })
  @IsPositive()
  @Min(1)
  stock: number;

  @ApiPropertyOptional({
    example: ['https://example.com/image1.jpg', 'https://example.com/image2.jpg'],
    description: 'List of media URLs for the product',
  })
  @IsOptional()
  @IsString({ each: true })
  mediaUrls?: string[];
}

export class CreateProductOptionTypeRequest {
  @ApiProperty({
    example: 'Color',
    description: 'Name of the option type',
  })
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiPropertyOptional({
    example: 1,
    description: 'Display order of the option type',
  })
  @IsOptional()
  @IsPositive()
  displayOrder?: number;
}

export class CreateProductOptionValueRequest {
  @ApiProperty({
    example: 'Red',
    description: 'Value of the option type',
  })
  @IsString()
  @IsNotEmpty()
  value: string;

  @ApiPropertyOptional({
    example: 'https://example.com/red.jpg',
    description: 'Media URL for the option value',
  })
  @IsOptional()
  @IsString()
  mediaUrl?: string;

  @ApiPropertyOptional({
    example: 1,
    description: 'Display order of the option value',
  })
  @IsOptional()
  @IsPositive()
  displayOrder?: number;
}

export class CreateProductVariantRequest {
  @ApiProperty({
    example: 'SKU12345',
    description: 'Stock Keeping Unit (SKU) of the product variant',
  })
  @IsString()
  @IsNotEmpty()
  sku: string;

  @ApiProperty({
    example: 9999.99,
    description: 'Price of the product variant in USD',
  })
  @IsNumber()
  @IsPositive()
  price: number;

  @ApiProperty({
    example: 50,
    description: 'Stock quantity of the product variant',
  })
  @IsPositive()
  stock: number;

  @ApiPropertyOptional({
    example: 'https://example.com/variant-image.jpg',
    description: 'Media URL for the product variant',
  })
  @IsOptional()
  @IsString()
  mediaUrl?: string;

  @ApiProperty({
    type: [String],
    description: 'List of option values associated with the product variant',
  })
  @IsString({ each: true })
  @IsNotEmpty({ each: true })
  options: string[];
}
