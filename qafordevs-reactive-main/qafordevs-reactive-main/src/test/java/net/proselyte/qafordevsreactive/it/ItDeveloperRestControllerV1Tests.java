package net.proselyte.qafordevsreactive.it;

import net.proselyte.qafordevsreactive.config.PostgreTestcontainerConfig;
import net.proselyte.qafordevsreactive.dto.DeveloperDto;
import net.proselyte.qafordevsreactive.entity.DeveloperEntity;
import net.proselyte.qafordevsreactive.repository.DeveloperRepository;
import net.proselyte.qafordevsreactive.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(PostgreTestcontainerConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItDeveloperRestControllerV1Tests {

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    public void setUp() {
        developerRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.status").isEqualTo("ACTIVE");

    }

    @Test
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDtoWithDuplicateEmail_whenCreateDeveloper_thenExceptionIsThrown() {
        //given
        String duplicateEmail = "duplicate@mail.com";
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
        dto.setEmail(duplicateEmail);
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        entity.setEmail(duplicateEmail);
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.post()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_DUPLICATE_EMAIL")
                .jsonPath("$.errors[0].message").isEqualTo("Developer with defined email already exists");
    }

    @Test
    @DisplayName("Test update developer functionality")
    public void givenDeveloperDto_whenUpdateDeveloper_thenSuccessResponse() {
        //given
        String updatedEmail = "updated@mail.com";
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        entity.setEmail(updatedEmail);
        developerRepository.save(entity).block();

        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
        dto.setId(entity.getId());
        dto.setEmail(updatedEmail);
        //when
        WebTestClient.ResponseSpec result = webTestClient.put()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(entity.getId())
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.email").isEqualTo(updatedEmail)
                .jsonPath("$.status").isEqualTo("ACTIVE");

    }

    @Test
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDtoWithIncorrectId_whenUpdateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
        //when
        WebTestClient.ResponseSpec result = webTestClient.put()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test
    @DisplayName("Test get all developers functionality")
    public void givenThreeDeveloper_whenGetAll_thenDevelopersAreReturned() {
        //given
        DeveloperEntity entity1 = DataUtils.getJohnDoeTransient();
        DeveloperEntity entity2 = DataUtils.getFrankJonesTransient();
        DeveloperEntity entity3 = DataUtils.getMikeSmithTransient();

        developerRepository.saveAll(List.of(entity1, entity2, entity3)).blockLast();
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.size()").isEqualTo(3);
    }


    @Test
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetById_thenDeveloperIsReturned() {
        //given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers/" + entity.getId())
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.firstName").isEqualTo(entity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(entity.getLastName())
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenExceptionIsThrown() {
        //given
        //when
        WebTestClient.ResponseSpec result = webTestClient.get()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test
    @DisplayName("Test soft delete developer by id functionality")
    public void givenId_whenSoftDeleteById_thenSuccessResponse() {
        //given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/" + entity.getId())
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }


    @Test
    @DisplayName("Test soft delete developer by incorrect id functionality")
    public void givenIncorrectId_whenSoftDeleteById_thenExceptionIsThrown() {
        //given
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test
    @DisplayName("Test hard delete developer by id functionality")
    public void givenId_whenHardDeleteById_thenSuccessResponse() {
        //given
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/" + entity.getId() + "?isHard=true")
                .exchange();
        //then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(entity.getId()).block();
        assertThat(obtainedDeveloper).isNull();
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test hard delete developer by incorrect id functionality")
    public void givenIncorrectId_whenHardDeleteById_thenExceptionIsThrown() {
        //given
        //when
        WebTestClient.ResponseSpec result = webTestClient.delete()
                .uri("/api/v1/developers/1?isHard=true")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }
}
