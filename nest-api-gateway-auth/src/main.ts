import { NestFactory } from '@nestjs/core';
import { Logger } from '@nestjs/common';
import { AppModule } from './app/app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  await app.listen(3000);

  return app;
}

bootstrap().then(async app => {
  Logger.debug(`API Gateway is running on: ${await app.getUrl()} - ${process.env.NODE_ENV}`, 'Bootstrap');
});
