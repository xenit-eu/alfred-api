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

def BuildVersionX(version) {
    def gradleCommand = "./gradlew --info --stacktrace "
    sh "${gradleCommand} :apix-impl:apix-impl-${version}:test"
    sh "${gradleCommand} :apix-integrationtests:test-${version}:integrationTest"
}

node {
    def gradleCommand = "./gradlew --info --stacktrace "
    try {
        stage("Checkout") {
            checkout scm
            sh "./setup.sh"
        }
        stage("Build 50") {
            BuildVersionX( "50")
        }
        stage("Build 51") {
            BuildVersionX("51")
        }
        stage("Build 52") {
            BuildVersionX("52")
        }
        stage("Build 60") {
            BuildVersionX("60")
        }
        stage("Build 61") {
            BuildVersionX("61")
        }
        stage("Publishing") {
            def gitBranch = env.BRANCH_NAME
            if(gitBranch.startsWith("release") || gitBranch == "master") {
                withCredentials([
                        usernamePassword(credentialsId: 'sonatype', passwordVariable: 'sonatypePassword', usernameVariable: 'sonatypeUsername'),
                        string(credentialsId: 'gpgpassphrase', variable: 'gpgPassPhrase')]) {
                    sh "${gradleCommand} publish " +
                            " -Ppublish_username=${sonatypeUsername} " +
                            " -Ppublish_password=${sonatypePassword} " +
                            " -PkeyId=DF8285F0 " +
                            " -Ppassword=${gpgPassPhrase} " +
                            " -PsecretKeyRingFile=/var/jenkins_home/secring.gpg "
                }
            }
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
