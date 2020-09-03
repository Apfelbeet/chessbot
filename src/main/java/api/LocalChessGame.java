package api;

import api.handlers.LocalChessHandlerAdapter;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.json.JSONObject;

public class LocalChessGame extends LiChessGame {

    private Side resign;

    public LocalChessGame(InputHandler whiteHandler, InputHandler blackHandler) {
        super(null, null, new LocalChessHandlerAdapter(whiteHandler, blackHandler));
        JSONObject p1 = new JSONObject().put("id", "p1").put("name", "local1");
        JSONObject p2 = new JSONObject().put("id", "p2").put("name", "local2");
        JSONObject body = new JSONObject().put("white", p1).put("black", p2);
        state = new GameState(body);

        /*Thread gameloop = new Thread(() -> {
            while (!isFinished()) {
            }
        });
        gameloop.start();*/

        nextTurn();
    }

    private void nextTurn() {
        if(isFinished()) {
            getHandler().onFinish(this);
        }else {
            getHandler().onTurn(this);
        }

    }

    @Override
    public void sendMessage(String message, String room) {
        System.out.printf("%s: %s\n", room, message);
    }

    @Override
    public void move(String move) {
        getState().getBoard().doMove(new Move(move, getSide()));
        nextTurn();
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException("you can't abort a local game!");
    }

    @Override
    public void resign() {
        resign = getSide();
    }

    @Override
    public void disconnect() {
        throw new UnsupportedOperationException("You can't disconnect a local game!");
    }

    @Override
    public GameState getState() {
        return super.getState();
    }

    @Override
    public Side getSide() {
        return getState().getBoard().getSideToMove();
    }

    @Override
    public boolean isFinished() {
        return getState().getBoard().isDraw() || getState().getBoard().isMated();
    }

    @Override
    public InputHandler getHandler() {
        return super.getHandler();
    }

    @Override
    public void setHandler(InputHandler handler) {
        super.setHandler(handler);
    }
}
