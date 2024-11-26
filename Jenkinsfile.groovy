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
        stage('Generate Ansible Inventory') {
            steps {
                script {
                    def publicIps = sh(script: "terraform output -json | jq -r '.vm_public_ips.value[]'", returnStdout: true).trim()
                    writeFile file: 'hosts', text: """
[azure]
${publicIps.split('\n').collect { "${it} ansible_user=azureadmin ansible_ssh_private_key_file=/path/to/private/key" }.join('\n')}
"""
                }
                sh 'cat hosts' // Вывод файла hosts для проверки
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
