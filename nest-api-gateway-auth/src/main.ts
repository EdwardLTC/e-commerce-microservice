import { NestFactory } from '@nestjs/core';
import { Logger } from '@nestjs/common';
import { AppModule } from './app/app.module';
import { EnvironmentsService } from './environments/environments.service';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const envService = app.get<EnvironmentsService>(EnvironmentsService);

  if (envService.isDevelopment) {
    const config = new DocumentBuilder().setTitle('API Gateway').setDescription('Ecommerce API Gateway').addBearerAuth().build();
    const documentFactory = () => SwaggerModule.createDocument(app, config);
    SwaggerModule.setup('docs', app, documentFactory);
    Logger.debug(`Swagger is running on: http://localhost:${envService.port}/docs`, 'Bootstrap');
  }

  await app.listen(envService.port);

  return app;
}

bootstrap().then(async app => {
  Logger.debug(`API Gateway is running on: ${await app.getUrl()} - ${app.get(EnvironmentsService).environment}`, 'Bootstrap');
});
