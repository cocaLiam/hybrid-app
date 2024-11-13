# Hybrid-app

## 구성
![구성](구성도.png)

## 앱 배포 과정
![배포과정](배포과정.png)

### 1. 두 개의 분리된 앱 배포
- **필요 서버**: 2개
  - **FrontEnd**: `AWS S3`, Firebase Hosting 등
  - **BackEnd**: AWS EC2/EB, `Heroku` 등
- **특징**: FrontEnd와 BackEnd 간의 통신을 위해 `CORS 헤더`가 필요

___

### 구성

- NodeJS -- Express -- React
- MVC 패턴 (model–view–controller, MVC)
- mongoose (mongoDB), (cloud Server DB : Atlas)
  - [MongoDB Atlas](https://cloud.mongodb.com/v2#/org/66fcba7d069a4d43c73cf7af/projects)
- 구글 MAP_API 사용
  - [Google Cloud Console](https://console.cloud.google.com/apis/credentials?hl=ko&project=effective-brook-437306-h0)
- FrontEnd 서버
  - ***AWS s3***
    - [awsS3] https://eu-north-1.console.aws.amazon.com/console/home?region=eu-north-1#
- BackEnd 서버
  - ***heroku***
    - [heroku] https://dashboard.heroku.com/

___

#### 
# 프로젝트 배포 방법
#### 

___

#### 
# FrontEnd( JavaScript, React ) 배포판 빌드
#### 

### 환경변수 설명
  - `.env` <- Local 전용
  - `.env.production` <- 배포 전용

### FrontEnd code 배포판 빌드
```bash
# npm run build 시, .env.production 환경변수를 참조해 빌드한다.
$ npm run build
```

### Local Test ( with hosted backend )
```bash
$ npm install -g serve
$ serve -s build  # localhost:3000 으로 서버구성해서 build 디렉토리에 있는 코드 로컬실행
```

### Local Test ( with local backend )
```bash
$ npm start  # localhost:3000 으로 서버구성해서 app.js 실행 (build 디렉토리 X)
```

### 실행 에러 정리
```bash
# node -v 버전이 12 이하인 경우 최신버전 설치
$ curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.5/install.sh | bash
$ source ~/.bashrc
$ nvm --version
$ nvm install --lts
$ nvm use --lts
$ node -v
```

___


#### 
# FrontEnd server 업로드
#### 

### AWS S3 업로드
  - `https://eu-north-1.console.aws.amazon.com/s3/home?region=eu-north-1#` 
  - 해당링크에서 APP 전용 도메인 버킷에 업로드

___

#### 
# BackEnd( JavaScript, NodeJS, ExpressJS ) 배포판 빌드
#### 

### 환경변수 설명
  - `.nodemoon` [개발전용]
  - `heroku 로그인 -> Settings -> Config Vars -> Reveal Config Vars` [배포전용]

### BackEnd code 배포판 빌드
```bash
X 필요 없음
```

### Local Test
```bash
$ npm install -g serve
$ serve -s build  # localhost:5000 으로 서버구성해서 build 디렉토리에 있는 코드 로컬실행
```

### Local Test ( with local backend )
```bash
$ nodemon app.js  # localhost:5000 으로 서버구성해서 app.js 실행 (build 디렉토리 X)
```

### 실행 에러 정리
```bash
# nodemon 없으면 설치 필요
$ npm install -g nodemon
$ nodemon -v
```

___

#### 
# BackEnd server 업로드
#### 

### heroku 업로드
  - Automatic deploys from ***본인 깃 Branch*** are enabled  설정 후, 
  - git push 하면 자동(CI/CD) 업로딩 진행

___

#### 
# Android APP 배포
#### 

___

#### 
# IOS APP 배포
#### 

___