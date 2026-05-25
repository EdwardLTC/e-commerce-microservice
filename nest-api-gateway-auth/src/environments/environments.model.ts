export type JWTConfig = {
  secret: string;
};

export type MicroserviceConfig = {
  userServiceURL: string;
  productServiceURL: string;
  orderServiceURL: string;
};

export type RedisConfig = {
  url: string;
};
