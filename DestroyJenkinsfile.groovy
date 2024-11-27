pipeline {
    agent any

    environment {
        TF_WORKSPACE = 'Deploy_Environment'
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo 'Cloning repository into Deploy_Environment directory...'
                dir('Deploy_Environment') {
                    git branch: 'main', url: 'https://github.com/Dmitriy-Didyk/DevopsHW_10'
                }
            }
        }

        stage('Initialize Terraform') {
            steps {
                echo 'Initializing Terraform...'
                dir('Deploy_Environment') {
                    sh '''
                    terraform init
                    '''
                }
            }
        }

        stage('Destroy Environment') {
            steps {
                echo 'Destroying environment with Terraform...'
                dir('Deploy_Environment') {
                    sh '''
                    terraform destroy -auto-approve
                    '''
                }
            }
        }

        stage('Cleanup') {
            steps {
                echo 'Cleanup completed.'
            }
        }
    }
}
