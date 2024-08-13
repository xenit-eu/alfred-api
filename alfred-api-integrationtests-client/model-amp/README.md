## Integration tests AMP ##

This sub project builds an AMP containing the model and messages used in integration tests.
The AMP is then added as part of the docker image used for running the tests.

### Rationale ###

This happens via an AMP and not a Bundle because when installed via a bundle,
the Docker health check and registration of the bundle's messages
keep fighting over the Alfresco dictionary lock, with unwanted timeouts as a consequence.
