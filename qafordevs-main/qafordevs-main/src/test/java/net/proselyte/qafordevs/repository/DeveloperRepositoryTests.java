package net.proselyte.qafordevs.repository;

import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.entity.Status;
import net.proselyte.qafordevs.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class DeveloperRepositoryTests {

    // Transient сущьность до сохранения в БД тоесть без ID
    // Persisted уже после сохраниния в БД с сгенерировынным ID от БД

    @Autowired//подтягиваем наш репозиторий
    private DeveloperRepository developerRepository;

    @BeforeEach
    public void setUp() {
        developerRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save developer functionality")//просто информационная строка
    public void givenDeveloperObject_whenSave_thenDeveloperIsCreated() {
        //given
        DeveloperEntity developerToSave = DataUtils.getJohnDoeTransient();//получаем через вспомогательный класс DataUtils сущьность DeveloperEntity
        //when
        DeveloperEntity savedDeveloper = developerRepository.save(developerToSave);//сохраняем в БД.
        //Метод .save ОТДАЕТ СОХРАНЯЕМОЮ СУЩЬНОСТЬ И К ТОМУЖЕ ОТДАЕТ ЕЕ С ID КОТОРЫЙ ГЕНЕРИРУЕТ УЖЕ БД, ПОЭТОМУ МЫ НИЖЕ
        //МОЖЕМ ПРОВЕРИТЬ ПОЛЕ ID СУЩЬНОСТИ НА НАЛИЧИЕ (НЕ НУЛЛ)
        //then
        assertThat(savedDeveloper).isNotNull();//проверяем после сохранения сущьность не нулл
        assertThat(savedDeveloper.getId()).isNotNull();//проверяем ID на нулл,
        //так как у нас ID генерируется базой (@GeneratedValue(strategy = GenerationType.IDENTITY))
    }

    @Test//изменение эмайла у сущьности уже находящейся в БД
    @DisplayName("Test update developer functionality")
    public void givenDeveloperToUpdate_whenSave_thenEmailIsChanged() {
        //given
        String updatedEmail = "updated@mail.com";
        DeveloperEntity developerToCreate = DataUtils.getJohnDoeTransient();//получаем через вспомогательный класс DataUtils сущьность DeveloperEntity
        developerRepository.save(developerToCreate);//сохранаем ее в БД
        //when
        DeveloperEntity developerToUpdate = developerRepository.findById(developerToCreate.getId())
                .orElse(null);//получаем по ID ее из бд
        developerToUpdate.setEmail(updatedEmail);//устанавливаем новые email
        DeveloperEntity updatedDeveloper = developerRepository.save(developerToUpdate);//сохраняем обновленную сущьность в БД
        //then
        assertThat(updatedDeveloper).isNotNull();//проверяем что сущьность сохранилась в БД
        assertThat(updatedDeveloper.getEmail()).isEqualTo(updatedEmail);//и проверяем на совпадение с изменненым эмайлом
    }

    @Test//на получение сущьности по ID из БД
    @DisplayName("Test get developer by id functionality")
    public void givenDeveloperCreated_whenGetById_thenDeveloperIsReturned() {
        //given
        DeveloperEntity developerToSave = DataUtils.getJohnDoeTransient();//получаем через вспомогательный класс DataUtils сущьность DeveloperEntity
        developerRepository.save(developerToSave);//сохранаем ее в БД
        //when
        DeveloperEntity obtainedDeveloper = developerRepository.findById(developerToSave.getId()).orElse(null);//получаем по ID ее из бд
        //then
        assertThat(obtainedDeveloper).isNotNull();//проверка на нулл
        assertThat(obtainedDeveloper.getEmail()).isEqualTo("john.doe@mail.com");//проверка по мылу что сохранилась именно наша сущьность
    }

    @Test//когда сущьность не найденна по ID
    @DisplayName("Test developer not found functionality")
    public void givenDeveloperIsNotCreated_whenGetById_thenOptionalIsEmpty() {
        //given

        //when
        DeveloperEntity obtainedDeveloper = developerRepository.findById(1).orElse(null);//Ищем сущьность в БД которой там нет
        //Метод .findById возвращает Optional, и там будет нулл
        //then
        assertThat(obtainedDeveloper).isNull();//проверяем что там нулл
    }

    @Test//плучения всех сущьностей из БД
    @DisplayName("Test get all developers functionality")
    public void givenThreeDevelopersAreStored_whenFindAll_thenAllDeveloperAreReturned() {
        //given
        DeveloperEntity developer1 = DataUtils.getJohnDoeTransient();
        DeveloperEntity developer2 = DataUtils.getFrankJonesTransient();
        DeveloperEntity developer3 = DataUtils.getMikeSmithTransient();

        developerRepository.saveAll(List.of(developer1, developer2, developer3));//сохраняем все сущности в БД
        //when
        List<DeveloperEntity> obtainedDevelopers = developerRepository.findAll();//obtained-полученные.
        //then
        assertThat(CollectionUtils.isEmpty(obtainedDevelopers)).isFalse();//CollectionUtils.isEmpty данный метод проверяет коллекцию на нулл и на пустоту (Empty)
    }

    @Test//нашего кастомного метода писку по эмайлу
    @DisplayName("Test get developer by email functionality")
    public void givenDeveloperSaved_whenGetByEmail_thenDeveloperIsReturned() {
        //given
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();
        developerRepository.save(developer);
        //when
        DeveloperEntity obtainedDeveloper = developerRepository.findByEmail(developer.getEmail());
        //then
        assertThat(obtainedDeveloper).isNotNull();
        assertThat(obtainedDeveloper.getEmail()).isEqualTo(developer.getEmail());
    }

    @Test//кастомного метода поиска с применением аннотации @Query
    @DisplayName("Test get all active developers by specialty functionality")
    public void givenThreeDevelopersAndTwoAreActive_whenFindAllActiveBySpecialty_thenReturnOnlyTwoDevelopers() {
        //given
        DeveloperEntity developer1 = DataUtils.getJohnDoeTransient();
        DeveloperEntity developer2 = DataUtils.getMikeSmithTransient();
        DeveloperEntity developer3 = DataUtils.getFrankJonesTransient();

        developerRepository.saveAll(List.of(developer1, developer2, developer3));//сохраняем сущьности в БД
        //when
        List<DeveloperEntity> obtainedDevelopers = developerRepository.findAllActiveBySpecialty("Java");
        //.findAllActiveBySpecialty("Java"); этот кастомный метод который мы определили в репозитории ищет по статусу активных и переданной специальности
        //then
        assertThat(CollectionUtils.isEmpty(obtainedDevelopers)).isFalse();//проверяем чтобы возвращаемая коллекция из БД не нулл
        assertThat(obtainedDevelopers.size()).isEqualTo(2);//и размер ее 2, так как мы передали 2 активных сущьности
    }

    @Test//удаления сущьности
    @DisplayName("Test delete developer by id functionality")
    public void givenDeveloperIsSaved_whenDeleteById_thenDeveloperIsRemovedFromDB() {
        //given
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();//получам сущьность из вспомогательного класса
        developerRepository.save(developer);//сохраняем в БД
        //when
        developerRepository.deleteById(developer.getId());//удаляем по ID
        //then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(developer.getId()).orElse(null);//вытаскиваем опшинал (пустой)
        assertThat(obtainedDeveloper).isNull();//проверяем на нулл
    }
}
