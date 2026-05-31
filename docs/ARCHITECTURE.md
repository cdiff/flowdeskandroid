# FlowDesk Android 아키텍처 가이드

본 문서는 FlowDesk Android 프로젝트의 전반적인 아키텍처 구조와 리팩토링 배경, 그리고 설계 원칙에 대해 설명합니다.

## 1. 아키텍처 개요 및 도입 배경

초기 프로젝트는 레이어 중심(Layer-based)으로 패키지가 구성되어 있어 프로젝트 규모가 커짐에 따라 여러 문제점이 발생했습니다. 예를 들어, 모든 화면의 ViewModel이나 Fragment가 하나의 패키지에 몰려 있었고, 관련된 기능들을 한눈에 파악하기 어려웠습니다.

이를 해결하기 위해 **기능 중심 모듈화(Feature-based Modularization)** 와 **클린 아키텍처(Clean Architecture)** 를 결합한 구조로 전면 리팩토링을 진행했습니다. 

### 💡 주요 이점 (Benefits)
1. **높은 응집도와 낮은 결합도**: `auth`, `user`, `role` 등 각 도메인 기능이 완벽하게 분리되어 있습니다. 특정 기능을 수정하더라도 다른 기능에 미치는 영향(Side-Effect)을 최소화할 수 있습니다.
2. **명확한 계층 분리와 의존성 방향**: 모든 기능은 `Presentation` ➡️ `Domain` ➡️ `Data` 방향으로만 의존합니다. Domain 계층은 외부 라이브러리나 안드로이드 프레임워크에 전혀 의존하지 않는 순수한 Kotlin 코드로 유지되어 비즈니스 로직 테스트가 매우 용이합니다.
3. **확장성 및 온보딩 효율 증가**: 일관된 구조(ViewModel -> UseCase -> Repository)를 강제하므로, 새로운 개발자가 프로젝트에 투입되었을 때 전체적인 데이터 흐름을 빠르게 파악할 수 있습니다.

---

## 2. 프로젝트 디렉토리 구조

현재 프로젝트의 핵심 코드는 `app/src/main/java/com/example/flowdesk_android/` 내에 다음과 같이 구성되어 있습니다.

```text
com.example.flowdesk_android
 ┣ 📂 core          # 앱 전반에서 공통으로 사용되는 기반 클래스 모음
 ┃  ┣ 📂 base         # BaseFragment, BaseViewModel 등
 ┃  ┣ 📂 domain       # 공통 Domain 모델 (Error 등)
 ┃  ┣ 📂 network      # 네트워크 에러 핸들러, ApiResult 처리 등
 ┃  ┣ 📂 ui           # 공통 UI 컴포넌트, 커스텀 뷰 등
 ┃  ┗ 📂 util         # 날짜 포맷터 등 유틸리티 함수
 ┃
 ┣ 📂 data          # 앱 전반의 공통 데이터 소스 (예: Local DB, DataStore 등)
 ┃  ┗ 📂 local
 ┃
 ┣ 📂 di            # 앱 레벨의 의존성 주입 (Hilt 모듈)
 ┃
 ┣ 📂 feature       # 도메인/기능별 독립된 모듈 (⭐ 핵심 구조)
 ┃  ┣ 📂 auth         # 로그인, 회원가입 등 인증 기능
 ┃  ┣ 📂 main         # 메인 화면 (Bottom Navigation 등)
 ┃  ┣ 📂 mypage       # 마이페이지, 프로필 수정 등
 ┃  ┣ 📂 role         # 역할 관리, 권한 설정 등
 ┃  ┣ 📂 super_admin  # 슈퍼 관리자용 대시보드
 ┃  ┗ 📂 user         # 사용자 목록, 상세 조회, 권한 부여 등
 ┃
 ┗ 📜 FlowDeskApplication.kt
```

---

## 3. 기능(Feature) 모듈 상세 구조

