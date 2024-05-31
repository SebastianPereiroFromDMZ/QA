package net.proselyte.qafordevs.repository;

import net.proselyte.qafordevs.entity.DeveloperEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DeveloperRepository extends JpaRepository<DeveloperEntity, Integer> {

    DeveloperEntity findByEmail(String email);//делаем свой кастомный запрос по имени метода, ХИБЕРНЭЙТ сам сделает запрос
    //к БД на основании имени метода

    @Query("SELECT d FROM DeveloperEntity d WHERE d.status = 'ACTIVE' AND d.specialty = ?1")
    //делаем конкретный запрос к БД с помощью аннотации @Query.
    //d.specialty = ?1 здесь под ?1 подставляется из параметров метода String specialty
    List<DeveloperEntity> findAllActiveBySpecialty(String specialty);
}
