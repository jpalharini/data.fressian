#!/usr/bin/env bash

struct_sizes=(0 1 8 16 32 64 256 1024)

jvm_opts="-server -Xms8g -Xmx8g -XX:+UseZGC"

# each job completion runs in an isolated pod/container, JOB_COMPLETION_INDEX identifies the pod/container
# this will run a single test per pod, then destroy it
for size in "${struct_sizes[@]}"; do
  case $JOB_COMPLETION_INDEX in
    0)
      java $jvm_opts -jar bin/fressian-old.jar "vec" ${size} false
      ;;
    1)
      java $jvm_opts -jar bin/fressian-old.jar "map" ${size} false
      ;;
    2)
      java $jvm_opts -jar bin/fressian-convert.jar "vec" ${size} false
      ;;
    3)
      java $jvm_opts -jar bin/fressian-convert.jar "map" ${size} false
      ;;
    4)
      java $jvm_opts -jar bin/fressian-convert.jar "vec" ${size} true
      ;;
    5)
      java $jvm_opts -jar bin/fressian-convert.jar "map" ${size} true
      ;;
    6)
      java $jvm_opts -jar bin/fressian-reduce.jar "vec" ${size} false
      ;;
    7)
      java $jvm_opts -jar bin/fressian-reduce.jar "map" ${size} false
      ;;
    8)
      java $jvm_opts -jar bin/fressian-reduce.jar "vec" ${size} true
      ;;
    9)
      java $jvm_opts -jar bin/fressian-reduce.jar "map" ${size} true
      ;;
    10)
      java $jvm_opts -jar bin/fressian-reducemap.jar "vec" ${size} true
      ;;
    11)
      java $jvm_opts -jar bin/fressian-reducemap.jar "map" ${size} true
      ;;
  esac
done