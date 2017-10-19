#!/bin/bash

set -e

bundle install --path vendor/bundle

pushd app

if [ -f routing/localhost_11311.route ]; then
    echo routing table was already created
else
    bundle exec mkroute localhost_11311 localhost_11411 --replication_in_host
    mv *.route routing
fi

for port in 11311 11411; do
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

# test
echo ROMA connect test

MAX_RETRY=30

for ((i=0;i<${MAX_RETRY};++i))
do
    bundle exec roma-adm 'get test' 11311
    set +e
    bundle exec roma-adm 'get test' 11311 | grep ERROR
    RES=$?
    set -e
    if [ $RES -eq 1 ]; then
        echo ROMA runs properly
        break
    fi
    echo wait for starting ROMA
    sleep 1
done

if [ $i -eq ${MAX_RETRY} ]; then
    echo ROMA could not start yet
    exit 1
fi

# check each port
for port in 11311 11411; do
    lsof -i:${port}
    ss | grep ${port}
done

popd
