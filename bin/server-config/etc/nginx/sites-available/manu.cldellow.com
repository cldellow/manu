server {
  listen 8080;

  server_name manu.cldellow.com;

  access_log /var/log/nginx/manu.cldellow.com.log combined_ms;
  error_log /var/log/nginx/error.manu.cldellow.com.log notice;

  location / {
    proxy_pass http://localhost:6268;

    expires 10m;
    add_header Cache-Control "public";
  }
}

server {
  listen 80;

  server_name manu.cldellow.com;

  access_log /var/log/nginx/manu.cldellow.com.log combined_ms;
  error_log /var/log/nginx/error.manu.cldellow.com.log notice;

  location /.well-known/acme-challenge {
    return 301 $scheme://certbot.cldellow.com$request_uri;
  }

  location / {
    return 301 https://$host$request_uri;
  }
}

server {
  listen 443 ssl;

  server_name manu.cldellow.com;

  access_log /var/log/nginx/manu.cldellow.com.log combined_ms;
  error_log /var/log/nginx/error.manu.cldellow.com.log notice;

  location /.well-known/acme-challenge {
    return 301 $scheme://certbot.cldellow.com$request_uri;
  }

  location / {
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $remote_addr;
    proxy_pass http://localhost:6081;
  }
}
