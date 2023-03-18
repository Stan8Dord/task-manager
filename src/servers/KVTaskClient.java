package servers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final URI uri;
    private final HttpClient client;
    private String apiToken;
    private final HttpResponse.BodyHandler<String> handler;

    public KVTaskClient(URI url) {
        this.uri = url;
        this.client = HttpClient.newHttpClient();
        this.handler = HttpResponse.BodyHandlers.ofString();

        try {
            this.apiToken = register(url);
        } catch (IOException | InterruptedException e) {
            System.out.println("Произошла ошибка подключения к " + url);
        }
    }

    private String register(URI url) throws IOException, InterruptedException {
        url = URI.create(url.toString() + "register");

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, handler);
        int status = response.statusCode();
        switch (status) {
            case 400:
                System.out.println("В запросе содержится ошибка.");
                break;
            case 500:
                System.out.println("На стороне сервера произошла непредвиденная ошибка.");
                break;
            case 503:
                System.out.println("Сервер временно недоступен.");
                break;
            default:
                System.out.println("status = " + status);
        }
        String token = response.body();

        return token;
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        URI url = URI.create(this.uri.toString() + "save/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, handler);

        int status = response.statusCode();
        if (status > 299)
            System.out.println("Что-то пошло не так. Код возврата = "+ status);
        else
            System.out.println("Client put Ок. Код возврата = " + status);
    }

    public String load(String key) throws IOException, InterruptedException {
        URI url = URI.create(this.uri.toString() + "load/" + key + "?API_TOKEN=" + apiToken);
        System.out.println("Load url = " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, handler);

        int status = response.statusCode();
        if (status == 200)
            return response.body();
        else
            return null;
    }
}
