package br.com.systemrpg.backend.hateoas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para a classe Link.
 */
@DisplayName("Link Tests")
class LinkTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve criar Link com construtor sem argumentos")
    void shouldCreateLinkWithNoArgsConstructor() {
        Link link = new Link();
        
        assertNotNull(link);
        assertNull(link.getHref());
        assertNull(link.getRel());
        assertNull(link.getMethod());
        assertNull(link.getType());
        assertNull(link.getTitle());
    }

    @Test
    @DisplayName("Deve criar Link com construtor completo")
    void shouldCreateLinkWithAllArgsConstructor() {
        String href = "http://localhost:8080/api/users/1";
        String rel = "self";
        String method = "GET";
        String type = "application/json";
        String title = "User Details";
        
        Link link = new Link(href, rel, method, type, title);
        
        assertNotNull(link);
        assertEquals(href, link.getHref());
        assertEquals(rel, link.getRel());
        assertEquals(method, link.getMethod());
        assertEquals(type, link.getType());
        assertEquals(title, link.getTitle());
    }

    @Test
    @DisplayName("Deve criar Link com construtor href e rel")
    void shouldCreateLinkWithHrefAndRel() {
        String href = "http://localhost:8080/api/users";
        String rel = "users";
        
        Link link = new Link(href, rel);
        
        assertNotNull(link);
        assertEquals(href, link.getHref());
        assertEquals(rel, link.getRel());
        assertNull(link.getMethod());
        assertNull(link.getType());
        assertNull(link.getTitle());
    }

    @Test
    @DisplayName("Deve criar Link com construtor href, rel e method")
    void shouldCreateLinkWithHrefRelAndMethod() {
        String href = "http://localhost:8080/api/users/1";
        String rel = "delete";
        String method = "DELETE";
        
        Link link = new Link(href, rel, method);
        
        assertNotNull(link);
        assertEquals(href, link.getHref());
        assertEquals(rel, link.getRel());
        assertEquals(method, link.getMethod());
        assertNull(link.getType());
        assertNull(link.getTitle());
    }

    @Test
    @DisplayName("Deve criar Link com builder")
    void shouldCreateLinkWithBuilder() {
        String href = "http://localhost:8080/api/users/1";
        String rel = "self";
        String method = "GET";
        String type = "application/json";
        String title = "User Details";
        
        Link link = Link.builder()
                .href(href)
                .rel(rel)
                .method(method)
                .type(type)
                .title(title)
                .build();
        
        assertNotNull(link);
        assertEquals(href, link.getHref());
        assertEquals(rel, link.getRel());
        assertEquals(method, link.getMethod());
        assertEquals(type, link.getType());
        assertEquals(title, link.getTitle());
    }

    @Test
    @DisplayName("Deve testar setters e getters")
    void shouldTestSettersAndGetters() {
        Link link = new Link();
        String href = "http://localhost:8080/api/users/1";
        String rel = "self";
        String method = "GET";
        String type = "application/json";
        String title = "User Details";
        
        link.setHref(href);
        link.setRel(rel);
        link.setMethod(method);
        link.setType(type);
        link.setTitle(title);
        
        assertEquals(href, link.getHref());
        assertEquals(rel, link.getRel());
        assertEquals(method, link.getMethod());
        assertEquals(type, link.getType());
        assertEquals(title, link.getTitle());
    }

    @Test
    @DisplayName("Deve testar equals e hashCode")
    void shouldTestEqualsAndHashCode() {
        Link link1 = new Link("http://localhost:8080/api/users/1", "self", "GET");
        Link link2 = new Link("http://localhost:8080/api/users/1", "self", "GET");
        Link link3 = new Link("http://localhost:8080/api/users/2", "self", "GET");
        
        assertEquals(link2, link1);
        assertEquals(link2.hashCode(), link1.hashCode());
        assertNotEquals(link3, link1);
        assertNotEquals(link3.hashCode(), link1.hashCode());
        
        assertNotEquals(null, link1);
        assertNotEquals("string", link1);
        assertEquals(link1, link1);
    }

    @Test
    @DisplayName("Deve testar toString")
    void shouldTestToString() {
        Link link = new Link("http://localhost:8080/api/users/1", "self", "GET");
        String toString = link.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("href"));
        assertTrue(toString.contains("rel"));
        assertTrue(toString.contains("method"));
        assertTrue(toString.contains("http://localhost:8080/api/users/1"));
        assertTrue(toString.contains("self"));
        assertTrue(toString.contains("GET"));
    }

    @Test
    @DisplayName("Deve serializar Link para JSON")
    void shouldSerializeLinkToJson() throws Exception {
        Link link = Link.builder()
                .href("http://localhost:8080/api/users/1")
                .rel("self")
                .method("GET")
                .type("application/json")
                .title("User Details")
                .build();
        
        String json = objectMapper.writeValueAsString(link);
        
        assertNotNull(json);
        assertTrue(json.contains("\"href\":\"http://localhost:8080/api/users/1\""));
        assertTrue(json.contains("\"rel\":\"self\""));
        assertTrue(json.contains("\"method\":\"GET\""));
        assertTrue(json.contains("\"type\":\"application/json\""));
        assertTrue(json.contains("\"title\":\"User Details\""));
    }

    @Test
    @DisplayName("Deve deserializar JSON para Link")
    void shouldDeserializeJsonToLink() throws Exception {
        String json = "{\"href\":\"http://localhost:8080/api/users/1\",\"rel\":\"self\",\"method\":\"GET\",\"type\":\"application/json\",\"title\":\"User Details\"}";
        
        Link link = objectMapper.readValue(json, Link.class);
        
        assertNotNull(link);
        assertEquals("http://localhost:8080/api/users/1", link.getHref());
        assertEquals("self", link.getRel());
        assertEquals("GET", link.getMethod());
        assertEquals("application/json", link.getType());
        assertEquals("User Details", link.getTitle());
    }

    @Test
    @DisplayName("Deve serializar Link com campos nulos (JsonInclude.NON_NULL)")
    void shouldSerializeLinkWithNullFields() throws Exception {
        Link link = new Link("http://localhost:8080/api/users/1", "self");
        
        String json = objectMapper.writeValueAsString(link);
        
        assertNotNull(json);
        assertTrue(json.contains("\"href\":\"http://localhost:8080/api/users/1\""));
        assertTrue(json.contains("\"rel\":\"self\""));
        assertFalse(json.contains("\"method\""));
        assertFalse(json.contains("\"type\""));
        assertFalse(json.contains("\"title\""));
    }

    @Test
    @DisplayName("Deve deserializar JSON com campos ausentes")
    void shouldDeserializeJsonWithMissingFields() throws Exception {
        String json = "{\"href\":\"http://localhost:8080/api/users/1\",\"rel\":\"self\"}";
        
        Link link = objectMapper.readValue(json, Link.class);
        
        assertNotNull(link);
        assertEquals("http://localhost:8080/api/users/1", link.getHref());
        assertEquals("self", link.getRel());
        assertNull(link.getMethod());
        assertNull(link.getType());
        assertNull(link.getTitle());
    }

    @Test
    @DisplayName("Deve testar Link com valores vazios")
    void shouldTestLinkWithEmptyValues() {
        Link link = new Link("", "", "");
        
        assertNotNull(link);
        assertEquals("", link.getHref());
        assertEquals("", link.getRel());
        assertEquals("", link.getMethod());
    }

    @Test
    @DisplayName("Deve testar Link com valores nulos")
    void shouldTestLinkWithNullValues() {
        Link link = new Link(null, null, null);
        
        assertNotNull(link);
        assertNull(link.getHref());
        assertNull(link.getRel());
        assertNull(link.getMethod());
    }
}
