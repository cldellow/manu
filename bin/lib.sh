#!/bin/bash

function run_java {
  class=${1:?must specify class}
  shift
  coverage_agent=""
  if [ "${coverage:-0}" == "1" ]; then
    coverage_agent="-javaagent:$HOME/.m2/repository/org/jacoco/org.jacoco.agent/0.8.0/org.jacoco.agent-0.8.0-runtime.jar=destfile=${here:?}/target/jacoco.exec"
  fi

  # Note that hprof can be misleading; only use this as a rough gauge.
  # See http://www.brendangregg.com/blog/2014-06-09/java-cpu-sampling-using-hprof.html
  profile_agent=""
  if [ "${profile:-0}" == "1" ]; then
    profile_agent="-agentlib:hprof=cpu=samples,depth=100,interval=10,lineno=y,thread=y,file=out.hprof"
  fi

  # shellcheck disable=SC2086
  java $profile_agent $coverage_agent \
    -cp "$(cat "$here"/cp.txt):$here/target/classes" "$class" "$@"
}

