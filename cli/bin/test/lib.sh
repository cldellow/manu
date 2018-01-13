#!/bin/bash

function find_tests {
  declare -F | sed -e 's/.* //' | grep ^test_
}
