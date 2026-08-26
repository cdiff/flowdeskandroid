<h1 align="center">
  <br>
  <img src="https://img.shields.io/badge/Flowdesk-0F172A?style=for-the-badge&logo=android&logoColor=38BDF8" alt="Flowdesk Android Logo" width="220">
  <br>
  <b>Flowdesk Android</b>
  <br>
</h1>

<p align="center">
  <strong>B2B SaaS형 엔터프라이즈 CRM & 맞춤형 업무 공간(Custom Workspace) 구축 플랫폼</strong>
  <br>
  <em>Clean Architecture • MVVM • Jetpack Components • Kotlin Coroutines & Flow</em>
</p>

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"></a>
  <a href="https://developer.android.com/"><img src="https://img.shields.io/badge/Android-SDK%2030+-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android SDK"></a>
  <a href="https://developer.android.com/jetpack"><img src="https://img.shields.io/badge/Jetpack-MVVM-4285F4?style=flat-square&logo=google&logoColor=white" alt="Jetpack"></a>
  <a href="https://github.com/square/retrofit"><img src="https://img.shields.io/badge/Networking-Retrofit2-000000?style=flat-square" alt="Retrofit2"></a>
  <a href="https://dagger.dev/hilt/"><img src="https://img.shields.io/badge/DI-Hilt-blue?style=flat-square" alt="Hilt"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square" alt="License"></a>
</p>

---

