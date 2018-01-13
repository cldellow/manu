#!/bin/bash

declare test_exit_code test_run_cmd test_result

function find_tests {
  declare -F | sed -e 's/.* //' | grep ^test_
}

function reset_test {
  test_exit_code=0
  test_result=0
  test_run_cmd=""
}

function log {
  echo "$@" >> "${tmpdir:?}"/logfile
}

function run {
  set +e
  #shellcheck disable=SC2034
  test_run_cmd="$*"
  log "  $ $test_run_cmd"
  "$@" &> "${tmpdir:?}"/lastcmd
  test_exit_code="$?"
  sed -e 's/^/    /' "${tmpdir:?}/lastcmd" >> "${tmpdir:?}"/logfile
  #shellcheck disable=SC2034
  set -e
}

function assert {
  msg=${1:?must provide msg}
  expected=${2:?must provide msg}
  actual=${3:?must provide msg}
  if [ "$expected" == "$actual" ]; then
    log "  ok   $msg $expected"
  else
    test_result=1
    log "  fail $msg $expected (but was: $actual)"
  fi
}

function assert_exit_code {
  expected=${1:?must provide exit code}
  assert "exit_code" "$expected" "$test_exit_code"
}

function log_if_errors {
  if [ "$test_result" == "1" ]; then
    cat "${tmpdir:?}/logfile"
  fi
}
