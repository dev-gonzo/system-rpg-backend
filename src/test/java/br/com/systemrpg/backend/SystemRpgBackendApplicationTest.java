package br.com.systemrpg.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe SystemRpgBackendApplication.
 */
@SpringBootTest
@ActiveProfiles("test")
class SystemRpgBackendApplicationTest {

    @Test
    void contextLoads() {
        // Este teste verifica se o contexto da aplicação Spring Boot carrega corretamente
        // Se o contexto não carregar, o teste falhará automaticamente
        assertTrue(true, "O contexto da aplicação deve carregar sem erros");
    }

    @Test
    void main_ShouldCallSpringApplicationRun() {
        // Arrange
        String[] args = {"--spring.profiles.active=test"};

        // Act & Assert
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            // Configurar o mock para não fazer nada quando SpringApplication.run for chamado
            mockedSpringApplication.when(() -> SpringApplication.run(eq(SystemRpgBackendApplication.class), eq(args)))
                    .thenReturn(null);

            // Chamar o método main
            SystemRpgBackendApplication.main(args);

            // Verificar se SpringApplication.run foi chamado com os parâmetros corretos
            mockedSpringApplication.verify(() -> SpringApplication.run(SystemRpgBackendApplication.class, args));
        }
    }

    @Test
    void main_WithEmptyArgs_ShouldCallSpringApplicationRun() {
        // Arrange
        String[] emptyArgs = {};

        // Act & Assert
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(eq(SystemRpgBackendApplication.class), eq(emptyArgs)))
                    .thenReturn(null);

            SystemRpgBackendApplication.main(emptyArgs);

            mockedSpringApplication.verify(() -> SpringApplication.run(SystemRpgBackendApplication.class, emptyArgs));
        }
    }

    @Test
    void main_WithNullArgs_ShouldCallSpringApplicationRun() {
        // Arrange
        String[] nullArgs = null;

        // Act & Assert
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(eq(SystemRpgBackendApplication.class), eq(nullArgs)))
                    .thenReturn(null);

            SystemRpgBackendApplication.main(nullArgs);

            mockedSpringApplication.verify(() -> SpringApplication.run(SystemRpgBackendApplication.class, nullArgs));
        }
    }

    @Test
    void applicationClass_ShouldHaveCorrectAnnotations() {
        // Arrange
        Class<SystemRpgBackendApplication> applicationClass = SystemRpgBackendApplication.class;

        // Act & Assert
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class),
                "A classe deve ter a anotação @SpringBootApplication");
        
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.scheduling.annotation.EnableScheduling.class),
                "A classe deve ter a anotação @EnableScheduling");
        
        assertTrue(applicationClass.isAnnotationPresent(org.springframework.data.web.config.EnableSpringDataWebSupport.class),
                "A classe deve ter a anotação @EnableSpringDataWebSupport");
    }

    @Test
    void applicationClass_ShouldBePublic() {
        // Arrange
        Class<SystemRpgBackendApplication> applicationClass = SystemRpgBackendApplication.class;

        // Act & Assert
        assertTrue(java.lang.reflect.Modifier.isPublic(applicationClass.getModifiers()),
                "A classe deve ser pública");
    }

    @Test
    void mainMethod_ShouldExistAndBePublicStatic() throws NoSuchMethodException {
        // Arrange
        Class<SystemRpgBackendApplication> applicationClass = SystemRpgBackendApplication.class;

        // Act
        java.lang.reflect.Method mainMethod = applicationClass.getMethod("main", String[].class);

        // Assert
        assertNotNull(mainMethod, "O método main deve existir");
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                "O método main deve ser público");
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                "O método main deve ser estático");
        assertEquals(void.class, mainMethod.getReturnType(),
                "O método main deve retornar void");
    }

    @Test
    void constructor_ShouldBeImplicit() {
        // Arrange & Act
        SystemRpgBackendApplication application = new SystemRpgBackendApplication();

        // Assert
        assertNotNull(application, "Deve ser possível instanciar a classe com o construtor padrão");
    }

    @Test
    void packageName_ShouldBeCorrect() {
        // Arrange
        Class<SystemRpgBackendApplication> applicationClass = SystemRpgBackendApplication.class;

        // Act & Assert
        assertEquals("br.com.systemrpg.backend", applicationClass.getPackage().getName(),
                "A classe deve estar no pacote correto");
    }
}
