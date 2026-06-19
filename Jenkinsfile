pipeline {
    agent any

    options {
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Pulling latest code from GitHub...'
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                echo 'Building all Spring Boot services...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Run Tests') {
            steps {
                echo 'Running unit tests...'
                sh 'mvn test'
            }
        }

        stage('Build Frontend') {
    steps {
        dir('frontend') {
            sh '''
            export PATH=/home/tanya/.nvm/versions/node/v22.15.0/bin:$PATH

            node -v
            npm -v

            npm ci
            npm run build
            '''
        }
    }
}

        stage('Deploy') {
            steps {
                sh 'docker compose down --remove-orphans'
                sh 'docker compose up -d --build'
                sh 'docker compose ps'
            }
        }
    }

    post {
        success {
            echo 'Stationery Management System deployed successfully.'
        }

        failure {
            echo 'Deployment failed. Check Jenkins logs.'
        }
    }
}