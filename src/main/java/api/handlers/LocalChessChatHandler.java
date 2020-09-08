package api.handlers;

import api.InputHandler;
import api.LiChessGame;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LocalChessChatHandler implements InputHandler {

    private static BufferedReader reader = new BufferedReader((new InputStreamReader(System.in)));

    @Override
    public void onTurn(LiChessGame game) {
        System.out.printf("%s moves: %s\n",
                game.getSide().toString(),
                game.getState().getLegalMoves().stream()
                        .map(Move::toString)
                        .reduce("", (s, s2) -> s + s2 + " ")
        );

        String move = read(this.getClass().getSimpleName());
        game.move(move);

    }

    /**
     * Comfort method so we only use one instance of the reader and it also allows you to easily pause your program to
     * enter the moves. Plus you see who is calling the method.
     * <p>
     * This method should be in another class but honestly I don't care.
     *
     * @param caller always type in {@code this.getClass().getSimpleName()} here
     * @return reader.readLine()
     */
    public static synchronized String read(String caller) {
        System.out.println(caller + " is listening");

        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onChatMessage(LiChessGame game, String message, String author, String room) {
    }

    @Override
    public void onFinish(LiChessGame game) {
    }
}
