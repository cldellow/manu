# 0.2.2

- `manu-cli`: faster read
- `manu-format`: add bulk add/get operations to the index

# 0.2.1

all: support "sparse" files, where not all keys have records
`manu-format`:

- support declaring a sentinel value that signals null
- support variable length size field
- ~4x better space use for fields with a single datapoint
- fix incorrect decoding of lossily encoded data
`manu-cli`: `read` is faster, support null fields
`manu-serve`: support null fields

# 0.2.0

`manu-serve` works.

`manu-serve` and `manu-cli` are published as [self-executable](https://skife.org/java/unix/2011/06/20/really_executable_jars.html) uberjars.

# 0.1.0

Initial release, `manu-format` and `manu-cli` work.
