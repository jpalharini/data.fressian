#!/usr/bin/env bash

struct_sizes=(0 1 8 16 32 64 256 1024)

for size in "${struct_sizes[@]}"; do
  case $JOB_COMPLETION_INDEX in
    0|1)
      if (( JOB_COMPLETION_INDEX == 0)); then
        lein with-profile +bench,+fressian-old run -- "vec" ${size} false
      else
        lein with-profile +bench,+fressian-old run -- "map" ${size} false
      fi
      ;;
    2|3)
      if (( JOB_COMPLETION_INDEX == 2)); then
        lein with-profile +bench,+fressian-convert run -- "vec" ${size} false
      else
        lein with-profile +bench,+fressian-convert run -- "map" ${size} false
      fi
      ;;
    4|5)
      if (( JOB_COMPLETION_INDEX == 4)); then
        lein with-profile +bench,+fressian-convert run -- "vec" ${size} true
      else
        lein with-profile +bench,+fressian-convert run -- "map" ${size} true
      fi
      ;;
    6|7)
      if (( JOB_COMPLETION_INDEX == 6)); then
        lein with-profile +bench,+fressian-reduce run -- "vec" ${size} false
      else
        lein with-profile +bench,+fressian-reduce run -- "map" ${size} false
      fi
      ;;
    8|9)
      if (( JOB_COMPLETION_INDEX == 8)); then
        lein with-profile +bench,+fressian-reduce run -- "vec" ${size} true
      else
        lein with-profile +bench,+fressian-reduce run -- "map" ${size} true
      fi
      ;;
  esac
done