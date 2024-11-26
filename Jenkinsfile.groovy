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
                    // Получение вывода Terraform
                    def terraformOutput = sh(script: "terraform output -json", returnStdout: true).trim()
                    
                    // Извлечение IP-адресов
                    def publicIps = sh(script: "echo '${terraformOutput}' | jq -r '.vm_public_ips.value[] // empty'", returnStdout: true).trim()
                    
                    // Проверка наличия IP-адресов
                    if (publicIps) {
                        // Генерация файла hosts
                        writeFile file: 'hosts', text: """
                        [azure]
                        ${publicIps.split('\n').collect { "${it} ansible_user=azureadmin ansible_ssh_pass='ВашПароль'" }.join('\n')}
                        """
                        echo "Ansible inventory generated successfully."
                    } else {
                        error("No public IPs found in Terraform output.")
                    }
                }
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
