#!/usr/bin/env bash
# Sequentially starts multiple sonarqube versions and runs
# the dummy verification projects against each and verifies
# that the expected build breaker behavior is met.
# Usage: ./test_e2e [space-separated list of versions]
BASEDIR=$(dirname "$0")
RESULTDIR=$(readlink -f $BASEDIR/results)

function print_error_log_if_ci() {
    log_file=$1
    if [ "$CI" = "true" ]; then
        echo "$log_file:"
        cat $log_file
    fi
}

function start_sonar() {
    sonar_ver=$1
    echo "- Starting sonarqube $sonar_ver..."

    $BASEDIR/run_sonar_with_plugin.sh $sonar_ver '' > $RESULTDIR/${sonar_ver}_start_sonar.log 2>&1
    res=$?
    if [ $res != 0 ]; then
        echo "- Failed to start sonarqube $sonar_ver (exited with $res)";
        docker logs sonarqube > $RESULTDIR/${sonar_ver}_docker_err.log
        print_error_log_if_ci $RESULTDIR/${sonar_ver}_docker_err.log
        exit 1;
    fi
}

function stop_sonar() {
    docker stop sonarqube > /dev/null
}

function test_maven_project() {
    sonar_ver=$1
    project=$2
    expected_exit_code=$3

    echo "- Running $project against sonarqube $sonar_ver..."
    pushd $BASEDIR/$project > /dev/null
    mvn sonar:sonar > $RESULTDIR/${sonar_ver}_${project}.log 2>&1
    res=$?
    if [ $res != $expected_exit_code ]; then 
        echo "- Expected sonar analysis of $project to exit with $expected_exit_code (exited with $res)"
        print_error_log_if_ci $RESULTDIR/${sonar_ver}_${project}.log
        exit 1
    fi
    popd > /dev/null
}

function check_maven_project_output() {
    sonar_ver=$1
    project=$2
    expected_line=$3
    grep "$expected_line" $RESULTDIR/${sonar_ver}_${project}.log > /dev/null
    res=$?
    if [ $res != 0 ]; then 
        echo "- Expected '$expected_line' in $project output."
        print_error_log_if_ci $RESULTDIR/${sonar_ver}_${project}.log
        exit 1
    fi
}

function test_sonar_version() {
    sonar_ver=$1

    echo "Testing against sonarqube $sonar_ver..."
    start_sonar $sonar_ver

    test_maven_project $sonar_ver passing-project 0
    test_maven_project $sonar_ver failing-project 1
    check_maven_project_output $sonar_ver failing-project '\[BUILD BREAKER\] Project did not meet'

    echo "sonarqube $sonar_ver OK"
    stop_sonar
}

trap 'docker kill sonarqube 2> /dev/null' EXIT
rm -rf $RESULTDIR
mkdir -p $RESULTDIR
versions=$@
if [ -z "$versions" ]; then
    versions="8.1-community-beta 8.0-community-beta 7.9-community 7.8-community 7.7-community 7.6-community 7.5-community 7.4-community 7.3-community"
fi

for ver in $versions; do
    test_sonar_version $ver
    echo ""
done