각 `feature` (예: `feature/user`) 내부는 클린 아키텍처 원칙에 따라 다시 세 개의 레이어로 나뉩니다. 각 기능은 자신만의 `data`, `domain`, `presentation` 레이어 및 독립적인 `di` 모듈을 가집니다.

### 예시: `feature/user` 구조

```text
feature/user/
 ┣ 📂 data
 ┃  ┣ 📂 api            # Retrofit Interface (User 통신)
 ┃  ┣ 📂 dto            # 네트워크 통신용 Data Transfer Object
 ┃  ┗ 📂 repository     # UserRepository 인터페이스의 실제 구현체
 ┃
 ┣ 📂 domain
 ┃  ┣ 📂 model          # 순수 비즈니스 데이터 모델 (User, UserDetail 등)
 ┃  ┣ 📂 repository     # Data 레이어와 소통하기 위한 인터페이스
 ┃  ┗ 📂 usecase        # 비즈니스 로직 단위 (GetUsersUseCase 등)
 ┃
 ┣ 📂 presentation
 ┃  ┣ 📂 list           # 사용자 목록 화면 (Fragment, ViewModel, Adapter)
 ┃  ┣ 📂 detail         # 사용자 상세 화면 (Fragment, ViewModel)
 ┃  ┗ 📂 dialog         # 관련된 BottomSheet, Dialog 등
 ┃
 ┗ 📂 di                # 이 기능에서만 사용되는 Repository/UseCase 주입 모듈
```

---

## 4. 레이어별 역할 및 규칙

### 1) Domain Layer (도메인 계층)
* **역할**: 앱의 핵심 비즈니스 로직과 비즈니스 모델을 정의합니다.
* **규칙**:
  * 안드로이드 프레임워크나 외부 라이브러리(Retrofit, Room 등)에 대한 의존성을 절대 가져서는 안 됩니다. (순수 Kotlin)
  * Data 레이어의 DTO를 직접 알지 못하며, 오직 Domain Model만 사용합니다.
  * 외부와의 통신은 `Repository Interface`를 통해서만 정의합니다.

### 2) Data Layer (데이터 계층)
* **역할**: 실제 데이터를 가져오고(Network, DB), Domain 계층에 전달합니다.
* **규칙**:
  * Domain 계층의 `Repository Interface`를 상속받아 구현(Impl)합니다.
  * 서버에서 받아온 `DTO` 객체를 Domain 계층으로 넘길 때는 반드시 순수한 `Domain Model`로 매핑(`toDomain()`)해서 전달해야 합니다.

### 3) Presentation Layer (프레젠테이션 계층)
* **역할**: 사용자에게 UI를 보여주고, 사용자 이벤트를 처리합니다.
* **규칙**:
  * UI 로직(Fragment, Adapter 등)과 상태 관리 로직(ViewModel)을 분리합니다.
  * ViewModel은 Data 레이어(Repository)에 직접 접근하지 않고, 반드시 **UseCase**를 통해서만 비즈니스 로직을 실행합니다.

---

## 5. 데이터 흐름 (Data Flow) 요약

UI에서 데이터를 요청하고 화면에 보여주기까지의 단방향 데이터 흐름(Unidirectional Data Flow)은 다음과 같습니다.

```
UI (Fragment) 
  ➡️ (이벤트 발생) ➡️ ViewModel 
  ➡️ (로직 실행) ➡️ UseCase (Domain) 
  ➡️ (데이터 요청) ➡️ Repository Interface (Domain) 
  ➡️ (실제 구현) ➡️ Repository Impl (Data) 
  ➡️ (통신) ➡️ API (Retrofit) / DB (Room)

  ... 데이터 반환 ...

API/DB 결과 
  ➡️ DTO 반환 
  ➡️ Mapper를 통해 Domain Model로 변환 
  ➡️ UseCase 
  ➡️ ViewModel (상태 업데이트: StateFlow) 
  ➡️ UI (화면 갱신)
```
