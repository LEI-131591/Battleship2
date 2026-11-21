package restserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
}
