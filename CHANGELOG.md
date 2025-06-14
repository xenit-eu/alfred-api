# Alfred API - Changelog

## 6.1.0 (2025-04-01)
### Added
* [ALFREDAPI-548](https://xenitsupport.jira.com/browse/ALFREDAPI-569): Support Alfresco V23.3 && V23.4
* [ALFREDAPI-576](https://xenitsupport.jira.com/browse/ALFREDAPI-576): Support Alfresco V25.1

### Fixed
* [ALFREDAPI-568](https://xenitsupport.jira.com/browse/ALFREDAPI-568) Make Alfred API work with new Tomcat base image
  environment variable for casual multipart parsing
* [ALFREDAPI-568](https://xenitsupport.jira.com/browse/ALFREDAPI-568) Separated upload to native Alfresco webscript as a workaround to broken multipart upload with Alfresco MVC
* [ALFREDAPI-572](https://xenitsupport.jira.com/browse/ALFREDAPI-572) Removed dependencies that caused warnings of 
being overwritten (jaxb-api, javax.activation-api)


## 6.0.1 (2024-11-25)

### Fixed
* [ALFREDAPI-563](https://xenitsupport.jira.com/browse/ALFREDAPI-563) Fix @GetMapping(value = "/v1/nodes/{space}/{store}/{guid}/content") Content-Type

## 6.0.0 (2024-08-22)
From this version onward Dynamic Extensions for integration-testing is replaced by [remote-junit](https://github.com/ruediste/remote-junit)
as framework to reduce maintenance efforts.

A subproject with the name of `alfred-api-integrationtests-server` has been added. (See README)
The artifact name of `alfred-api-integrationtests` has been changed to `alfred-api-integrationtests-client`.

**Breaking changes:** 
  * JDK11 --> JDK 17
  * Library changes from Javax to Jakarta
  * Tomcat V10.1: this comes with a breaking change in the dispatchservlet, blocking the MultiPart handling and blocks double forward slashes
    * Fix has to be deployed from your tomcat image. You will have to update your META-INF/context.xml in tomcat. See subproject alfred-api-docker. 
  * Dropped all support for older Alfresco version prior to V23.1 just as alfresco-mvc
  * **All package names have been updated from `eu.xenit.apix` to `eu.xenit.alfred.api`.**
  * **All class names have been updated from `apix-...` to `alfred.api-...`.**

* [ALFREDAPI-548](https://xenitsupport.jira.com/browse/ALFREDAPI-548): Support Alfresco V23.1 && V23.2, dropping V7.x
* [ALFREDAPI-556](https://xenitsupport.jira.com/browse/ALFREDAPI-556): Change apix package-names to `eu.xenit.alfred.api`.

### Fixed
* [ALFREDAPI-552](https://xenitsupport.jira.com/browse/ALFREDAPI-552) Make swagger spec Open Api v2 compliant

## 5.0.3 (2024-06-17)

### Fixed
* [ALFREDAPI-554](https://xenitsupport.jira.com/browse/ALFREDAPI-554): expose `apix-impl`
  beans in main application context (to be used by other AMPs)

## 5.0.2 (2024-05-14)

### Fixed
* [ALFREDAPI-544](https://xenitsupport.jira.com/browse/ALFREDAPI-544): fix Date range search by dropping unprocessed facet labels

## 5.0.1 (2024-03-19)

The artifact name of `apix-interface` has been changed to `alfred-api-interface`.

### Fixed
* [ALFREDAPI-537](https://xenitsupport.jira.com/browse/ALFREDAPI-537): Fix conflicts between artifacts when publishing to Sonatype
* [ALFREDAPI-538](https://xenitsupport.jira.com/browse/ALFREDAPI-538): Fixed issue where errors related to jackson library conflicts would occurs while Alfresco is running
* [ALFREDAPI-540](https://xenitsupport.jira.com/browse/ALFREDAPI-540): Realign interface artifact name
* [ALFREDAPI-541](https://xenitsupport.jira.com/browse/ALFREDAPI-541): Clarify dependency installation instructions


## 5.0.0 (2023-12-12)
From this version onward Dynamic Extensions is replaced by [Alfresco MVC](https://github.com/dgradecak/alfresco-mvc)
as framework to reduce maintenance efforts.

To make this change clearer the Alfred API Maven group ID has been updated from `eu.xenit.apix`
to `eu.xenit.alfred.api`.

This release also drops support for Alfresco 6.2 and adds support for 7.4.

### Added
* [ALFREDAPI-519](https://xenitsupport.jira.com/browse/ALFREDAPI-519): Add support for Alfresco 7.4

### Changed
* [ALFREDAPI-527](https://xenitsupport.jira.com/browse/ALFREDAPI-527):
Alfresco containers use port 8080 now instead of ephemeral ports 
* [ALFREDAPI-536](https://xenitsupport.jira.com/browse/ALFREDAPI-536): Reabsorb alfred-api-docs repo into this

### Fixed
* [ALFREDAPI-520](https://xenitsupport.jira.com/browse/ALFREDAPI-520): Enforce encoding on bulk json responses to guarantee clean text
* [ALFREDAPI-531](https://xenitsupport.jira.com/browse/ALFREDAPI-531): Fix facet qname splitting for dates
* [ALFREDAPI-532](https://xenitsupport.jira.com/browse/ALFREDAPI-532): Fix :apix-interface:javadoc
* [ALFREDAPI-535](https://xenitsupport.jira.com/browse/ALFREDAPI-535): Fix incomplete POMs

### Removed
* [ALFREDAPI-504](https://xenitsupport.jira.com/browse/ALFREDAPI-504): Drop Dynamic Extensions in favor of Alfresco MVC
* [ALFREDAPI-519](https://xenitsupport.jira.com/browse/ALFREDAPI-519): Remove support for Alfresco 6.2



## 4.0.1 (2023-06-13)
This release removes swaggerui_5x from alfred-api artifact and changes generation of Snapshot qualifier to comform to maven format.

### Changed
* [ALFREDAPI-509](https://xenitsupport.jira.com/browse/ALFREDAPI-509): Moved CI to Github Actions
* [ALFREDAPI-513](https://xenitsupport.jira.com/browse/ALFREDAPI-513): Remove swaggerui_5x from alfred-api artifact
* [ALFREDAPI-514](https://xenitsupport.jira.com/browse/ALFREDAPI-514): Change generation of Snapshot qualifier to comform to maven format.
* [ALFREDAPI-522](https://xenitsupport.jira.com/browse/ALFREDAPI-522): Change gradle repositories from artifactory.xenit to cloudsmith && artifactory.alfresco.
* [ALFREDAPI-516](https://xenitsupport.jira.com/browse/ALFREDAPI-516): Classpath cleanup


## 4.0.0 (2023-01-17)
This release adds support for Alfresco 7.3 and drops support for Alfresco 5.2 and 6.1.

### Added
* [ALFREDAPI-505](https://xenitsupport.jira.com/browse/ALFREDAPI-505): Added support for Alfresco 7.3

### Fixed
* [ALFREDAPI-503](https://xenitsupport.jira.com/browse/ALFREDAPI-503): Fixed issue where site information could not be retrieved if the user does not have permissions to one of the components

### Removed
* [ALFREDAPI-505](https://xenitsupport.jira.com/browse/ALFREDAPI-505): Drop support for Alfresco 5.2 and 6.1


## 3.1.0 (2022-04-21)

### Added
* [ALFREDAPI-501](https://xenitsupport.jira.com/browse/ALFREDAPI-501): Added support for Alfresco 7.1 and 7.2

### Changed

* [ALFREDAPI-498](https://xenitsupport.jira.com/browse/ALFREDAPI-497): improve handling of version node association retrieval



## 3.0.2 (2021-11-30)

### Added
* [ALFREDAPI-492](https://xenitsupport.jira.com/browse/ALFREDAPI-492): /v1/nodes POST enpoint now accepts aspects to add/remove
* [ALFREDAPI-497](https://xenitsupport.jira.com/browse/ALFREDAPI-497): Re-enable `composeDown` after `integrationTest` in build



## 3.0.1 (2021-09-29)

### Changed
* [XENOPS-891](https://xenitsupport.jira.com/browse/XENOPS-891): Update docker images and wars to latest hotfix for 52, 62 & 70
* [ALFREDAPI-378](https://xenitsupport.jira.com/browse/ALFREDAPI-378): Replace deprecated calls to serviceregistry

### Fixed
* [ALFREDAPI-469](https://xenitsupport.jira.com/browse/ALFREDAPI-469): Add swagger-doc-extractor subproject for http://docs.xenit.eu/alfred-api
* [ALFREDAPI-489](https://xenitsupport.jira.com/browse/ALFREDAPI-489): Implemented check on content indexing in solrtesthelper
* [ALFREDAPI-490](https://xenitsupport.jira.com/browse/ALFREDAPI-490): Improve performance of retrieving categories


## 3.0.0 (2021-07-29)
This release adds support for Alfresco 7.0 and drops support for Alfresco 5.0, 5.1 and 6.0.

### Changed
* [ALFREDAPI-481](https://xenitsupport.jira.com/browse/ALFREDAPI-481): Add blurb about Xenit to README
* [ALFREDAPI-482](https://xenitsupport.jira.com/browse/ALFREDAPI-482): Add support for Alfresco 7.0

### Deleted
* [ALFREDAPI-483](https://xenitsupport.jira.com/browse/ALFREDAPI-483): Drop support for Alfresco 5.0 and 5.1
* [ALFREDAPI-486](https://xenitsupport.jira.com/browse/ALFREDAPI-483): Drop support for Alfresco 6.0


## 2.7.1 (2021-01-13)

### Changed
* [ALFREDAPI-398](https://xenitsupport.jira.com/browse/ALFREDAPI-398): Move documentation to docs.xenit.eu/alfred-api
* [ALFREDAPI-401](https://xenitsupport.jira.com/browse/ALFREDAPI-463): Replace `org.codehaus.jackson` dependencies with 
  `com.fasterxml` dependencies, and moved shared source of integration tests to integration tests module.
* [ALFREDAPI-470](https://xenitsupport.jira.com/browse/ALFREDAPI-470): Prevent problematic memory usage using /search on 
  large datasets by not using 'unlimited' (-1) as a default.   
* [ALFREDAPI-464](https://xenitsupport.jira.com/browse/ALFREDAPI-464): Change JavaDoc generation on AlfredApi Interface 
  to avoid jdk bug
* [ALFREDAPI-296](https://xenitSupport.jira.com/browse/ALFREDAPI-296): Change dictionaryService to return mandatory aspects for type- and aspectdefinitions
* [ALFREDAPI-445](https://xenitsupport.jira.com/browse/ALFREDAPI-445): Improve exceptionhandling for duplicate files in nodes Create/Copy/Move

### Fixed
* [ALFREDAPI-184](https://xenitsupport.jira.com/browse/ALFREDAPI-184): Improve graceful handling of bad input in 
  ConfigurationWebscript1
* [ALFREDAPI-463](https://xenitsupport.jira.com/browse/ALFREDAPI-463): Fix quotation marks in searches being improperly 
  escaped. Search queries can now be escaped properly. E.g. \"Compas Format\" instead of \\\"Compas Format\\\" as was needed previously.
* [ALFREDAPI-466](https://xenitsupport.jira.com/browse/ALFREDAPI-466): Fix usage of the special `-me-` argument for the 
  peopleAPI v1 & v2
* [ALFREDAPI-472](https://xenitsupport.jira.com/browse/ALFREDAPI-472): Fix AccessDeniedException in sitesService 
  (primarily from Alfresco Records Management)
* [ALFREDAPI-473](https://xenitsupport.jira.com/browse/ALFREDAPI-473): Integration tests are again run

### Deleted



## 2.7.0 (2020-10-09)

### Changed
* [ALFREDAPI-461](https://xenitsupport.jira.com/browse/ALFREDAPI-461): Reintroduce xenit maven repo definition

### Added
* [ALFREDAPI-418](https://xenitsupport.jira.com/browse/ALFREDAPI-418): Add support for Alfresco 6.2
* [ALFREDAPI-442](https://xenitsupport.jira.com/browse/ALFREDAPI-442): Add webscript to get all properties and aspects

### Fixed
* [ALFREDAPI-390](https://xenitsupport.jira.com/browse/ALFREDAPI-390): Remove symlinks in code
* [ALFREDAPI-259](https://xenitsupport.jira.com/browse/ALFREDAPI-259): Intellij does not load the code of the integration tests project correctly
* [ALFREDAPI-453](https://xenitsupport.jira.com/browse/ALFREDAPI-453): Fix publishing which was broken due to above fixes


## 2.6.1 (2020-09-07)

### Changed
* [ALFREDAPI-444](https://xenitsupport.jira.com/browse/ALFREDAPI-444): Updated and extended Create & Copy node integrationtests for "/nodes"
* [ALFREDAPI-449](https://xenitsupport.jira.com/browse/ALFREDAPI-449): Changed endpoints of the v1 CRUD comments api. 
**This changes the comment REST API added in 2.6.0 based on received feedback.** 

### Fixed
* [ALFREDAPI-438](https://xenitsupport.jira.com/browse/ALFREDAPI-438): Fixed issue where wrong permissions would be checked when retrieving ancestors of a node

### Deleted
* [ALFREDAPI-447](https://xenitsupport.jira.com/browse/ALFREDAPI-447): Removed unused class LuceneNodeVisitor. This does not affect functionality of the API.


## 2.6.0 (2020-08-26)

### Added
* [ALFREDAPI-298](https://xenitsupport.jira.com/browse/ALFREDAPI-298): Add CRUD api for comments
* [ALFREDAPI-403](https://xenitsupport.jira.com/browse/ALFREDAPI-403): Added support for special search terms 'isunset', 'isnull', 'isnotnull' and 'exists'

### Changed
* [ALFREDAPI-395](https://xenitsupport.jira/com/browse/ALFREDAPI-395): Change loglevels of PermissionSerivce#hasPermission

### Fixed
* [ALFREDAPI-428](https://xenitsupport.jira.com/browse/ALFREDAPI-428): Replace PersonService#getPerson(String userName) with getPersonOrNull(String userName) to avoid creation of new users when getPerson is called with a nonexistent userName.


## 2.5.2 (2020-08-12)

### Fixed
* [ALFREDAPI-338](https://xenitsupport.jira.com/browse/ALFREDAPI-338): Fixed issue when the provided name would not be set while copying a node

## 2.5.1 (2020-08-05)

### Fixed
* [ALFREDAPI-438](https://xenitsupport.jira.com/browse/ALFREDAPI-438): Replace hasReadPermission with hasPermission to avoid an Alfresco bug that prevents usage of that method and throws AcessDeniedException
* [ALFREDAPI-439](https://xenitsupport.jira.com/browse/ALFREDAPI-439): Fixed issue where category facet values would be displayed with their noderef instead of their name
* [ALFREDAPI-437](https://xenitsupport.jira.com/browse/ALFREDAPI-437): Fixed issue where paged searches for transactional queries could not fetch more than 1000 results


## 2.5.0 (2020-07-15)

### Added
* [HA-29](https://xenitsupport.jira.com/browse/HA-29): Added webscript for retrieving available sites + webscript for retrieving ancestors of a node

### Changed
* [ALFREDAPI-416](https://xenitsupport.jira.com/browse/ALFREDAPI-416): Removed max repository version from module.properties for builds for most recent Alfresco
* [ALFREDAPI-420](https://xenitsupport.jira.com/browse/ALFREDAPI-420): Add missing Associations to getAssociations call; `getAssociations` now also returns source associations for a node. `/nodes/nodeInfo` will now also returns source associations by default.
* [ALFREDAPI-421](https://xenitsupport.jira.com/browse/ALFREDAPI-421): Add 403 Not Authorized responses to `NodesWebscript1.java`
* [ALFREDAPI-365](https://xenitsupport.jira.com/browse/ALFREDAPI-365): Decrease log levels in `propertyServiceImpl` and `ResourceBundleTranslationKey` to avoid logspam

### Fixed
* [ALFREDAPI-419](https://xenitsupport.jira.com/browse/ALFREDAPI-419): Add urldecoding to deleteAssociation endpoint
* [ALFREDAPI-422](https://xenitsupport.jira.com/browse/ALFREDAPI-422): Fix totalResults and limits for TDMQ's
* [ALFREDAPI-427](https://xenitsupport.jira.com/browse/ALFREDAPI-427): Add workaround to prevent Solr Exceptions when searching for numeric values that overflow when parsed as int or long
* [ALFREDAPI-412](https://xenitsupport.jira.com/browse/ALFREDAPI-427): Fix getAllNodeInfo endpoint to handle null values and non-existing nodes better
* [ALFREDAPI-388](https://xenitsupport.jira.com/browse/ALFREDAPI-388): Add handling for preexisting file in upload


## 2.4.0 (2020-03-26)

### Changed
* [ALFREDOPS-457](https://xenitsupport.jira.com/browse/ALFREDOPS-457): Updated Alfresco 6.1 base war to 'org.alfresco:content-services:6.1.1@war'
* [ALFREDAPI-407](https://xenitsupport.jira.com/browse/ALFREDAPI-407): Updated documentation (cleaning javadoc warnings)

### Fixed
* [ALFREDAPI-410](https://xenitsupport.jira.com/browse/ALFREDAPI-410): Configuration webscript requires read access to Company Home


## 2.3.0 (2020-02-13)

### Changed
* [ALFREDAPI-385](https://xenitsupport.jira.com/browse/ALFREDAPI-385): Change docker & compose files for integration tests to use harbor and docker.io/xenit
* [ALFREDAPI-387](https://xenitsupport.jira.com/browse/ALFREDAPI-387): Add existence check and 404 to working copies endpoint
* [ALFREDAPI-368](https://xenitsupport.jira.com/browse/ALFREDAPI-368): Replace obsoleted Gradle plugins (org.dm.bundle and ampde)
* [ALFREDAPI-386](https://xenitsupport.jira.com/browse/ALFREDAPI-386): Upgrade to Gradle 5
* [ALFREDAPI-404](https://xenitsupport.jira.com/browse/ALFREDAPI-404): Use HTTPS to connect to artifact repositories

### Fixed
* [ALFREDAPI-392](https://xenitsupport.jira.com/browse/ALFREDAPI-392): Stop the incorrect publishing of (development use case only) apix-impl JARs
* [ALFREDAPI-377](https://xenitsupport.jira.com/browse/ALFREDAPI-377): Changed default query consistency to transactional if possible
* [ALFREDAPI-406](https://xenitsupport.jira.com/browse/ALFREDAPI-406): Deletion of temporary files after an upload finishes

### Deleted
* [ALFREDAPI-402](https://xenitsupport.jira.com/browse/ALFREDAPI-402): Removed obsolete webscript (rest-v0.categories.CategoryGetWebscript)


## 2.2.0 (2019-09-17)

### Changed
* [ALFREDAPI-346](https://xenitsupport.jira.com/browse/ALFREDAPI-346): Moved Alfred API to Github: https://github.com/xenit-eu/alfred-api
* [ALFREDAPI-384](https://xenitsupport.jira.com/browse/ALFREDAPI-384): Added hasPermission to IPermissionService and used it to fix `ApixV1Webscript#getAllInfoOfNodes()`.
    **The endpoint `POST /alfresco/s/apix/v1/nodeInfo` now returns a list with any faulty nodes (e.g. `Access Denied` or `Does not Exist`) removed instead of HTTP 500**


## 2.1.0 (2019-08-26)

### Changed
* [ALFREDAPI-382](https://xenitsupport.jira.com/browse/ALFREDAPI-382): Configure docker project to run with debug enabled locally, but regularly on Jenkins

### Deleted
* [ALFREDAPI-380](https://xenitsupport.jira.com/browse/ALFREDAPI-380): Disable archiving of Jenkins artefacts

### Fixed
* [ALFREDAPI-325](https://xenitsupport.jira.com/browse/ALFREDAPI-325): HTTP 500 when requesting a dictionary definition with unregisterd namespace
* [ALFREDAPI-349](https://xenitsupport.jira.com/browse/ALFREDAPI-349): 500 Internal Error upon order by parties
* [ALFREDAPI-357](https://xenitsupport.jira.com/browse/ALFREDAPI-357): Query with special character in it (e.g. - or {) causes 500 exception
* [ALFREDAPI-381](https://xenitsupport.jira.com/browse/ALFREDAPI-381): Changing the name of a node now also updates the qname path
* [ALFREDAPI-382](https://xenitsupport.jira.com/browse/ALFREDAPI-382): Return 400 when PUTting acls with malformed body
* [ALFREDAPI-367](https://xenitsupport.jira.com/browse/ALFREDAPI-367): Get content of non existing node results in SC 500


## 2.0.2 (2019-06-25)

### Added
* [ALFREDAPI-362](https://xenitsupport.jira.com/browse/ALFREDAPI-362),
[ALFREDAPI-373](https://xenitsupport.jira.com/browse/ALFREDAPI-373),
[ALFREDAPI-379](https://xenitsupport.jira.com/browse/ALFREDAPI-379): Added Alfresco 6.1 support

### Fixed
* [ALFREDAPI-322](https://xenitsupport.jira.com/browse/ALFREDAPI-322): Fix HTTP response codes for node details and node delete
* [ALFREDAPI-372](https://xenitsupport.jira.com/browse/ALFREDAPI-372): Workaround for bad Highlight handling (Epic: phase out 500 Errors)

## 2.0.1 RC1 (2019-04-29)
This is a release candidate for 2.0, intended to test out installations
with Alfresco 6.0. It is not yet intended for production.

### Added
* [ALFREDAPI-355](https://xenitsupport.jira.com/browse/ALFREDAPI-355),
  [ALFREDAPI-360](https://xenitsupport.jira.com/browse/ALFREDAPI-360): Added Alfresco 6.0 support

### Deleted
* [ALFREDAPI-355](https://xenitsupport.jira.com/browse/ALFREDAPI-355): Removed Alfresco 4.2 support


## 1.20.0 (2019-04-17)

### Added
* [ALFREDAPI-291](https://xenitsupport.jira.com/browse/ALFREDAPI-291): Expose Java MimetypeService

### Changed
* [ALFREDAPI-361](https://xenitsupport.jira.com/browse/ALFREDAPI-361): Create multiple release branches


## 1.19.0 (2019-03-26)

###	Added
* [FREDSUP-594](https://xenitsupport.jira.com/browse/FREDSUP-594): Added optional parameter to metadata post webscript to clean up default aspects when generalizing the type

### Fixed
* [ALFREDAPI-347](https://xenitsupport.jira.com/browse/ALFREDAPI-347): Fixes HTTP 500 when using range-filters in search query
* [ALFREDAPI-348](https://xenitsupport.jira.com/browse/ALFREDAPI-348): Fixes build-issue that caused some tests to be skipped


## 1.18.0 (2019-03-07)

### Added
* [ALFREDAPI-344](https://xenitsupport.jira.com/browse/ALFREDAPI-344): Translated Bucketed Facets


## 1.17.3 (2019-02-27)

### Fixed
* [ALFREDAPI-343](https://xenitsupport.jira.com/browse/ALFREDAPI-343): Fix publishing AMP release


## 1.17.0 (2019-02-27)

### Added
* [INNOSEARCH-4](https://xenitsupport.jira.com/browse/INNOSEARCH-4): Extended /search with term hit highlights

### Changed
* [ALFREDAPI-336](https://xenitsupport.jira.com/browse/ALFREDAPI-336): Improve project structure
* [ALFREDAPI-334](https://xenitsupport.jira.com/browse/ALFREDAPI-334): Comply with XEP-7

### Fixed
* [ALFREDAPI-340](https://xenitsupport.jira.com/browse/ALFREDAPI-340): Fixed issue where NullPointerException would occur when property definition of facet is null


## 1.16.1 (2019-01-17)

### Fixed
* [ALFREDAPI-334](https://xenitsupport.jira.com/browse/ALFREDAPI-334): Hotfix for building AMPs containing multiple, conflicting JARs


## 1.16.0 (2019-01-17)

### Added
* [AVP-81](https://xenitsupport.jira.com/browse/AVP-81): Make Start Workflow return a json object with the workflow instance ID
* [AVP-103](https://xenitsupport.jira.com/browse/AVP-103): Make "key" part of the Apix json model for the workflow definition
* [ALFREDAPI-297](https://xenitsupport.jira.com/browse/ALFREDAPI-297): Added Workspace parameter to Search Query to specify Store to search in
* [ALFREDAPI-330](https://xenitsupport.jira.com/browse/ALFREDAPI-330) Add assertion for the correct facet begin present

### Changed
* [ALFREDAPI-327](https://xenitsupport.jira.com/browse/ALFREDAPI-327): Use correct logging levels in bulk web script
* [ALFREDAPI-328](https://xenitsupport.jira.com/browse/ALFREDAPI-328): Use correct logging levels in SearchService
* [ALFREDAPI-306](https://xenitsupport.jira.com/browse/ALFREDAPI-306): Facets returned in a search on 5.x now include bucketed facets
    * Bucketed facets in Alfresco 4.2 remain unsupported

### Fixed
* [ALFREDAPI-307](https://xenitsupport.jira.com/browse/ALFREDAPI-307): Fix HTTP 500 in /dictionary/types/ for a document type with an (unencoded) qname that contains a dot (.)
* [ALFREDAPI-326](https://xenitsupport.jira.com/browse/ALFREDAPI-326): Search endpoint leaves off facets property when no facets match
* [ALFREDAPI-329](https://xenitsupport.jira.com/browse/ALFREDAPI-329): Default BPM selection when none/no valid config entry is found



## 1.15.0 (2018-11-28)

### Added
* [ALFREDAPI-311](https://xenitsupport.jira.com/browse/ALFREDAPI-311): Improve PermissionService.setNodePermissions to clean up empty inheriting ACL definitions
* [ALFREDAPI-304](https://xenitsupport.jira.com/browse/ALFREDAPI-304): Sort configuration files
* [ALFREDAPI-311](https://xenitsupport.jira.com/browse/ALFREDAPI-311),
    [AVP-81](https://xenitsupport.jira.com/browse/AVP-81),
    [AVP-100](https://xenitsupport.jira.com/browse/AVP-100);
    [AVP-75](https://xenitsupport.jira.com/browse/AVP-75): Add partial implementation of Alfresco Process Service as BPM
* [AVP-34](https://xenitsupport.jira.com/browse/AVP-34),
    [AVP-43](https://xenitsupport.jira.com/browse/AVP-43): Add Start Workflow functionality (for VDL Archive)

### Changed
* [ALFREDAPI-305](https://xenitsupport.jira.com/browse/ALFREDAPI-305): Search: Add support for sorting by TYPE

### Fixed
* [ALFREDAPI-299](https://xenitsupport.jira.com/browse/ALFREDAPI-299): AlfrescoPropertyConvertor throwing errors
* [ALFREDAPI-309](https://xenitsupport.jira.com/browse/ALFREDAPI-309): Add ?exclude=… option to /workflows/definitions

* [ALFREDAPI-295](https://xenitsupport.jira.com/browse/ALFREDAPI-295): publishAmpPublicationToReleaseRepository task not available
* [ALFREDAPI-270](https://xenitsupport.jira.com/browse/ALFREDAPI-270): Permanent delete does not work when used in bulk call



## 1.14.4 (2018-09-04)

### Fixed
* [ALFREDAPI-301](https://xenitsupport.jira.com/browse/ALFREDAPI-301): apix-interface module not published to artifactory
* [ALFREDAPI-293](https://xenitsupport.jira.com/browse/ALFREDAPI-293): Alfred API not returning all facets
* [ALFREDAPI-275](https://xenitsupport.jira.com/browse/ALFREDAPI-275): Searchqueries with noderef always return no results
* [ALFREDAPI-295](https://xenitsupport.jira.com/browse/ALFREDAPI-295): publishAmpPublicationToReleaseRepository task not available
* [ALFREDAPI-285](https://xenitsupport.jira.com/browse/ALFREDAPI-285): Total results not correct when skip is used in queries for 4.2.8
* [ALFREDAPI-268](https://xenitsupport.jira.com/browse/ALFREDAPI-268): Build fails always on Jenkins due to test bundle install



## 1.14.3 (2018-07-11)

### Added
* [ALFREDAPI-260](https://xenitsupport.jira.com/browse/ALFREDAPI-260): SetInheritParentPermission Webscript

### Changed
* [ALFREDAPI-272](https://xenitsupport.jira.com/browse/ALFREDAPI-272), [ALFREDAPI-271](https://xenitsupport.jira.com/browse/ALFREDAPI-271): Update Xenit gradle plugin
* [ALFREDAPI-262](https://xenitsupport.jira.com/browse/ALFREDAPI-262): Improve Jenkins build

### Fixed
* [ALFREDAPI-279](https://xenitsupport.jira.com/browse/ALFREDAPI-279): Missing dependency to apache StringUtils
* [ALFREDAPI-277](https://xenitsupport.jira.com/browse/ALFREDAPI-277): Corrected Activiti version for Alfresco 5.0 and 5.1
* [ALFREDAPI-267](https://xenitsupport.jira.com/browse/ALFREDAPI-267): getVersion always returns 1.11.2
* [ALFGDPR-61](https://xenitsupport.jira.com/browse/ALFGDPR-61): Requesting non existing properties should return null if the namespace of the qname is invalid
