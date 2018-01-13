#!/bin/bash

function run_java {
  class=${1:?must specify class}
  shift
  coverage_agent=""
  if [ "${coverage:-0}" == "1" ]; then
    coverage_agent="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.7.9/org.jacoco.agent-0.7.9-runtime.jar=destfile=${here:?}/../format/target/jacoco.exec"
  fi

  # shellcheck disable=SC2086
  java $coverage_agent \
    -cp "$(cat "$here"/cp.txt):$here/target/classes" "$class" "$@"
}

