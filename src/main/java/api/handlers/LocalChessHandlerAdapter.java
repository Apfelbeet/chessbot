package api.handlers;

import api.InputHandler;
import api.LiChessGame;
import com.github.bhlangonijr.chesslib.Side;

public class LocalChessHandlerAdapter implements InputHandler {
    private final InputHandler whiteHandler;
    private final InputHandler blackHandler;

    public LocalChessHandlerAdapter(InputHandler whiteHandler, InputHandler blackHandler) {
        this.whiteHandler = whiteHandler;
        this.blackHandler = blackHandler;
    }

    @Override
    public void onTurn(LiChessGame game) {
        if(game.getSide() == Side.WHITE)
            whiteHandler.onTurn(game);
        else
            blackHandler.onTurn(game);
    }

    @Override
    public void onChatMessage(LiChessGame game, String message, String author, String room) {
        if(game.getSide() == Side.WHITE)
            whiteHandler.onChatMessage(game, message, author, room);
        else
            blackHandler.onChatMessage(game, message, author, room);
    }

    @Override
    public void onFinish(LiChessGame game) {
        whiteHandler.onFinish(game);
        blackHandler.onFinish(game);
    }
}
