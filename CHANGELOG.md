# Alfred API - Changelog

## 2.5.1 UNRELEASED

### Fixed

* [ALFREDAPI-438](https://xenitsupport.jira.com/browse/ALFREDAPI-438): Replace hasReadPermission with hasPermission to avoid an Alfresco bug that prevents usage of that method and throws AcessDeniedException

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

### Deleted



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
