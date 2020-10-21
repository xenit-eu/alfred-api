# Alfred API

## License

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)

## Continuous Integration Build

[![Jenkins Build Status](https://jenkins-2.xenit.eu/buildStatus/icon?job=Xenit+Github%2Falfred-api%2Fmaster&subject=Jenkins)](https://jenkins-2.xenit.eu/job/Xenit%20Github/job/alfred-api/job/master/)

## Publishing

[![Maven Central](https://img.shields.io/maven-central/v/eu.xenit.apix/apix-interface.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22eu.xenit.apix%22%20AND%20a%3A%22apix-interface%22)

## Documentation

### Full
[![Developer guide](https://img.shields.io/badge/Developer_guide-docs.xenit.eu-yellow)](https://docs.xenit.eu/alfred-api)

## Quickstart

### Rules for pull requests
* Common sense trumps all rules.
* For every pull request please extend the [CHANGELOG.md](https://github.com/xenit-eu/alfred-api/blob/master/CHANGELOG.md).
* Do not make breaking changes since this is an API used by customers. Breaking changes include 
  adding, changing or removing endpoints or JSON objects used in requests and responses.
  * If you are forced to make a breaking change:
    * Notify maintainers
    * Add a note to the changelog with upgrade instructions
    * Notify all customers at the next release
* When working in REST code, please comply to **REST HTTP result codes** policy outlined in the
  [user guide](https://docs.xenit.eu/alfred-api/stable-user).
* Prefer unit tests over integration tests to keep builds fast
  
### Project structure
* *apix-interface* builds the interface of Alfred API. This part is agnostic of the 
Alfresco version used.
* *apix-rest-v1* builds the REST API of Alfred API. 
* *apix-impl* builds the AMP which is the main deliverable for Alfred API. The AMP contains the JARs of 
*apix-interface* and *apix-rest-v1*.
  * The top directory also contains code shared over different Alfresco versions.
  * *apix-impl/xx* contains all code per Alfresco version. It has a *src/java* folder
  for code specific to that Alfresco version and a *src/java-shared code* for the code shared between
  versions. This code is automatically symlinked from the *apix-impl* directory.   
* *apix-integrationtests* contains the integration tests for each Alfresco version.

### How to

#### Run
If it is the first time you build Alfred API on your machine:
```bash
./setup.sh  # or ./setup.bat on Windows
```
Then:
```bash
./gradlew :apix-docker:docker-${VERSION}:composeUp --info
```
Where `VERSION` is e.g. `51`.
This starts up all docker containers required for an Alfresco running Alfred API.


#### Run integration tests
```bash
./gradlew :apix-integrationtests:test-${VERSION}:integrationTest
```  
Again, where `VERSION` is e.g. `51`.

However, this starts (and afterwards stops) docker containers. This includes starting an Alfresco container,
 adding a startup time of several minutes. To circumvent this you also run the test on already running containers with
 for example:
 ```bash
./gradlew -x composeUp -x composeDown :apix-integrationtests:test-61:integrationTest -Pprotocol=http -Phost=localhost -Pport=8061
```


#### Run integration tests under debugger
1. Debugging settings are already added by `apix-docker/${VERSION}/debug-extension.docker-compose.yml`, including a 
portmapping `8000:8000`. This file does not get loaded when running in Jenkins.
2. Prepare your remote debugger in IntelliJ and set breakpoints where you want in your tests
 (or Alfred API code).
3. Run the integration tests (see section above).
4. Wait until the container is started and healthy, then attach the debugger.

Again, where `VERSION` is e.g. `51`.

#### Deploy code changes for development
In a development scenario, it is possible to upload code changes to a running alfresco through dynamic extensions.
This requires the running alfresco to already have an older or equal version of alfred-api installed, and
the use of the jar artifact instead of the amp to do the new install. 
The JAR has the format `apix-impl-{ALFRESCO-VERSION}-{APIX-VERSION}.jar` and can be found under 
`apix-impl/{ALFRESCO-VERSION}/build/libs/`, where `ALFRESCO-VERSION` is one of *(50|51|52|60|61|62)*.
The new installation can be done either through the DE web interface, or with the following gradle task.
```bash
./gradlew :apix-impl:apix-impl-{ALFRESCO-VERSION}:installBundle -Phost={ALFRESCO-HOST} -Pport={ALFRESCO-PORT}
```

*Protip:* If you get tired of changing the port after every `docker-compose up`, you can temporarily put a
fixed port in the *docker-compose.yml* of the version you are working with. (The rationale behind using 
variable ephemeral ports is that during parallel builds on Jenkins port clashes must be avoided.)

For example for version 5.1, change in *apix-docker/51/docker-compose.yml* 
the ports line from:
```yaml
services:
  alfresco-core:
    ports:
      - ${DOCKER_IP}:8080
``` 
to: 
```yaml
services:
  alfresco-core:
    ports:
      - ${DOCKER_IP}:9051:8080
```
and then restart the containers with:

```bash
./gradlew :apix-docker:docker-51:composeUp --info
```


### REST HTTP result codes
REST responses can return the following HTTP status codes:


#### 2xx Success

Indicates request sent by client was understood and accepted.

**Code**                    | **Meaning**
----------------------      |-----------------------
200 OK                      | Generic success.
202 Accepted                | The request was successful and will be processed asynchronously.
207 Multi-Status            | A bulk request completed successfully. These responses should contain multi-status response that can be correlated to each individual request in the bulk request. Can be returned even if individual requests fail.


#### 3xx Redirection

Indicates the client must take additional steps to complete the request.

**Code**                    | **Meaning**
----------------------      | -----------------------
301 Moved Permanently       | This and all future requests should be directed to the given URI.


#### 4xx Client error

Indicates anticipated failures, such as requests for non-existant resources, 
requests with missing input and malformed requests.

A body *may* be provided in the response that clarifies the error.

**Code**                    | **Meaning**
----------------------      | -----------------------
400 Bad Request             | Generic client error.
401 Unauthorized            | User must log in.
403 Forbidden               | User not authorized to use this resource.
404 Not Found               | Requested resource not found. Returned also for e.g. requesting a node with an incorrect id, as well as unhandled URI's. For security reasons, a 404 can aso be returned when the requester has insufficient permissions.
405 Method Not Allowed      | A request method is not supported (e.g. PUT on an endpoint that only accepts GET).


#### 5xx Server error

Indicates unexpected failures.

**Code**                    | **Meaning**
----------------------      | -----------------------
500 Internal Server Error   | Generic server error.
503 Service Unavailable     | Temporary server error. Retry later is sensible.

## Installation

### Pre-requisites
Alfred API requires **_Dynamic Extensions For Alfresco_**, version 2.0.1 or later. This module should be installed first.
Acquisition and installation instructions can be found [here](https://github.com/xenit-eu/dynamic-extensions-for-alfresco).
