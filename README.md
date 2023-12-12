# Alfred API

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
![CI status](https://github.com/xenit-eu/alfred-api/actions/workflows/ci.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/eu.xenit.alfred.api/apix-interface.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22eu.xenit.alfred.api%22%20AND%20a%3A%22apix-interface%22)

Alfred API abstracts away past and future changes to the Alfresco, across major and minor versions, providing a stable
interface to Alfresco on which client-side applications can be built.

It also provides functional grouping of related operations from the Alfresco Public API, and additional endpoints that
are not supported by the Alfresco Public API.

> [![Xenit Logo](https://xenit.eu/wp-content/uploads/2017/09/XeniT_Website_Logo.png)](https://xenit.eu/open-source)
> 
> Alfred API is a part of the Xenit Open Source Tooling around Alfresco. Xenit is company with a deep expertise and
> strong team centered around Alfresco. If you'd like to learn more about our [tools](https://xenit.eu/open-source), 
> [services](https://xenit.eu/alfresco) and [products](https://xenit.eu/alfresco-products), please visit our 
> [website](https://xenit.eu).


## Usage
Full documentation can be found at the [project's documentation](https://docs.xenit.eu/alfred-api/stable-user/index.html).

## Contributing

### Rules for pull requests
* Common sense trumps all rules.
* For every pull request please extend the [CHANGELOG.md](./CHANGELOG.md).
* Do not make breaking changes since this is an API used by customers. Breaking changes include 
  adding, changing or removing endpoints or JSON objects used in requests and responses.
  * If you are forced to make a breaking change:
    * Notify maintainers
    * Add a note to the changelog with upgrade instructions
    * Notify all customers at the next release
* When working in REST code, please comply to **REST HTTP result codes** policy outlined in the
  [user guide](https://docs.xenit.eu/alfred-api/stable-user/rest-api/index.html#rest-http-result-codes).
* Prefer unit tests over integration tests to keep builds fast
* Avoid `this.` prefix for consistency (unless the scope is ambiguous).
* Follow our [coding styleguide and other active procedures](https://xenitsupport.jira.com/wiki/spaces/XEN/pages/624558081/XeniT+Enhancement+Proposals+XEP).
  
### Project structure
* *apix-interface* builds the interface of Alfred API. This part is agnostic of the 
Alfresco version used.
* *apix-rest-v1* builds the REST API of Alfred API. 
* *apix-impl* builds the Java code for each version of Alfresco.
* *alfresco* builds the AMP for each Alfresco version that is the main deliverable for Alfred API. The AMP contains
  the JARs of *apix-interface* and *apix-rest-v1*.
    * *alfresco/xx* contains the correct properties for each Alfresco version.
* *apix-integrationtests* contains the integration tests for each Alfresco version.

### How to

#### Run

The following command starts up all docker containers required for an Alfresco running Alfred API.
```bash
./gradlew :apix-docker:docker-${VERSION}:composeUp --info
```
Where `VERSION` is e.g. `70`.


#### Run integration tests
```bash
./gradlew :apix-integrationtests:alfresco:${VERSION}:integrationTest
```  
Again, where `VERSION` is e.g. `70`.

However, this starts (and afterwards stops) docker containers. This includes starting an Alfresco container,
 adding a startup time of several minutes. To circumvent this you also run the test on already running containers with
 for example:
 ```bash
./gradlew -x composeUp -x composeDown :apix-integrationtests:test-61:integrationTest -Pprotocol=http -Phost=localhost -Pport=8061
```

If you only want to run specific tests, you can specify this on the Gradle invocation with a pattern. For example:
 ```bash
./gradlew  :apix-integrationtests:alfresco:74:integrationTest -x composeDown --tests C*ServiceTest
 ```

#### Run integration tests under debugger
1. Debugging settings are already added by `apix-docker/${VERSION}/debug-extension.docker-compose.yml`, including a 
portmapping `8000:8000`. This file does not get loaded when running in CI.
2. Prepare your remote debugger in IntelliJ and set breakpoints where you want in your tests
 (or Alfred API code).
3. Run the integration tests (see section above).
4. Wait until the container is started and healthy, then attach the debugger.

Again, where `VERSION` is e.g. `70`.

*Protip:* If you get tired of changing the port after every `docker-compose up`, you can temporarily put a
fixed port in the *docker-compose.yml* of the version you are working with. (The rationale behind using 
variable ephemeral ports is that during parallel builds on Jenkins port clashes must be avoided.)

For example for version 7.0, change in *apix-docker/70/docker-compose.yml* 
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
      - ${DOCKER_IP}:9070:8080
```
and then restart the containers with:

```bash
./gradlew :apix-docker:docker-70:composeUp --info
```
