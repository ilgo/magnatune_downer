#!/bin/bash

WWW=$1
PORT=10808

cd $WWW
python3 -m http.server $PORT &

