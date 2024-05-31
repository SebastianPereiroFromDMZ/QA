package net.proselyte.qafordevs.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.proselyte.qafordevs.dto.DeveloperDto;
import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.exception.DeveloperNotFoundException;
import net.proselyte.qafordevs.exception.DeveloperWithDuplicateEmailException;
import net.proselyte.qafordevs.service.DeveloperService;
import net.proselyte.qafordevs.util.DataUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest
public class DeveloperRestControllerV1Tests {

    @Autowired//для того что бы генерировать запросы
    private MockMvc mockMvc;

    @Autowired//для работы с ДТОшками, что бы их преобразовывать в JSON и обратно, важно чтобы ObjectMapper
    //был из import com.fasterxml.jackson.databind.ObjectMapper тоесть из джексона а не из тесконтейнера
    private ObjectMapper objectMapper;

    @MockBean//мокаем наш сервис
    private DeveloperService developerService;

    @Test
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() throws Exception {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();//берем транзиентного девелопера
        DeveloperEntity entity = DataUtils.getJohnDoePersisted();//берем персистентного девелопера
        BDDMockito.given(developerService.saveDeveloper(any(DeveloperEntity.class)))//задаем логику нашей заглушке сервиса: при вызове .saveDeveloper
                .willReturn(entity);//возвращаем персистентного девелопера
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/developers")//MockMvc как написанно выше
                //предназначен для конструирования запроса, и его метод .perform эвляется выполнением чего то, также указываем какой метод будет: post
                .contentType(MediaType.APPLICATION_JSON)//указываем контент тайп
                .content(objectMapper.writeValueAsString(dto)));//говорим что приходит в теле этого запроса, а приходит естественно ДТО
        //then
        result
                .andDo(MockMvcResultHandlers.print())//выведем в печать (консоль) что у нас в результе
                .andExpect(MockMvcResultMatchers.status().isOk())//проверяем что статус ок
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))//id не нулл
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))//проверяем firstName - John
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))//проверяем lastName - Doe
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));//проверяем status - ACTIVE
    }

    @Test//проверки на дупликат эмайла тру (такой эмайл есть)
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDeveloperDtoWithDuplicateEmail_whenCreateDeveloper_thenErrorResponse() throws Exception {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();//берем транзиентного девелопера
        BDDMockito.given(developerService.saveDeveloper(any(DeveloperEntity.class)))//задаем логуку заглушке сервиса
                .willThrow(new DeveloperWithDuplicateEmailException("Developer with defined email is already exists"));//где при вызове
        //метода saveDeveloper сервиса developerService вылетает ошибка new DeveloperWithDuplicateEmailException
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/developers")//также делаем запрос по указанному эндпоинту
                .contentType(MediaType.APPLICATION_JSON)//тип контента
                .content(objectMapper.writeValueAsString(dto)));//тело контента
        //then
        result
                .andDo(MockMvcResultHandlers.print())//в консоль печатание
                .andExpect(MockMvcResultMatchers.status().isBadRequest())//проверяем что это бэд реквест
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))//статус 400
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer with defined email is already exists")));//сооб-
        //щение
    }

    @Test//тест изменения девелопера
    @DisplayName("Test update developer functionality")
    public void givenDeveloperDto_whenUpdateDeveloper_thenSuccessResponse() throws Exception {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();//берем персистентного девелопера (что бы был с ID)
        DeveloperEntity entity = DataUtils.getJohnDoePersisted();//так же берем персистентного девелопера
        BDDMockito.given(developerService.updateDeveloper(any(DeveloperEntity.class)))//задаем логику заглушке сервиса
                .willReturn(entity);//когда при вызове метода updateDeveloper сервиса developerService возвращается entity
        //when
        ResultActions result = mockMvc.perform(put("/api/v1/developers")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
        //then
        result//смотрим резулт
                .andDo(MockMvcResultHandlers.print())//выводим его в консоль
                .andExpect(MockMvcResultMatchers.status().isOk())//что статус ок
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))//проверяем id на не нулл
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))//проверяем firstName - John
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))//проверяем lastName - Doe
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));//проверяем status - ACTIVE
    }

    @Test//тест прокидывания исключения при изменении девелопера с неккоректным id
    @DisplayName("Test update developer with incorrect id")
    public void givenDeveloperDtoWithIncorrectId_whenUpdateDeveloper_thenErrorResponse() throws Exception {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();//берем транзиентного девелопера
        BDDMockito.given(developerService.updateDeveloper(any(DeveloperEntity.class)))//задаем логуку заглушке
                .willThrow(new DeveloperNotFoundException("Developer not found"));//где при вызове мотода updateDeveloper
        //сервиса developerService прокидывается исключение DeveloperNotFoundException
        //when
        ResultActions result = mockMvc.perform(put("/api/v1/developers")//делаем запрос на указанный эндпоинт
                .contentType(MediaType.APPLICATION_JSON)//тип контента
                .content(objectMapper.writeValueAsString(dto)));//тело контента
        //then
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())//смотрим статус
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(400)))//проверяем 400 ошибку
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));//проверяем ответ об ошибки
    }

    @Test//получаем девелопера по ID
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenSuccessResponse() throws Exception {
        //given
        BDDMockito.given(developerService.getDeveloperById(anyInt()))//задаем логику заглушки
                .willReturn(DataUtils.getJohnDoePersisted());//при вызове метода getDeveloperById сервиса developerService возвращается getJohnDoePersisted()
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/developers/1")//делаем get запрос на /api/v1/developers/1
                .contentType(MediaType.APPLICATION_JSON));
        //then
        result //проверяем резулт:
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstName", CoreMatchers.is("John")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastName", CoreMatchers.is("Doe")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is("ACTIVE")));
    }

    @Test//проброса исключения при попытки получения девелопера по неккоректному id
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() throws Exception {
        //given
        BDDMockito.given(developerService.getDeveloperById(anyInt()))//задаем логику заглушки
                .willThrow(new DeveloperNotFoundException("Developer not found"));//что при вызове метода getDeveloperById сервиса
        //developerService выбрасывается исключение new DeveloperNotFoundException("Developer not found")
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/developers/1")//делаем get запрос на указанный эндпоинт
                .contentType(MediaType.APPLICATION_JSON));
        //then
        result//проверяем резулт
                .andDo(MockMvcResultHandlers.print())//выводим в консоль
                .andExpect(MockMvcResultMatchers.status().isNotFound())//статус не найден
                .andExpect(MockMvcResultMatchers.jsonPath("$.status", CoreMatchers.is(404)))//смотрим статус
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is("Developer not found")));//смотрим сообщение
    }

    @Test//смены статуса с АКТИВ на ДЕЛЕТЕД
    @DisplayName("Test soft delete by id functionality")
    public void givenId_whenSoftDelete_thenSuccessResponse() throws Exception {
        //given
        BDDMockito.doNothing().when(developerService).softDeleteById(anyInt());//не делай ничего .doNothing() когда
        //.when у сервиса developerService вызовется метод softDeleteById
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        verify(developerService, times(1)).softDeleteById(anyInt());//проверяем что именно был вызван метод softDeleteById
        result//проверяем ответ
                .andDo(MockMvcResultHandlers.print())//выводим информацию в консоль
                .andExpect(MockMvcResultMatchers.status().isOk());//смотрим статус
    }

    @Test//выброса исключения при софт (смены статуса с АКТИВ на ДЕЛЕТЕД) удалении
    @DisplayName("Test soft delete by incorrect id functionality")
    public void givenIncorrectId_whenSoftDelete_thenErrorResponse() throws Exception {
        //given
        BDDMockito.doThrow(new DeveloperNotFoundException("Developer not found"))//задаем логику заглушке:
                //выброси исключение new DeveloperNotFoundException("Developer not found")
                .when(developerService).softDeleteById(anyInt());//когда when будет вызван softDeleteById сервиса developerService
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        verify(developerService, times(1)).softDeleteById(anyInt());//проверяем что именно был вызван метод softDeleteById
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());//проверяем что плохой запрос
    }

    @Test//окончательного удаления девелопера из БД
    @DisplayName("Test hard delete by id functionality")
    public void givenId_whenHardDelete_thenSuccessResponse() throws Exception {
        //given
        BDDMockito.doNothing().when(developerService).hardDeleteById(anyInt());//также задаем логику заглушке,
        //говорим что просто будет вызван метод hardDeleteById и не надо ничего возвращать так как у нас сам метод void
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1?isHard=true")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        verify(developerService, times(1)).hardDeleteById(anyInt());//проверяем что бы был вызван метод hardDeleteById 1 раз
        result//проверяем резулт
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());//смотрим что бы статус был ОК
    }

    @Test//выброса исключения при окончательном удалении
    @DisplayName("Test hard delete by incorrect id functionality")
    public void givenIncorrectId_whenHardDelete_thenErrorResponse() throws Exception {
        //given
        BDDMockito.doThrow(new DeveloperNotFoundException("Developer not found"))//задаем логику заглушке, что выкидывается исключение  DeveloperNotFoundException
                .when(developerService).hardDeleteById(anyInt());//при вызове hardDeleteById
        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/developers/1?isHard=true")//делаем запрос
                .contentType(MediaType.APPLICATION_JSON));
        //then
        verify(developerService, times(1)).hardDeleteById(anyInt());//проверяем что бы был вызван метод hardDeleteById 1 раз
        result
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());//проверяем что это у нас плохой запрос
    }

}
