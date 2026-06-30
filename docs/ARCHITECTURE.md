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
  * UI 로직(Fragment, Adapter 등)과 상태 관리 로직(ViewModel)을 철저히 분리합니다.
  * ViewModel은 Data 레이어(Repository)에 직접 접근하지 않고, 반드시 **UseCase**를 통해서만 비즈니스 로직을 실행합니다.
  * **ViewBinding 엄격 적용**: 수동 `findViewById` 호출을 배제하고 뷰바인딩을 사용하며, Fragment/BottomSheet 수명 주기에 맞춰 메모리 해제(`onDestroyView() -> _binding = null`)를 수행합니다.
  * **하드코딩 배제**: 문자열은 `strings.xml`, 색상(HEX)은 `colors.xml` 시맨틱 자원으로 추출하여 참조합니다.
  * **단일 UiState 지향**: 상태 일관성 유지를 위해 화면 단위로 단일 `UiState` 모델을 제공하고 단방향 데이터 흐름(UDF)을 보장합니다.

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

  ➡️ API/DB 결과 
  ➡️ DTO 반환 
  ➡️ Mapper를 통해 Domain Model로 변환 
  ➡️ UseCase 
  ➡️ ViewModel (상태 업데이트: StateFlow) 
  ➡️ UI (화면 갱신)
```

---

## 6. UI 상태 및 뷰바인딩 관리 표준 가이드

본 프로젝트에서는 모던 안드로이드 개발(MAD) 트렌드에 맞춰 일관되고 버그 없는 UI 화면을 보장하기 위해 아래의 UI 프레젠테이션 개발 가이드라인을 강제합니다.

### 1) ViewBinding 수명 주기 및 메모리 누수 방지
Fragment나 BottomSheetDialogFragment는 뷰의 수명 주기가 객체 자체의 수명 주기보다 짧기 때문에, 바인딩 참조 해제가 누락되면 메모리 누수(Memory Leak)가 발생합니다.
* **표준 템플릿**:
  ```kotlin
  class ExampleFragment : Fragment(R.layout.fragment_example) {
      private var _binding: FragmentExampleBinding? = null
      private val binding get() = _binding!!

      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
          super.onViewCreated(view, savedInstanceState)
          _binding = FragmentExampleBinding.bind(view)
          // binding.tvTitle.text = ...
      }

      override fun onDestroyView() {
          super.onDestroyView()
          _binding = null // 메모리 누수 방지를 위한 명시적 해제
      }
  }
  ```

### 2) MVVM + 단일 UiState + combine 흐름
여러 개의 상태가 복합적으로 작용하는 화면에서는 상태 파편화를 막기 위해 **단일 UiState 데이터 클래스**와 **선택적 Flow 결합(combine)**을 적용합니다.

* **동작 아키텍처 흐름**:
```
[ViewModel]
 1. 원천 소스 Flow 분리 관리 (Private)
    - _isLoading: MutableStateFlow
    - _selectedGroup: MutableStateFlow
    - _filteredGroups: MutableStateFlow
    
 2. combine + stateIn 결합 발행 (Public)
    - val uiState: StateFlow<UiState> = combine(...) { ... }
      -> 통계 카운트 등 파생 상태(Derived State)는 combine 내에서 실시간 유도 연산하여 방출

 3. 중복 방출 차단
    - distinctUntilChanged() 필터를 적용해 동일 상태의 재발행 차단

          ▼ (uiState 발행)

[View (Fragment)]
 1. 단일 수집 파이프라인으로 통합
    - lifecycleScope.launch { 
          uiState.collectLatest { state ->
              // 로딩 바 활성 제어
              // 칩 및 아코디언 목록 리프레시
              // 통계 카운트 텍스트 갱신
          }
      }
```

### 3) 상태 기반 반응형 비동기 파이프라인 (Declarative Pipeline)
여러 필터 조건 및 페이지 정보가 실시간으로 상호작용하며 데이터를 불러와야 하는 복잡한 목록 화면에서는, **상태를 업데이트한 후 직접 비동기 함수를 호출하는 명령형(Imperative) 구조를 금지**합니다. 

필터 상태(`filterState`), 페이지(`currentPage`), 리프레시 트리거(`refreshTrigger`) 등의 스트림을 하나로 결합하고, **`flatMapLatest`** 연산자를 활용하여 **상태 변경 시 비동기 API가 자동으로 트리거되는 반응형 파이프라인**을 표준 모델로 구축합니다.

* **반응형 아키텍처 흐름**:
```
 [사용자 이벤트] ──> 상태 변경 (State Update)
                          │
                          ▼
             [ combine(Filter, Page, Trigger) ]
                          │
                          ▼
            [ flatMapLatest { (filter, page) -> ... } ]
             - 최신 요청 자동 실행 / 이전 비동기 작업 자동 취소(Cancel)
             - Loading ➡️ Success/Error 상태 자동 emit
                          │
                          ▼
                [ uiState (StateFlow) ]
                          │
                          ▼
                 [ View (render) ]
