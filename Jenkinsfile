def getPublishingRepository() {
    def gitBranch = env.BRANCH_NAME // https://issues.jenkins-ci.org/browse/JENKINS-30252
    if(gitBranch.startsWith("release")) {
        return 'Release'
    }
    if(gitBranch == "master") {
        return 'Snapshot'
    }
    return null
}

def BuildVersionX(publishingRepo, version) {
    def gradleCommand = "./gradlew --info --stacktrace "
    def implProject = ":apix-impl:apix-impl-${version}"

    // Unit tests
    echo 'WARNING'
    echo 'Unit tests temporarily disabled for Alfresco 6.0 support integration!'
    echo 'Enable as soon possible!'
    //sh "${gradleCommand} ${implProject}:test"

    // Integration tests
    sh "${gradleCommand} :apix-integrationtests:test-${version}:integrationTest"

    // Publishing
    if (publishingRepo) {
        sh "${gradleCommand} ${implProject}:ampde ${implProject}:publishMavenJavaPublicationTo${publishingRepo}Repository"
        archiveArtifacts artifacts: '**/build/libs/**/*.jar', excludes: null

        sh "${gradleCommand} ${implProject}:ampde ${implProject}:publishAmpPublicationTo${publishingRepo}Repository"
        archiveArtifacts artifacts: '**/build/distributions/*.amp', excludes: null
    }
}

node {
    def gradleCommand = "./gradlew --info --stacktrace "
    def publishingRepo = getPublishingRepository()

    try {
        stage("Checkout + Initialize") {
            checkout scm
            sh "./setup.sh"
        }
        stage("Publish apix-interface") {
            if (publishingRepo) {
                sh "${gradleCommand} :apix-interface:publishMavenJavaPublicationTo${publishingRepo}Repository"
            }
        }
        stage("Build 50") {
            BuildVersionX(publishingRepo, "50")
        }
        stage("Build 51") {
            BuildVersionX(publishingRepo, "51")
        }
        stage("Build 52") {
            BuildVersionX(publishingRepo, "52")
        }
        stage("Build 60") {
            BuildVersionX(publishingRepo, "60")
        }
    }
    finally {
        stage("Final cleanup") {
            junit '**/build/test-results/**/*.xml'
            sh "${gradleCommand} composeDownForced"
            cleanWs()
        }
    }
}
