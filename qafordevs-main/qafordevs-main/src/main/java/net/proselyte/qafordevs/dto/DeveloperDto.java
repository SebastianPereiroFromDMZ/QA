package net.proselyte.qafordevs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.proselyte.qafordevs.entity.DeveloperEntity;
import net.proselyte.qafordevs.entity.Status;

import static net.proselyte.qafordevs.entity.DeveloperEntity.*;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)//В работе с Jackson часто встречается задача, когда требуется игнорировать поля с null
//значениями при сериализации. Например, у нас есть класс с некоторыми полями, и мы хотим игнорировать определенные поля, если их значение равно null.
//В Jackson для решения этой задачи используется аннотация @JsonInclude. Эта аннотация определяет правила включения свойств при сериализации.
//Используя ее, можно указать Jackson игнорировать поля с null значениями. Аннотацию @JsonInclude можно применить как к классу, так и к отдельным полям.
//Если применить аннотацию к классу, то она будет действовать на все поля этого класса

//JSON (от англ JavaScript Object Notation) — это текстовый формат для представления структурированных данных на основе синтаксиса объектов JavaScript.
//Благодаря своему гибкому и простому формату он стал чрезвычайно популярным. По сути, он следует модели карты «ключ-значение»,
//допускающей вложенные объекты и массивы.

//Jackson в основном известен как библиотека, которая конвертирует строки JSON и простые объекты Java (англ POJO — Plain Old Java Object).
//Он также поддерживает многие другие форматы данных, такие как CSV, YML и XML.
//Многие предпочитают Jackson благодаря его зрелости (он существует уже 13 лет) и отличной интеграции с популярными фреймворками, такими как Spring.
//Более того, это проект с открытым исходным кодом, который активно развивается и поддерживается широким сообществом.
//Под капотом у Jackson есть три основных пакета: Streaming, Databind и Annotations. При этом Jackson предлагает нам три способа обработки преобразования JSON-POJO
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialty;
    private Status status;

    public DeveloperEntity toEntity() {//метод маппинга из ДТО в Энтити
        return DeveloperEntity.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .specialty(specialty)
                .email(email)
                .status(status)
                .build();
    }

    public static DeveloperDto fromEntity(DeveloperEntity developer) {//мапптнг из Энтити в ДТО
        return DeveloperDto.builder()
                .id(developer.getId())
                .firstName(developer.getFirstName())
                .lastName(developer.getLastName())
                .email(developer.getEmail())
                .specialty(developer.getSpecialty())
                .status(developer.getStatus())
                .build();
    }
}
