#!/bin/sh
set -e

cd /var/dead-drop-app

dumb-init bin/dead-drop &
APP_PID=$!

cleanup() {
    kill "$APP_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

cd /e2e
npm test
