package com.alkemy.testing;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;

public class ClientesApiTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
        RestAssured.basePath = "/posts";
    }

    @Test
    public void alCrearUnRecursoConDatosValidos_deberiaDevolver201YElRecursoCreado() {

        String nuevoPostJson = "{\n"
                + "    \"title\": \"Mi post de prueba\",\n"
                + "    \"body\": \"Este es el contenido del post.\",\n"
                + "    \"userId\": 1\n"
                + "}";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .body(nuevoPostJson)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(201)
                .header("Content-Type", containsString("application/json"))
                .body("title", equalTo("Mi post de prueba"))
                .body("id", notNullValue())
                .body("id", instanceOf(Integer.class));
    }

    @Test
    public void alConsultarUnClienteExistente_deberiaDevolver200YLosDatosCorrectos() {

        int postId = 1;
        given()
                .log().all()
                .pathParam("id", postId)
                .when()
                .get("/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .header("Content-Type", containsString("application/json"))
                .body("id", equalTo(postId))
                .body("userId", notNullValue());
    }

    @Test
    public void alEliminarUnClienteExistente_deberiaDevolver200() {
        int postId = 1;

        given()
                .log().all()
                .pathParam("id", postId)
                .when()
                .delete("/{id}")
                .then()
                .log().all()
                .statusCode(200)
                .body(equalTo("{}"));
    }
}
