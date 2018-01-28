server {
  listen 80;

  server_name www.cldellow.com;

  access_log /var/log/nginx/www.cldellow.com.log combined_ms;
  error_log /var/log/nginx/error.www.cldellow.com.log notice;

  location /.well-known/acme-challenge {
    return 301 $scheme://certbot.cldellow.com$request_uri;
  }

  location / {
    return 301 https://cldellow.com$request_uri;
  }
}

server {
  listen 443 ssl;

  server_name www.cldellow.com;

  access_log /var/log/nginx/www.cldellow.com.log combined_ms;
  error_log /var/log/nginx/error.www.cldellow.com.log notice;

  location /.well-known/acme-challenge {
    return 301 $scheme://certbot.cldellow.com$request_uri;
  }

  location / {
    return 301 https://cldellow.com$request_uri;
  }
}
