# Alfred API

## Rules for pull requests
* Common sense trumps all rules.
* For every pull request please extend [CHANGELOG.md](./CHANGELOG.md).
* Do not make breaking changes since this is an API used by customers. Breaking changes include 
  adding, changing or removing endpoints or JSON objects used in requests and responses.
  * If you are forced to make a breaking change:
    * Notify maintainers
    * Add a note to the changelog with upgrade instructions
    * Notify all customers at the next release
* When working in REST code, please comply to **REST HTTP result codes** policy outlined in the
  [developer guide](./developer-documentation).
* Prefer unit tests over integration tests to keep builds fast
* Follow our 
  [coding styleguide and other active procedures](https://xenitsupport.jira.com/wiki/spaces/XEN/pages/624558081/XeniT+Enhancement+Proposals+XEP).
  * Avoid `this.` prefix for consistency (unless the scope is ambiguous).


## Project structure
* *apix-interface* builds the interface of Alfred API. This part is agnostic of the 
Alfresco version used.
* *apix-rest-v1* builds the REST API of Alfred API. 
* *apix-impl* builds the AMP which is the main deliverable for Alfred API. The AMP and JAR contains the JARs of 
*apix-interface* and *apix-rest-v1*.
  * The top directory also contains code shared over different Alfresco versions.
  * *apix-impl/xx* contains all code per Alfresco version. It has a *src/java* folder
  for code specific to that Alfresco version and a *src/java-shared code* for the code shared between
  versions. This code is automatically symlinked from the *apix-impl* directory.   
* *apix-integrationtests* contains the integration tests for each Alfresco version.


## How to

### Run
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


### Run integration tests
```bash
./gradlew :apix-integrationtests:test-${VERSION}:integrationTest --info
```  
Again, where `VERSION` is e.g. `51`.

### Run integration tests under debugger
1. Edit the `apix-docker/${VERSION}/docker-compose.yml` to include a portmapping `8000:8000`, and 
  add the environment variable `DEBUG=true`.
2. Prepare your remote debugger in IntelliJ and set breakpoints where you want in your tests
 (or Alfred API code).
3. Run the integration tests (see section above).
4. Wait until the container is started and healthy, then attach the debugger.

Again, where `VERSION` is e.g. `51`.


### Install
See the [developer guide](./developer-documentation) for installation instructions.

#### Deploy code changes for development
Additionally, if you want to install via JAR for a fast development workflow,
you can sue the following gradle command:

```bash
./gradlew :apix-impl:apix-impl-${VERSION}:installBundle -Pport=${PORT}
```
Where `VERSION` is e.g. `51` and here `PORT` is the port mapping of the *alfresco-core* container e.g. `32774`.

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

### Generate development guide 
The developer guide is a PDF intended for integrators of Alfred API at both customers
and Xenit.

To generate it execute:
```bash
cd developer-documentation
./generate-doc
```
The resulting PDF is written in the same directory.

## Common problems
* Look in the Alfresco log.
* When calling Alfred API webscript from inside an integration JUnit test, you must be aware of 
  transaction effects.
    * A webscript runs in a separate independent transaction when called over http from an integration 
      test. Therefore, to have the webscript see changes made in your test, before calling over HTTP, 
      you have to put in a new transaction. Similarly to see the changes from the webscript in your 
      JUnit test, create a new transaction after calling HTTP. 
    * Warning: Do not forget to set `requiresNew` on the transaction in Alfresco as the JUnit test by 
      default already runs inside a transaction.
