pipeline {
    
    environment {
        repository = "jongmin0821/sidproject"
        DOCKERHUB_CREDENTIALS = credentials('sidDocker')
        APP_PORT = "8082"
    }

    agent any 

    stages {
        
        stage('Test Docker Access') {
                steps {
                    script {
                        // Docker 버전 확인 (권한 테스트)
                        sh 'docker --version'
                    }
                }
            }
    stage('File Tree') {
        steps {
            script {
                sh 'ls -la'
            }
        }
    }
    
    stage('Git Clone') {
            steps {
                git branch: 'dev', credentialsId: 'Prosid', url: 'https://lab.ssafy.com/s12-webmobile2-sub1/S12P11C110'
            }
        }



    stage('Verify Repo Structure') {
            steps {
                // 최상위 폴더의 파일 목록 출력
                sh 'ls -la'  // 이 줄 추가
            }
        }


        stage('Create .env') {
            steps {
                withCredentials([
                    string(credentialsId: 'sid-spring.mail.username', variable: 'MAIL_USERNAME'),
                    string(credentialsId: 'sid-spring.mail.password', variable: 'MAIL_PASSWORD'),
                    string(credentialsId: 'back-image', variable: 'IMAGE_BACK')
                
                ]) {
                    sh '''
                    echo "MAIL_ADDRESS=$MAIL_USERNAME" > Back/SID/src/main/resources/.env
                    echo "APP_PASSWORD=$MAIL_PASSWORD" >> Back/SID/src/main/resources/.env
                    echo "IMAGE_SRC=$IMAGE_BACK" >> Back/SID/src/main/resources/.env

			
                    echo ".env 파일 생성 후 내용 확인:"
                    ls -la Back/SID/src/main/resources/.env
                    cat Back/SID/src/main/resources/.env  # 파일 내용을 출력
                    '''
                }
            }
        }

        stage('Create .env for React') {
                steps {
                    withCredentials([string(credentialsId: 'sid-react.env', variable: 'REACT_ENV')]) {
                        sh '''
                        echo "$REACT_ENV" > Front/SID/.env
                        cat Front/SID/.env

                        echo ".env 파일 생성 후 내용 확인:"
                        ls -la Front/SID/.env
                        cat Front/SID/.env  # 파일 내용을 출력

                        '''
                    }
                }
            }

	 stage('Create .env for Curate') {
    steps {
        withCredentials([string(credentialsId: 'curating_credit', variable: 'CURATING')]) {
            sh '''
            # 세미콜론으로 구분된 값 처리
            CURATING="$CURATING"
            IFS=';' 
            for var in $CURATING; do
                echo "$var" >> Curating/.env
            done

            # 생성된 .env 파일 내용 확인
            echo ".env 파일 생성 후 내용 확인:"
            ls -la Curating/.env
            cat Curating/.env  # 파일 내용을 출력
            '''
        }
    }
}
        
        
        stage('Compile') {
            steps {
                dir('Back/SID') {
                    sh 'chmod +x ./gradlew'
                    sh './gradlew clean bootJar'
                }
            }
        }

        
        stage('Spring Build Image') {
            steps {
                dir('Back/SID') {
                    sh 'docker build -t $repository:spring-latest .'
                }
            }
        }
        stage('React Build Image') {
            steps {
                dir('Front/SID') {
                    sh 'docker build -t $repository:react-latest .'
                }
            }
        }
        stage('Python Build Image') {
            steps {
                dir('Curating') {
                    sh 'docker build -t $repository:curating-latest .'
                }
            }
        }


				
        
        stage('Docker Login') {
            steps {
                script {
                    // Docker Hub 로그인
                    sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
                }
            }
        }
        
        stage('Spring Image Push') {
            steps {
                 script {
                    // Spring 이미지를 Docker Hub에 푸시
                    sh 'docker push $repository:spring-latest'
                }
            }
        }

        stage('React Image Push') {
            steps {
                 script {
                    // Spring 이미지를 Docker Hub에 푸시
                    sh 'docker push $repository:react-latest'
                }
            }
        }
        
         stage('Python Image Push') {
            steps {
                 script {
                    // Spring 이미지를 Docker Hub에 푸시
                    sh 'docker push $repository:curating-latest'
                }
            }
        }

        stage('Docker Run') {
    steps {
        script {
            sh '''
            # 기존 컨테이너 확인 및 제거
            if [ $(docker ps -aq -f name=sid-spring) ]; then
              echo "Stopping and removing existing Spring container"
              docker stop sid-spring
              docker rm sid-spring
            fi

            if [ $(docker ps -aq -f name=sid-react) ]; then
              echo "Stopping and removing existing React container"
              docker stop sid-react
              docker rm sid-react
            fi
            
             if [ $(docker ps -aq -f name=sid-python) ]; then
              echo "Stopping and removing existing Python container"
              docker stop sid-python
              docker rm sid-python
            fi

            # 포트 사용 여부 확인 (Spring)
            if [ $(netstat -tuln | grep :${APP_PORT}) ]; then
              echo "Port ${APP_PORT} is in use. Attempting to free it."
              fuser -k ${APP_PORT}/tcp
            fi

            # 포트 사용 여부 확인 (React)
            if [ $(netstat -tuln | grep :8084) ]; then
              echo "Port 8084 is in use. Attempting to free it."
              fuser -k 8084/tcp
            fi
            
            if [ $(netstat -tuln | grep :8221) ]; then
              echo "Port 8221 is in use. Attempting to free it."
              fuser -k 8221/tcp
            fi

            # Spring 새 컨테이너 실행
            echo "Starting new Spring container on port ${APP_PORT}"
            docker run -d --name sid-spring -p ${APP_PORT}:8080 ${repository}:spring-latest

            # React 새 컨테이너 실행
            echo "Starting new React container on port 8084"
            docker run -d --name sid-react -p 8084:3000 ${repository}:react-latest
            
            # Python 새 컨테이너 실행
            echo "Starting new Python container on port 8221"
            docker run -d --name sid-python -p 8221:8221 ${repository}:curating-latest
            '''
        }
    }
}


	

        stage('Test') { 
            steps {
                echo 'Starting Test Stage...'
                echo 'Running tests...'
                echo 'Test Stage Completed!'
            }
        }

        stage('Deploy') { 
            steps {
                echo 'Starting Deploy Stage...'
                echo 'Deploying the application...'
                echo 'Deploy Stage Completed!'
            }
        }
    }

    post {
        always {
            echo 'Pipeline Execution Completed!'
        }
        success {
            echo 'Pipeline Succeeded!'
        }
        failure {
            echo 'Pipeline Failed!'
        }
    }
}
