import os
import re

files = [
    'app/src/main/java/com/example/flowdesk_android/feature/role/presentation/catalog/PermissionCatalogViewModel.kt',
    'app/src/main/java/com/example/flowdesk_android/feature/role/presentation/detail/RoleDetailViewModel.kt',
    'app/src/main/java/com/example/flowdesk_android/feature/role/presentation/list/RolesViewModel.kt',
    'app/src/main/java/com/example/flowdesk_android/feature/role/presentation/permissions/ManagePermissionsViewModel.kt'
]

for file_path in files:
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        with open(file_path, 'r', encoding='cp949') as f:
            content = f.read()

    # PermissionCatalogViewModel
    if 'PermissionCatalogViewModel' in file_path:
        content = content.replace('import com.example.flowdesk_android.feature.role.domain.usecase.GetPermissionCatalogUseCase', 'import com.example.flowdesk_android.core.domain.repository.RoleRepository')
        content = content.replace('private val getPermissionCatalogUseCase: GetPermissionCatalogUseCase', 'private val roleRepository: RoleRepository')
        content = content.replace('getPermissionCatalogUseCase()', 'roleRepository.getPermissionCatalog()')

    # RoleDetailViewModel
    elif 'RoleDetailViewModel' in file_path:
        content = content.replace('import com.example.flowdesk_android.feature.role.domain.usecase.GetRoleDetailUseCase', 'import com.example.flowdesk_android.core.domain.repository.RoleRepository')
        content = content.replace('private val getRoleDetailUseCase: GetRoleDetailUseCase', 'private val roleRepository: RoleRepository')
        content = content.replace('getRoleDetailUseCase(id)', 'roleRepository.getRoleDetail(id)')

    # RolesViewModel
    elif 'RolesViewModel' in file_path:
        content = re.sub(r'import com.example.flowdesk_android.feature.role.domain.usecase.*?\n', '', content)
        content = content.replace('import com.example.flowdesk_android.core.domain.model.Role\n', 'import com.example.flowdesk_android.core.domain.model.Role\nimport com.example.flowdesk_android.core.domain.repository.RoleRepository\n')
        
        # Replace constructor
        content = re.sub(r'@Inject constructor\(.*?\)', '@Inject constructor(\n    private val roleRepository: RoleRepository\n)', content, flags=re.DOTALL)
        
        # Replace method calls
        content = content.replace('getRolesUseCase()', 'roleRepository.getRoles()')
        content = content.replace('createRoleUseCase(name, displayName, description)', 'roleRepository.createRole(name, displayName, description)')
        content = content.replace('deleteRoleUseCase(id)', 'roleRepository.deleteRole(id)')
        content = content.replace('toggleRoleStatusUseCase(id, isActive)', 'roleRepository.toggleRoleStatus(id, isActive)')

    # ManagePermissionsViewModel
    elif 'ManagePermissionsViewModel' in file_path:
        content = re.sub(r'import com.example.flowdesk_android.feature.role.domain.usecase.*?\n', '', content)
        content = content.replace('import com.example.flowdesk_android.core.domain.model.RoleDetail\n', 'import com.example.flowdesk_android.core.domain.model.RoleDetail\nimport com.example.flowdesk_android.core.domain.repository.RoleRepository\n')
        
        # Replace constructor
        content = re.sub(r'@Inject constructor\(.*?\)', '@Inject constructor(\n    private val roleRepository: RoleRepository\n)', content, flags=re.DOTALL)
        
        # Replace method calls
        content = content.replace('getRoleDetailUseCase(roleId)', 'roleRepository.getRoleDetail(roleId)')
        content = content.replace('getRolesUseCase()', 'roleRepository.getRoles()')
        content = content.replace('updateRoleInfoUseCase(id, name, displayName, description)', 'roleRepository.updateRoleInfo(id, name, displayName, description)')
        content = content.replace('updateRolePermissionsUseCase(id, add, remove)', 'roleRepository.updateRolePermissions(id, add, remove)')
        content = content.replace('copyRolePermissionsUseCase(targetRoleId, sourceRoleId)', 'roleRepository.copyRolePermissions(targetRoleId, sourceRoleId)')

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

print('Done Role ViewModels')
