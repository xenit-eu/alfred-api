\include{metadata.md}

# About
Alfred API abstracts away past and future changes to the Alfresco, across major and minor 
versions, providing a stable interface to Alfresco on which client-side applications can be built.

It also provides functional grouping of related operations from the Alfresco Public API,
and additional endpoints that are not supported by the Alfresco Public API.
 

# Alfred API Concepts
Alfred API is composed of two logical layers.

* The base layer is a *Java API* built on top of the Alfresco.
* A *REST API* is built on top of the Java abstraction layer, exposing a stable HTTP API.

Instead of mirroring the Alfresco API one-to-one, Alfred API groups frequently used operations
together in a single method call. This allows you to focus on business concerns instead of focusing
on fetching all required data from Alfresco.

For example, the `NodeService.getMetadata()` method returns an object with all metadata about 
a node: its type, aspects and properties in a single function call. It groups together the 
information that would otherwise have to be obtained by combining requests for type, aspects 
and properties separately.

## Data objects
Alfred API has data objects that mirror the Alfresco concepts of QName, NodeRef, StoreRef, Path, 
ContentData and ContentInputStream. These data objects are used to communicate with the 
Alfred Java API without being dependent on Alfresco data types.

Conversion between Alfresco and Alfred API data objects is the responsibility of the 
`ApixToAlfrescoConversion` service. It is also possible to construct an Alfred API data object by
passing its string representation to the constructor.


# Java API
The Java API is the core to exposing Alfresco functionality and normalizing operations across version.
Any extensions written while depending on Alfred API can be easily ported to a new Alfresco version.

When the API is installed, all of its service are available as beans and can be wired into your own classes.

