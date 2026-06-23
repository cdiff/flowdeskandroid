# ViewBinding 마이그레이션 및 어댑터 리팩토링 가이드

본 문서는 프로젝트 내의 기존 `findViewById` 패턴을 제거하고 Android 표준 **ViewBinding** 적용 및 **RecyclerView Adapter** 리팩토링을 진행할 때 사용한 개발 기준과 구현 가이드를 제공합니다.

---

## 1. Fragment / BottomSheet의 ViewBinding 마이그레이션

Fragments와 BottomSheetDialog 등에서 수동으로 `findViewById`를 사용하여 뷰 객체를 매핑하던 부분을 제거하고, 메모리 누수를 완벽히 방지하는 바인딩 라이프사이클 구조를 적용합니다.

### 1) 구현 규칙 및 라이프사이클 관리
- **바인딩 백업 변수 (`_binding`)**: Nullable 속성을 가지는 `private` 변수로 선언합니다.
- **바인딩 Getter 변수 (`binding`)**: Null-safe 및 읽기 전용 속성(`val`)으로 구성하여 실제 뷰 참조 시 사용합니다.
- **메모리 누수 방지**: Fragment가 파괴(Destroy)될 때 메모리 해제를 위해 `onDestroyView()`에서 `_binding = null` 처리를 강제합니다.

### 2) 적용 예시 코드
```kotlin
package com.example.flowdesk_android.feature.user_management.presentation.roles.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.flowdesk_android.R
import com.example.flowdesk_android.databinding.FragmentRoleListBinding

class RolesFragment : Fragment(R.layout.fragment_role_list) {

    // 1. 바인딩 변수 선언
    private var _binding: FragmentRoleListBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 2. 바인딩 객체 연결
        _binding = FragmentRoleListBinding.bind(view)

        // 3. binding 객체를 활용한 뷰 제어 (findViewById 미사용)
        binding.tvTitle.text = "역할 목록"
        binding.rvRoles.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 4. 메모리 누수 방지를 위한 null 처리
        _binding = null
    }
}
```

---

## 2. RecyclerView Adapter 및 ViewHolder 뷰바인딩 최적화

ViewHolder 생성 단계에서 원시 `View` 객체 대신 **생성된 XML Binding 객체**를 주입받아 사용하도록 설계하여, ViewHolder 내의 수동 변수 선언 및 `findViewById` 호출을 배제합니다.

### 1) 구현 규칙
- `onCreateViewHolder`에서 생성되는 개별 XML의 바인딩 클래스(예: `ItemUserCardBinding`)를 인플레이션합니다.
- ViewHolder 생성 시 매개변수로 `ItemXXXBinding` 객체를 전달받고, super constructor 호출 시 `binding.root`를 전달합니다.
- `onBindViewHolder` 시점에 데이터를 바인딩할 때 ViewHolder 내부에서 `binding.tvName.text` 형태로 뷰에 직접 접근합니다.

### 2) 적용 예시 코드
```kotlin
package com.example.flowdesk_android.feature.user_management.presentation.users.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flowdesk_android.feature.user_management.domain.model.User
import com.example.flowdesk_android.databinding.ItemUserCardBinding

class UserAdapter(
    private val onItemClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        // 1. Item 레이아웃 바인딩 객체 생성
        val binding = ItemUserCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 2. 바인딩 객체를 생성자로 받는 ViewHolder 구현
    inner class UserViewHolder(
        private val binding: ItemUserCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.root.setOnClickListener { onItemClick(user) }

            // 3. findViewById 없이 뷰바인딩 멤버 프로퍼티로 바로 접근
            binding.tvUserName.text = user.userName
            binding.tvUserEmail.text = user.userEmail
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem.userSeq == newItem.userSeq

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
            oldItem == newItem
    }
}
```

---

## 3. 레이아웃 리소스 정리 및 시맨틱 컬러 바인딩

레이아웃 리소스 적용 시 하드코딩된 스타일 대신 중앙 집중식 리소스를 활용하여 일관된 디자인 가이드를 준수합니다.

