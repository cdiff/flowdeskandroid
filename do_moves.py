import os
import glob
import shutil

base_dir = 'app/src/main/java/com/example/flowdesk_android'

def process_files():
    # 1. Move TopToast
    os.makedirs(f'{base_dir}/core/ui/toast', exist_ok=True)
    if os.path.exists(f'{base_dir}/feature/common/toast/TopToast.kt'):
        shutil.move(f'{base_dir}/feature/common/toast/TopToast.kt', f'{base_dir}/core/ui/toast/TopToast.kt')
        shutil.rmtree(f'{base_dir}/feature/common', ignore_errors=True)

    # 2. Move presentation/ui to feature/main
    os.makedirs(f'{base_dir}/feature/main', exist_ok=True)
    if os.path.exists(f'{base_dir}/presentation/ui'):
        for item in os.listdir(f'{base_dir}/presentation/ui'):
            shutil.move(os.path.join(f'{base_dir}/presentation/ui', item), f'{base_dir}/feature/main/')
        shutil.rmtree(f'{base_dir}/presentation', ignore_errors=True)

    # 3. Move domain models
    os.makedirs(f'{base_dir}/core/domain/model', exist_ok=True)
    for f in glob.glob(f'{base_dir}/feature/role/domain/model/*.kt'):
        shutil.move(f, f'{base_dir}/core/domain/model/')
    for f in glob.glob(f'{base_dir}/feature/user/domain/model/*.kt'):
        shutil.move(f, f'{base_dir}/core/domain/model/')
    shutil.rmtree(f'{base_dir}/feature/role/domain/model', ignore_errors=True)
    shutil.rmtree(f'{base_dir}/feature/user/domain/model', ignore_errors=True)

    # 4. Move domain repositories
    os.makedirs(f'{base_dir}/core/domain/repository', exist_ok=True)
    for f in glob.glob(f'{base_dir}/feature/role/domain/repository/*.kt'):
        shutil.move(f, f'{base_dir}/core/domain/repository/')
    for f in glob.glob(f'{base_dir}/feature/user/domain/repository/*.kt'):
        shutil.move(f, f'{base_dir}/core/domain/repository/')
    shutil.rmtree(f'{base_dir}/feature/role/domain/repository', ignore_errors=True)
    shutil.rmtree(f'{base_dir}/feature/user/domain/repository', ignore_errors=True)

    # 5. Delete UseCases
    for f in glob.glob(f'{base_dir}/feature/role/domain/usecase/*.kt'):
        os.remove(f)
    for f in glob.glob(f'{base_dir}/feature/user/domain/usecase/*.kt'):
        os.remove(f)

    print('Moves and deletes completed')

process_files()
