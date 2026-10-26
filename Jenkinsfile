pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'skini5001/calculator_devops'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .'
                sh 'docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${DOCKER_IMAGE}:latest'
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKERHUB_USER',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USER" --password-stdin
                        docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                        docker push ${DOCKER_IMAGE}:latest
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    IMAGE_TAG=$BUILD_NUMBER docker compose pull calculator
                    IMAGE_TAG=$BUILD_NUMBER docker compose up -d --no-build
                '''
            }
        }

        stage('Verify Health') {
            steps {
                sh '''
                    for i in $(seq 1 12); do
                        if curl -fsS http://localhost:8082/actuator/health; then
                            echo "Deployment healthy"
                            exit 0
                        fi
                        sleep 5
                    done

                    echo "Deployment health check failed"
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            echo "Testing pipeline email to: YOUR_EMAIL@gmail.com"
            emailext(
                to: 'YOUR_EMAIL@gmail.com',
                subject: "Jenkins Pipeline Test - Build ${env.BUILD_NUMBER}",
                body: "This is a test email from Jenkins build ${env.BUILD_NUMBER}.\nBuild URL: ${env.BUILD_URL}",
                mimeType: 'text/plain',
                attachLog: true
            )
        }
        failure {
            echo 'CI/CD pipeline failed. Check the stage logs.'
            emailext(
                subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Build failed. Check the console output: ${env.BUILD_URL}",
                to: 'siddharthkini24@gmail.com'
            )
        }
}
}