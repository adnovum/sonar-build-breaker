#!/usr/bin/env bash
# Starts Sonarqube as a docker container including the locally built
# sonar-build-breaker plugin.
# Usage: run_sonar_with_plugin.sh [sonarqube docker tag]
set -euo pipefail
BASEDIR=$(dirname "$0")
DEV_PLUGIN_JAR=$(readlink -f $BASEDIR/../target/sonar-build-breaker-plugin-*.jar)
TAG=${1:-lts}
RM=${2:---rm}
PLUGIN_JAR=$(readlink -f ${3:-$DEV_PLUGIN_JAR})

echo "Starting sonarqube:$TAG and plugin $PLUGIN_JAR"
docker run -d $RM --name sonarqube -p 9000:9000 \
    -v $PLUGIN_JAR:/opt/sonarqube/extensions/plugins/sonar-build-breaker-plugin.jar \
    sonarqube:$TAG

# Create a custom quality gate which breaks on any vulnerability issues, not just in new code.
# If we don't use this, our dummy projects won't break unless we analyze them, then add issues manually, then reanalyze them.
gateId=2
timeout 5m docker exec -i sonarqube bash <<-EOF
    apk --no-cache add curl

    while [[ "\$(curl -s -o /dev/null -w '%{http_code}' localhost:9000/api/qualitygates/list)" != "200" ]]; do
        echo "Waiting for Sonarqube..."
        sleep 3
    done

    sonarqube_version=\$(curl -s localhost:9000/api/server/version)
    echo "SonarQube version \$sonarqube_version"

    if [[ "\$sonarqube_version" =~ ^((8\.[4-9])|9) ]]; then
        # 8.4+:
        curl -s -X POST -u admin:admin "localhost:9000/api/qualitygates/copy?sourceName=Sonar%20way&name=MyQualityGate"
        curl -s -X POST -u admin:admin "localhost:9000/api/qualitygates/create_condition?gateName=MyQualityGate&metric=bugs&op=GT&error=0"
        curl -s -X POST -u admin:admin "localhost:9000/api/qualitygates/set_as_default?name=MyQualityGate"
    else
        # Pre 8.4:
        curl -s -X POST -u admin:admin "localhost:9000/api/qualitygates/copy?id=1&name=MyQualityGate"
        curl -s -X POST -u admin:admin "localhost:9000/api/qualitygates/create_condition?gateId=${gateId}&metric=bugs&op=GT&error=0"
        curl -s -X POST -u admin:admin "localhost:9000/api/qualitygates/set_as_default?id=${gateId}"
    fi
EOF
