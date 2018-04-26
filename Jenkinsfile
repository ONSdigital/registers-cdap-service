pipeline {
    agent any
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
                sh 'mvn test'
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

