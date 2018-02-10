# 0.2.3

`manu-cli`:

- add `merge` command to merge multiple manu files into a single file
- add `--write` flag to `read` command, to fix broken WAL entries

# 0.2.2

`manu-format`:

- use WAL mode in SQLite for faster writes
- return IDs in bulk add operation to avoid need for subsequent fetch

`manu-cli`:

- fetch names in bulk for faster reads

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
