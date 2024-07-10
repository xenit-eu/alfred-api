# Documentation Alfred API

**Process is automatically executed by GHA on release"

## Generate website

This directory generates the product documentation website at https://docs.xenit.eu/alfred-api/ 

This is done by manually executing:
```bash
./build-website.sh
```

This will generate a ZIP containing the documentation website, which still needs to be
uploaded and unzipped on our host.

## Publishing

To upload to docs.xenit.eu, you first need to have key-based SSH access to the `xeniteu` account.
Example to upload the website tarball:
```bash
scp -i ~/.ssh/a2hosting-key -P 7822 build/website-alfred-api_2024-02-21.tar.gz xeniteu@nl1-ts102.a2hosting.com:~/docs.xenit.eu/
```

After that login via SSH to unpack the website. The folder hosting the Alfred API docs is `~/docs.xenit.eu/alfred-api`.
Example to unpack:
```bash
ssh -i ~/.ssh/a2hosting-key -p 7822 xeniteu@nl1-ts102.a2hosting.com
$ cd docs.xenit.eu
$ mv alfred-api alfred-api_backup2024-02-21
$ tar xvzf website-alfred-api_2024-02-21.tar.gz
```