```

* **표준 ViewModel 구현 예시**:
  ```kotlin
  class ExampleListViewModel(private val repository: ExampleRepository) : ViewModel() {
      // 1. 상태 변수 정의 (검색 디바운스 분리 구조)
      private val _searchQuery = MutableStateFlow<String?>(null)
      private val _filtersWithoutQuery = MutableStateFlow(FilterState())
      private val _currentPage = MutableStateFlow(1)
      private val _refreshTrigger = MutableStateFlow(0)

      val filterState = combine(
          _searchQuery.debounce(300).distinctUntilChanged(),
          _filtersWithoutQuery
      ) { query, filters -> filters.copy(q = query) }
      .stateIn(viewModelScope, SharingStarted.Eagerly, FilterState())

      // 2. [핵심] 선언형 UI 상태 파이프라인
      val uiState: StateFlow<ListUiState> = combine(
          filterState,
          _currentPage,
          _refreshTrigger
      ) { filter, page, _ -> filter to page }
      .flatMapLatest { (filter, page) ->
          flow {
              if (page == 1) emit(ListUiState.Loading)
              repository.getItems(page = page, query = filter.q)
                  .fold(
                      onSuccess = { list -> emit(ListUiState.Success(list)) },
                      onFailure = { err -> emit(ListUiState.Error(err.message)) }
                  )
          }
      }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState.Loading)

      // 3. UI 액션 함수 (비동기 함수를 직접 호출하지 않고 오직 "상태만 변경")
      fun updateSearchQuery(query: String?) {
          _searchQuery.value = query
      }
      
      fun triggerRefresh() {
          _refreshTrigger.value += 1
      }
  }
  ```

### 4) 리소스 추출 및 시맨틱 바인딩 규칙
* **한국어 문자열**: Kotlin/XML 상의 모든 한글 문자열은 `strings.xml`에 자원으로 선언하고 `context.getString(R.string.key)` 또는 XML `@string/key`로 연동하여 하드코딩을 원천 배제합니다.
* **색상(Color)**: 소스 내 `Color.parseColor("#3B82F6")` 형태의 하드코딩 HEX 지정을 금지하고, `colors.xml`에 정의된 시맨틱 자원(예: `R.color.brand_primary`)을 `ContextCompat.getColor(context, id)`를 통해 획득하여 디자인 일관성을 준수합니다.

---

## 7. 실용주의 개발 핵심 철학 (Pragmatic Guidelines)

본 프로젝트는 구조적 완성도와 개발 생산성 사이의 균형을 유지하기 위해 아래의 **5가지 실용주의 설계 가이드라인**을 최우선으로 삼아 설계 및 리팩토링을 진행합니다.

> [!IMPORTANT]
> **FlowDesk Android 개발 핵심 5원칙**
>
> 1. **기본은 MVVM + 단일 UiState**
>    - 화면 상태 관리는 단방향 데이터 흐름(UDF) 보장을 위해 단일 `UiState` 관찰 구조를 표준으로 채택하되, 불필요한 보일러플레이트가 많은 MVI 대신 가볍고 직관적인 MVVM을 뼈대로 삼습니다.
> 2. **combine은 “진짜 필요할 때만”**
>    - 여러 소스의 유기적인 결합(예: 필터, 검색, 정렬 조건)이 상태를 결정할 때에만 `combine`을 활용합니다. 단순 상태나 독립적 라이프사이클을 갖는 데이터는 억지로 묶지 않고 개별 Flow로 깔끔하게 노출합니다.
> 3. **UseCase는 “비즈니스 로직 있을 때만”**
>    - 단순 조회 및 CRUD 전달만 하는 무의미한 Passthrough UseCase(단순히 Repository의 함수를 호출하여 넘기기만 하는 클래스) 생성을 금지합니다. 복잡한 유효성 검사, 결합, 비즈니스 계산 등이 필요한 경우에만 UseCase를 추가하고, 그 외에는 ViewModel에서 Repository에 직접 접근하는 것을 지향하여 구조를 경량화합니다.
> 4. **Base는 최소화**
>    - 공통 코드를 줄이겠다는 목적으로 무분별하게 Base 클래스(`BaseFragment`, `BaseViewModel` 등)에 의존하거나 비대하게 기능을 밀어 넣지 않습니다. 상속(Inheritance)보다는 조합(Composition)과 Kotlin 확장 함수(Extension)를 활용해 독립적이고 가벼운 설계를 유지합니다.
> 5. **화면 단순하면 과감히 단순하게**
>    - 아키텍처 원칙은 절대적인 법률이 아닌 도구입니다. 화면이 단순하고 데이터 흐름이 단조로운 경우, 복잡한 상태 결합이나 구조화 단계를 생략하고 과감하게 단순(Simple State / Direct Flow)하게 작성하여 생산성을 유지합니다.


