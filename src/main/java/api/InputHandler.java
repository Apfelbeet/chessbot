package api;

/**
 * InputHandler is used to handle certain events raised by the lichess api.
 * It enables to adapt the behavior of a LiChessGame instance to your own needs.
 */
public interface InputHandler {

    /**
     * Called when it is the bot's turn.
     * It isn't really necessary to make move here, as long as you can ensure that the move is made at another point.
     * Otherwise the game will not continue.
     *
     * @param game the game that raised this event.
     */

    void onTurn(LiChessGame game);

    /**
     * Called with each incoming chat messages from the lichess chat.
     * You can use it to control your program via the chat.
     *
     * All messages the bot sends will also raise this event.
     * Thus, messages that cause an endless loop should be filtered.
     *
     * If the game is over (e.g. one player won). The lichess api ends the transmission,
     * so that there will be no event for messages after the game is finished.
     *
     * @param game the game that raised this event.
     * @param message the text of the chat message.
     * @param author the username of the author.
     * @param room the room in that the message was send.
     *             According to the lichess API documentation it assumes either "player" or "spectator"
     */

    void onChatMessage(LiChessGame game, String message, String author, String room);

    /**
     * Called if the game is over.
     *
     * @param game the game that raised this event.
     */

    void onFinish(LiChessGame game);
}
