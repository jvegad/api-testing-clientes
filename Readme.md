# Análisis de Caso: Aserciones en Java para Pruebas de APIs REST
_Alumna: Josselyn Vega Devia_

## Situación inicial
Una empresa de tecnología llamada ApiSecure S.A. se encuentra desarrollando una API REST pública para la gestión de clientes. El equipo de QA está encargado de garantizar que cada endpoint cumpla con los requisitos funcionales y devuelva respuestas confi ables y predecibles.
Hasta el momento, han estado realizando pruebas manuales en Postman, pero debido a un error en producción que no fue detectado a tiempo (un endpoint devolvía un código 200 OK con un mensaje de error en el cuerpo), la dirección del proyecto decidió implementar pruebas automatizadas con aserciones en Java.
La empresa utiliza Spring Boot y JUnit 5 como stack principal de desarrollo y testing.

## 1. Análisis por Endpoint

A continuación, se detallan los aspectos clave a validar para cada uno de los endpoints de la API de gestión de clientes.

### ### POST /clientes

*   **Propósito:** Crear un nuevo cliente.
*   **Aspectos Clave a Testear:**
    *   **Código de estado esperado:** `201 Created` para una creación exitosa.
    *   **Encabezados obligatorios:** La respuesta debe incluir `Content-Type: application/json`.
    *   **Cuerpo de respuesta:** Debe contener los datos del cliente enviado en la petición y, adicionalmente, un `id` único generado por el servidor.
    *   **Casos de error:**
        *   `400 Bad Request` si se envía un cuerpo de petición inválido (ej. campos obligatorios faltantes).
        *   `409 Conflict` si se intenta crear un cliente con un email que ya existe.

### ### GET /clientes/{id}

*   **Propósito:** Obtener los datos de un cliente específico.
*   **Aspectos Clave a Testear:**
    *   **Código de estado esperado:** `200 OK` si el cliente existe.
    *   **Encabezados obligatorios:** La respuesta debe incluir `Content-Type: application/json`.
    *   **Cuerpo de respuesta:** Debe contener la estructura y datos completos del cliente correspondiente al `id` solicitado.
    *   **Casos de error:**
        *   `404 Not Found` si se solicita un cliente con un `id` que no existe.

### ### DELETE /clientes/{id}

*   **Propósito:** Eliminar un cliente existente.
*   **Aspectos Clave a Testear:**
    *   **Código de estado esperado:** `200 OK` o `204 No Content` para una eliminación exitosa.
    *   **Cuerpo de respuesta:** Generalmente vacío.
    *   **Verificación posterior:** Una petición `GET` subsecuente al mismo `id` debe devolver `404 Not Found` para confirmar la eliminación.
    *   **Casos de error:**
        *   `404 Not Found` si se intenta eliminar un cliente con un `id` que no existe.

## 2. Estrategia de Automatización y Herramientas Seleccionadas

### ### Librerías Utilizadas

*   **Rest-Assured:** Se elige como la librería principal para interactuar con la API. Su sintaxis fluida y legible, basada en el patrón **Given-When-Then**, simplifica enormemente la creación de peticiones HTTP y la validación de sus respuestas.
*   **JUnit 5:** Es el framework de testing seleccionado para estructurar, organizar y ejecutar los casos de prueba. Sus anotaciones (`@Test`, `@BeforeAll`, etc.) permiten un control claro sobre el ciclo de vida de las pruebas.
*   **Hamcrest:** Utilizado de forma implícita por Rest-Assured para las aserciones (`.body()`). Provee *matchers* declarativos y legibles como `equalTo()`, `notNullValue()`, y `containsString()`.

### ### Patrones de Diseño y Organización

