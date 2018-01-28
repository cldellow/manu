# server-config

_This doesn't really belong in the manu repo, but it's going to start here._

## How to deploy a machine

This directory gets synced to the target server backing https://manu.cldellow.com/

On a dev machine:

```
./bin/server-config/configure publish [machine] [command]
```

## How load balancing works

* nginx listens on port 80 and redirects to SSL
* nginx listens on port 443, terminates SSL and forwards to Varnish
* Varnish listens on port 6081 and forwards to port 8080
* nginx listens on port 8080 and does something app specific
  * manu: service runs on port 6268
  * cldellow.com: proxy passes to cldellow.github.io. This is mainly laziness, I should move that blog to these servers.

