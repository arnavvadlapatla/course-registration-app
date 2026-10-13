pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                  git branch: 'main', url: 'https://github.com/arnavvadlapatla/course-registration-app.git'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn clean test'
            }
            post {
                always {
                    junit testResults: 'course-registration-app/target/surefire-reports/TEST-*.xml',
                          allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package -DskipTests'
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'course-registration-app/target/*.jar',
                                 fingerprint: true
            }
        }
    }
}