*   **Test por Escenario:** Cada método `@Test` se enfoca en validar un único comportamiento o escenario (ej. "crear un cliente válido", "consultar un cliente inexistente"). Esto mejora la claridad y facilita la depuración.
*   **Configuración Centralizada:** Se utiliza el método `@BeforeAll` de JUnit 5 para establecer la configuración base (como la `baseURI`) una sola vez por clase, evitando la duplicación de código.
*   **Clases por Recurso:** Las pruebas se organizan en clases que corresponden a los recursos de la API. En este caso, todas las pruebas relacionadas con `/clientes` se agrupan en la clase `ClientesApiTest.java`.

## 3. Fragmento de Código de Prueba de Ejemplo

El siguiente es el código completo de la clase de prueba desarrollada, cubriendo los endpoints `POST`, `GET` y `DELETE` y utilizando una API pública para la demostración.

```java
package com.alkemy.testing;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ClientesApiTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
        RestAssured.basePath = "/posts";
    }

    @Test
    public void alCrearUnRecursoConDatosValidos_deberiaDevolver201YElRecursoCreado() {
        String nuevoPostJson = "{\n" +
                               "    \"title\": \"Mi post de prueba\",\n" +
                               "    \"body\": \"Este es el contenido del post.\",\n" +
                               "    \"userId\": 1\n" +
                               "}";
        given()
            .contentType(ContentType.JSON)
            .body(nuevoPostJson)
        .when()
            .post()
        .then()
            .statusCode(201)
            .header("Content-Type", containsString("application/json"))
            .body("title", equalTo("Mi post de prueba"))
            .body("id", notNullValue());
    }

    @Test
    public void alConsultarUnClienteExistente_deberiaDevolver200YLosDatosCorrectos() {
        int postId = 1;
        given()
            .pathParam("id", postId)
        .when()
            .get("/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(postId))
            .body("userId", notNullValue());
    }

    @Test
    public void alEliminarUnClienteExistente_deberiaDevolver200() {
        int postId = 1;
        given()
            .pathParam("id", postId)
        .when()
            .delete("/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));
    }
}
```
## 4. Reflexión Final

La implementación de pruebas automatizadas con **aserciones robustas** es un pilar fundamental para garantizar la calidad y fiabilidad de una API REST. Este proceso va más allá de simplemente verificar que un endpoint "responde"; se trata de asegurar que responde *exactamente como se espera*.

### ¿Cómo previenen errores las aserciones?

Las aserciones actúan como un **contrato digital**. El caso inicial que motivó el proyecto (un código `200 OK` con un mensaje de error en el cuerpo) es un "falso positivo" que las pruebas manuales pueden pasar por alto fácilmente. Una aserción automatizada que verifique un campo específico en el cuerpo de la respuesta (ej. `.body("status", equalTo("success"))`) habría detectado este error de forma instantánea y automática, impidiendo su llegada a producción. Las aserciones fuerzan la validación explícita de cada componente de la respuesta: el código de estado, los encabezados y, crucialmente, la estructura y el contenido del payload.

### ¿Cómo mejoran la calidad del producto?

*   **Red de Seguridad contra Regresiones:** Crean una barrera de contención que detecta inmediatamente si un nuevo cambio en el código ha roto una funcionalidad existente. Esto da al equipo de desarrollo la confianza para refactorizar y añadir nuevas características de forma ágil.
*   **Documentación Viva:** Un conjunto de pruebas bien escritas sirve como la documentación más fiable y actualizada del comportamiento esperado de la API. Un nuevo desarrollador puede leer las pruebas para entender qué esperar de cada endpoint.
*   **Consistencia y Previsibilidad:** Garantizan que la API se comporte de manera consistente a lo largo del tiempo, algo esencial para los consumidores de la misma (ya sean aplicaciones frontend u otros servicios).
*   **Eficiencia del Equipo de QA:** Al automatizar estas verificaciones fundamentales, el equipo de QA puede dedicar su valioso tiempo a pruebas más complejas y de mayor impacto, como las exploratorias, de seguridad o de rendimiento.

En resumen, la inversión en un framework de pruebas automatizadas con aserciones detalladas se traduce directamente en un producto de **mayor calidad**, un ciclo de desarrollo más **rápido** y una reducción significativa de los **errores en producción**.