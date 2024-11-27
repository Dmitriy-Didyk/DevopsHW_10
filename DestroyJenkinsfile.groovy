pipeline {
    agent any

    environment {
        TF_WORKSPACE = 'Deploy_Environment'
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo 'Cloning repository...'
                git branch: 'main', url: 'https://github.com/Dmitriy-Didyk/DevopsHW_10'
            }
        }

        stage('Switch to Deploy Environment') {
            steps {
                dir('Deploy_Environment') {
                    echo 'Switched to Deploy directory'
                }
            }
        }

        stage('Initialize Terraform') {
            steps {
                echo 'Initializing Terraform...'
                dir('Destroy_Environment') {
                    sh '''
                    terraform init
                    '''
                }
            }
        }

        stage('Destroy Environment') {
            steps {
                echo 'Destroying environment with Terraform...'
                dir('Destroy_Environment') {
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
