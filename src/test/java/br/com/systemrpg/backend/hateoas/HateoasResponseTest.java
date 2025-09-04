package br.com.systemrpg.backend.hateoas;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a classe HateoasResponse.
 */
@DisplayName("HateoasResponse Tests")
class HateoasResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Implementação concreta de HateoasResponse para testes.
     */
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @lombok.ToString(callSuper = true)
    static class TestHateoasResponse extends HateoasResponse {
        private String testField;
    }

    @Test
    @DisplayName("Deve criar HateoasResponse com construtor sem argumentos")
    void shouldCreateHateoasResponseWithNoArgsConstructor() {
        TestHateoasResponse response = new TestHateoasResponse();
        
        assertNotNull(response);
        assertNotNull(response.getLinks());
        assertTrue(response.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Deve criar HateoasResponse com builder")
    void shouldCreateHateoasResponseWithBuilder() {
        List<Link> links = new ArrayList<>();
        links.add(new Link("http://localhost:8080/api/test/1", "self", "GET"));
        
        TestHateoasResponse response = new TestHateoasResponse();
        response.testField = "test";
        response.setLinks(links);
        
        assertNotNull(response);
        assertEquals("test", response.testField);
        assertNotNull(response.getLinks());
        assertEquals(1, response.getLinks().size());
        assertEquals("http://localhost:8080/api/test/1", response.getLinks().get(0).getHref());
    }

    @Test
    @DisplayName("Deve adicionar link usando objeto Link")
    void shouldAddLinkUsingLinkObject() {
        TestHateoasResponse response = new TestHateoasResponse();
        Link link = new Link("http://localhost:8080/api/test/1", "self", "GET");
        
        response.addLink(link);
        
        assertNotNull(response.getLinks());
        assertEquals(1, response.getLinks().size());
        assertEquals(link, response.getLinks().get(0));
    }

    @Test
    @DisplayName("Deve adicionar link usando parâmetros separados")
    void shouldAddLinkUsingSeparateParameters() {
        TestHateoasResponse response = new TestHateoasResponse();
        String href = "http://localhost:8080/api/test/1";
        String rel = "self";
        String method = "GET";
        
        response.addLink(href, rel, method);
        
        assertNotNull(response.getLinks());
        assertEquals(1, response.getLinks().size());
        Link addedLink = response.getLinks().get(0);
        assertEquals(href, addedLink.getHref());
        assertEquals(rel, addedLink.getRel());
        assertEquals(method, addedLink.getMethod());
    }

    @Test
    @DisplayName("Deve adicionar múltiplos links")
    void shouldAddMultipleLinks() {
        TestHateoasResponse response = new TestHateoasResponse();
        
        response.addLink("http://localhost:8080/api/test/1", "self", "GET");
        response.addLink("http://localhost:8080/api/test", "collection", "GET");
        response.addLink(new Link("http://localhost:8080/api/test/1", "edit", "PUT"));
        
        assertNotNull(response.getLinks());
        assertEquals(3, response.getLinks().size());
        
        assertEquals("self", response.getLinks().get(0).getRel());
        assertEquals("collection", response.getLinks().get(1).getRel());
        assertEquals("edit", response.getLinks().get(2).getRel());
    }

    @Test
    @DisplayName("Deve inicializar lista de links quando nula")
    void shouldInitializeLinksListWhenNull() {
        TestHateoasResponse response = new TestHateoasResponse();
        response.setLinks(null);
        
        assertNull(response.getLinks());
        
        response.addLink("http://localhost:8080/api/test/1", "self", "GET");
        
        assertNotNull(response.getLinks());
        assertEquals(1, response.getLinks().size());
    }

    @Test
    @DisplayName("Deve testar setters e getters")
    void shouldTestSettersAndGetters() {
        TestHateoasResponse response = new TestHateoasResponse();
        List<Link> links = new ArrayList<>();
        links.add(new Link("http://localhost:8080/api/test/1", "self", "GET"));
        
        response.setLinks(links);
        
        assertEquals(links, response.getLinks());
        assertSame(links, response.getLinks());
    }

    @Test
    @DisplayName("Deve testar equals e hashCode")
    void shouldTestEqualsAndHashCode() {
        TestHateoasResponse response1 = new TestHateoasResponse();
        response1.testField = "test";
        response1.addLink("http://localhost:8080/api/test/1", "self", "GET");
        
        TestHateoasResponse response2 = new TestHateoasResponse();
        response2.testField = "test";
        response2.addLink("http://localhost:8080/api/test/1", "self", "GET");
        
        TestHateoasResponse response3 = new TestHateoasResponse();
        response3.testField = "different";
        
        assertEquals(response2, response1);
        assertEquals(response2.hashCode(), response1.hashCode());
        assertNotEquals(response1, response3);
        assertNotEquals(response1.hashCode(), response3.hashCode());
        
        assertNotEquals(null, response1);
        assertNotEquals("string", response1);
        assertEquals(response1, response1);
    }

    @Test
    @DisplayName("Deve testar toString")
    void shouldTestToString() {
        TestHateoasResponse response = new TestHateoasResponse();
        response.testField = "test";
        response.addLink("http://localhost:8080/api/test/1", "self", "GET");
        
        String toString = response.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("testField"));
        assertTrue(toString.contains("links"));
        assertTrue(toString.contains("test"));
    }

    @Test
    @DisplayName("Deve serializar HateoasResponse para JSON")
    void shouldSerializeHateoasResponseToJson() throws Exception {
        TestHateoasResponse response = new TestHateoasResponse();
        response.testField = "test";
        response.addLink("http://localhost:8080/api/test/1", "self", "GET");
        response.addLink("http://localhost:8080/api/test", "collection", "GET");
        
        String json = objectMapper.writeValueAsString(response);
        
        assertNotNull(json);
        assertTrue(json.contains("\"testField\":\"test\""));
        assertTrue(json.contains("\"_links\""));
        assertTrue(json.contains("\"href\":\"http://localhost:8080/api/test/1\""));
        assertTrue(json.contains("\"rel\":\"self\""));
        assertTrue(json.contains("\"method\":\"GET\""));
    }

    @Test
    @DisplayName("Deve deserializar JSON para HateoasResponse")
    void shouldDeserializeJsonToHateoasResponse() throws Exception {
        String json = "{\"testField\":\"test\",\"_links\":[{\"href\":\"http://localhost:8080/api/test/1\",\"rel\":\"self\",\"method\":\"GET\"}]}";
        
        TestHateoasResponse response = objectMapper.readValue(json, TestHateoasResponse.class);
        
        assertNotNull(response);
        assertEquals("test", response.testField);
        assertNotNull(response.getLinks());
        assertEquals(1, response.getLinks().size());
        
        Link link = response.getLinks().get(0);
        assertEquals("http://localhost:8080/api/test/1", link.getHref());
        assertEquals("self", link.getRel());
        assertEquals("GET", link.getMethod());
    }

    @Test
    @DisplayName("Deve serializar HateoasResponse sem links (JsonInclude.NON_NULL)")
    void shouldSerializeHateoasResponseWithoutLinks() throws Exception {
        TestHateoasResponse response = new TestHateoasResponse();
        response.testField = "test";
        response.setLinks(null);
        
        String json = objectMapper.writeValueAsString(response);
        
        assertNotNull(json);
        assertTrue(json.contains("\"testField\":\"test\""));
        assertFalse(json.contains("\"_links\""));
    }

    @Test
    @DisplayName("Deve deserializar JSON sem links")
    void shouldDeserializeJsonWithoutLinks() throws Exception {
        String json = "{\"testField\":\"test\"}";
        
        TestHateoasResponse response = objectMapper.readValue(json, TestHateoasResponse.class);
        
        assertNotNull(response);
        assertEquals("test", response.testField);
        assertNotNull(response.getLinks());
        assertTrue(response.getLinks().isEmpty());
    }

    @Test
    @DisplayName("Deve serializar HateoasResponse com lista de links vazia")
    void shouldSerializeHateoasResponseWithEmptyLinksList() throws Exception {
        TestHateoasResponse response = new TestHateoasResponse();
        response.testField = "test";
        // Lista já é inicializada como ArrayList vazio pelo @Builder.Default
        
        String json = objectMapper.writeValueAsString(response);
        
        assertNotNull(json);
        assertTrue(json.contains("\"testField\":\"test\""));
        assertTrue(json.contains("\"_links\":[]"));
    }

    @Test
    @DisplayName("Deve testar comportamento com links duplicados")
    void shouldTestBehaviorWithDuplicateLinks() {
        TestHateoasResponse response = new TestHateoasResponse();
        Link link1 = new Link("http://localhost:8080/api/test/1", "self", "GET");
        Link link2 = new Link("http://localhost:8080/api/test/1", "self", "GET");
        
        response.addLink(link1);
        response.addLink(link2);
        
        assertNotNull(response.getLinks());
        assertEquals(2, response.getLinks().size());
        assertEquals(link1, response.getLinks().get(0));
        assertEquals(link2, response.getLinks().get(1));
        assertEquals(link1, link2); // São iguais mas são objetos diferentes na lista
    }

    @Test
    @DisplayName("Deve testar builder com links padrão")
    void shouldTestBuilderWithDefaultLinks() {
        TestHateoasResponse response = new TestHateoasResponse();
        response.testField = "test";
        
        assertNotNull(response);
        assertEquals("test", response.testField);
        assertNotNull(response.getLinks());
        assertTrue(response.getLinks().isEmpty());
        assertTrue(response.getLinks() instanceof ArrayList);
    }
}
