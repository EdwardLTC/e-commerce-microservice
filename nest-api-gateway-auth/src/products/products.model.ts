import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsNumber, IsOptional, IsPositive, IsString, IsUUID, Min } from 'class-validator';
import { randomUUID } from 'crypto';

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

  @ApiPropertyOptional({
    example: [randomUUID(), randomUUID()],
    description: 'List of category IDs to filter products by, uuid format',
  })
  @IsOptional()
  @IsUUID('all', { each: true })
  categoryIds?: string[];
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

  @ApiPropertyOptional({
    example: [randomUUID(), randomUUID()],
    description: 'List of category IDs to associate with the product, uuid format',
  })
  @IsUUID('all', { each: true })
  categoryIds?: string[];
}

export class GetProductsResponse {}
