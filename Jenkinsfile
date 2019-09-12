def sendEmailNotifications() {
    def color = 'purple'
    switch(currentBuild.currentResult) {
        case 'FAILURE': case 'UNSTABLE':
            color = 'red'
            break
        case 'SUCCESS':
            color = 'green'
            break
    }

    subject = "Jenkins job ${env.JOB_NAME} #${env.BUILD_NUMBER}: ${currentBuild.currentResult}"
    body = """<html><body>
        <p>
            <b>Job ${env.JOB_NAME} #${env.BUILD_NUMBER}: <span style="color: ${color};">${currentBuild.currentResult}</span></b>
        </p>
        <p>
            Check console output at <a href='${env.BUILD_URL}'>${env.JOB_NAME}</a>
        </p></body></html>"""

    emailext(
            subject: subject,
            body: body,
            mimeType: 'text/html',
            recipientProviders: [requestor(), culprits(), brokenBuildSuspects()]
    )
}

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
    sh "${gradleCommand} ${implProject}:test"

    // Integration tests
    sh "${gradleCommand} :apix-integrationtests:test-${version}:integrationTest"

    // Publishing
    if(publishingRepo) {
        withCredentials([
                usernamePassword(credentialsId: 'sonatype', passwordVariable: 'sonatypePassword', usernameVariable: 'sonatypeUsername'),
                string(credentialsId: 'gpgpassphrase', variable: 'gpgPassPhrase')]) {
            sh "${gradleCommand} ${implProject}:ampde " +
                    " ${implProject}:publishMavenJavaPublicationToMavenRepository " +
                    " ${implProject}:publishAmpPublicationToMavenRepository" +
                    " -Ppublish_username=${sonatypeUsername} " +
                    " -Ppublish_password=${sonatypePassword} " +
                    " -PkeyId=DF8285F0 " +
                    " -Ppassword=${gpgPassPhrase} " +
                    " -PsecretKeyRingFile=/var/jenkins_home/secring.gpg "
        }
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
            if(publishingRepo) {
                withCredentials([
                        usernamePassword(credentialsId: 'sonatype', passwordVariable: 'sonatypePassword', usernameVariable: 'sonatypeUsername'),
                        string(credentialsId: 'gpgpassphrase', variable: 'gpgPassPhrase')]) {
                    sh "${gradleCommand} :apix-interface:publish " +
                            " -Ppublish_username=${sonatypeUsername} " +
                            " -Ppublish_password=${sonatypePassword} " +
                            " -PkeyId=DF8285F0 " +
                            " -Ppassword=${gpgPassPhrase} " +
                            " -PsecretKeyRingFile=/var/jenkins_home/secring.gpg "
                }
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
        stage("Build 61") {
            BuildVersionX(publishingRepo, "61")
        }
    }
    finally {
        stage("Final cleanup") {
            junit '**/build/test-results/**/*.xml'
            sh "${gradleCommand} composeDownForced"
            sendEmailNotifications()
            cleanWs()
        }
    }
}
