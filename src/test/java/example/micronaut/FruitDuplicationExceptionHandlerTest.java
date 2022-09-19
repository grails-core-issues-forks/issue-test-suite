package example.micronaut;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest // <1>
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // <2>
class FruitDuplicationExceptionHandlerTest implements TestPropertyProvider {

    @TempDir
    static File tempDir;

    @Override
    @NotNull
    public Map<String, String> getProperties() {
        return Collections.singletonMap(
                "microstream.storage.main.storage-directory", tempDir.getAbsolutePath()
        );
    }
    @Inject
    @Client("/")
    HttpClient httpClient; // <3>

    @Test
    void duplicatedFruitsReturns400() {
        FruitCommand banana = new FruitCommand("Banana");
        HttpRequest<?> request = HttpRequest.POST("/fruits", banana);
        HttpResponse<?> response = httpClient.toBlocking().exchange(request);
        assertEquals(HttpStatus.CREATED, response.status());
        HttpClientResponseException exception = assertThrows(
                HttpClientResponseException.class,
                () -> httpClient.toBlocking().exchange(request)
        );
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        HttpRequest<?> deleteRequest = HttpRequest.DELETE("/fruits", banana);
        HttpResponse<?> deleteResponse = httpClient.toBlocking().exchange(deleteRequest);
        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.status());
    }
}
