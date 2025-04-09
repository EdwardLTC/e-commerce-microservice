FROM node:20-alpine AS build
WORKDIR /app

COPY nest-api-gateway-auth/package*.json ./
RUN npm install

COPY nest-api-gateway-auth ./
RUN npm run build

FROM node:20-alpine
WORKDIR /app

COPY --from=build /app/dist ./dist
COPY --from=build /app/node_modules ./node_modules
COPY nest-api-gateway-auth/.env .

EXPOSE 3000

CMD ["node", "dist/main.js"]
