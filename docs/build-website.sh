#!/bin/bash
set -e
scriptPath="$( dirname "${BASH_SOURCE[0]}" )" # cd to the directory containing the script
cd "$scriptPath"

MARKDOWNTOPDF_VERSION=v1.1.4
MARKDOWNTOWEBSITE_VERSION=1.0.5

MARKDOWNTOPDF_IMAGE="private.docker.xenit.eu/xenit-markdowntopdf:$MARKDOWNTOPDF_VERSION"
MARKDOWN_SPLITTER_IMAGE="private.docker.xenit.eu/customer/xenit/xenit-manuals-markdown-splitter:$MARKDOWNTOWEBSITE_VERSION"
MANUALS_HUGO_GENERATOR_IMAGE="private.docker.xenit.eu/customer/xenit/xenit-manuals-hugo-generator:$MARKDOWNTOWEBSITE_VERSION"

WEIGHT=0

build_manual() {
    echo "===== Building manual ====="
    local productName="$1"
    local versionName="$2"
    shift 2;

    mkdir -p "build/normalized/$productName"
    tar c --portability -C "docs/$productName/$versionName" . | \
    docker run --rm -i $MARKDOWNTOPDF_IMAGE --tar \
    --template default -t markdown-simple_tables-multiline_tables-grid_tables --extract-media assets \
    --resource-path . \
    "$@" \
    -o normalized.md > "build/normalized/$productName/$versionName.tar"
    sync
}

split_manual() {
    echo "===== Splitting manual ====="
    local productName="$1"
    local versionName="$2"
    WEIGHT=$[$WEIGHT + 1]
    mkdir -p "build/product/$productName"
    tar tf "build/normalized/$productName/$versionName.tar"
    cat "build/normalized/$productName/$versionName.tar" | docker run --rm -i $MARKDOWN_SPLITTER_IMAGE normalized.md "target-path=$versionName" "weight=$WEIGHT" > "build/normalized/$productName/$versionName-out.tar"
    sync
    tar xf "build/normalized/$productName/$versionName-out.tar" -C "build/product/$productName"
}

build_and_split_manual() {
    build_manual "$@"
    split_manual "$1" "$2"
}

build_product_website() {
    echo "===== Building website ====="
    local productName="$1"
    mkdir -p "build/website/$productName"
    cp -r "docs/$productName/_hugo" "build/product/$productName/_hugo"
    tar c --portability -C "build/product/$productName" . | \
        docker run --rm -i $MANUALS_HUGO_GENERATOR_IMAGE | \
        tar x -C "build/website/$productName"
    sync
}

build_javadoc() {
    echo "===== Generating javadoc ====="
    local productName="$1"
    local alfredapidir=".."

    pushd "$alfredapidir"
    ./gradlew clean :apix-interface:javadoc
    popd

    local outputdir="build/website/$productName/"
    mkdir -p "$outputdir"
    cp -a "$alfredapidir/apix-interface/build/docs/javadoc" $outputdir
}

build_swaggerdoc() {
    echo "===== Copying swaggerdoc ====="
    local productName="$1"
    local swaggerdir="docs/$productName/swagger-ui"
    local outputdir="build/website/$productName/"

    mkdir -p $outputdir
    cp -a $swaggerdir $outputdir
}

rm -rf build/
build_and_split_manual alfred-api user "user-guide.md"
build_product_website alfred-api
build_javadoc alfred-api
build_swaggerdoc alfred-api

find build/website -type f -name '*.html' -print0 | xargs -0 sed -i "/^<\!DOCTYPE html>$/a\
\<\!-- alfred-docs@$(git describe --always --dirty) --\>"
tar czf build/website-alfred-api.tar.gz -C build/website .
