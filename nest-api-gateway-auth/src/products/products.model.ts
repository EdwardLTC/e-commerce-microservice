import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { ArrayMinSize, IsArray, IsNotEmpty, IsNumber, IsOptional, IsPositive, IsString, IsUUID, Min } from 'class-validator';

export class GetProductsRequest {
  @ApiProperty({
    example: 0,
    description: 'Number of products to skip for pagination',
  })
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  skip: number;

  @ApiProperty({
    example: 10,
    description: 'Number of products to take for pagination',
  })
  @Type(() => Number)
  @IsNumber()
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
  @Type(() => Number)
  @IsNumber()
  @Min(1)
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
  @Type(() => Number)
  @IsNumber()
  @Min(1)
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
  @Type(() => Number)
  @IsNumber()
  @IsPositive()
  price: number;

  @ApiProperty({
    example: 50,
    description: 'Stock quantity of the product variant',
  })
  @Type(() => Number)
  @IsNumber()
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

export class GetVariantsByIdsRequest {
  @ApiProperty({
    type: [String],
    example: ['2b2d34d5-6c6f-4cb3-a1b4-4fca2ea8a0dd', '28e95fa8-6ed4-4898-a5b3-e93e0bd66472'],
  })
  @Type(() => String)
  @IsArray()
  @ArrayMinSize(1)
  @IsUUID('4', { each: true })
  ids: string[];
}

export class ResourceCreatedResponse {
  @ApiProperty({
    example: '2b2d34d5-6c6f-4cb3-a1b4-4fca2ea8a0dd',
  })
  id: string;
}

export class OptionValueResponse {
  @ApiProperty({
    example: '2b2d34d5-6c6f-4cb3-a1b4-4fca2ea8a0dd',
  })
  id: string;

  @ApiProperty({
    example: 'Red',
  })
  value: string;

  @ApiProperty({
    example: 'https://example.com/red.jpg',
  })
  mediaUrl: string;

  @ApiProperty({
    example: 1,
  })
  displayOrder: number;
}

export class OptionTypeResponse {
  @ApiProperty({
    example: '2b2d34d5-6c6f-4cb3-a1b4-4fca2ea8a0dd',
  })
  id: string;

  @ApiProperty({
    example: 'Color',
  })
  name: string;

  @ApiProperty({
    example: 1,
  })
  displayOrder: number;

  @ApiProperty({
    type: [OptionValueResponse],
  })
  optionValues: OptionValueResponse[];
}

export class SelectedOptionResponse {
  @ApiProperty({
    example: 'c5cdf6be-b5e1-4f16-aec5-d24e632ae2ff',
  })
  optionTypeId: string;

  @ApiProperty({
    example: '4aa5ad92-2fdc-4d1a-a7e5-b0db7db91502',
  })
  optionValueId: string;
}

export class VariantResponse {
  @ApiProperty({
    example: '78d28f3b-bf4f-4fe7-81d5-266ca6a4c234',
  })
  id: string;

  @ApiProperty({
    example: 'SKU-RED-L',
  })
  sku: string;

  @ApiProperty({
    example: 99.99,
  })
  price: number;

  @ApiProperty({
    example: 79.99,
  })
  salePrice: number;

  @ApiProperty({
    example: 10,
  })
  stock: number;

  @ApiProperty({
    example: 'ACTIVE',
  })
  status: string;

  @ApiProperty({
    example: 'https://example.com/variant-image.jpg',
  })
  mediaUrl: string;

  @ApiProperty({
    type: [SelectedOptionResponse],
  })
  selectedOptions: SelectedOptionResponse[];
}

export class ProductListItemResponse {
  @ApiProperty({
    example: '2b2d34d5-6c6f-4cb3-a1b4-4fca2ea8a0dd',
  })
  id: string;

  @ApiProperty({
    example: 'High Quality Product',
  })
  name: string;

  @ApiProperty({
    example: 'A high-quality product that meets all your needs.',
  })
  description: string;

  @ApiProperty({
    example: 'BrandName',
  })
  brand: string;

  @ApiProperty({
    example: 29.99,
  })
  minPrice: number;

  @ApiProperty({
    example: 99.99,
  })
  maxPrice: number;

  @ApiProperty({
    example: 150,
  })
  totalSaleCount: number;

  @ApiProperty({
    example: 4.7,
  })
  rating: number;

  @ApiProperty({
    type: [String],
  })
  mediaUrls: string[];
}

export class ProductDetailResponse {
  @ApiProperty({
    example: '2b2d34d5-6c6f-4cb3-a1b4-4fca2ea8a0dd',
  })
  id: string;

  @ApiProperty({
    example: 'High Quality Product',
  })
  name: string;

  @ApiProperty({
    example: 'A high-quality product that meets all your needs.',
  })
  description: string;

  @ApiProperty({
    example: 'BrandName',
  })
  brand: string;

  @ApiProperty({
    example: 150,
  })
  totalSaleCount: number;

  @ApiProperty({
    example: 4.7,
  })
  averageRating: number;

  @ApiProperty({
    type: [String],
  })
  mediaUrls: string[];

  @ApiProperty({
    type: [OptionTypeResponse],
  })
  optionTypes: OptionTypeResponse[];

  @ApiProperty({
    type: [VariantResponse],
  })
  variants: VariantResponse[];
}

export class VariantSummaryResponse {
  @ApiProperty({
    example: '78d28f3b-bf4f-4fe7-81d5-266ca6a4c234',
  })
  id: string;

  @ApiProperty({
    example: 'SKU-RED-L',
  })
  sku: string;

  @ApiProperty({
    example: 99.99,
  })
  price: number;

  @ApiProperty({
    example: 79.99,
  })
  salePrice: number;

  @ApiProperty({
    example: 10,
  })
  stock: number;

  @ApiProperty({
    example: 'https://example.com/variant-image.jpg',
  })
  mediaUrl: string;

  @ApiProperty({
    example: '2b2d34d5-6c6f-4cb3-a1b4-4fca2ea8a0dd',
  })
  productId: string;

  @ApiProperty({
    example: 'High Quality Product',
  })
  productName: string;
}

export class VariantListResponse {
  @ApiProperty({
    type: [VariantSummaryResponse],
  })
  variants: VariantSummaryResponse[];
}
