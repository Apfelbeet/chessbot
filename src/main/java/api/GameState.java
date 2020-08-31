package api;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.json.JSONObject;

public class GameState {

    /**
     * Chessboard: all necessary information.
     */
    private Board board;

    /**
     * Status send by the API (e.g. "started").
     */
    private String status;

    /**
     * Draw Offer.
     */
    private boolean whiteDrawOffer;
    private boolean blackDrawOffer;

    /**
     * Player information.
     */
    private Player whitePlayerData;
    private Player blackPlayerData;

    /**
     * API declared winner.
     */
    private Side winner;

    /**
     * @param gameFull "gameFull"-type response.
     */
    GameState(JSONObject gameFull) {
        board = new Board();
        whitePlayerData = new Player(gameFull.getJSONObject("white"));
        blackPlayerData = new Player(gameFull.getJSONObject("black"));
    }

    /**
     * Renew all data with data from new response of type "gameState"
     *
     * @param state body of response
     */
    void renew(JSONObject state) {
        status = state.getString("status");
        whiteDrawOffer = state.getBoolean("wdraw");
        blackDrawOffer = state.getBoolean("bdraw");

        try {
            if (state.has("moves") && !state.getString("moves").isEmpty()) {
                MoveList moveList = new MoveList();
                moveList.loadFromText(state.getString("moves"));
                board.loadFromFen(moveList.getFen());
            } else {
                //If there is no "moves"-object or it is an empty string, the board will be reset.
                board = new Board();
            }
        } catch (MoveConversionException e) {
            e.printStackTrace();
        }

        if (state.has("winner")) {
            winner = state.getString("winner").equals("white") ? Side.WHITE : Side.BLACK;
        }


    }

    public String getStatus() {
        return status;
    }

    public boolean isWhiteDrawOffer() {
        return whiteDrawOffer;
    }

    public boolean isBlackDrawOffer() {
        return blackDrawOffer;
    }

    public Player getWhitePlayerData() {
        return whitePlayerData;
    }

    public Player getBlackPlayerData() {
        return blackPlayerData;
    }

    public Board getBoard() {
        return board;
    }

    public Side getWinner() {
        return winner;
    }

    /**
     * Generates all legal moves.
     *
     * @return All possible/legal moves
     */
    public MoveList getLegalMoves() {

        try {
            return MoveGenerator.generateLegalMoves(board);
        } catch (MoveGeneratorException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static class Player {
        private String id;
        private String name;


        public Player(JSONObject player) {
            if (player.has("id"))
                id = player.getString("id");
            else
                id = "ai";
            if (player.has("name"))
                name = player.getString("name");
            else
                name = "stockfish";
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }


}
