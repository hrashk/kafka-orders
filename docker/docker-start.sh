#!/bin/bash
DOCKERHOST=$(hostname -I | xargs) docker compose -f docker-compose.yml up -d
