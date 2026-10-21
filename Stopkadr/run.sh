#!/usr/bin/env bash
set -e
export EQDB_NAME=postgres
export EQDB_USER=postgres
export EQDB_PASS=equipment_tracker
export EQDB_HOST=localhost
cd "$(dirname "$0")"
./build/stopkadr