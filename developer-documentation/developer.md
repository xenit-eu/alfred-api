---
title:
  - Alfred API
  - Developer guide
copyright-year: 2019

pandoc-args:
  template: manual

product-color: 3EB549
footer-img: images/AlfredAPI-User-Guide-Footer-Logo.jpg
frontpage-background-img: images/AlfredAPI-User-Guide-FrontCover.jpg
backcover-background-img: images/AlfredAPI-User-Guide-BackCover.jpg

numbersections: true
toc: true
listings-break-lines: true
---

# About
Alfred API abstracts away past and future changes to the Alfresco, across major and minor 
versions, providing a stable interface to Alfresco on which client-side applications can be built.
 
Currently Alfred supports the following Alfresco versions:

* 5.0
* 5.1
* 5.2
* 6.0
* 6.1

Alfred API is composed of two components. Artifacts for both components are published 
to the [Xenit artifactory](https://artifactory.xenit.eu).  

## Java API
The base layer is a Java API built on top of the Alfresco. 

## REST API
A REST API is built on top of the Java abstraction layer, exposing a stable HTTP API.


# Installation

## Pre-requisites
As Alfred API is built upon *Dynamic Extensions*, this module should be installed first.
Installation instructions for can be found 
[here](https://github.com/xenit-eu/dynamic-extensions-for-alfresco).

## Installation of Alfred API

### Development scenario
For development use only Alfred API can be deployed as JAR (which is actually an OSGi *Bundle*). 
File format of this JAR is `apix-impl-{ALFRESCO-VERSION}-{APIX-VERSION}.jar`, where `ALFRESCO-VERSION` 
is one of *(50|51|52|60)*. This JAR can be deployed through the Dynamic Extensions web interface.
 
When installing the JAR through DE, you have to install beforehand those modules:

* com.fasterxml.jackson.core:jackson-annotations:jar:2.6.3
* com.fasterxml.jackson.core:jackson-core:jar:2.6.3
* com.fasterxml.jackson.core:jackson-databind:jar:2.6.3
* com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:jar:2.6.3
* io.swagger:swagger-annotations:jar:1.5.7
* io.swagger:swagger-core:jar:1.5.7
* io.swagger:swagger-models:jar:1.5.7
* javax.validation:validation-api:jar:1.1.0.Final
* eu.xenit.testing:integration-testing:1.1.0


### Production scenario
Alfred API is also distributed as an Alfresco Module Package (AMP), which should be used for 
production installations.

File format of the AMP is `apix-impl-{ALFRESCO-VERSION}-{APIX-VERSION}.amp`, where `ALFRESCO-VERSION`
is one of *(50|51|52|60)*.
  
To install the AMP, follow the Alfresco AMP installation guidelines your version of Alfresco:
[5.0](https://docs.alfresco.com/5.0/tasks/amp-install.html), 
[5.1](https://docs.alfresco.com/5.1/tasks/amp-install.html),
[5.2](https://docs.alfresco.com/5.2/tasks/amp-install.html),
[6.0](https://docs.alfresco.com/6.0/tasks/amp-install.html) or
[6.1](https://docs.alfresco.com/6.1/tasks/amp-install.html).



# Concepts

## Data objects
Alfred API has data objects that mirror the Alfresco concepts of QName, NodeRef, StoreRef, Path, 
ContentData and ContentInputStream. These data objects are used to communicate with the 
Alfred Java API without being dependent on Alfresco data types.

Conversion between Alfresco and Alfred API data objects is the responsibility of the 
`ApixToAlfrescoConversion` service. It is also possible to construct an Alfred API data object by
passing its string representation to the constructor.

## Higher level functionality
Instead of mirroring the Alfresco API one-to-one, Alfred API groups frequently used operations
together in a single method call. This allows you to focus on business concerns instead of focusing
on fetching all required data from Alfresco.

For example, the `NodeService.getMetadata()` method returns an object with all metadata about 
a node: its type, aspects and properties in a single function call. It groups together the 
information that would otherwise have to be obtained by combining requests for type, aspects 
and properties separately.

## REST API
The Alfred REST API is a thin wrapper around the Java abstraction layer. It converts its received
parameters to the corresponding Alfred API data objects, then calls the corresponding service and
serializes its return value to JSON.

## REST HTTP result codes
REST responses can return the following HTTP status codes:


### 2xx Success

Indicates request sent by client was understood and accepted.

**Code**                    | **Meaning**
----------------------      |-----------------------
200 OK                      | Generic success.
202 Accepted                | The request was successful and will be processed asynchronously.
207 Multi-Status            | A bulk request completed successfully. These responses should contain multi-status response that can be correlated to each individual request in the bulk request. Can be returned even if individual requests fail.


### 3xx Redirection

Indicates the client must take additional steps to complete the request.

**Code**                    | **Meaning**
----------------------      | -----------------------
301 Moved Permanently       | This and all future requests should be directed to the given URI.


### 4xx Client error

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


### 5xx Server error

Indicates unexpected failures.

**Code**                    | **Meaning**
----------------------      | -----------------------
500 Internal Server Error   | Generic server error.
503 Service Unavailable     | Temporary server error. Retry later is sensible.


# Services
Only the most important services are described here. Full documentation is available in 
[the generated JavaDoc](#viewing-javadoc).

## NodeService
The `NodeService` provides operations on nodes.

* Fetch and modify the metadata for a Node
* Fetch the root node of a Store
* Fetch, create and remove child, parent and target associations for a Node
* Copy or move a Node to another parent
* Create and delete Nodes
* Checkout, checkin and fetch working copies for a Node

## SearchService
The `SearchService` allows searching for nodes based on an object tree.

The `SearchService.query()` method takes a `SearchQuery` object which contains the search query to execute,
as well as pagination, faceting and ordering options.

\define{EXAMPLE_IMPORTS}
\define{EXAMPLE_SEARCH_QUERY_OPTS}
```java
\include{examples/src/main/java/searchQuery.java}
```
\undef{EXAMPLE_IMPORTS}
\undef{EXAMPLE_SEARCH_QUERY_OPTS}

The query itself can be constructed using the `QueryBuilder`, which provides a fluent interface to 
build search queries.

\define{EXAMPLE_SEARCH_QUERY_QUERY}
```java
\include{examples/src/main/java/searchQuery.java}
```
\undef{EXAMPLE_SEARCH_QUERY_QUERY}

When using the REST API, a JSON payload describing the search query has to be POST'ed to the 
`apix/v1/search` endpoint. 
This JSON document reflects the node structure created by the query builder, and is shown below:

```json
\include{examples/src/main/resources/jsonsearchquery.json}
```

## DictionaryService
The `DictionaryService` provides meta-information about the metadata model.
It allows to fetch information about registered types, aspect and properties.

## Viewing JavaDoc
Full JavaDoc documentation of the Alfred API Java interface is available in the JavaDoc. You can
view the JavaDoc by browsing to `/alfresco/s/apix/javadocs/index.html` on your Alfresco host that
has Alfred API installed.

# Examples
```java
\include{examples/src/main/java/DespecializeWebscript.java} 
```
