This directory offers an Alfred API stack using Elasticsearch instead of Solr.

It is only intended to serve as documentation, or a future starting point. As such, it is not part of the Gradle build.

If you want to use it, the simplest way is the use this directory instead of the standard `234` directory.
You will also need to modify to build to have access to quai.io for Alfresco's indexing images:

```
    - name: Log in to Alfresco Quay
    uses: docker/login-action@v3
    with:
      registry: quay.io
      username: ${{ secrets.QUAY_IO_USERNAME }}
      password: ${{ secrets.QUAY_IO_PASSWORD }}
```
