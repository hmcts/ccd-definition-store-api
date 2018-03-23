#!groovy
@Library("Reform")
import uk.gov.hmcts.Ansible
import uk.gov.hmcts.Packager
import uk.gov.hmcts.RPMTagger

packager = new Packager(this, 'ccdata');
ansible = new Ansible(this, 'ccdata');
server = Artifactory.server 'artifactory.reform'
buildInfo = Artifactory.newBuildInfo()

properties(
    [[$class: 'GithubProjectProperty', displayName: 'Case Definition Store API', projectUrlStr: 'https://github.com/hmcts/ccd-definition-store-api'],
     pipelineTriggers([[$class: 'GitHubPushTrigger']]),
   [$class: 'BuildDiscarderProperty', strategy: [$class: 'LogRotator', artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '7', numToKeepStr: '10']]
]
)

milestone()
lock(resource: "case-definition-store-app-${env.BRANCH_NAME}", inversePrecedence: true) {
    node {
        try {
            timeout(time: 35, unit: 'MINUTES') {

                stage('Checkout') {
                    deleteDir()
                    checkout scm
                }

                stage('Build') {
                    sh "./gradlew clean build sonar -Dsonar.host.url=https://sonar.reform.hmcts.net/ " +
                        " -Dsonar.projectName=\"CCD :: Case Definition Store API\" "}

                onMaster {
                    publishAndDeploy('master', 'test')
                }

                onDevelop {
                    publishAndDeploy('develop', 'dev')
                }

                milestone()
            }
        } catch (err) {
            notifyBuildFailure channel: '#ccd-notifications'
            throw err
        } finally {
            junit '**/build/test-results/**/*.xml'
        }
    }
}

def publishAndDeploy(branch, env) {
    def rpmVersion
    def version
    // Temporary port offset avoiding collision till Dev and Test environments are fully separated by DevOps
    def backendPort = (env == 'test') ? '4481' : '4451'

    stage('Publish JAR') {
        server.publishBuildInfo buildInfo
    }

    stage('Publish RPM') {
        rpmVersion = packager.javaRPM(branch,
            'definition-store-application',
            'application/build/libs/case-definition-store-api-$(./gradlew -q projectVersion).jar',
            'springboot',
            'application/src/main/resources/application.properties')
        packager.publishJavaRPM('definition-store-application')
    }

    stage('Package (Docker)') {
        definitionStoreVersion = dockerImage imageName: 'ccd/ccd-definition-store-api', tags: [branch]
        definitionStoreDatabaseVersion = dockerImage imageName: 'ccd/ccd-definition-store-database', context: 'docker/database', tags: [branch]
    }

    def rpmTagger = new RPMTagger(
        this,
        'definition-store-application',
        packager.rpmName('definition-store-application', rpmVersion),
        'ccdata-local'
    )

    stage('Deploy: ' + env) {
        version = "{ccd_definition_api_version: ${rpmVersion}}"
        ansible.runDeployPlaybook(version, env, branch)
        rpmTagger.tagDeploymentSuccessfulOn(env)
    }

    stage('Smoke Tests: ' + env) {
        sh "curl -vf https://case-definition-app." + env + ".ccd.reform.hmcts.net:" + backendPort + "/status/health"
        rpmTagger.tagTestingPassedOn(env)
    }
}
