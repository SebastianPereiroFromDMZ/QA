package net.proselyte.qafordevs.it;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class AbstractRestControllerBaseTest {

    @Container
    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;

    static {//статический блок кода: Порой до создания объекта нужно сделать подготовительные или инициализирующие действия.
        //Например, вычислить какие-либо статические величины, собрать системные данные, подключиться к базе данных или удалённому серверу.
        //Для выполнения подобных действий в языке Java может использоваться статический блок.
        //Код, расположенный в статическом блоке, будет выполнен во время запуска программы, при первой загрузке класса, ещё до того,
        //как этот самый класс будет использоваться в программе (то есть до создания его экземпляров, вызова статических методов или обращения к полям и т. п.).
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer("postgres:latest")//здесь говорим что бы подтянулся последний образ постгреса в докер
                .withUsername("proselyte")//с именем пользователя
                .withPassword("proselyte")//и паролем
                .withDatabaseName("qafordevs_testcontainers");//название ДБ

        POSTGRE_SQL_CONTAINER.start();//стартуем контейнер
    }

    @DynamicPropertySource
    public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
    }
}
