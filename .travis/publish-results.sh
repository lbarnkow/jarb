#!/bin/bash

export BRANCH=$(if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then echo $TRAVIS_BRANCH; else echo $TRAVIS_PULL_REQUEST_BRANCH; fi)

export REPO_NAME=$(basename -s .git $(git config --get remote.origin.url))
export STATS_BRANCH="${REPO_NAME}/stats-${BRANCH}"

if [[ "${BRANCH}" == "develop" || "${BRANCH}" == "master" ]]; then
    START_DIR=$(pwd)

    mkdir publish-stats
    cd publish-stats

    git clone https://github.com/lbarnkow/ci-output.git
    cd ci-output
    git checkout -b ${STATS_BRANCH}

    cp ${START_DIR}/build/stats/stats-deps.json .
    cp ${START_DIR}/build/stats/stats-vuln.json .
    cp ${START_DIR}/build/stats/stats-checkstyle.json .

    cp ${START_DIR}/build/dependencyUpdates/report.txt report-deps.md
    cp ${START_DIR}/build/reports/dependency-check-vulnerability.md report-vuln.md
    cp ${START_DIR}/build/reports/checkstyle/report.md report-checkstyle.md

    date > last-update

    git add .
    git commit --amend --message "ci update - $(date)"
    git push --force https://lbarnkow-ci:${PUSH_TOKEN_FOR_GITHUB}@github.com/lbarnkow/ci-output.git

    cd ${START_DIR}
    rm -rf publish-stats

    echo "Published CI stats to branch '${STATS_BRANCH}'."

else

    echo "Only publishing CI stats for branches 'develop' and 'master'; skipping."

fi