## 📌 목차 (Table of Contents)
- [✨ 프로젝트 소개 (Overview)](#-프로젝트-소개-overview)
- [📱 주요 기능 및 화면 스크린샷 (Features & Screenshots)](#-주요-기능-및-화면-스크린샷-features--screenshots)
  - [📊 1. 상담 분석 대시보드 & 상담 일정 관리](#-1-상담-분석-대시보드--상담-일정-관리)
  - [👥 2. 사용자 및 세분화된 역할 권한 관리 (RBAC)](#-2-사용자-및-세분화된-역할-권한-관리-rbac)
  - [⚙️ 3. 테넌트 및 시스템 차단 설정 관리](#️-3-테넌트-및-시스템-차단-설정-관리)
  - [📝 4. 컨텐츠 게시판 및 계정 설정](#-4-컨텐츠-게시판-및-계정-설정)
- [🛠️ 기술 스택 (Tech Stack)](#️-기술-스택-tech-stack)
- [🏛️ 아키텍처 구조 (Architecture)](#️-아키텍처-구조-architecture)
- [🎨 UI/UX 디자인 시스템 수칙](#-uiux-디자인-시스템-수칙)
- [🚀 시작하기 (Quick Start)](#-시작하기-quick-start)
- [📖 문서 및 온보딩 (Documentation)](#-문서-및-온보딩-documentation)

---

## ✨ 프로젝트 소개 (Overview)

**Flowdesk Android**는 기업 맞춤형 B2B SaaS CRM 모바일 플랫폼입니다.  
멀티 테넌트 환경에서의 효율적인 고객 상담 분석, 캘린더 기반 일정 추적, 팀원별 세부 역할(Role) 및 권한(Permission) 제어, 시스템 보안 정책을 모바일 환경에서 완벽하게 제어할 수 있도록 설계되었습니다.

### 🌟 핵심 가치
* **실시간 CRM 분석 & 모바일 일정 동기화**: 상담 현황 실시간 차트 통계 및 월간/일간 캘린더 연동
* **세분화된 엔터프라이즈 RBAC**: 팀원별 맞춤형 접근 제어 및 권한 복사/위임 시스템
* **안전한 인증 & 세션**: `AuthInterceptor` 및 `TokenAuthenticator`를 통한 401 자동 토큰 갱신 파이프라인
* **정돈된 모던 디자인 시스템**: WindowInsets 기반 노치 침범 방지, 아코디언 드로어 네비게이션, 언더라인 입력 폼

---

## 📱 주요 기능 및 화면 스크린샷 (Features & Screenshots)

### 📊 1. 상담 분석 대시보드 & 상담 일정 관리

고객 인입 통계, 접수 현황, 처리율을 한눈에 파악하는 직관적인 대시보드와 월간/일간 상담 일정 캘린더를 제공합니다.

<p align="center">
  <img src="docs/screenshots/counsel_dashboard_1.jpg" width="31%" alt="상담 통계 대시보드 1" />
  <img src="docs/screenshots/counsel_dashboard_2.jpg" width="31%" alt="상담 통계 대시보드 2" />
  <img src="docs/screenshots/counsel_dashboard_3.jpg" width="31%" alt="상담 통계 대시보드 3" />
</p>

<p align="center">
  <img src="docs/screenshots/counsel_list.jpg" width="48%" alt="상담 목록 및 내역" />
  <img src="docs/screenshots/counsel_calendar.jpg" width="48%" alt="상담 캘린더 일정 관리" />
</p>

* **대시보드 통계**: 상담 유형별 분포 차트, 시간대별 접수량, 담당자별 처리 통계 시각화 (MPAndroidChart)
* **상담 목록 & 필터링**: 상담 상태(접수, 진행중, 완료), 기간, 담당자별 맞춤 검색
* **상담 캘린더**: 날짜별 예약된 상담 일정을 타임라인 형태로 직관적 조회

---

### 👥 2. 사용자 및 세분화된 역할 권한 관리 (RBAC)

조직의 구성원을 안전하게 초대하고, 각 메뉴 및 기능별 세부 권한을 매트릭스 형태로 부여합니다.

<p align="center">
  <img src="docs/screenshots/user_management.jpg" width="48%" alt="사용자 관리" />
  <img src="docs/screenshots/role_management.jpg" width="48%" alt="역할 및 권한 관리" />
</p>

* **사용자 관리**: 팀원 목록 조회, 상태(활성/비활성) 변경, 팀 초대 및 계정 제어
* **역할(Role) & 권한(Permission)**: 권한 카탈로그 기반 읽기/쓰기 권한 부여, 기존 역할 권한 복사 기능

---

### ⚙️ 3. 테넌트 및 시스템 차단 설정 관리

시스템 관리자 전용 테넌트 상태 모니터링 및 악성 접근 방지를 위한 보안 차단 정책을 관리합니다.

<p align="center">
  <img src="docs/screenshots/tenant_management.jpg" width="48%" alt="테넌트 관리" />
  <img src="docs/screenshots/block_management.jpg" width="48%" alt="차단 관리" />
</p>

* **테넌트 관리**: 테넌트별 운영 상태 및 웹사이트 설정, 커스텀 상태 컬러 피커 연동
* **차단 설정**: 비정상 접근 IP 차단 및 악성 키워드 필터링 실시간 제어

---

### 📝 4. 컨텐츠 게시판 및 계정 설정

사내 공지 및 게시판 운영과 개인 계정 알림/보안 옵션을 손쉽게 관리할 수 있습니다.

<p align="center">
  <img src="docs/screenshots/board_management.jpg" width="48%" alt="게시판 관리" />
  <img src="docs/screenshots/mypage.jpg" width="48%" alt="마이페이지 및 설정" />
</p>

* **게시판 관리**: 게시글 작성/수정/삭제 및 게시판 카테고리별 분류
* **마이페이지**: 프로필 수정, 비밀번호 변경, 서비스 알림 수신 설정

---

## 🛠️ 기술 스택 (Tech Stack)

| 영역 | 사용 기술 / 라이브러리 |
|---|---|
| **Language** | Kotlin 2.0+ |
| **Target SDK** | minSdk 30 (Android 11) / targetSdk 36 |
| **Architecture** | Clean Architecture + MVVM (Model-View-ViewModel) |
| **UI Components** | Android Jetpack (Fragment, ViewBinding, Navigation Component) |
| **Asynchronous & State** | Kotlin Coroutines, StateFlow, LiveData |
| **Dependency Injection** | Hilt (Dagger-Hilt) |
| **Network & Serialization** | Retrofit2, OkHttp3 (HttpLoggingInterceptor), Gson |
| **Custom UI & Charts** | MPAndroidChart, ColorPickerView, Lottie Animation, Flexbox |
| **Build & Tooling** | Gradle 9.3.1, ProGuard / R8 Obfuscation & Resource Shrinking |

---

## 🏛️ 아키텍처 구조 (Architecture)

관심사의 명확한 분리(Separation of Concerns)와 유지보수성을 위해 **3계층 Clean Architecture** 레이어로 구성되어 있습니다.

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
        REPO --> DB[TokenManager / Local Storage]
    end
```

### 계층별 역할
1. **`Presentation Layer`**: 화면 UI, ViewBinding, 네비게이션 액션, ViewModel 상태 관리
2. **`Domain Layer`**: 순수 비즈니스 로직 유스케이스 (`UpdateRolePermissionsUseCase` 등), 도메인 엔티티
3. **`Data Layer`**: `UserRepositoryImpl`, API 데이터 전송 객체(DTO), `TokenManager` 및 세션 토큰 관리

---

## 🎨 UI/UX 디자인 시스템 수칙

본 프로젝트는 [`AGENTS.md`](.agents/AGENTS.md) 표준 가이드라인에 맞춰 제작되었습니다.

1. 📱 **WindowInsets 카메라 노치 영역 보호**
   * 상단 시스템 바 패딩을 동적으로 확보하여 서브 화면의 레이아웃 가림 방지
2. 🚫 **서브 화면 하단 네비게이션 바 자동 숨김**
   * 상세/수정/초대 서브 페이지 진입 시 하단 바 비노출 처리로 화면 몰입감 향상
3. 📝 **깔끔한 밑줄 스타일(Underline Input Form)**
   * 투명 배경(`@null`)과 1dp 하단 구분선, 필수 입력 항목 우측 빨간 별표(`*`) 표기
4. ⏳ **로딩 가시성 보장 (Loading State)**
   * API 호출 중 뼈대 깜빡임을 방지하기 위해 `View.INVISIBLE` 상태에서 ProgressBar만 표출

---

## 🚀 시작하기 (Quick Start)

### 사전 요구 사항
* Android Studio (2024.x 이상)
* JDK 17 이상
* Android SDK 30 (Android 11) 이상

### 빌드 및 실행 방법
```bash
# 레포지토리 클론
git clone https://github.com/pasic/flowdeskandroid.git
cd flowdeskandroid

# 디버그 APK 빌드
./gradlew assembleDebug

# 릴리즈 번들(AAB) 빌드
./gradlew bundleRelease
```

---

## 📖 문서 및 온보딩 (Documentation)

* 📖 **[신규 개발자 온보딩 가이드](docs/ONBOARDING.md)**: 전체 아키텍처 및 추천 학습 경로
* 🎨 **[프로젝트 개발 규칙 (AGENTS.md)](.agents/AGENTS.md)**: UI/UX 디자인 규격 및 상태 관리 표준
* 🔒 **[개인정보처리방침 (Privacy Policy)](https://cdiff.github.io/flowdeskandroid/privacy-policy/)**: 구글 플레이스토어 정책 웹페이지

---

<p align="center">
  <sub>Built with ❤️ for Flowdesk Android Team</sub>
</p>
