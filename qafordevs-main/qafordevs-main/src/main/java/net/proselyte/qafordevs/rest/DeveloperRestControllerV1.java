package net.proselyte.qafordevs.rest;

import lombok.RequiredArgsConstructor;
import net.proselyte.qafordevs.dto.DeveloperDto;
import net.proselyte.qafordevs.dto.ErrorDto;
import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.exception.DeveloperNotFoundException;
import net.proselyte.qafordevs.exception.DeveloperWithDuplicateEmailException;
import net.proselyte.qafordevs.service.DeveloperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1/developers")
@RequiredArgsConstructor
public class DeveloperRestControllerV1 {

    private final DeveloperService developerService;


    @PostMapping
    public ResponseEntity<?> createDeveloper(@RequestBody DeveloperDto dto) {// <?> означает что возвращаемое значание может быть не одного типа,
        //тоесть разные значания (в данном случае у нас 2 возвращаемых значения: 1- DeveloperDto. 2-ErrorDto )
        try {
            DeveloperEntity entity = dto.toEntity();//переделываем из ДТО в Энтити
            DeveloperEntity createdDeveloper = developerService.saveDeveloper(entity);//сохраняем энтити
            DeveloperDto result = DeveloperDto.fromEntity(createdDeveloper);//переводим сохраненную Энтити в ДТО
            return ResponseEntity.ok(result);//и возвращаем ДТО в ответе
        } catch (DeveloperWithDuplicateEmailException e) {//у нас в работе метода saveDeveloper может возникнуть наше исключение
            //DeveloperWithDuplicateEmailException, поэтому мы его обрабатываем и отправляем также в ответе
            return ResponseEntity.badRequest()
                    .body(ErrorDto.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateDeveloper(@RequestBody DeveloperDto dto) {
        try {
            DeveloperEntity entity = dto.toEntity();//мапим на нашу сущьность сущьность будет с ID так как нужен ID для поиска ее в БД
            DeveloperEntity updatedEntity = developerService.updateDeveloper(entity);//передаем ее дальше в сервис
            DeveloperDto result = DeveloperDto.fromEntity(updatedEntity);//мапим в ДТО
            return ResponseEntity.ok(result);//отдаем ДТО в ответе
        } catch (DeveloperNotFoundException e) {//если же не нашли девелопера и вылетела наша ошибка DeveloperNotFoundException
            return ResponseEntity.badRequest()//отдаем ответ с ошибкой
                    .body(ErrorDto.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeveloperById(@PathVariable("id") Integer id) {
        try {//также делаем в блоке try т.к. может выскочить ошибка DeveloperNotFoundException
            DeveloperEntity entity = developerService.getDeveloperById(id);//ище девелопера по ID
            DeveloperDto result = DeveloperDto.fromEntity(entity);//если нашли мапим на ДТО
            return ResponseEntity.ok(result);//и отдаем в ответ
        } catch (DeveloperNotFoundException e) {
            return ResponseEntity//если не нашли
                    .status(404)
                    .body(ErrorDto.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllDevelopers() {
        List<DeveloperEntity> entities = developerService.getAllDevelopers();//получаем всех DeveloperEntity из БД
        List<DeveloperDto> dtos = entities.stream()
                .map(DeveloperDto::fromEntity).toList();//мапим полученные сущьности из БД на ДТО
        return ResponseEntity.ok(dtos);//возвращаем ДТОшки
    }

    @GetMapping("/specialty/{specialty}")
    public ResponseEntity<?> getAllDevelopersBySpecialty(@PathVariable("specialty") String specialty) {
        List<DeveloperEntity> entities = developerService.getAllActiveBySpecialty(specialty);
        List<Object> dtos = entities.stream()
                .map(new Function<DeveloperEntity, Object>() {//реализация без лямбды, через анонимный класс
                    @Override
                    public Object apply(DeveloperEntity developerEntity) {
                        return DeveloperDto.fromEntity(developerEntity);
                    }
                })
//                .map(e -> { //можно так реализовать лямбду
//                    return DeveloperDto.fromEntity(e);
//                })
                //.map(DeveloperDto::fromEntity)//а можно так через ссылку на метод
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeveloperById(@PathVariable("id") Integer id, @RequestParam(value = "isHard", defaultValue = "false") boolean isHard) {
        //метод удаления по ID и в зависимости от переданного булевского параметра boolean isHard,
        //будет это совтовское удаление (просто будет меняться флаг статуса с ACTIVE на DELETED) или хардовское удаление (полное удаление из БД)
        try {
            if (isHard) {
                developerService.hardDeleteById(id);//если хард то удаление хард
            } else {
                developerService.softDeleteById(id);//если не хард то просто будет меняться флаг статуса с ACTIVE на DELETED
            }
            return ResponseEntity.ok().build();//возвращаем ответ об успешной операцией
        } catch (DeveloperNotFoundException e) {//если выкинута наша ошибка
            return ResponseEntity.badRequest()
                    .body(ErrorDto.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build());
        }
    }
}
