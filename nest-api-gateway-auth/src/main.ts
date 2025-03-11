import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { Logger } from '@nestjs/common';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  await app.listen(3000);

  return app;
}

bootstrap().then(async app => {
  Logger.debug(`API Gateway is running on: ${await app.getUrl()} - ${process.env.NODE_ENV}`, 'Bootstrap');
});
