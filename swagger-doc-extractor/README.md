# Swagger Documentation Extractor
Tiny Java application that extracts the Swagger specification YAML for
Alfred API basis on its source code.

It is only used by https://bitbucket.org/xenit/alfred-docs to build the
Swagger documentation served at https://docs.xenit.eu/alfred-api when a
version of Alfred API is released.


### Background
In Alfred API —contrary to the usual workflow— the Swagger spec is generated
from the source code. The spec is then served by a running Alfred API (by
using the code in `de-swagger-reader`). For the building of the documentation
however we don't want the overhead of starting up an entire Alfred API stack.

## Usage
```bash
./gradle --quiet :swagger-doc-extractor:run
```