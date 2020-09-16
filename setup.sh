#!/bin/bash
# Setup shared symlinks

cd "${0%/*}"

rm -f "apix-impl/62/src/main/java-shared"
rm -f "apix-impl/61/src/main/java-shared"
rm -f "apix-impl/60/src/main/java-shared"
rm -f "apix-impl/52/src/main/java-shared"
rm -f "apix-impl/51/src/main/java-shared"
rm -f "apix-impl/50/src/main/java-shared"

ln -s "../../../src/main/java/" "apix-impl/62/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/61/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/60/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/52/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/51/src/main/java-shared"
ln -s "../../../src/main/java/" "apix-impl/50/src/main/java-shared"

rm -f "apix-impl/62/src/test/java-shared"
rm -f "apix-impl/61/src/test/java-shared"
rm -f "apix-impl/60/src/test/java-shared"
rm -f "apix-impl/52/src/test/java-shared"
rm -f "apix-impl/51/src/test/java-shared"
rm -f "apix-impl/50/src/test/java-shared"

mkdir -p "apix-impl/62/src/test"
mkdir -p "apix-impl/61/src/test"
mkdir -p "apix-impl/60/src/test"
mkdir -p "apix-impl/52/src/test"
mkdir -p "apix-impl/51/src/test"
mkdir -p "apix-impl/50/src/test"

ln -s "../../../src/test/java/" "apix-impl/62/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/61/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/60/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/52/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/51/src/test/java-shared"
ln -s "../../../src/test/java/" "apix-impl/50/src/test/java-shared"

rm -f "apix-integrationtests/62/src/integration-test/java-shared"
rm -f "apix-integrationtests/61/src/integration-test/java-shared"
rm -f "apix-integrationtests/60/src/integration-test/java-shared"
rm -f "apix-integrationtests/52/src/integration-test/java-shared"
rm -f "apix-integrationtests/51/src/integration-test/java-shared"
rm -f "apix-integrationtests/50/src/integration-test/java-shared"

mkdir -p "apix-integrationtests/62/src/integration-test"
mkdir -p "apix-integrationtests/61/src/integration-test"
mkdir -p "apix-integrationtests/60/src/integration-test"
mkdir -p "apix-integrationtests/52/src/integration-test"
mkdir -p "apix-integrationtests/51/src/integration-test"
mkdir -p "apix-integrationtests/50/src/integration-test"

ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/62/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/61/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/60/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/52/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/51/src/integration-test/java-shared"
ln -s "../../../../apix-impl/src/integration-test/java" "apix-integrationtests/50/src/integration-test/java-shared"

echo "Setup done"