#!/usr/bin/env bash

struct_sizes=(0 1 8 16 32 64 256 1024)

jvm_opts="-server -Xms8g -Xmx8g -XX:+UseZGC"

for size in "${struct_sizes[@]}"; do
  case $JOB_COMPLETION_INDEX in
    0|1)
      if (( JOB_COMPLETION_INDEX == 0)); then
        java $jvm_opts -jar bin/fressian-old.jar "vec" ${size} false
      else
        java $jvm_opts -jar bin/fressian-old.jar "map" ${size} false
      fi
      ;;
    2|3)
      if (( JOB_COMPLETION_INDEX == 2)); then
        java $jvm_opts -jar bin/fressian-convert.jar "vec" ${size} false
      else
        java $jvm_opts -jar bin/fressian-convert.jar "map" ${size} false
      fi
      ;;
    4|5)
      if (( JOB_COMPLETION_INDEX == 4)); then
        java $jvm_opts -jar bin/fressian-convert.jar "vec" ${size} true
      else
        java $jvm_opts -jar bin/fressian-convert.jar "map" ${size} true
      fi
      ;;
    6|7)
      if (( JOB_COMPLETION_INDEX == 6)); then
        java $jvm_opts -jar bin/fressian-reduce.jar "vec" ${size} false
      else
        java $jvm_opts -jar bin/fressian-reduce.jar "map" ${size} false
      fi
      ;;
    8|9)
      if (( JOB_COMPLETION_INDEX == 8)); then
        java $jvm_opts -jar bin/fressian-reduce.jar "vec" ${size} true
      else
        java $jvm_opts -jar bin/fressian-reduce.jar "map" ${size} true
      fi
      ;;
    10|11)
      if (( JOB_COMPLETION_INDEX == 10)); then
        java $jvm_opts -jar bin/fressian-reducemap.jar "vec" ${size} true
      else
        java $jvm_opts -jar bin/fressian-reducemap.jar "map" ${size} true
      fi
      ;;
  esac
done