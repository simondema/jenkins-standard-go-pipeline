def call(String goToolName = 'go-1.12', String goLangCiVersion = 'v1.57.2') {
    pipeline {
    agent any
    environment {
        GO111MODULES = 'on'
    }
    tools {
        go "$goToolName"
    }
    stages {
        stage('Build') {
            steps {
                echo 'Building.'
                sh 'go build'
            }
        }
        stage('Test') {
            environment {
                CODECOV_TOKEN = credentials('CODECOV_TOKEN')
            }
            steps {
                sh 'go test ./... -coverprofile=coverage.txt'
                sh 'curl -s https://codecov.io/bash | bash -s -'
            }
        }
        stage('Code Analysis') {
            steps {
                sh "curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b $(go env GOPATH)/bin ${goLangCiVersion}"
                sh '$(go env GOPATH)/bin/golangci-lint --version'
                sh '$(go env GOPATH)/bin/golangci-lint run'
            }
        }
        stage('Release') {
            when {
                buildingTag()
            }
            environment {
                GITHUB_TOKEN = credentials('GITHUB_TOKEN')
            }
            steps {
                sh 'curl -sL https://git.io/goreleaser | bash'
            }
        }
    }
}
}