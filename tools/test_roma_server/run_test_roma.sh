#!/bin/bash

set -e

bundle install --path vendor/bundle

pushd app

if [ -f routing/localhost_11211.route ]; then
    echo routing table was already created
else
    bundle exec mkroute localhost_11211 localhost_11311 --replication_in_host
    mv *.route routing
fi

for port in 11211 11311; do
    set +e
    ps aux | grep romad | grep localhost | grep $port > /dev/null 2>&1
    result=$?
    set -e
    if [ $result -eq 0 ]; then
        echo ROMA localhost_${port} is running
    else
        bundle exec romad localhost -p $port -d --replication_in_host -c config.rb
    fi
done

popd
