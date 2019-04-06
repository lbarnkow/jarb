#!/bin/bash

DIR=$(dirname $0)
cd $DIR/..

RC=0

set -x

gem install license_finder
RC=$(($RC + $?))

exit $RC
