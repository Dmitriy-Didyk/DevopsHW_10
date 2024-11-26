pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo 'Cloning repository...'
                git branch: 'main', url: 'https://github.com/Dmitriy-Didyk/DevopsHW_10'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying environment using Terraform...'
                sh '''
                terraform init
                terraform apply -auto-approve
                '''
            }
        }
        stage('Configure') {
            steps {
                echo 'Configuring environment using Ansible...'
                sh '''
                ansible-playbook -i hosts install_docker.yml
                ansible-playbook -i hosts install_nginx.yml
                '''
            }
        }
    }
}
