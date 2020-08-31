package api;

import com.github.bhlangonijr.chesslib.Side;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;

/**
 * Connection to a game on lichess.
 * Received game events will be passed to the InputHandler.
 * New events can send to the api with corresponding methods.
 */
public class LiChessGame {

    /**
     * The lichess api gives every game an ID which is needed to receive and send game events.
     */
    private final String id;

    /**
     * The client is used to send http-requests
     */
    private final WebClient client;

    /**
     * All receiving events will passed to the handler.
     */
    private InputHandler handler;

    /**
     * The access that created this game.
     */
    private final LiChessAccess access;

    /**
     * The current state of the game, it contains information about the players, other API level information, game level information.
     */
    protected GameState state;

    /**
     * The side the bot controls.
     */
    private Side side;

    /**
     * If this game is finished. (Connection closed or disconnect() was called)
     */
    private boolean finished;

    /**
     * Stream with all game events.
     */
    private final Disposable eventCatcher;

    /**
     * @param id The lichess api gives every game an ID which is needed to receive and send game events.
     * @param access The access that created this game.
     * @param handler All receiving events will passed to the handler.
     */
    public LiChessGame(String id, LiChessAccess access, InputHandler handler) {
        this.id = id;
        this.access = access;
        this.handler = handler;
        if(access != null && id != null) {
            this.client = access.getClient();
            eventCatcher = client.method(HttpMethod.GET).uri(String.format("/api/bot/game/stream/%s", id)).exchange().subscribe(
                    clientResponse -> {
                        clientResponse
                                .bodyToFlux(String.class).doOnComplete(() -> {
                            finished = true;
                            handler.onFinish(this);
                        })
                                .subscribe(s -> {
                                    if (!s.isEmpty())
                                        evaluateGameEvent(new JSONObject(s));
                                });
                    }
            );
        }else{
            eventCatcher = null;
            client = null;
        }


    }

    /**
     * Processes information of response.
     * Refreshes state and raises events in handler.
     *
     * @param body of the http-response
     */
    private void evaluateGameEvent(JSONObject body) {
        if (body.has("type")) {
            switch (body.getString("type")) {
                case "gameFull":
                    state = new GameState(body);
                    side = access.getBotId().equals(state.getWhitePlayerData().getId()) ? Side.WHITE : Side.BLACK;

                    evaluateGameEvent(body.getJSONObject("state"));
                    break;

                case "gameState":

                    state.renew(body);
                    if ("started".equals(state.getStatus())) {
                        if (state.getBoard().getSideToMove() == side) {
                            handler.onTurn(this);
                        }
                    }
                    break;
                case "chatLine":
                    handler.onChatMessage(this, body.getString("text"), body.getString("username"),
                            body.getString("room"));
                    break;
            }
        }
    }


    /**
     * Send message in lichess chat.
     *
     * @param message text of the message
     * @param room room that in which the message get posted. Possible values: "player", "spectator"
     */
    public void sendMessage(String message, String room) {
        client.post()
                .uri(String.format("/api/bot/game/%s/chat", id))
                .header("Content-Type", "application/json; charset=UTF-8")
                .body(BodyInserters
                        .fromFormData("room", room) //"player" "spectator"
                        .with("text", message)
                ).exchange().subscribe();
    }

    /**
     * Make a move on the chessboard.
     *
     * @param move in UCI format e.g. a2a4
     */
    public void move(String move) {
        client.post().uri(String.format("/api/bot/game/%s/move/%s", id, move)).exchange().subscribe();
    }

    /*public void draw(boolean draw) {
        client.post().uri(String.format("/api/board/game/%s/draw/%s", id, draw ? "yes" : "no"));
    }*/

    /**
     * Abort the game.
     */
    public void abort() {
        client.post().uri(String.format("/api/bot/game/%s/abort", id)).exchange().subscribe();
    }

    /**
     * Resign the game.
     */
    public void resign() {
        client.post().uri(String.format("/api/bot/game/%s/resign", id)).exchange().subscribe();
    }

    /**
     * Kill connection to game API
     */
    public void disconnect() {
        finished = true;
        eventCatcher.dispose();
    }

    public GameState getState() {
        return state;
    }

    public Side getSide() {
        return side;
    }

    public boolean isFinished() {
        return finished;
    }

    public InputHandler getHandler() {
        return handler;
    }

    public void setHandler(InputHandler handler) {
        this.handler = handler;
    }
}
