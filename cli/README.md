# Command line interface

## ensure-keys

`./bin/ensure-keys keys < list-of-keys`

Reads a list of newline-separated keys from `list-of-keys` and ensures they exist in `keys`.

`keys` will be created if it does not exist.

## write

`./bin/write keys output timestamp interval [[field-kind-1] [key-kind-1] field-name-1 field-source-1],  ...]`

`field-kind` is one of:

- `--int`, represents an integer _(default)_
- `--fixed1`, represents a number with 1 decimal point
- `--fixed2`, represents a number with 2 decimal points
- `--lossy`, represents an integer; series that have only small numbers with little variation may be lossily stored

`key-kind` is one of:

- `--key`, the key is a string with an entry in `keys.index` _(default)_
- `--id`, the key is an integer for an entry in `keys.index`

eg:

`./bin/write keys output 2017-01-01T00:00Z hour --lossy pageviews pageviews-file`

## read

`./bin/read [--write] keys input [key-kind] [[--key-id key-id-1], ...] [[--key-name needle-1], ...] [[--key-regex needle-1], ...] [[field-name-1], ...]`

By default, the keys file is opened in read-only mode. This makes it possible to use
on read-only media (such as a squashfs filesystem). If the keys file was not correctly
closed, you will need to pass the `--write` flag to open it in read-write mode, so that
the rollback journal can be applied.

`key-kind` is one of:

- `--key`, the key is printed as a string _(default)_
- `--id`, the key is printed as an integer

`--key-name` (`-n`), `--key-regex` (`-r`) and `--key-id` (`-i`) filter the
results to only keys that match those clauses.

If no field names are specified, all fields are printed in the order
they are present in the file.

## merge

`./bin/merge output input-1 ... input-N [--lossy[=fields]]`

The output file contains the union of fields in the inputs, for the minimal time range
that spans all the input files.

`--lossy` indicates which fields can be made lossy.

If a datapoint is present in multiple files, the last file on the command line wins.

The order of fields in the output file is based on the order of discovery in the input files.
