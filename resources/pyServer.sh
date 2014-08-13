#!/bin/bash

WWW=$1

chdir $WWW
python3 -m http.server 10808

