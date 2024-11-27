pipeline {
    agent any

    environment {
        TF_WORKSPACE = 'Deploy_Environment'
    }

    stage('Switch to Destroy Directory') {
    steps {
        dir('Destroy_Environment') {
            echo 'Switched to Destroy directory'
        }
    }
}
    
    stages {
        stage('Checkout Code') {
            steps {
                echo 'Cloning repository...'
                git branch: 'main', url: 'https://github.com/Dmitriy-Didyk/DevopsHW_10'
            }
        }

        stage('Initialize Terraform') {
            steps {
                echo 'Initializing Terraform...'
                sh '''
                terraform init
                '''
            }
        }

        stage('Destroy Environment') {
            steps {
                echo 'Destroying environment with Terraform...'
                sh '''
                terraform destroy -auto-approve
                '''
            }
        }

        stage('Cleanup') {
            steps {
                echo 'Cleanup completed.'
            }
        }
    }
}
