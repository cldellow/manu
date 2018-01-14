# Command line interface

## ensure-keys

`./bin/ensure-keys keys.index < list-of-keys`

Reads a list of newline-separated keys from `list-of-keys` and ensures they exist in `keys.index`.

`keys.index` will be created if it does not exist.

## write

`./bin/write keys.index output-file timestamp interval [[field-kind-1] [key-kind-1] field-name-1 field-source-1],  ...]`

`field-kind` is one of:

- `--int`, represents an integer _(default)_
- `--fixed1`, represents a number with 1 decimal point
- `--fixed2`, represents a number with 2 decimal points
- `--lossy`, represents an integer; series that have only small numbers with little variation may be lossily stored

`key-kind` is one of:

- `--key`, the key is a string with an entry in `keys.index` _(default)_
- `--id`, the key is an integer for an entry in `keys.index`

eg:

`./bin/write keys.index output 2017-01-01T00:00Z hour --lossy pageviews pageviews-file`

## read

`./bin/read keys.indes input-file key-kind [[--key-name needle-1], ...] [[--key-regex needle-1], ...] [[field-name-1], ...]`

`key-kind` is one of:

- `--key`, the key is printed as a string _(default)_
- `--id`, the key is printed as an integer

`--key-name` (`-n`) and `--key-regex` (`-r`) filter the results to only keys that
are exactly that string or that match a regex with that string.

If no field names are specified, all fields are printed in the order
they are present in the file.
