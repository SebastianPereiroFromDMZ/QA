package net.proselyte.qafordevsreactive.rest;

import lombok.RequiredArgsConstructor;
import net.proselyte.qafordevsreactive.dto.DeveloperDto;
import net.proselyte.qafordevsreactive.service.DeveloperService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/developers")
public class DeveloperRestControllerV1 {

    private final DeveloperService developerService;

    @PostMapping
    public Mono<?> createDeveloper(@RequestBody DeveloperDto developerDto) {
        return developerService.createDeveloper(developerDto.toEntity())
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @PutMapping
    public Mono<?> updateDeveloper(@RequestBody DeveloperDto developerDto) {
        return developerService.updateDeveloper(developerDto.toEntity())
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @GetMapping
    public Flux<?> getAll() {
        return developerService.getAll()
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @GetMapping("/specialty/{specialty}")
    public Flux<?> getAllBySpecialty(@PathVariable("specialty") String specialty) {
        return developerService.findAllActiveBySpecialty(specialty)
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @GetMapping("/{id}")
    public Mono<?> getById(@PathVariable("id") Integer id) {
        return developerService.getById(id)
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @DeleteMapping("/{id}")
    public Mono<?> deleteById(@PathVariable("id") Integer id, @RequestParam(value = "isHard", defaultValue = "false") boolean isHard) {
        if(isHard) {
            return developerService.hardDeleteById(id);
        }
        return developerService.softDeleteById(id);
    }
}
