#!/usr/bin/env bash

jvm_opts="-server -Xms8g -Xmx8g -XX:+UseZGC"

struct_sizes=(0 1 8 16 32 64 256 1024)

java $jvm_opts -jar bin/fressian-mapreadlist.jar "map" ${struct_sizes[$JOB_COMPLETION_INDEX]} false