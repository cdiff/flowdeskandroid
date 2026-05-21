import os
import re
import glob

base_dir = 'app/src/main'

replacements = {
    'com.example.flowdesk_android.feature.common.toast': 'com.example.flowdesk_android.core.ui.toast',
    'com.example.flowdesk_android.presentation.ui': 'com.example.flowdesk_android.feature.main',
    'com.example.flowdesk_android.feature.role.domain.model': 'com.example.flowdesk_android.core.domain.model',
    'com.example.flowdesk_android.feature.user.domain.model': 'com.example.flowdesk_android.core.domain.model',
    'com.example.flowdesk_android.feature.role.domain.repository': 'com.example.flowdesk_android.core.domain.repository',
    'com.example.flowdesk_android.feature.user.domain.repository': 'com.example.flowdesk_android.core.domain.repository'
}

def fix_view_models(content, file_path):
    # InviteTeamViewModel
    if 'InviteTeamViewModel.kt' in file_path:
        content = content.replace('import com.example.flowdesk_android.feature.role.domain.usecase.GetRolesUseCase', 'import com.example.flowdesk_android.core.domain.repository.RoleRepository')
        content = content.replace('import com.example.flowdesk_android.feature.user.domain.usecase.CreateUserUseCase', 'import com.example.flowdesk_android.core.domain.repository.UserRepository')
        content = content.replace('private val createUserUseCase: CreateUserUseCase,', 'private val userRepository: UserRepository,')
        content = content.replace('private val getRolesUseCase: GetRolesUseCase', 'private val roleRepository: RoleRepository')
        content = content.replace('getRolesUseCase()', 'roleRepository.getRoles()')
        content = content.replace('createUserUseCase(', 'userRepository.createUser(')
    
    # UserListViewModel
    elif 'UserListViewModel.kt' in file_path:
        content = content.replace('import com.example.flowdesk_android.feature.user.domain.usecase.GetUsersUseCase', 'import com.example.flowdesk_android.core.domain.repository.UserRepository')
        content = content.replace('private val getUsersUseCase: GetUsersUseCase', 'private val userRepository: UserRepository')
        content = content.replace('getUsersUseCase()', 'userRepository.getUsers()')

    # UserDetailViewModel
    elif 'UserDetailViewModel.kt' in file_path:
        content = re.sub(r'import com.example.flowdesk_android.feature.user.domain.usecase.*?\n', '', content)
        if 'import com.example.flowdesk_android.core.domain.repository.UserRepository' not in content:
            content = content.replace('import com.example.flowdesk_android.core.domain.model.UserDetail\n', 'import com.example.flowdesk_android.core.domain.model.UserDetail\nimport com.example.flowdesk_android.core.domain.repository.UserRepository\n')
        content = re.sub(r'@Inject constructor\(.*?\)', '@Inject constructor(\n    private val userRepository: UserRepository\n)', content, flags=re.DOTALL)
        content = content.replace('getUserDetailUseCase(id)', 'userRepository.getUserDetail(id)')
        content = content.replace('updateUserStatusUseCase(id, isActive)', 'userRepository.updateUserStatus(id, isActive)')
        content = content.replace('updateUserRolesUseCase(id, add, remove)', 'userRepository.updateUserRoles(id, add, remove)')
        content = content.replace('adminChangePasswordUseCase(id, newPassword)', 'userRepository.adminChangePassword(id, newPassword)')
        content = content.replace('invalidateUserTokensUseCase(id)', 'userRepository.invalidateUserTokens(id)')
        content = content.replace('updateUserUseCase(id, corpName, userName, userEmail, userTel, userHp)', 'userRepository.updateUser(id, corpName, userName, userEmail, userTel, userHp)')

    # PermissionCatalogViewModel
    elif 'PermissionCatalogViewModel.kt' in file_path:
        content = content.replace('import com.example.flowdesk_android.feature.role.domain.usecase.GetPermissionCatalogUseCase', 'import com.example.flowdesk_android.core.domain.repository.RoleRepository')
        content = content.replace('private val getPermissionCatalogUseCase: GetPermissionCatalogUseCase', 'private val roleRepository: RoleRepository')
        content = content.replace('getPermissionCatalogUseCase()', 'roleRepository.getPermissionCatalog()')

    # RoleDetailViewModel
    elif 'RoleDetailViewModel.kt' in file_path:
        content = re.sub(r'import com.example.flowdesk_android.feature.role.domain.usecase.*?\n', '', content)
        if 'import com.example.flowdesk_android.core.domain.repository.RoleRepository' not in content:
            content = content.replace('import com.example.flowdesk_android.core.domain.model.RoleDetail\n', 'import com.example.flowdesk_android.core.domain.model.RoleDetail\nimport com.example.flowdesk_android.core.domain.repository.RoleRepository\n')
        content = re.sub(r'@Inject constructor\(.*?\)', '@Inject constructor(\n    private val roleRepository: RoleRepository\n)', content, flags=re.DOTALL)
        content = content.replace('getRoleDetailUseCase(roleId)', 'roleRepository.getRoleDetail(roleId)')
        content = content.replace('toggleRoleStatusUseCase(roleId, newStatus)', 'roleRepository.toggleRoleStatus(roleId, newStatus)')
        content = content.replace('updateRoleInfoUseCase(roleId, roleName, displayName, description)', 'roleRepository.updateRoleInfo(roleId, roleName, displayName, description)')
        content = content.replace('deleteRoleUseCase(roleId)', 'roleRepository.deleteRole(roleId)')

    # RolesViewModel
    elif 'RolesViewModel.kt' in file_path:
        content = re.sub(r'import com.example.flowdesk_android.feature.role.domain.usecase.*?\n', '', content)
        if 'import com.example.flowdesk_android.core.domain.repository.RoleRepository' not in content:
            content = content.replace('import com.example.flowdesk_android.core.domain.model.Role\n', 'import com.example.flowdesk_android.core.domain.model.Role\nimport com.example.flowdesk_android.core.domain.repository.RoleRepository\n')
        content = re.sub(r'@Inject constructor\(.*?\)', '@Inject constructor(\n    private val roleRepository: RoleRepository\n)', content, flags=re.DOTALL)
        content = content.replace('getRolesUseCase()', 'roleRepository.getRoles()')
        content = content.replace('createRoleUseCase(roleName, displayName, description)', 'roleRepository.createRole(roleName, displayName, description)')
        content = content.replace('deleteRoleUseCase(roleId)', 'roleRepository.deleteRole(roleId)')
        content = content.replace('toggleRoleStatusUseCase(roleId, newStatus)', 'roleRepository.toggleRoleStatus(roleId, newStatus)')

    # ManagePermissionsViewModel
    elif 'ManagePermissionsViewModel.kt' in file_path:
        content = re.sub(r'import com.example.flowdesk_android.feature.role.domain.usecase.*?\n', '', content)
        if 'import com.example.flowdesk_android.core.domain.repository.RoleRepository' not in content:
            content = content.replace('import com.example.flowdesk_android.core.domain.model.RoleDetail\n', 'import com.example.flowdesk_android.core.domain.model.RoleDetail\nimport com.example.flowdesk_android.core.domain.repository.RoleRepository\n')
        content = re.sub(r'@Inject constructor\(.*?\)', '@Inject constructor(\n    private val roleRepository: RoleRepository\n)', content, flags=re.DOTALL)
        content = content.replace('getRoleDetailUseCase(roleId)', 'roleRepository.getRoleDetail(roleId)')
        content = content.replace('getPermissionCatalogUseCase()', 'roleRepository.getPermissionCatalog()')
        content = content.replace('updateRolePermissionsUseCase(currentRoleId, toAdd.ifEmpty { null }, toRemove.ifEmpty { null })', 'roleRepository.updateRolePermissions(currentRoleId, toAdd.ifEmpty { null }, toRemove.ifEmpty { null })')
        content = content.replace('copyRolePermissionsUseCase(currentRoleId, sourceRoleId)', 'roleRepository.copyRolePermissions(currentRoleId, sourceRoleId)')
        content = content.replace('updateRoleInfoUseCase(currentRoleId, roleName, displayName, description)', 'roleRepository.updateRoleInfo(currentRoleId, roleName, displayName, description)')

    return content

for root, _, files in os.walk(base_dir):
    for file in files:
        if file.endswith('.kt') or file.endswith('.xml'):
            file_path = os.path.join(root, file)
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                with open(file_path, 'r', encoding='cp949', errors='ignore') as f:
                    content = f.read()

            original_content = content
            for old, new in replacements.items():
                content = content.replace(old, new)
            
            content = fix_view_models(content, file_path)

            if content != original_content:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)

print('Replacements finished safely')
