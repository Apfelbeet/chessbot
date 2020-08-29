package api.handlers;

import api.InputHandler;
import api.LiChessGame;
import com.github.bhlangonijr.chesslib.move.Move;

/**
 * Control bot through the lichess chat.
 * When it's the turn of the bot, it will show all possible moves.
 * A move can be chosen, by writing in the chat: "!" + move. e.g "!a3a4"
 * Invalid messages or illegal moves may cause the api to crash.
 */

public class LiChessChatHandler implements InputHandler {


    @Override
    public void onTurn(LiChessGame game) {
        game.sendMessage("possible moves: " +
                        game.getState().getLegalMoves().stream()
                                .map(Move::toString)
                                .reduce("", (s, s2) -> s + s2 + " "),
                "player");

    }

    @Override
    public void onChatMessage(LiChessGame game, String message, String author, String room) {
        if (game.getState().getBoard().getSideToMove() == game.getSide()) {
            if (!message.startsWith("!") || message.length() < 5 || message.length() > 6) return;
            message = message.substring(1);
            Move move = new Move(message, game.getSide());
            if (game.getState().getLegalMoves().contains(move)) {
                game.move(message);
            }
        }
    }

    @Override
    public void onFinish(LiChessGame game) {
        game.sendMessage("ggwp", "player");
    }

}
