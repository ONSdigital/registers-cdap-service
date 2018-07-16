#!groovy
import groovy.json.JsonSlurper

def jsonMaker() {
        // stage('Get CDAP Token') {
            def response = httpRequest 'http://username:password@localhost:10009/token'
            def jsonResponce = response.content
            def json = new JsonSlurper().parseText(jsonResponce)
            String authHeader = "Authorization: Bearer " + json.access_token
            authHeader
        // }
    }

pipeline {
    agent any
    environment {
        CDAP_ADDRESS = credentials('cdap-address')
    }
        stages {
            stage('Build') {
                steps {
                    sh 'mvn clean compile'
                }
            }
            stage('Unit Test') {
                steps {
                    sh 'mvn test'
                    }
            }
            stage('Package') {
                steps {
                    sh 'mvn package'
                    }
            }
            stage('Deploy') {
                environment {
                    AUTH_HEADER = jsonMaker()
                    }
                steps {
                    // //Stop all services then delete Application
                    sh "curl -X POST --data-binary @CDAPJson/allServices.json $CDAP_ADDRESS/v3/namespaces/default/stop --header '$AUTH_HEADER'"
                    sh "curl -X DELETE $CDAP_ADDRESS/v3/namespaces/default/apps/sic07_lookup --header '$AUTH_HEADER'"
                    // //Deploy new Jar to CDAP
                    sh "curl -X POST --header 'X-Archive-Name: registers-cdap-service-0.0.1-SNAPSHOT.jar' --data-binary @target/registers-cdap-service-0.0.1-SNAPSHOT.jar $CDAP_ADDRESS/v3/namespaces/default/apps --header '$AUTH_HEADER'"
                    // //Start all services
                    sh "curl -X POST --data-binary @CDAPJson/allServices.json $CDAP_ADDRESS/v3/namespaces/default/start --header '$AUTH_HEADER'"
                    sh "curl -v $CDAP_ADDRESS/v3/namespaces/default/apps/sic07_lookup/services/CompanyHouseService/methods/CH/bussinesnumber/11240759~2018-06 --header '$AUTH_HEADER'"
                }
            }
        }
    post {
        always {
            junit '**/target/*-reports/*.xml'
            deleteDir()
        }
    }
}




