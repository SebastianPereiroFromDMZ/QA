package net.proselyte.qafordevs.service;

import lombok.RequiredArgsConstructor;
import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.entity.Status;
import net.proselyte.qafordevs.exception.DeveloperNotFoundException;
import net.proselyte.qafordevs.exception.DeveloperWithDuplicateEmailException;
import net.proselyte.qafordevs.repository.DeveloperRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;

    //метод сохранения нашей сущьности в БД
    @Override
    public DeveloperEntity saveDeveloper(DeveloperEntity developer) {
        DeveloperEntity duplicateCandidate = developerRepository.findByEmail(developer.getEmail());//поле Email у нас уникальное,
        //поэтому мы ищем в БД нет ли там такойже сущьности как и ту которую сохраняем (ищем по эмайлу)

        if (Objects.nonNull(duplicateCandidate)) {//если есть в БД такой же обьект
            throw new DeveloperWithDuplicateEmailException("Developer with defined email is already exists");//кидаем наше кастомное исключение
        }
        developer.setStatus(Status.ACTIVE);
        return developerRepository.save(developer);//и передаем его на уровень ниже в репозиторий
    }

    @Override
    public DeveloperEntity updateDeveloper(DeveloperEntity developer) {//метод изменения сущьности в БД
        boolean isExists = developerRepository.existsById(developer.getId());//проверяем есть ли такая сущьнсть в БД

        if (!isExists) { //если isExists фолс
            throw new DeveloperNotFoundException("Developer not found");//кидаем наше кастомное исключение
        }
        return developerRepository.save(developer);//перезаписываем сущбность
    }

    @Override
    public DeveloperEntity getDeveloperById(Integer id) {//метод поиска сущьнсти по ID
        return developerRepository.findById(id)//ищем
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));//если не находим
    }

    @Override
    public DeveloperEntity getDeveloperByEmail(String email) {//поиск мо эмайлу
        DeveloperEntity obtainedDeveloper = developerRepository.findByEmail(email);//смотрим в БД

        if (Objects.isNull(obtainedDeveloper)) {//если ничего не найдено
            throw new DeveloperNotFoundException("Developer not found");//кидаем исключение
        }
        return obtainedDeveloper;//отдаем найденного
    }

    @Override
    public List<DeveloperEntity> getAllDevelopers() {//возвращаем только активных сущьностей (Status.ACTIVE)
        return developerRepository.findAll()//ищем всех
                .stream().filter(d -> {//стримуем и фильтруем
                    return d.getStatus().equals(Status.ACTIVE);//по статусу
                })
                .collect(Collectors.toList());//собираем в лист полученные экземпляры
    }

    @Override
    public List<DeveloperEntity> getAllActiveBySpecialty(String specialty) {//просто ищем по нашему кастомному методу
        return developerRepository.findAllActiveBySpecialty(specialty);
    }

    @Override
    public void softDeleteById(Integer id) {//меняем статус сущьности с Status.ACTIVE на Status.DELETED
        DeveloperEntity obtainedDeveloper = developerRepository.findById(id)//ищем по ID
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));//если не найден

        obtainedDeveloper.setStatus(Status.DELETED);//меняем статус
        developerRepository.save(obtainedDeveloper);//апдейтим
    }

    @Override
    public void hardDeleteById(Integer id) {//удаляем из БД
        DeveloperEntity obtainedDeveloper = developerRepository.findById(id)//Ищем
                .orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));//если не найден
        developerRepository.deleteById(obtainedDeveloper.getId());//удаляем
    }


    //Тест лямбды выше .orElseThrow(() -> new DeveloperNotFoundException("Developer not found"));
    //ниже тоже самое но без лямбды
    public void hardDeleteByIdо(Integer id) {
        DeveloperEntity obtainedDeveloper = developerRepository.findById(id)
                .orElseThrow(new Supplier<RuntimeException>() {
                    @Override
                    public RuntimeException get() {
                        return new DeveloperNotFoundException("Developer not found");
                    }
                });
    }
}
