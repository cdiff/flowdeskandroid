<h1 align="center">
  <br>
  <img src="https://img.shields.io/badge/Flowdesk-0F172A?style=for-the-badge&logo=android&logoColor=38BDF8" alt="Flowdesk Android Logo" width="200">
  <br>
  <b>Flowdesk Android</b>
  <br>
</h1>

<p align="center">
  <strong>B2B SaaS형 CRM & 맞춤형 업무 공간(Custom Workspace) 구축 플랫폼</strong>
  <br>
  <em>Clean Architecture • MVVM • Jetpack Components • Kotlin Coroutines & Flow</em>
</p>

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"></a>
  <a href="https://developer.android.com/"><img src="https://img.shields.io/badge/Android-SDK%2024+-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android SDK"></a>
  <a href="https://developer.android.com/jetpack"><img src="https://img.shields.io/badge/Jetpack-MVVM-4285F4?style=flat-square&logo=google&logoColor=white" alt="Jetpack"></a>
  <a href="https://github.com/square/retrofit"><img src="https://img.shields.io/badge/Networking-Retrofit2-000000?style=flat-square" alt="Retrofit2"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square" alt="License"></a>
</p>

---

## 📌 목차 (Table of Contents)
- [✨ 프로젝트 소개 (Overview)](#-프로젝트-소개-overview)
- [🛠️ 기술 스택 (Tech Stack)](#️-기술-스택-tech-stack)
- [🏛️ 아키텍처 구조 (Architecture)](#️-아키텍처-구조-architecture)
- [✨ 주요 기능 (Key Features)](#-주요-기능-key-features)
- [🎨 UI/UX 디자인 시스템 수칙](#-uiux-디자인-시스템-수칙)
- [🚀 시작하기 (Quick Start)](#-시작하기-quick-start)
- [📖 문서 및 온보딩 (Documentation)](#-문서-및-온보딩-documentation)

---

## ✨ 프로젝트 소개 (Overview)

**Flowdesk Android**는 기업 맞춤형 B2B SaaS CRM 모바일 플랫폼입니다.  
기업 테넌트 환경에 맞춘 멀티 테넌시 상태 관리, 팀/역할 기반 유저 초대 및 권한 제어, 시스템 게시판 및 업무 공간 커스텀 설정을 제공합니다.

### 🌟 핵심 가치
* **맞춤형 모바일 업무 환경**: 유연한 역할(Role) 및 팀(Team) 권한 관리 시스템
* **안전한 인증 & 세션**: AuthInterceptor 및 TokenAuthenticator를 통한 자동 토큰 갱신 및 보안 처리
* **일관된 UI/UX 톤앤매너**: 가이드라인(`AGENTS.md`)에 정의된 모던 플랫 인터페이스 및 밑줄 폼 스타일

---

## 🛠️ 기술 스택 (Tech Stack)

| 영역 | 사용 기술 / 라이브러리 |
|---|---|
| **Language** | Kotlin 1.9+ |
| **Architecture** | Clean Architecture + MVVM (Model-View-ViewModel) |
| **UI Components** | Android Jetpack (Fragment, ViewBinding, Navigation Component) |
| **Asynchronous** | Kotlin Coroutines, StateFlow, LiveData |
| **Dependency Injection** | Hilt / Koin / Network Modules |
| **Network & Storage** | Retrofit2, OkHttp3, EncryptedSharedPreferences (`SessionManager`) |
| **Tooling & Analysis** | Understand Anything (`.ua`), Antigravity Agentic Framework |

---

## 🏛️ 아키텍처 구조 (Architecture)

프로젝트는 관심사 분리(Separation of Concerns)를 위해 **3계층 Clean Architecture** 레이어로 구성되어 있습니다.

```mermaid
graph TD
    subgraph Presentation Layer
        UI[Fragment / Custom View] --> VM[ViewModel & StateFlow]
    end

    subgraph Domain Layer
        VM --> UC[UseCase]
        UC --> DM[Domain Model & Repository Interface]
    end

    subgraph Data Layer
        UC -.-> REPO[Repository Impl]
        REPO --> API[Retrofit Remote API]
        REPO --> DB[SessionManager / Local Storage]
    end
```

### 레이어 구분 (`.ua/knowledge-graph.json`)
1. **`Presentation Layer`**: 화면 UI, ViewBinding, 사용자 이벤트 수신, ViewModel 상태 관리
2. **`Domain Layer`**: 순수 비즈니스 유스케이스 (`UpdateRolePermissionsUseCase` 등), 도메인 엔티티
3. **`Data Layer`**: `UserRepositoryImpl`, API 데이터 전송 객체(DTO), `TokenManager` 및 보안 세션

---

## ✨ 주요 기능 (Key Features)

### 🔐 1. 인증 및 세션 보안 (Auth & Session Security)
- 로그인 / 회원가입 및 토큰 기반 인증
- HTTP 401 오류 발생 시 `TokenAuthenticator`를 통한 백그라운드 자동 토큰 재발급

### 👥 2. 회원 및 권한 관리 (User & Role Management)
- 팀/역할 선택 및 유저 초대 기능 (`InviteRoleFragment`, `InviteTeamViewModel`)
- 커스텀 권한 매핑 및 복사 (`CopyRolePermissionsUseCase`, `ManagePermissionsViewModel`)

### ⚙️ 3. 테넌트 & 시스템 관리 (System & Workspace Customization)
- 테넌트 활성/비활성 상태 편집 (`fragment_status_edit.xml`)
- 게시판 유형 관리 (`BoardTypeListViewModel`) 및 커스텀 업무 공간 빌딩

---

## 🎨 UI/UX 디자인 시스템 수칙

본 프로젝트는 [`AGENTS.md`](.agents/AGENTS.md) 표준 가이드라인에 맞춰 제작됩니다.

1. 📱 **WindowInsets 카메라 노치 영역 보호**
   * 상단 시스템 바 패딩을 동적으로 확보하여 서브 화면의 레이아웃 가림 방지
2. 🚫 **서브 화면 하단 네비게이션 바 자동 숨김**
   * 상세/수정/초대 서브 페이지 진입 시 하단 바 비노출 처리로 몰입감 향상
3. 📝 **깔끔한 밑줄 스타일(Underline Input Form)**
   * 투명 배경(`@null`)과 1dp 하단 구분선, 필수 입력 항목 우측 빨간 별표(`*`) 표기
4. ⏳ **로딩 가시성 보장 (Loading State)**
   * API 로딩 중 뼈대 박스 노출을 지양하고 `View.INVISIBLE` 상태에서 ProgressBar만 표출하여 화면 깜빡임 차단

---

## 🚀 시작하기 (Quick Start)

### 사전 요구 사항
* Android Studio Jellyfish (2024.1.1) 이상
* JDK 17 이상
* Android SDK 24 (Android 7.0) 이상

### 빌드 및 실행 방법
```bash
# 레포지토리 클론
git clone https://github.com/pasic/flowdeskandroid.git
cd flowdeskandroid

# Gradle 빌드
./gradlew assembleDebug
```

---

## 📖 문서 및 온보딩 (Documentation)

* 📖 **[신규 개발자 온보딩 가이드](docs/ONBOARDING.md)**: 전체 아키텍처 및 추천 학습 경로
* 🎨 **[프로젝트 개발 규칙 (AGENTS.md)](.agents/AGENTS.md)**: UI/UX 디자인 규격 및 상태 관리 표준
* 🗺️ **[코드베이스 지식 그래프 (.ua)](.ua/knowledge-graph.json)**: 프로젝트 구조 분석 데이터베이스

---

<p align="center">
  <sub>Built with ❤️ for Flowdesk Android Team</sub>
</p>
