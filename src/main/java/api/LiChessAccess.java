package api;

import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Accesses the API of lichess.org as bot.
 * It can accept, decline, create challenges and join all games that are active on the bot's account.
 * Therefore you need to promote a new lichess.org account as bot.
 * <p>
 * This implementation is not capable to change account settings or similar stuff.
 * <p>
 * The constructor needs an API access token as "password" for the bot:
 * - key: API Access token: you can create one on the settings page of lichess.org.
 * Permission the key needs: 1. Read incoming challenges 2. Create, accept, decline challenges 3. Play games with bot API
 * Don't show this key in public sourcecode!

 * <p>
 * LiChessAccess forces to program to continue running until all services (thus the games that are under control of this access)
 * are finished. You can force a LiChessAccess instance to stop by using expire() or dispose().
 * expire() will stop all event streams and prevent new streams to open so that it will no further accept new games and services.
 * dispose() will stop all event streams and all connections to running games. The games are still running on lichess,
 * only the particular connection get killed.
 */

public class LiChessAccess {

    /**
     * API Access token from lichess.
     * Permission the key needs:
     * 1. Read incoming challenges
     * 2. Create, accept, decline challenges
     * 3. Play games with bot API
     * <p>
     * It will be used as authorization in the header of http-requests to the API.
     * It's similar to an password, therefore don't show this key as plain text in public code segments.
     */
    private final String key;

    /**
     * The ID of the account the bot controls.
     * It is used to identify the right parts of the data send by the API.
     */
    private final String botId;

    /**
     * The username of the account the bot controls.
     * Similar to the botId, it is needed to identify and classify some data from the API.
     */
    private final String username;

    /**
     * Client is used to send and receive http-requests/responses.
     */
    private WebClient client;

    /**
     * All games that are under control of this access-instance.
     */
    private List<LiChessGame> games;

    /**
     * Stores all connections that stream incoming events on /api/stream/event.
     */
    private List<Disposable> services;

    /**
     * If true, this access will not longer join new games or open streams to the api.
     */
    private boolean expire;

    /**
     * If true, all waiting threads will stop waiting.
     */
    private boolean dispose;

    /**
     * @param key      API Access token: you can create one on the settings page of lichess.org.
     *                 Permission the key needs: 1. Read incoming challenges 2. Create, accept, decline challenges 3. Play games with bot API
     *                 Don't show this key in public sourcecode!
     */
    public LiChessAccess(String key) {
        this.key = key;
        games = new ArrayList<>();
        services = new ArrayList<>();
        client = WebClient.builder()
                .baseUrl("https://lichess.org")
                .defaultHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", this.key))
                .build();

        String responseBodyRaw = client.get().uri("/api/account").retrieve().bodyToMono(String.class).block();
        if(responseBodyRaw != null) {
            JSONObject responseBodyJson = new JSONObject(responseBodyRaw);
            if(responseBodyJson.has("id") && responseBodyJson.has("username")) {
                this.username = responseBodyJson.getString("username");
                this.botId = responseBodyJson.getString("id");
            }else throw new RuntimeException("Couldn't load account information!");
        }else throw new RuntimeException("Couldn't load account information!");
    }

    /**
     * Opens stream to /api/stream/event and passes all responses to the "consumer" argument.
     * There will always be a thread running until all matches accepted via this stream are finished.
     * You can force the instance to stop by using expire() or dispose().
     *
     * @param consumer will be called when the API sends a response.
     * @param games    stores all games that under control of this service.
     *                 It is used to notice when to stop the waiting thread.
     *                 In the most cases the List should be empty.
     * @param count    the amount of games this service will accept. -1 for unlimited.
     * @param onFinish called after the created service is finished.
     */
    public void catchAndHandleEvents(Consumer<? super String> consumer, List<LiChessGame> games, int count,
                                     Runnable onFinish) {
        runAndAwaitService(client.method(HttpMethod.GET).uri("/api/stream/event").exchange().subscribe(
                clientResponse -> {
                    clientResponse.bodyToFlux(String.class).subscribe(consumer);
                }), games, count, onFinish);
    }

    /**
     * The event stream passes all existing games, this service will join the given amount (see count) of games.
     * Games that are used in another service, still get passed by the API. Therefore this service will still join this games.
     * //TODO: Service that only joins alone
     *
     * @param count    the amount of games this service will accept. -1 for unlimited.
     * @param handler  used in a LiChessGame instance, that will pass all game events through the handler.
     * @param onFinish called after the created service is finished.
     */
    public void joinExistingGames(int count, InputHandler handler, Runnable onFinish) {
        List<LiChessGame> games = new ArrayList<>();
        catchAndHandleEvents((body -> {

            //New response
            if (!body.isEmpty()) {

                if (count == -1 || games.size() < count) {
                    JSONObject json = new JSONObject(body);
                    if ("gameStart".equals(json.getString("type"))) {
                        joinGame(json.getJSONObject("game").getString("id"), handler, games);
                    }
                }

            }

        }), games, count, onFinish);
    }

    /**
     * @param handler used in a LiChessGame instance, that will pass all game events through the handler.
     */

    public void joinExistingGames(InputHandler handler) {
        joinExistingGames(-1, handler, () -> {
        });
    }

    /**
     * @param count   the amount of games this service will accept. -1 for unlimited.
     * @param handler used in a LiChessGame instance, that will pass all game events through the handler.
     */
    public void joinExistingGames(int count, InputHandler handler) {
        joinExistingGames(count, handler, () -> {
        });
    }

