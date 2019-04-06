#!/bin/bash

set -x

export BRANCH=$(if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then echo $TRAVIS_BRANCH; else echo $TRAVIS_PULL_REQUEST_BRANCH; fi)

export REPO_NAME=$(basename -s .git $(git config --get remote.origin.url))
export STATS_BRANCH="${REPO_NAME}/stats-${BRANCH}"

export DIR=$(realpath $(dirname ${0}))
cd ${DIR}


if [[ "${BRANCH}" == "develop" || "${BRANCH}" == "master" ]]; then
    mkdir publish-stats
    cd publish-stats

    git clone https://github.com/lbarnkow/ci-output.git
    cd ci-output
    git checkout -b ${STATS_BRANCH}

    git config user.name "lbarnkow-ci"
    git config user.email "48982208+lbarnkow-ci@users.noreply.github.com"

    cp ${DIR}/../build/stats/* .

    cp ${DIR}/../build/dependencyUpdates/report.txt report-deps.md
    cp ${DIR}/../build/reports/dependency-check-vulnerability.md report-vuln.md
    cp ${DIR}/../build/reports/checkstyle/report.md report-checkstyle.md
    cp ${DIR}/../build/reports/pmd/report.md report-pmd.md

    date > last-update

    git add .
    git commit --amend --reset-author --message "ci update - $(date)"
    git push --force https://lbarnkow-ci:${PUSH_TOKEN_FOR_GITHUB}@github.com/lbarnkow/ci-output.git

    cd ${DIR}
    rm -rf publish-stats

    echo "Published CI stats to branch '${STATS_BRANCH}'."

else
    echo "Only publishing CI stats for branches 'develop' and 'master'; skipping."

fi
