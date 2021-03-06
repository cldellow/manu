server {
  listen 80 ;

  server_name certbot.cldellow.com;

  access_log /var/log/nginx/certbot.cldellow.com.log combined_ms;
  error_log /var/log/nginx/error.certbot.cldellow.com.log notice;

  root /var/www/certbot;
  index index.html;

  location / {
    try_files $uri $uri/ =404;
  }
}

server {
  listen 443 ssl;

  server_name certbot.cldellow.com;

  access_log /var/log/nginx/certbot.cldellow.com.log combined_ms;
  error_log /var/log/nginx/error.certbot.cldellow.com.log notice;

  root /var/www/certbot;
  index index.html;

  location / {
    try_files $uri $uri/ =404;
  }
}
