#!/bin/bash

declare test_exit_code test_run_cmd test_result
declare -a test_logs test_kinds

function find_tests {
  declare -F | sed -e 's/.* //' | grep ^test_
}

function reset_test {
  test_exit_code=0
  test_result=0
  test_run_cmd=""

  test_logs=()
  test_kinds=()
}

function push {
  kind=${1:?must provide kind}
  log=${2:?must provide log}
  test_logs+=("$log")
  test_kinds+=("$kind")
}


function run {
  set +e
  #shellcheck disable=SC2034
  test_run_cmd="$*"
  push "run" "$test_run_cmd"
  "$@" &> "${log_tmp:?}"
  #shellcheck disable=SC2034
  test_exit_code="$?"
  set -e
}

function assert {
  msg=${1:?must provide msg}
  expected=${2:?must provide msg}
  actual=${3:?must provide msg}
  if [ "$expected" == "$actual" ]; then
    push "ok  " "$msg $expected"
  else
    test_result=1
    push "fail" "$msg $expected (but was: $actual)"
  fi
}

function assert_exit_code {
  expected=${1:?must provide exit code}
  assert "exit_code" "$expected" "$test_exit_code"
}

function log_if_errors {
  if [ "$test_result" == "1" ]; then
    i=0
    n=${#test_logs[@]}
    while [ "$i" -lt "$n" ]; do
      kind=${test_kinds[$i]}
      log=${test_logs[$i]}

      if [ "$kind" == "run" ]; then
        echo "  $log"
      else
        echo "    $kind $log"
      fi
      i=$((i+1))
    done
  fi
}
