# erp-frontend — built ON THE SERVER (x86_64) by deploy.py.
# Build context: ~/erp-deploy/frontend (the mxdashboard sources, no node_modules/dist/.env*).
# VITE_API_BASE_URL stays empty: the app calls relative /api/..., proxied by nginx.conf.
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