### 1) 디자인 단순화 및 콤팩트화
- 복잡하게 중첩되거나 불필요한 장식적 요소(예: 리스트 내의 아바타 이미지 뷰 `tv_avatar`)를 XML에서 과감히 제거하여 미니멀하고 단정한 구조를 만듭니다.
- 3점 더보기 메뉴(`iv_more`), 계정 활성 상태 배지(`tv_active_status`) 등을 도입하여 직관적인 뷰 배치를 지원합니다.

### 2) 시맨틱 컬러 시스템 적용
- 코드에서 직접적으로 헥사 색상 코드(예: `#4CAF50`)를 직접 지정하는 패턴을 제거합니다.
- `colors.xml`에 정의된 의미 중심의 시맨틱 컬러 리소스를 결합하여 유지보수성을 극대화합니다.
  - 활성 상태: `@color/color_success_active` (내부적으로 `@color/green_500`과 바인딩)
  - 비활성 상태: `@color/slate_400`
- Kotlin 소스 코드에서는 `ContextCompat.getColor(context, R.color.color_success_active)` 형태로 리소스를 가져옵니다.

---

## 4. 구조적 리팩토링 및 하드코딩 제거 (Structural Cleanups)

유지보수성과 아키텍처 완성도를 높이기 위해 다음과 같은 하드코딩 제거 및 구조 개선 작업을 진행했습니다.

### 1) 하드코딩 텍스트 자원화 (Localization 준비)
- **문제점**: Kotlin 파일 및 XML 레이아웃 곳곳에 한국어 메시지(유효성 검사, 성공 토스트, 다이얼로그 텍스트 등)가 하드코딩되어 다국어 지원이 불가능하고 일관성이 결여되었습니다.
- **개선안**: 모든 한국어 안내/에러 메시지를 `strings.xml`에 고유한 `name` 리소스로 추출하고, 코드 내에서는 `getString(R.string.key)` 또는 XML의 `android:text="@string/key"` 속성으로 변경했습니다.

### 2) 비즈니스 로직 및 API 호출 통합 (트랜잭션 최적화)
- **문제점**: 팀원 상세 정보 수정 시, 기본 정보 저장과 역할 정보(Role) 저장이 각각 다른 화면 버튼으로 나뉘어 있어 사용자에게 중복 클릭을 유도하고, API 다중 호출로 인한 불필요한 네트워크 오버헤드가 발생했습니다.
- **개선안**: 
  - 기본 정보 수정 API(`PATCH /users/{id}`) DTO 규격에 `roleIds: List<Int>`를 포함하도록 스펙을 결합했습니다.
  - 이를 통해 `UpdateUserInfoRequest` DTO, Repository, ViewModel 레이어를 수정하여 하단의 통합 "저장하기" 버튼 클릭 한 번으로 모든 정보가 원스톱 트랜잭션으로 업데이트되도록 API를 단일화했습니다.

### 3) 중복된 목업 레이아웃 코드 제거 및 `<include>` 전환
- **문제점**: 역할 상세 화면(`fragment_role_detail.xml`) 내에 세부 권한 목록 및 할당된 팀원 목록을 보여주기 위해, 리스트 아이템 디자인 코드가 무의미하게 복사/붙여넣기 형태로 중복 기술되어 레이아웃 파일이 비대해졌습니다.
- **개선안**: 중복된 목업 코드 블록들을 모두 제거하고, 실제 렌더링에 사용되는 단일 아이템 레이아웃(`item_role_detail_page`, `item_role_assigned_user`)을 `<include>` 태그로 대체했습니다. 이로써 XML 레이아웃 편집기 프리뷰 환경에서도 가독성을 유지하면서 유지보수성을 극대화했습니다.

### 4) 단일 책임 원칙(SRP)에 따른 화면 분리 (바텀시트화)
- **문제점**: 팀원 상세 조회 화면 내에 비밀번호 변경 폼이 항상 노출되어 화면의 주 기능인 '정보 조회 및 역할 수정'에 집중하기 어렵고 오인 입력의 위험이 있었습니다.
- **개선안**: 비밀번호 변경 기능을 독립된 `UserChangePasswordBottomSheet` 다이얼로그 클래스로 분리하고, 상세 화면의 우측 툴바 더보기(3점) 메뉴를 클릭할 때만 동적으로 열리도록 유도하여 각 컴포넌트의 가시성과 단일 책임을 명확히 했습니다.

