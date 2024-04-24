package http_client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private URI KVServerURI;
    private String api_token;
    private HttpClient client;

    public KVTaskClient(URI uri) {
        KVServerURI = uri;
        client = HttpClient.newHttpClient();
        URI registerUrl = URI.create(KVServerURI.toString() + "/register");
        HttpRequest request = HttpRequest.newBuilder().uri(registerUrl).GET().build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            api_token = response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время отправки запроса на KVServer возникло исключение " + e);
        }
    }

    public void put(String key, String json) {
        URI putUrl = URI.create(KVServerURI.toString() + "/save/" + key + "?API_TOKEN=" + api_token);
        HttpRequest request = HttpRequest.newBuilder().uri(putUrl).POST(HttpRequest.BodyPublishers.ofString(json)).
                build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время сохранения данных на сервере возникло исключение " + e);
            ;
        }
    }

    public String load(String key) {
        URI loadUrl = URI.create(KVServerURI.toString() + "/load/" + key + "?API_TOKEN=" + api_token);
        HttpRequest request = HttpRequest.newBuilder().uri(loadUrl).GET().build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время получения данных с сервера возникло исключение " + e);
        }
        return null;
    }
}
