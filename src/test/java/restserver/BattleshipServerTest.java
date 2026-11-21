package restserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class BattleshipServerTest {

    @Test
    @DisplayName("Deve estar anotada como aplicação Spring Boot")
    void deveTerAnotacaoSpringBootApplication() {
        assertNotNull(
                BattleshipServer.class.getAnnotation(SpringBootApplication.class)
        );
    }

    @Test
    @DisplayName("Deve disponibilizar um método main público e estático")
    void deveTerMetodoMainPublicoEstatico() throws NoSuchMethodException {
        Method main = BattleshipServer.class.getDeclaredMethod("main", String[].class);

        assertTrue(Modifier.isPublic(main.getModifiers()));
        assertTrue(Modifier.isStatic(main.getModifiers()));
        assertEquals(void.class, main.getReturnType());
    }

    @Test
    @DisplayName("Deve delegar o arranque ao Spring Boot sem iniciar o servidor")
    void deveInvocarSpringApplicationRun() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<SpringApplication> springApplication =
                     Mockito.mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(BattleshipServer.class, args))
                    .thenReturn(null);

            BattleshipServer.main(args);

            springApplication.verify(
                    () -> SpringApplication.run(BattleshipServer.class, args)
            );
        }
    }

    @Test
    @DisplayName("Deve permitir criar uma instância da classe")
    void deveCriarInstancia() {
        assertNotNull(new BattleshipServer());
    }
}
