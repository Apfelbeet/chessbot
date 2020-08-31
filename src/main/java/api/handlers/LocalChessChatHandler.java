package api.handlers;

import api.InputHandler;
import api.LiChessGame;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LocalChessChatHandler implements InputHandler {
    @Override
    public void onTurn(LiChessGame game) {
        System.out.printf("%s moves: %s\n",
                game.getSide().toString(),
                game.getState().getLegalMoves().stream()
                        .map(Move::toString)
                        .reduce("", (s, s2) -> s + s2 + " ")
        );

        BufferedReader reader = new BufferedReader((new InputStreamReader(System.in)));
        try {
            String move = reader.readLine();
            game.move(move);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onChatMessage(LiChessGame game, String message, String author, String room) {
    }

    @Override
    public void onFinish(LiChessGame game) {
    }
}
