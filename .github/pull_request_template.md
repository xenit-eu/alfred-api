Fixes https://xenitsupport.jira.com/browse/ALFREDAPI-<**YOUR TICKET ID**>

- [ ] Is [CHANGELOG.md](https://github.com/xenit-eu/alfred-api/blob/master/CHANGELOG.md) extended?
- [ ] Does this PR avoid breaking the API? 
    Breaking changes include adding, changing or removing endpoints and/or JSON objects used in requests and responses.
- [ ] Are all Alfresco services wired via ServiceRegistry (and not per service)?
- [ ] Does the PR comply to REST HTTP result codes policy outlined in the [user guide](https://docs.xenit.eu/alfred-api/user/rest-api/index.html#rest-http-result-codes)?
- [ ] Is error handling done through a method annotated with `@ExceptionHandler` in the webscript classes?
- [ ] Does the PR follow our [coding styleguide and other active procedures](https://xenitsupport.jira.com/wiki/spaces/XEN/pages/624558081/XeniT+Enhancement+Proposals+XEP)?
- [ ] Is usage of `this.` prefix avoided?

See [README.md](./README.md) for full explanation.
