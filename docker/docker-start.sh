#!/bin/bash
cd $(dirname $0)
DOCKERHOST=$(hostname -I | xargs) docker compose -f docker-compose.yml up -d