## Services
Only the most important services are described below. Full documentation is available in
[the generated JavaDoc](https://docs.xenit.eu/alfred-api/stable-user/javadoc/).

### NodeService
The `NodeService` provides operations on nodes.

* Fetch and modify the metadata for a Node
* Fetch the root node of a Store
* Fetch, create and remove child, parent and target associations for a Node
* Copy or move a Node to another parent
* Create and delete Nodes
* Checkout, checkin and fetch working copies for a Node

### SearchService
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

### DictionaryService
The `DictionaryService` provides meta-information about the metadata model.
It allows to fetch information about registered types, aspect and properties.


# REST API
The Alfred REST API is a thin wrapper around the Java abstraction layer. It converts its received
parameters to the corresponding Alfred API data objects, then calls the corresponding service and
serializes its return value to JSON.

For a full overview of the REST API, please refer to 
[the swagger specification](https://docs.xenit.eu/alfred-api/stable-user/swagger-ui/).


## Search Requests

### Query
Object containing subcomponents that build the requested query.
All queries are translated to Alfresco Full Text Search (see 
[AFTS](https://community.alfresco.com/docs/DOC-5729-full-text-search-query-syntax)) by Alfred API when executed.


#### Syntax

The query parameter takes a tree structure of search nodes as its argument.
A search node is either an operator, or a search term.

##### Operators

Operators currently include only the standard `AND`, `OR` and `NOT` logical operations.
An operator is structured in the JSON payload as a named list.

* And

Translating `cm:name:"Budget 2020.xls" AND example:customProperty:"#AA3BF5"`
```json
{
  "and": [
    {
      "property" : {
        "name": "cm:name",
        "value": "Budget 2020.xls"
      }
    },
    {
      "property" : {
        "name" : "example:customProperty",
        "value" : "#AA3BF5"
      }
    }
  ]
}
```

* Or

Translating `example:customProperty:"#FFF233" OR example:customProperty:"#AA3BF5"`
```json
{
  "or": [
    {
      "property" : {
          "name" : "example:customProperty",
          "value" : "#FFF233"
        }
    },
    {
      "property" : {
        "name" : "example:customProperty",
        "value" : "#AA3BF5"
      }
    }
  ]
}
```

* Not

Translating `NOT cm:name:"Budget 2020.xls"`
```json
{
  "not": {
    "property": {
      "name": "cm:name",
      "value": "Budget 2020.xls"
    }
  }
}
```

* Nesting

Operators can be nested to form complex queries:
```json
{
  "and" : [
    {
      "not": {
        "property" : {
          "name" : "example:customProperty",
          "value" : "#FFF233"
        }
      }
    },
    {
      "or": [
          {
            "property" : {
                "name" : "example:customProperty",
                "value" : "#FFF233"
              }
          },
          {
            "property" : {
              "name" : "example:customProperty",
              "value" : "#AA3BF5"
            }
          }
      ]
    }
  ]
}
```
##### Search terms

Search terms can be split into two groups: special terms and generic terms.
Special terms are used to search for specific concepts, generic terms are used to search for property values at large.

**Special terms**

* type

`Supports transactional metadata queries`

Lookup for any nodes of the given contentmodel type.
```json
{ "type": "cm:folder" }
```


* aspect

`Supports transactional metadata queries`

Lookup for any nodes which have the give aspect.
```json
{ "aspect":  "sys:folder"}
```


* noderef

`Supports transactional metadata queries`

Lookup for a specific Alfresco noderef.
```json
{ "noderef": "workspace://SpacesStore/c4ebd508-b9e3-4c48-9e93-cdd774af8bbc" }
```


* path

`Supports transactional metadata queries`

Lookup for nodes at the given XPath.
```json
{ "path": "/app:company_home/cm:Fred_x0020_Performance_x0020_Test/*" }
```


* parent

`Supports transactional metadata queries`

Lookup for any nodes with the given node as parent. Takes a noderef as argument.
```json
{ "parent": "workspace://SpacesStore/c4ebd508-b9e3-4c48-9e93-cdd774af8bbc" }
```

* text

Lookup for any nodes that have given text in their content. Requires content of nodes to be indexed by Solr to be found.
```json
{ "text": "xenit solutions" }
```

* category

Lookup for any nodes are of the given category.
```json
{ "category": "/cm:generalclassifiable/cm:Software_x0020_Document_x0020_Classification/member" }
```

* all

Lookup for any nodes with a hit for the searchterm in any field or in the content.
```json
{ "all":  "Xenit solutions"}
```

* isunset

Lookup for any nodes where the value of given propety is not set.
```json
{ "isunset":  "cm:author"}
```

* exists

Lookup for any nodes where the given property is present.
```json
{ "exists": "cm:author" }
```

* isnull

Lookup for any nodes where the value of the given property is set to null.
```json
{ "isnull":  "cm:author"}
```

* isnotnull

Lookup for any nodes where the value of the given property is set and not null.
```json
{ "isnotnull": "cm:author" }
```

**Unsupported terms**

The following terms are available in Alfresco, but are currently not supported by Alfred API:

* isroot
* tx
* primaryparent
* class
* exactclass
* qname
* exactaspect
* tag


**Generic terms**

Generic terms are search terms for any given property with any given value or range.
```json
{ "property": 
    {
      "name": "example:customProperty",
      "value": "som*value"
    }
}
```
```json
{ "property": 
  {
    "name":"cm:modified",
    "range":{ 
      "start":"2015-10-05T10:41:42+00:00",
      "end":"2020-02-25T09:36:01+00:00"
    }
  }
}
```
Terms using values can also take the `exact` boolean parameter (by default it is considered set to false). 
This signifies that the given value needs to be matched exactly, as opposed to the default fuzzy search. 
This also implies that wildcards are not compatible with the exact parameter.

**For Transactional Metadata Queries, the `exact` parameter is mandatory.**
```json
{ "property": 
    {
      "name": "example:customProperty",
      "value": "som*value",
      "exact": true
    }
}
```

### paging

`Optional`

Options to page through the search results by setting a skipcount and a page size limit.
```json
{
  "paging": {
     "limit": 10,
     "skip": 0
   }
}
```

### facets

`Optional`

Options to enable facets. This will return facet results as configured on the server.
The Alfresco `limit` and `minCount` parameters are not implemented.

**Note:** facets with 0 hits are not returned in the result
```json
{
  "facets": {
    "enabled": true
  }
}
```


### orderBy

`Optional`

Options to define a list of properties to order by and the direction of ordering per property.
Expression based sorting is not implemented.
```json
{ 
  "orderBy": [
    {
      "property": "cm:name",
      "order": "descending"
    },
    {
      "property": "cm:modified",
      "order": "ascending"
    }
  ]
}
```

### consistency

`Optional`

Option to request specific consistency. Options are:

* `EVENTUAL`
* `TRANSACTIONAL`
* `TRANSACTIONAL_IF_POSSIBLE` (default)
```json
{
  "consistency": "TRANSACTIONAL"
}
```

#### Note on search, consistency and fuzziness

Alfresco internally supports two types of search queries: database-backed and Solr-backed. Based on the server configuration
and the search query, it will determine which of these 2 to use. Alfresco will attempt to use the database, but for any query 
on indexed properties, content or for fuzzy matching, Solr is required.

In the documentation database-backed queries are known as `Transactional Metadata Queries` or `TDMQ`'s.
To enable TDMQ's for Alfred API the `exact` parameter is required for generic search terms to be searched in the database,
and from the special search terms, only a subset is available for use. See the search term section 

The other search, Solr-backed, allows the usage of wild cards and other forms of fuzziness, but requires the Solr component
to build indexes against the Alfresco repository. This means that some time and resources are spent to bring the search index
up to date with the repository. As a consequence, after any given change, a window of time exists where a searchrequest against
Solr will return a result inconsistent with the repo. This effect will pass with time, hence the name eventual consistency.

### locale

`Optional`

Options to request specific locale and encoding options.
```json
{
  "locale": "BE"
}
```

### workspace

`Optional`

Options to change the target Alfresco workspace. This would allow searches on the archive or on custom workspaces.
A workspace is in this context defined by the `workspace name` + the protocol delimiter `://` + `the target store name`.

**Note**: if the target store is not indexed by Solr, eventually consistent queries will result in errors.
```json
{
  "workspace": "archive://SpacesStore"
}
```

### highlight

`Optional`

Options to change the highlight configuration.
Minimal requirement is the `fields` array, which takes object containing at least the `field` property. 
Each list element specifies a property on which higlighting needs to be applied. 
When no fields are specified, the call defaults to `cm:content` as field.
Full documentation can be found on the Alfresco documentation page: 

* [7.0](https://docs.alfresco.com/content-services/7.0/develop/rest-api-guide/searching/#term-highlighting-search)
* [7.1](https://docs.alfresco.com/content-services/7.1/develop/rest-api-guide/searching/#term-highlighting-search)
* [7.2](https://docs.alfresco.com/content-services/7.2/develop/rest-api-guide/searching/#term-highlighting-search)
* [7.3](https://docs.alfresco.com/content-services/7.3/develop/rest-api-guide/searching/#term-highlighting-search)
* [7.4](https://docs.alfresco.com/content-services/7.4/develop/rest-api-guide/searching/#term-highlighting-search)

```json
{
  "highlights": 
    {
      "prefix": "<highlight>",
      "postfix": "</highlight>",
      "snippetCount": 2,
      "fields": [
        {
          "field": "cm:content"
        }
      ]
    }
}
```

### Example Search Queries

Search for the first 10 nodes in the `cm:content` namespace with facets:
```json
{
  "query": {"type":"{http://www.alfresco.org/model/content/1.0}content"},
  "paging": {
    "limit": 10,
    "skip": 0
  },
  "facets": {
    "enabled": false
  }
}
```

Search for all nodes with the term 'budget' in the `cm:content` property (fulltext), and show two highlighted hits with
delimiter \\<highlight>\\</highlight>:
```json
{
    "query": {
        "property": {
            "exact": false,
            "name": "cm:content",
            "value": "budget"
        }
    },
    "highlight":{
        "prefix":"<highlight>",
        "postfix":"</highlight>",
        "snippetCount":2,
        "fields":[{"field":"cm:content"}]
    }
}
```

### JSON response

The search REST call returns a JSON object of the following form:
```json
{
  "noderefs": [ // List of noderefs
    "workspace://SpacesStore/c4ebd508-b9e3-4c48-9e93-cdd774af8bbc",
    "workspace://SpacesStore/ff5874ad-b9e3-4c48-9e93-cddaaa9746ec"
  ],
  "facets": [ // list of facets
    {
      "name": "TYPE", // facet field name
      "values": [ // Facet values
        {
          "value": "Factuur",
          "label": "Factuur",
          "count": 16
        }
      ]
    },
    {
      "name": "cm:modified", // facet field name
      "values": [ // Facet values
        {
          "range":
          {
            "start":"2020-05-01T00:00:00+00:00",
            "end":"2020-05-31T00:00:00+00:00"
          },
          "label": "One month ago",
          "count": 16 // value and count
        }
      ]
    }
  ],
  "totalResultCount": 2, //Sum of all results
  "highlights": {
      "noderefs": { //A set of objects with an entry for each noderef for which a highlight can be returned
          "workspace://SpacesStore/e818fd1c-262e-43f1-8751-461cd8de9293": [ //list of highlights per specified field for the given node
              {
                  "field": "cm:name",
                  "snippets": [
                      "Stretch your research <highlight>budget</highlight>-20150813-155144.msg"
                  ]
              }
          ]
      }
  }
}
```

### Additional notes

#### Date format

Dates and times must be specified in [ISO-8601](https://www.iso.org/iso-8601-date-and-time-format.html) format.

#### Unimplemented search options:
These options have some code towards handling, but are not implemented such that they are used in the search.

* `facets.mincount`
* `facets.limit`
* `orderBy.expression`


## Usage Example

In this example a new node will be created and its metadata set:

**1.** Find the default Alfresco folder named "Shared" to use as parent folder for the new node.
```bash
curl -X POST \
--header "Authorization: Basic a8d46fw84649" \
--header "Accepts: application/json" \
--header "Content-Type: application/json" \
--data '{ "query": { "property": { "name":"cm:name", "value":"Shared", "exact": true } } }' \
https://www.alfresco.example/alfresco/s/apix/v1/search 
```
Response:
```json
{
  "noderefs": [ "workspace://SpacesStore/df18bcde-531b-4b39-9698-e460cbff2bb5" ],
  "totalResultCount": 1
}
```
**2.** Create the new node
```bash
curl -X POST \
--header "Authorization: Basic a8d46fw84649" \
--header "Content-Type: application/json" \
--data '{ "parent": "workspace://SpacesStore/df18bcde-531b-4b39-9698-e460cbff2bb5", "name": "Red test node", "type": "{http://www.alfresco.org/model/content/1.0}content" }' \
https://www.alfresco.example/alfresco/s/apix/v1/nodes
```
Response:
```json
{
  "noderef":"workspace://SpacesStore/d26176d6-11d9-4381-a327-cccb7600efc4",
  "metadata": {
      "id":"workspace://SpacesStore/d26176d6-11d9-4381-a327-cccb7600efc4",
      "type":"{http://www.alfresco.org/model/content/1.0}content",
      "baseType":"{http://www.alfresco.org/model/content/1.0}content",
      "transactionId":66261,
      "properties": {
          "{http://www.alfresco.org/model/system/1.0}store-protocol":["workspace"],
          "{http://www.alfresco.org/model/system/1.0}node-dbid":["128993"],
          "{http://www.alfresco.org/model/content/1.0}name":["Red test node"],
          "{http://www.alfresco.org/model/content/1.0}modified":["2020-10-06T12:31:12.356Z"],
          "{http://www.alfresco.org/model/content/1.0}creator":["admin"],
          "{http://www.alfresco.org/model/system/1.0}locale":["en_US"],
          "{http://www.alfresco.org/model/content/1.0}created":["2020-10-06T12:31:12.356Z"],
          "{http://www.alfresco.org/model/system/1.0}store-identifier":["SpacesStore"],
          "{http://www.alfresco.org/model/content/1.0}modifier":["admin"],
          "{http://www.alfresco.org/model/system/1.0}node-uuid":["d26176d6-11d9-4381-a327-cccb7600efc4"],
      },
      "aspects":[
        "{http://www.alfresco.org/model/content/1.0}auditable",
        "{http://www.alfresco.org/model/system/1.0}referenceable",
        "{http://www.alfresco.org/model/system/1.0}localized"
        ]
    },
  "permissions": {
      "Read":"ALLOW",
      "Write":"ALLOW",
      "Delete":"ALLOW",
      "AddChildren":"ALLOW",
      "ReadPermissions":"ALLOW",
      "ReadRecords":"DENY",
      "Filing":"DENY",
      "CreateChildren":"ALLOW",
      "ChangePermissions":"ALLOW",
  },
  "associations":{
      "children":[],
      "parents":[
          {
            "source":"workspace://SpacesStore/d26176d6-11d9-4381-a327-cccb7600efc4",
            "target":"workspace://SpacesStore/df18bcde-531b-4b39-9698-e460cbff2bb5",
            "type":"{http://www.alfresco.org/model/content/1.0}contains",
            "primary":true
          }
      ],
      "targets":[]
  },
  "path":{
      "displayPath":"/Company Home/Shared",
      "qnamePath":"/app:company_home/app:shared/cm:Red_x0020_test_x0020_node"
  }
}
```

## REST HTTP result codes
REST responses can return the following HTTP status codes:


### 2xx Success

Indicates request sent by client was understood and accepted.

* **200 OK**: Generic success.
* **202 Accepted**: The request was successful and will be processed asynchronously.
* **207 Multi-Status**: A bulk request completed successfully. These responses should contain multi-status response that
  can be correlated to each individual request in the bulk request. Can be returned even if individual requests fail.


### 3xx Redirection

Indicates the client must take additional steps to complete the request.

* **301 Moved Permanently**: This and all future requests should be directed to the given URI.


### 4xx Client error

Indicates anticipated failures, such as requests for non-existant resources, 
requests with missing input and malformed requests.

A body *may* be provided in the response that clarifies the error.

* **400 Bad Request**: Generic client error.
* **401 Unauthorized**: User must log in.
* **403 Forbidden**: User not authorized to use this resource.
* **404 Not Found**: Requested resource not found. Returned also for e.g. requesting a node with an incorrect id, as 
  well as unhandled URI's. For security reasons, a 404 can aso be returned when the requester has insufficient permissions.
* **405 Method Not Allowed**: A request method is not supported (e.g. PUT on an endpoint that only accepts GET).


### 5xx Server error

Indicates unexpected failures.

* **500 Internal Server Error**: Generic server error.
* **503 Service Unavailable**: Temporary server error. Retry later is sensible.

# Installation

## Supported Alfresco versions
Currently Alfred API supports the following Alfresco versions:

* 7.0
* 7.1
* 7.2
* 7.3
* 7.4

Note that previous versions of Alfred API needed
[Dynamic Extensions For Alfresco](https://github.com/xenit-eu/dynamic-extensions-for-alfresco).
Since version 5.0.0, however, Dynamic Extensions is no longer needed.

## Artifacts
### Prebuilt
Artifacts can be freely obtained through [Maven Central](https://search.maven.org/search?q=g:eu.xenit.apix).
The application is available as an Alfresco AMP artifact, which is the preferred distribution for production environments.

To install the AMP, follow the Alfresco AMP installation guidelines your version of Alfresco: 

* [7.0](https://docs.alfresco.com/content-services/7.0/install/zip/amp/)
* [7.1](https://docs.alfresco.com/content-services/7.1/install/zip/amp/)
* [7.2](https://docs.alfresco.com/content-services/7.2/install/zip/amp/)
* [7.3](https://docs.alfresco.com/content-services/7.3/install/zip/amp/)
* [7.4](https://docs.alfresco.com/content-services/7.4/install/zip/amp/)

# Contributing
Alfred API is open source. The code is available from [Github](https://github.com/xenit-eu/alfred-api). Building the artifacts requires
access to Alfresco Enterprise libraries to satisfy Enterprise dependencies.

If you wish to contribute, please refer to the [README.md](https://github.com/xenit-eu/alfred-api/blob/master/README.md)
in its code repository.

Note: For legacy reasons the older name `apix` can still be found in certain locations.
