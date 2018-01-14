# Manu: "Mostly archived, not updated"

[![Build Status](https://travis-ci.org/cldellow/manu.svg?branch=master)](https://travis-ci.org/cldellow/manu)
[![codecov](https://codecov.io/gh/cldellow/manu/branch/master/graph/badge.svg)](https://codecov.io/gh/cldellow/manu)
[![Maven Central](https://img.shields.io/maven-central/v/com.cldellow/manu.svg)](https://mvnrepository.com/artifact/com.cldellow/manu)

A time series storage format for integers and floats, using efficient delta encodings from [FastPFOR](https://github.com/lemire/JavaFastPFOR).

Examples: pageviews by article in Wikipedia, stock open/close/high/low prices, weather temperatures.

## Components
- [manu-format](format), a library for maintaining the data on disk
- [manu-cli](cli), a command-line tool for ingesting data into the format
- [manu-serve](serve), a web server to expose the data over REST

## Design criteria
### Priorities
- Cheap
  - I'm doing this to drive a hobby project; my dream would be to host a variety of datasets for $10/month.
  - A Fermi estimate suggests Wikipedia pageviews has 100B datapoints over the last 10 years. This implies that storage costs will dominate.
- Doesn’t need to be always-on
  - This sort of follows from cheap -- the ability to load subsets of data, or to run on spot instances will be a useful tool to cut costs.

### Non-priorities
- Concurrent / fast writes
  - These can happen offline.
- Fast reads
  - The pareto principle will likely apply to queries - 1% of keys will get 99% of reads. We can use Varnish or similar to cache at the application level.

### Assumptions
- Dense datasets
  - Keys: if we see a key once, we expect to see it again.
  - Values: if key X has a datapoint at T1, we expect most other keys will as well.
- Correlated values
  - Value for key X at T1 is likely related to value at T2.
- Some datasets can be lossy
  - Wikipedia pageviews, e.g., are likely insensitive to precision so long as the trend is generally correct.

## Obligatory

![Manu](https://www.smbc-comics.com/comics/1429540032-20150420.png)
