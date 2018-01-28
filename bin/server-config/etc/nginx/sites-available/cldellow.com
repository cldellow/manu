server {
  listen 8080 default_server;

  server_name cldellow.com;

  access_log /var/log/nginx/$host.log combined_ms;
  error_log /var/log/nginx/error.$host.log notice;

  location / {
    proxy_pass http://cldellow.github.io;
    proxy_set_header Host            $host;
    proxy_set_header X-Forwarded-For $remote_addr;

    expires 10m;
    add_header Cache-Control "public";
  }
}

server {
  listen 80 default_server;

  server_name cldellow.com;

  access_log /var/log/nginx/$host.log combined_ms;
  error_log /var/log/nginx/error.$host.log notice;

  location /.well-known/acme-challenge {
    return 301 $scheme://certbot.cldellow.com$request_uri;
  }

  location / {
    return 301 https://$host$request_uri;
  }
}

server {
  listen 443 ssl;

  server_name cldellow.com;

  access_log /var/log/nginx/$host.log combined_ms;
  error_log /var/log/nginx/error.$host.log notice;

  location /.well-known/acme-challenge {
    return 301 $scheme://certbot.cldellow.com$request_uri;
  }

  location / {
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $remote_addr;
    proxy_pass http://localhost:6081;
  }
}