    /**
     * This service will accept all incoming challenges and joins all games in the same way as joinExistingGames().
     *
     * @param count    the amount of games this service will accept. -1 for unlimited.
     * @param handler  used in a LiChessGame instance, that will pass all game events through the handler.
     * @param onFinish called after the created service is finished.
     */
    public void acceptAndJoinGames(int count, InputHandler handler, Runnable onFinish) {
        List<LiChessGame> games = new ArrayList<>();
        catchAndHandleEvents((body -> {

            //New response
            if (!body.isEmpty()) {
                System.out.println(body);
                if (count == -1 || games.size() < count) {
                    JSONObject json = new JSONObject(body);
                    switch (json.getString("type")) {
                        case "gameStart":
                            joinGame(json.getJSONObject("game").getString("id"), handler, games);
                            break;
                        case "challenge":
                            acceptChallenge(json.getJSONObject("challenge").getString("id"));
                    }
                }

            }

        }), games, count, onFinish);
    }

    /**
     * @param handler used in a LiChessGame instance, that will pass all game events through the handler.
     */
    public void acceptAndJoinGames(InputHandler handler) {
        acceptAndJoinGames(-1, handler, () -> {
        });
    }

    /**
     * @param count   the amount of games this service will accept. -1 for unlimited.
     * @param handler used in a LiChessGame instance, that will pass all game events through the handler.
     */
    public void acceptAndJoinGames(int count, InputHandler handler) {
        acceptAndJoinGames(count, handler, () -> {
        });
    }

    /**
     * Starts AwaitThread, that waits until all games are finished or this access is disposed.
     *
     * @param service  response stream from the api, that will be disposed when the service is finished.
     * @param count    amount of games to wait for. -1 for unlimited.
     * @param games    list of games to wait for.
     * @param onFinish called after waiting is done.
     */
    private void await(Disposable service, int count, List<LiChessGame> games, Runnable onFinish) {
        new AwaitThread(count, games, service, onFinish).start();
    }

    /**
     * If the access isn't expired or disposed, a new service will be added to a general list, stared and awaited.
     * If this access is expired or disposed, all new service will be disposed instantly.
     *
     * @param service  response stream from the api, that will be disposed when the service is finished.
     * @param games    list of games to wait for.
     * @param count    amount of games to wait for. -1 for unlimited.
     * @param onFinish called after waiting is done.
     */
    private void runAndAwaitService(Disposable service, List<LiChessGame> games, int count, Runnable onFinish) {
        if (!expire) {
            services.add(service);
            await(service, count, games, onFinish);
        } else {
            service.dispose();
        }
    }

    /**
     * Join new Game: Creates new LiChessGame instance, that communicates with the API and receives all game events
     * The game will be added in a local list (for the AwaitThread) and in a global list (for disposing this access)
     *
     * @param gameId  the lichess api gives every game an ID which is needed to receive and send game events
     * @param handler processes all game events
     * @param games   list of games to wait for.
     */
    private void joinGame(String gameId, InputHandler handler, List<LiChessGame> games) {
        if (!expire) {
            var game = new LiChessGame(gameId, this, handler);
            games.add(game);
            this.games.add(game);
        }
    }

    /**
     * Accept a challenge on lichess.
     *
     * @param id the lichess api gives every challenge an ID which is needed to accept it
     */
    public void acceptChallenge(String id) {
        client.post().uri(String.format("/api/challenge/%s/accept", id)).exchange().subscribe();
    }

    /**
     * Decline a challenge on lichess.
     *
     * @param id the lichess api gives every challenge an ID which is needed to decline it
     */
    private void declineChallenge(String id) {
        client.post().uri(String.format("/api/challenge/%s/decline", id)).exchange().subscribe();
    }

    /**
     * Challenge a user on lichess.
     * This feature is untested; it might not work.
     *
     * @param username name of challenged person.
     */
    public void challenge(String username) {
        client.post().uri(String.format("/api/challenge/%s", username)).exchange().subscribe();
    }

    /**
     * Challenge the AI on lichess, with an strength between 1 and 8
     * This feature is untested; it might not work.
     *
     * @param strength of the AI in range of 1 to 8
     */
    public void challengeAI(int strength) {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("level", strength);
        client.post()
                .uri("/api/challenge/ai")
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(BodyInserters.fromMultipartData(map)).exchange().subscribe();
    }

    /**
     * Stops all services and prevents new ones. All running games will still run until they end.
     */

    public void expire() {
        expire = true;
        services.forEach(Disposable::dispose);
    }

    /**
     * Will stop all services and all connections to running games.
     * The games are still running on lichess, only the particular connection get killed.
     * The onFinish functions will still be called (I hope, I'm not sure)
     */
    public void dispose() {
        expire();
        games.forEach(LiChessGame::disconnect);
        dispose = true;
    }

    String getBotId() {
        return botId;
    }

    String getKey() {
        return key;
    }

    public String getUsername() {
        return username;
    }

    WebClient getClient() {
        return client;
    }


    private final class AwaitThread extends Thread {
        private final int count;
        private final List<LiChessGame> games;
        private final Runnable onFinish;
        private final Disposable service;

        public AwaitThread(int count, List<LiChessGame> games, Disposable service, Runnable onFinish) {
            this.count = count;
            this.games = games;
            this.onFinish = onFinish;
            this.service = service;
        }

        @Override
        public void run() {
            while (!dispose && (games.size() != count || !games.stream().allMatch(LiChessGame::isFinished))) {
                //TODO: Remove Busy Waiting
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            service.dispose();
            onFinish.run();
        }
    }
}
