# Web server

## API

### GET `/api/collection-name/meta`

#### Request

Example usage:

```
curl -v https://manu.cldellow.com/api/wikipedia-hourly-pageviews/meta
```

#### Response

The response is a map:

```
{
  "name": "wikipedia-hourly-pageviews",
  "fields": ["pageviews"],
  "interval": "hour",
  "from": "2007-12-09T18:00Z",
  "to": "2018-01-14T00:00Z"
}
```

Interval is one of `hour`, `day`, `week`, `month`, `year`.

### POST `/api/collection-name`

#### Request

Parameters, sent as `application/x-www-form-urlencoded`

* `from` - ISO8601 datetime, assumed to be UTC. Include datapoints starting from this time
* `to` - ISO8601 datetime, assumed to be UTC Include datapoints up to, but not including, this time
* `key` - string, name of key to return
* `field` - string, name of field to return _(optional)_

`key` and `field` may be specified multiple times.

Example usage:

```
curl -v https://manu.cldellow.com/api/wikipedia-hourly-pageviews \
  -d from=2008-01-01 \
  -d to=2008-02-01 \
  -d key=Heath_Ledger \
  -d "key=Michelle_Williams_(actress)"
```

#### Response

The response is a map:

```
{
  "meta": {
    "interval": "hour",
    "from": "2008-01-01",
    "to": "2008-02-01",
  },
  "values": {
    "Heath_Ledger": {
      "pageviews": [123, 234, null, 4, ... ]
    },
    "Michelle_Williams_(actress)": {
      "pageviews": [...]
    }
  }
}
```

Interval is one of `hour`, `day`, `week`, `month`, `year`.

Nulls indicate an unknown datapoint.

## Admin

`./serve/bin/serve [--port 6268] [--datadir ./data]`

Note: the system inspects the files in datadir at startup. Once the service
is launched, you cannot add or remove files.

The datadir should consist of directories containing your data. The
name of the directory will be the name exposed in the API.

A collection's directory has:
- `keys` - an index file
- `*.manu` - manu files

All `.manu` files must have the same interval, the same fields, 
and they must not overlap in the time period they cover.

Queries will read from multiple manu files as needed to return
a unified dataset.
