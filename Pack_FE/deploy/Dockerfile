FROM node:18-alpine AS svelte_builder
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN npm run build

FROM nginx:1.25-alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY ./deploy/nginx_config/nginx.conf /etc/nginx/conf.d/app.conf

COPY --from=svelte_builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]