package net.proselyte.qafordevs.service;

import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.exception.DeveloperNotFoundException;
import net.proselyte.qafordevs.exception.DeveloperWithDuplicateEmailException;
import net.proselyte.qafordevs.repository.DeveloperRepository;
import net.proselyte.qafordevs.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeveloperServiceImplTests {

//    //можно замокать репозитормй так, и встроить в сервис ка книже, но лучше делать так как еще ниже с аннотациями
//    private DeveloperRepository developerRepository = Mockito.mock(DeveloperRepository.class);
//    private DeveloperServiceImpl serviceUnderTest = new DeveloperServiceImpl(developerRepository);

    @Mock//говорим что это заглушка, и ее поведение мы будем описывать в тестах
    private DeveloperRepository developerRepository;
    @InjectMocks//инжектируем сюда ее и над классом ставим @ExtendWith, без этой аннотации спринг не сделает инжект
    private DeveloperServiceImpl serviceUnderTest;



    @Test//сохранения сущьности в репозиторий
    @DisplayName("Test save developer functionality")
    public void givenDeveloperToSave_whenSaveDeveloper_thenRepositoryIsCalled() {
        //given
        DeveloperEntity developerToSave = DataUtils.getJohnDoeTransient();//берем сущьность из вспомогательного класса
        BDDMockito.given(developerRepository.findByEmail(anyString()))//так как у нас репозиторий заглушка, нам необходимо ПРОПИСАТЬ ЕГО ПОВЕДЕНИЕ,
                // для этого есть специальный класс BDDMockito
                .willReturn(null);//говорим что если в вызов метода findByEmail передаем любую строку то возвратим налл
        //(это нам необходимо что бы не было исключения в методе serviceUnderTest.saveDeveloper проверка на такойже эмайл, поэтому возвращаем налл)
        BDDMockito.given(developerRepository.save(any(DeveloperEntity.class)))//далее задаем поведение и говорим что при вызове метода
                // developerRepository.save передаем любую сущьность из КЛАССА DeveloperEntity.class
                .willReturn(DataUtils.getJohnDoePersisted());//и возвращаем персистентную сущьность DataUtils.getJohnDoePersisted()
        //when
        DeveloperEntity savedDeveloper = serviceUnderTest.saveDeveloper(developerToSave);//и тогда при вызове этого метода сервиса заглушка репозитория
        //на вернет персистентного девелопера (девелопера которого сохранила БД и присвоила ему ID)
        //then
        assertThat(savedDeveloper).isNotNull();//проверяем на нулл
    }

    @Test//на сохранение девелопера НО у нас уже есть девелопер в БД с таким эмайлом (дубликат)
    @DisplayName("Test save developer with duplicate email functionality")
    public void givenDeveloperToSaveWithDuplicateEmail_whenSaveDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperEntity developerToSave = DataUtils.getJohnDoeTransient();//берем девелопера
        BDDMockito.given(developerRepository.findByEmail(anyString()))//задаем заглушке логику: говорим что если передаем эмайл
                .willReturn(DataUtils.getJohnDoePersisted());//из бд возвращается девелопер (тоесть проверка на дубликат показывает что такой
        //девелопер с таким же эмайлом уже есть в БД) значит будет кинуто исключение
        //when
        assertThrows(//проверяем на прокинутое исключение:
                DeveloperWithDuplicateEmailException.class, () -> serviceUnderTest.saveDeveloper(developerToSave));//убедись что было прокинуто
        //конкретное исключение (DeveloperWithDuplicateEmailException.class)
        //В тот момент когда был вызван метод .saveDeveloper(developerToSave) у обьекта serviceUnderTest
        //then
        verify(developerRepository, never()).save(any(DeveloperEntity.class));//убеждаемся что метод .save обьекта developerRepository
        //не был ни разу вызван с каким либо обьектом класса DeveloperEntity.class
    }

    @Test//для изменения девелопера
    @DisplayName("Test update developer functionality")
    public void givenDeveloperToUpdate_whenUpdateDeveloper_thenRepositoryIsCalled() {
        //given
        DeveloperEntity developerToUpdate = DataUtils.getJohnDoePersisted();//получаем уже Persisted девелопера
        //от БД с ID (т.к. по логике метода .updateDeveloper(developerToUpdate) он у нас уже с ID)
        BDDMockito.given(developerRepository.existsById(anyInt()))//задаем логику заглушки репозитория: при вызове метода
                //наличия девелопера по ID в бд с любым инт значением developerRepository.existsById(anyInt())
                .willReturn(true);//возвращает тру
        BDDMockito.given(developerRepository.save(any(DeveloperEntity.class)))//при вызове .save обьекта developerRepository
                //в парметрах передается любой обьект класса DeveloperEntity.class
                .willReturn(developerToUpdate);//тогда возвращается наш персистный девелопер
        //when
        DeveloperEntity updatedDeveloper = serviceUnderTest.updateDeveloper(developerToUpdate);//вызываем метод сервиса в котором
        //определенна логика заглушки репозитория выше, и инициализируем это в обьект updatedDeveloper
        //then
        assertThat(updatedDeveloper).isNotNull();//проверяем его на нулл
        verify(developerRepository, times(1)).save(any(DeveloperEntity.class));//убеждаемся что вызвали
        //хоть 1 раз метод .save обькта developerRepository
    }

    @Test//на прокидывание исключения DeveloperNotFoundException, так как такого девелопера нет в БД
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDeveloperToUpdateWithIncorrectId_whenUpdateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperEntity developerToUpdate = DataUtils.getJohnDoePersisted();//получаем персистентного девелопера (после сохранения в БД)
        BDDMockito.given(developerRepository.existsById(anyInt()))//задаем логику заглушки:говорим при вызове метода репозитория
                .willReturn(false);//возвращается фолс, тоесть у нас нет сущьности в БД по переданногму ID
        //when
        assertThrows(//тести чтобы было брошено исключение в методе updateDeveloper как мы и прописывали логику заглушки (что такого девелопера нет в БД)
                DeveloperNotFoundException.class, () -> serviceUnderTest.updateDeveloper(developerToUpdate));
        //then
        verify(developerRepository, never()).save(any(DeveloperEntity.class));//убеждаемся что не вызван ни разу (never)
        //метод (save) для любой (any) сущьности класса (DeveloperEntity.class) потому что у нас метод updateDeveloper
        //который мы тут тестим упадет с исключением DeveloperNotFoundException
    }

    @Test//получения девелопера по ID
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenDeveloperIsReturned() {
        //given
        BDDMockito.given(developerRepository.findById(anyInt()))//пишем логику заглушки поиска девелопера по ID
                .willReturn(Optional.of(DataUtils.getJohnDoePersisted()));//возвращаем персистентного девелопера
        //when
        DeveloperEntity obtainedDeveloper = serviceUnderTest.getDeveloperById(1);//вызываем сам метод
        //then
        assertThat(obtainedDeveloper).isNotNull();//сравниваем на нулл
    }

    @Test//прокидывания исключения DeveloperNotFoundException во время поиска девелопера в БД по ID, а его там нет
    @DisplayName("Test get developer by id functionality")
    public void givenIncorrectId_whenGetById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerRepository.findById(anyInt()))//пишем логику заглушки поиска девелопера по ID
                .willThrow(DeveloperNotFoundException.class);//выбрасывается исключение
        //when
        assertThrows(DeveloperNotFoundException.class, () -> serviceUnderTest.getDeveloperById(1));//тест на выброс исключения
        //then
    }

    @Test//получения девелопера по эмайлу
    @DisplayName("Test get developer by email functionality")
    public void givenEmail_whenGetDeveloperByEmail_thenDeveloperIsReturned() {
        //given
        String email = "john.doe@mail.com";
        BDDMockito.given(developerRepository.findByEmail(anyString()))//снова задаем логику заглушке
                .willReturn(DataUtils.getJohnDoePersisted());
        //when
        DeveloperEntity obtainedDeveloper = serviceUnderTest.getDeveloperByEmail(email);//вызов тестируемого самого метода
        //then
        assertThat(obtainedDeveloper).isNotNull();//сравнение результатов
    }

    @Test//прокидывания исключения если девелопера такого нет по эмайлу
    @DisplayName("Test get developer by email functionality")
    public void givenIncorrectEmail_whenGetDeveloperByEmail_thenExceptionIsThrown() {
        //given
        String email = "john.doe@mail.com";
        BDDMockito.given(developerRepository.findByEmail(anyString()))//снова задаем логику заглушке
                .willThrow(DeveloperNotFoundException.class);//на этот раз выбрасывается исключение
        //when
        assertThrows(DeveloperNotFoundException.class, () -> serviceUnderTest.getDeveloperByEmail(email));//смотрим результат
        //then
    }

    @Test//получение все пользователей с статусом Status.ACTIVE
    @DisplayName("Test get all developers functionality")
    public void givenThreeDeveloper_whenGetAll_thenOnlyActiveAreReturned() {
        //given
        DeveloperEntity developer1 = DataUtils.getJohnDoePersisted();//получаем девелопера со статусом Status.ACTIVE
        DeveloperEntity developer2 = DataUtils.getMikeSmithPersisted();//получаем девелопера со статусом Status.ACTIVE
        DeveloperEntity developer3 = DataUtils.getFrankJonesPersisted();//получаем девелопера со статусом Status.DELETED

        List<DeveloperEntity> developers = List.of(developer1, developer2, developer3);//делаем лист из девелоперов
        BDDMockito.given(developerRepository.findAll())//задаем логику заглушке которая при
                .willReturn(developers);//которая при вызове developerRepository.findAll() возвращает прописанных выше девелоперов
        //when
        List<DeveloperEntity> obtainedDevelopers = serviceUnderTest.getAllDevelopers();//вызываем тестируемый метод
        //then
        assertThat(CollectionUtils.isEmpty(obtainedDevelopers)).isFalse();//проверяем что вершувшаяся коллекция не налл
        assertThat(obtainedDevelopers.size()).isEqualTo(2);//и что у нас 2 активных девелопера
    }

    @Test//кастомного метода с Query запросом, получении девелоперов активных с передаваемым параметром specialty
    @DisplayName("Test get all active by specialty functionality")
    public void givenThreeDevelopersAndTwoActive_whenGetAllActiveBySpecialty_thenDevelopersAreReturned() {
        //given
        DeveloperEntity developer1 = DataUtils.getJohnDoePersisted();//получаем девелопера со статусом Status.ACTIVE
        DeveloperEntity developer2 = DataUtils.getMikeSmithPersisted();//получаем девелопера со статусом Status.ACTIVE

        List<DeveloperEntity> developers = List.of(developer1, developer2);//делаем лист из девелоперов

        BDDMockito.given(developerRepository.findAllActiveBySpecialty(anyString()))//задаем логику заглушке
                .willReturn(developers);
        //when
        List<DeveloperEntity> obtainedDevelopers = serviceUnderTest.getAllActiveBySpecialty("Java");//вызываем тестируемый метод
        //then
        assertThat(CollectionUtils.isEmpty(obtainedDevelopers)).isFalse();//проверяем что вершувшаяся коллекция не налл
        assertThat(obtainedDevelopers.size()).isEqualTo(2);//и что отданная коллекция содержит 2 элемента
    }

    @Test//как бы удаления (изменения статуса с Status.ACTIVE на Status.DELETED) девелопера по ID
    @DisplayName("Test soft delete by id functionality")
    public void givenId_whenSoftDeleteById_thenRepositorySaveMethodIsCalled() {
        //given
        BDDMockito.given(developerRepository.findById(anyInt()))//задаем логику заглушке
                .willReturn(Optional.of(DataUtils.getJohnDoePersisted()));//возвращаем персистентного девелопера
        //when
        serviceUnderTest.softDeleteById(1);//вызываем тестируемый метод
        //then
        verify(developerRepository, times(1)).save(any(DeveloperEntity.class));//проверяем что у
        //заглушки developerRepository вызвался метод save 1 раз
        verify(developerRepository, never()).deleteById(anyInt());//а метод deleteById ниразу
    }

    @Test//выброса исключения как бы удаления (изменения статуса с Status.ACTIVE на Status.DELETED) девелопера по ID
    @DisplayName("Test soft delete by id functionality")
    public void givenIncorrectId_whenSoftDeleteById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerRepository.findById(anyInt()))//пишем логику заглушке
                .willReturn(Optional.empty());//и возвращаем пустой опшионал
        //when
        assertThrows(DeveloperNotFoundException.class, () -> serviceUnderTest.softDeleteById(1));//смотрим чтбы было выброшенно
        //исключение DeveloperNotFoundException из вызова нашего метода () -> serviceUnderTest.softDeleteById(1)
        //then
        verify(developerRepository, never()).save(any(DeveloperEntity.class));//и ни разу не был вызван метод save
    }

    @Test//окончательное удаление девелопера из БД
    @DisplayName("Test hard delete by id functionality")
    public void givenCorrectId_whenHardDeleteById_thenDeleteRepoMethodIsCalled() {
        //given
        BDDMockito.given(developerRepository.findById(anyInt()))//задаем логику заглушке
                .willReturn(Optional.of(DataUtils.getJohnDoePersisted()));//возвращаем девелопера по ID
        //when
        serviceUnderTest.hardDeleteById(1);//удаляем девелопера найденного по ID
        //then
        verify(developerRepository, times(1)).deleteById(anyInt());//проверяем что бы метод удаления был вызван 1 раз
    }


    @Test//выброса исключения окончательного удаления девелопера из БД которого там нет
    @DisplayName("Test hard delete by id functionality")
    public void givenIncorrectId_whenHardDeleteById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerRepository.findById(anyInt()))//задаем логику заглушке
                .willReturn(Optional.empty());//возвращаем пустой опшионал
        //when
        assertThrows(DeveloperNotFoundException.class, () -> serviceUnderTest.hardDeleteById(1));//проверяем на выброшенное
        //исключение DeveloperNotFoundException при вызове проверяемого метода () -> serviceUnderTest.hardDeleteById(1)
        //then
        verify(developerRepository, never()).deleteById(anyInt());//смотрим что бы ни разу не был вызван метод deleteById(anyInt()) так как до него
        //не должно доходить, так как выбрасывается исключение до него, смотри логику метода .hardDeleteById обьекта репозитлрия developerRepository
    }
}
