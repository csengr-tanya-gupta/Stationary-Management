pipeline {
agent any

```
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
                echo 'Installing frontend dependencies...'
                sh 'npm ci'

                echo 'Building React frontend...'
                sh 'npm run build'
            }
        }
    }

    stage('Deploy') {
        steps {
            echo 'Stopping old containers...'
            sh 'docker compose down --remove-orphans'

            echo 'Building and starting containers...'
            sh 'docker compose up -d --build'

            echo 'Checking running containers...'
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
```

}
