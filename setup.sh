#!/bin/bash
# Setup shared symlinks

cd "${0%/*}"

rm -f "apix-impl/52/src/main/java-shared"
rm -f "apix-impl/51/src/main/java-shared"
rm -f "apix-impl/50/src/main/java-shared"
rm -f "apix-impl/42/src/main/java-shared"

ln -s "../../../src/main/java/" "apix-impl/52/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/51/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/50/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/42/src/main/java-shared"

rm -f "apix-impl/52/src/test/java-shared"
rm -f "apix-impl/51/src/test/java-shared"
rm -f "apix-impl/50/src/test/java-shared"
rm -f "apix-impl/42/src/test/java-shared"

mkdir -p "apix-impl/52/src/test"
mkdir -p "apix-impl/51/src/test"
mkdir -p "apix-impl/50/src/test"
mkdir -p "apix-impl/42/src/test"

ln -s "../../../src/test/java/" "apix-impl/52/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/51/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/50/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/42/src/test/java-shared"

rm -f "apix-integrationtests/52/src/integration-test/java-shared"
rm -f "apix-integrationtests/51/src/integration-test/java-shared"
rm -f "apix-integrationtests/50/src/integration-test/java-shared"
rm -f "apix-integrationtests/42/src/integration-test/java-shared"

mkdir -p "apix-integrationtests/42/src/integration-test"
mkdir -p "apix-integrationtests/50/src/integration-test"
mkdir -p "apix-integrationtests/51/src/integration-test"
mkdir -p "apix-integrationtests/52/src/integration-test"

ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/52/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/51/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/50/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/42/src/integration-test/java-shared"

echo "Setup done"