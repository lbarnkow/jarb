#!/bin/bash

DIR=$(dirname $0)
cd $DIR/..

DECISIONS_FILE="config/license_finder/dependency_decisions.yml"
RC=0

set -x

./gradlew compileJava compileTestJava
RC=$(($RC + $?))

./gradlew license
RC=$(($RC + $?))

license_finder action_items \
    --decisions_file ${DECISIONS_FILE}
RC=$(($RC + $?))

license_finder report \
    --decisions_file ${DECISIONS_FILE} \
    --format=html \
    > build/reports/license/license_finder.html
RC=$(($RC + $?))

./gradlew checkstyleMain
RC=$(($RC + $?))

./gradlew pmdMain
RC=$(($RC + $?))

./gradlew test
RC=$(($RC + $?))

./gradlew dependencyCheckAnalyze
RC=$(($RC + $?))

./gradlew dependencyUpdates
RC=$(($RC + $?))

./gradlew jacocoTestReport
RC=$(($RC + $?))

./gradlew jacocoTestCoverageVerification
RC=$(($RC + $?))

$DIR/publish-results.sh
RC=$(($RC + $?))

exit $RC
