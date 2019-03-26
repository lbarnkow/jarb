#!/bin/bash

export BRANCH=$(if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then echo $TRAVIS_BRANCH; else echo $TRAVIS_PULL_REQUEST_BRANCH; fi)

STATS_BRANCH="ci/stats-${BRANCH}"

if [[ "${BRANCH}" == "develop" && "${BRANCH}" == "master" ]]; then
    START_DIR=$(pwd)

    mkdir publish-stats
    cd publish-stats

    git clone https://github.com/lbarnkow/jarb.git
    cd jarb
    git checkout ${STATS_BRANCH}

    mv ${START_DIR}/stats-deps.json .
    mv ${START_DIR}/stats-vuln.json .
    date > last-update

    git add .
    git commit --amend --message "cu update"
    git push --force https://lbarnkow:${PUSH_TOKEN_FOR_GITHUB}@github.com/lbarnkow/jarb.git

    cd ${START_DIR}
    rm -rf publish-stats

    echo "Published CI stats to branch '${STATS_BRANCH}'."

else

    echo "Only publishing CI stats for branches 'develop' and 'master'; skipping."

fi
