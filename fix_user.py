import os
import re

files = [
    'app/src/main/java/com/example/flowdesk_android/feature/user/presentation/invite/InviteTeamViewModel.kt',
    'app/src/main/java/com/example/flowdesk_android/feature/user/presentation/list/UserListViewModel.kt',
    'app/src/main/java/com/example/flowdesk_android/feature/user/presentation/detail/UserDetailViewModel.kt'
]

for file_path in files:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # InviteTeamViewModel
    if 'InviteTeamViewModel' in file_path:
        content = content.replace('import com.example.flowdesk_android.feature.role.domain.usecase.GetRolesUseCase', 'import com.example.flowdesk_android.core.domain.repository.RoleRepository')
        content = content.replace('import com.example.flowdesk_android.feature.user.domain.usecase.CreateUserUseCase', 'import com.example.flowdesk_android.core.domain.repository.UserRepository')
        content = content.replace('private val createUserUseCase: CreateUserUseCase,', 'private val userRepository: UserRepository,')
        content = content.replace('private val getRolesUseCase: GetRolesUseCase', 'private val roleRepository: RoleRepository')
        content = content.replace('getRolesUseCase()', 'roleRepository.getRoles()')
        content = content.replace('createUserUseCase(', 'userRepository.createUser(')

    # UserListViewModel
    elif 'UserListViewModel' in file_path:
        content = content.replace('import com.example.flowdesk_android.feature.user.domain.usecase.GetUsersUseCase', 'import com.example.flowdesk_android.core.domain.repository.UserRepository')
        content = content.replace('private val getUsersUseCase: GetUsersUseCase', 'private val userRepository: UserRepository')
        content = content.replace('getUsersUseCase()', 'userRepository.getUsers()')

    # UserDetailViewModel
    elif 'UserDetailViewModel' in file_path:
        content = re.sub(r'import com.example.flowdesk_android.feature.user.domain.usecase.*?\n', '', content)
        content = content.replace('import com.example.flowdesk_android.core.domain.model.UserDetail\n', 'import com.example.flowdesk_android.core.domain.model.UserDetail\nimport com.example.flowdesk_android.core.domain.repository.UserRepository\n')
        
        # Replace constructor
        content = re.sub(r'@Inject constructor\(.*?\)', '@Inject constructor(\n    private val userRepository: UserRepository\n)', content, flags=re.DOTALL)
        
        # Replace method calls
        content = content.replace('getUserDetailUseCase(id)', 'userRepository.getUserDetail(id)')
        content = content.replace('updateUserStatusUseCase(id, isActive)', 'userRepository.updateUserStatus(id, isActive)')
        content = content.replace('updateUserRolesUseCase(id, add, remove)', 'userRepository.updateUserRoles(id, add, remove)')
        content = content.replace('adminChangePasswordUseCase(id, newPassword)', 'userRepository.adminChangePassword(id, newPassword)')
        content = content.replace('invalidateUserTokensUseCase(id)', 'userRepository.invalidateUserTokens(id)')
        content = content.replace('updateUserUseCase(id, corpName, userName, userEmail, userTel, userHp)', 'userRepository.updateUser(id, corpName, userName, userEmail, userTel, userHp)')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

print('Done')
