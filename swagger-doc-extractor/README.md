# Swagger Documentation Extractor
Tiny Java application that extracts the Swagger specification for
Alfred API based on its source code.

It is only used by https://github.com/xenit-eu/alfred-docs to build the
Swagger documentation served at https://docs.xenit.eu/alfred-api when a
version of Alfred API is released.

**Writes output to stdout.**


### Background
In Alfred API —contrary to the usual workflow— the Swagger spec is generated
from the source code. The spec is then served by a running Alfred API (by
using the code in `de-swagger-reader`). For the building of the documentation
however we don't want the overhead of starting up an entire Alfred API stack.

## Usage
```bash
./gradlew --quiet :swagger-doc-extractor:run > ./swagger-doc-extractor/build/apix-swagger.json
```