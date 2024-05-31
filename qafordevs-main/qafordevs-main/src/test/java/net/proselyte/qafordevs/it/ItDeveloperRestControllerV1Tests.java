package net.proselyte.qafordevs.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.proselyte.qafordevs.dto.DeveloperDto;
import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.entity.Status;
import net.proselyte.qafordevs.repository.DeveloperRepository;
import net.proselyte.qafordevs.util.DataUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ActiveProfiles("test")//этот весь класс запускается под профилем test
@AutoConfigureMockMvc//Эта аннотация нужна для того, чтобы появилась возможность внедрить в тестовый класс бин MockMvc
//Класс MockMvc предназначен для тестирования контроллеров. Он позволяет тестировать контроллеры без запуска http-сервера.
//То есть при выполнении тестов сетевое соединение не создается.
//С MockMvc можно писать как интеграционные тесты, так и unit-тесты.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)//SpringBootTest аннотация указывающая на интеграционные
//тесты (она подгружает полностью ввесь аппликейшн контекст)

@Testcontainers//говорим что будем работать с БД которая поднимается в докере в контейнере

//@SpringBootTest будет поднимать весь контекст приложения для теста. При использовании этой аннотации важно понимать атрибут webEnvironment.
//Без указания этого атрибута такие тесты не будут запускать встроенный контейнер сервлетов (например, Tomcat)
//и вместо этого будут использовать имитацию среды сервлетов. Следовательно, ваше приложение не будет доступно через локальный порт.


public class ItDeveloperRestControllerV1Tests extends AbstractRestControllerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeveloperRepository developerRepository;//инжектим репозиторий, а сервис будет подниматься потому что у нас стартует контроллер

    @BeforeEach
    public void setUp() {
        developerRepository.deleteAll();
    }


    @Test//интеграционный тест создания девелопера без заглушек
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() throws Exception {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();//создаем транзиентного девелопера
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/developers")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
        //then
        result//проверяем
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));
    }

    @Test//интеграционный тест на дубликат почты
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDeveloperDtoWithDuplicateEmail_whenCreateDeveloper_thenErrorResponse() throws Exception {
        //given
        String duplicateEmail = "duplicate@mail.com";//делаем рандомную почту
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();//берем девелопера
        developer.setEmail(duplicateEmail);//присваиваем ему нашу рандомную почту
        developerRepository.save(developer);//сохраняем в БД
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();//берем ДТОшку
        dto.setEmail(duplicateEmail);//присваеваем ему все тотже эмайл
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/developers")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));//в тело запроса вкладываем ДТО с нашей почтой
        //then
        result//проверяем что должна вылететь ошибка
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer with defined email is already exists")));
    }

    @Test//интеграционный тест изменения девелопера в БД
    @DisplayName("Test update developer functionality")
    public void givenDeveloperDto_whenUpdateDeveloper_thenSuccessResponse() throws Exception {
        //given
        String updatedEmail = "updated@mail.com";//создаем новую почту
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();//берем девелопера без ID
        developerRepository.save(entity);//сохраняем в БД и выше девелопер который был без ID ему присваевается ID !!!!

        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();//берем персистентного девелопера
        dto.setId(entity.getId());//устанавливам ID
        dto.setEmail(updatedEmail);//устанавливаем новую почту новую
        //when
        ResultActions result = mockMvc.perform(put("/api/v1/developers")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
        //then
        result//проверяем
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", CoreMatchers.is(updatedEmail)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));
    }

    @Test//интеграционный тест выброса ошибки некорректного id обновления девелопера
    @DisplayName("Test update developer with incorrect id")
    public void givenDeveloperDtoWithIncorrectId_whenUpdateDeveloper_thenErrorResponse() throws Exception {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();//берем ДТОшку персистентную
        //when
        ResultActions result = mockMvc.perform(put("/api/v1/developers")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));//с нашей дтошкой
        //then
        result//проверяем выброс ошибки т.к. у нас БД пуста и соответственно по ID никого нет
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));
    }

    @Test//интеграционный тест получения девелопера по ID
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenSuccessResponse() throws Exception {
        //given
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();//берем девелопера транзиентного
        developerRepository.save(developer);//сохраняем в БД, девелопер (developer) становится персистентным
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/developers/" + developer.getId())//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        result//проверяем
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));
    }

    @Test//интерграционный тест выброса ошибки при поиска девелопера с некорректным ID
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() throws Exception {
        //given

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/developers/1")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        result//проверяем выброс ошибки т.к. БД пуста
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));
    }

    @Test//интеграционный тест софт удаления (изменения статуса)
    @DisplayName("Test soft delete by id functionality")
    public void givenId_whenSoftDelete_thenSuccessResponse() throws Exception {
        //given
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();//берем транзиентного девелопера
        developerRepository.save(developer);//сохряняем в бд, developer становится персистентным (БД присваевает ему ID)
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/" + developer.getId())//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(developer.getId()).orElse(null);//вытаскиваем девелопера из БД
        //ниже в ассертах мы проверяем результат так как в самом обьекте result не будет возвращаемого тела потому что методы:
        //public void softDeleteById(Integer id) и  public void hardDeleteById(Integer id) они войдовые, ничего не возвращают,
        //поэтому и тело ответа будет без обьекта девелопера тоеть:
        //.andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE"))) вот так мы не сможем, т.к. там просто его нет )))
        assertThat(obtainedDeveloper).isNotNull();//проверяем что он не нулл
        assertThat(obtainedDeveloper.getStatus()).isEqualTo(Status.DELETED);//проверяем что статус изменился на DELETED
        result//смотрим резулт
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test//интеграционный тест выброса исключения при неккоректным ID софт удаления
    @DisplayName("Test soft delete by incorrect id functionality")
    public void givenIncorrectId_whenSoftDelete_thenErrorResponse() throws Exception {
        //given
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        result//проверяем выброс ошибки т.к. БД пуста
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));
    }

    @Test//интеграционный тест удаления окончательного из БД девелопера
    @DisplayName("Test hard delete by id functionality")
    public void givenId_whenHardDelete_thenSuccessResponse() throws Exception {
        //given
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();//берем девелопера без ID
        developerRepository.save(developer);//сохраняем в БД, метод save присваивает девелоперу ID
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/" + developer.getId() + "?isHard=true")//делаем запрос с параметром в квери:
                //"?isHard=true",  так как у нас метод в контроллере преложения deleteDeveloperById делит логику на два пути
                //в зависимости указан ли этот параметр в квери или нет (совтовоу удаление либо хардовое)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(developer.getId()).orElse(null);//достаем девелопера из БД
        assertThat(obtainedDeveloper).isNull();//проверка что он нулл т.к. его нет в БД )))
        result//проверяем ответ что он ОК
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test//тест выброса ошибки при хард удалении что такого девелопера нет
    @DisplayName("Test hard delete by incorrect id functionality")
    public void givenIncorrectId_whenHardDelete_thenErrorResponse() throws Exception {
        //given
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1?isHard=true")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        result//проверяем ответ бэд реквест
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));
    }
}
