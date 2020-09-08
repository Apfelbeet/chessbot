package api.handlers;

import AI.Constants;
import api.InputHandler;
import api.LiChessGame;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.stream.Collectors;

public class AIHandler implements InputHandler {

    private Board boardC;
    private short turn;
    private Side side;


    /**
     * Called before every turn
     */
    private void init(LiChessGame game) {
        boardC = game.getState().getBoard().clone();

        if(side == null) {  //should only be called at the first turn of the game. However if the game crashes and has
            // to be restarted turn needs to represent the current turn, not just the first one (if it was restarted at
            // turn 40 for example that would be hella bad then)
            side = boardC.getSideToMove();
            turn = (short) (boardC.getMoveCounter() - 1);
        }
    }

    @Override
    public void onTurn(LiChessGame game) {

        init(game);

        try {
//            Thread.sleep(5000);

            var t1 = System.currentTimeMillis();
            int eval = alphaBeta();
            var t2 = System.currentTimeMillis();

            int recalculations = 0;
            long t3, t4;
            if(t2 - t1 < 1000) {
                do {
                    t3 = System.currentTimeMillis();
                    eval = alphaBeta(Constants.depth + ++recalculations);
                    t4 = System.currentTimeMillis();
                } while (t4 - t3 < 1000);
            }

            t2 = System.currentTimeMillis();   //this may be called twice in a row. This is intended as I don't have a
            // better workaround (finally block for while loops would be niche)


//            String.format("Thanks to Alpha-Beta-Pruning we only needed to look into %d moves%n",  comparisons);
//            String.format("Evaluated the best move to be %s with an evaluation score of %d%n", getNextBestMove().toString(), eval);
            game.sendMessage(String.format("Evaluation took %g seconds at a depth of %d", (t2 - t1) / 1000d, Constants.depth + recalculations), Constants.rooms[0]);

            turn += 2;
            boardC.doMove(getNextBestMove());
            game.move(getNextBestMove().toString());

        } catch (MoveGeneratorException e) {
            e.printStackTrace();
        }


    }

    /**
     * Convenience method for searching with a depth of {@link Constants#depth}
     */
    public int alphaBeta() throws MoveGeneratorException {
        return alphaBeta(Constants.depth);
    }

    /**
     * Alpha beta search until a certain depth has been reached
     * @param depth how many moves in the future we want to calculate everything
     */
    public int alphaBeta(int depth) throws MoveGeneratorException {
        if(side == Side.WHITE)
            return alphaBetaMax(Integer.MIN_VALUE, Integer.MAX_VALUE, depth, true);
        else
            return alphaBetaMin(Integer.MIN_VALUE, Integer.MAX_VALUE, depth, true);
    }

    /**
     * Alpha beta max function
     *
     * @param first indicates whether this instance of this method is the first one in it's recursive chain
     */
    private int alphaBetaMax(int alpha, int beta, int depthleft, boolean first) throws MoveGeneratorException {
        if(depthleft == 0) {
            return evalBB(boardC, boardC.getSideToMove());
//            return quiescenceSearch(alpha, beta, false);  //quiesence search increases the search time by a GIGANTIC amount
        }

        var moves = MoveGenerator.generateLegalMoves(boardC);

        if(moves.size() == 0 && (!boardC.isKingAttacked() || boardC.isRepetition() || boardC.getHalfMoveCounter() >= 100))
            //Stalemate check &&  threefold repetition check && halfMoveCounterCheck
            return 0;

        if(first)
            nextBestMove = moves.get(0);    //initialize with at least one move so there can't be any null moves

        for (Move move : moves) {
            boardC.doMove(move, false);  //make the move for the next recursive call
            turn++;
            int score = alphaBetaMin(alpha, beta, depthleft - 1, false);
            boardC.undoMove(); //undo the move because the recursive call already took place
            turn--;

            if(score >= beta)
                return beta;   // fail hard beta-cutoff
            if(score > alpha) {
                if(first)
                    nextBestMove = move;
                alpha = score; // alpha acts like max in MiniMax
            }
        }
        return alpha;
    }

    /**
     * Alpha beta min function
     *
     * @param first indicates whether this instance of this method is the first one in it's recursive chain
     */
    private int alphaBetaMin(int alpha, int beta, int depthleft, boolean first) throws MoveGeneratorException {
        if(depthleft == 0) {
            return evalBB(boardC, boardC.getSideToMove());
//            return quiescenceSearch(alpha, beta, true);   //quiesence search increases the search time by a GIGANTIC amount
        }

        var moves = MoveGenerator.generateLegalMoves(boardC);

        if(moves.size() == 0 && (!boardC.isKingAttacked() || boardC.isRepetition() || boardC.getHalfMoveCounter() >= 100))
            //Stalemate check &&  threefold repetition check && halfMoveCounterCheck
            return 0;

        if(first)  //no check for the existence of a move necessary because if there was none the game would've ended anyways by now
            nextBestMove = moves.get(0);    //initialize with at least one move so there can't be any null moves

        for (Move move : moves) {
            boardC.doMove(move, false);  //make the move for the next recursive call
            turn++;
            int score = alphaBetaMax(alpha, beta, depthleft - 1, false);
            boardC.undoMove(); //undo the move because the recursive call already took place
            turn--;

            if(score <= alpha)
                return alpha; // fail hard alpha-cutoff
            if(score < beta) {
                if(first)
                    nextBestMove = move;
                beta = score; // beta acts like min in MiniMax
            }
        }
        return beta;
    }

    /**
     * Expand the search tree to make it more or less stable.
     * Lead to horrible time results, that's why it's not used right now.
     *
     * @param min Indicates whether or not the calling method is the alphaBetaMIN or ...MAX method
     */
    private int quiescenceSearch(int alpha, int beta, boolean min) throws MoveGeneratorException {
        var captures = MoveGenerator.generatePseudoLegalCaptures(boardC).stream().filter(capt -> boardC.isMoveLegal(capt, false)).collect(Collectors.toList());
        if(captures.size() > 0) {
            for (Move capt : captures) {
                boardC.doMove(capt, false);  //make the move for the next recursive call
                turn++;
                int score = min ? alphaBetaMax(alpha, beta, 0, false) : alphaBetaMin(alpha, beta, 0, false);
                boardC.undoMove(); //undo the move because the recursive call already took place
                turn--;

                if(min) {    //Behaviour as seen in AlphaBetaMin method - but without the first check
                    if(score <= alpha)
                        return alpha; // fail hard alpha-cutoff
                    if(score < beta)
                        beta = score; // beta acts like min in MiniMax

                } else {      //Behaviour as seen in AlphaBetaMax method - but without the first check
                    if(score >= beta)
                        return beta;   // fail hard beta-cutoff
                    if(score > alpha)
                        alpha = score; // alpha acts like max in MiniMax

                }
            }

            return min ? beta : alpha;
        } else {
            return evalBB(boardC, boardC.getSideToMove());
        }
    }

    /**
     * the next best move
     */
    private Move nextBestMove = null;

    public Move getNextBestMove() {
        return nextBestMove;
    }

    /**
     * Evaluation function
     *
     * @param board the board the evaluation should look at
     * @param side  the side to move in the next move
     * @return evaluation of the current position
     */
    public int evalBB(Board board, Side side) {


        //Draw checks (apart from the halfMoveCounter) are technically necessary but are already treated to major parts
        // in the Alpha-Beta-Pruning and also are super fucking expensive. Check for insufficient material isn't necessary
        // imho because the AI is so bad that this would be a success already
        if(board.getHalfMoveCounter() >= 100)
            return 0;


        if(board.isMated())
            return side == Side.WHITE ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        int centipawns = 0;

        if(board.isKingAttacked())
            centipawns += side == Side.WHITE ? -300 : 300;  //because it would be bad for white if a king is attacked on his turn

        long white_pawns, white_bishops, white_knights, white_queens, white_rooks, black_pawns, black_bishops,
                black_knights, black_queens, black_rooks, white_king, black_king;

        white_pawns = board.getBitboard(Piece.WHITE_PAWN);
        black_pawns = board.getBitboard(Piece.BLACK_PAWN);
        white_bishops = board.getBitboard(Piece.WHITE_BISHOP);
        black_bishops = board.getBitboard(Piece.BLACK_BISHOP);
        white_knights = board.getBitboard(Piece.WHITE_KNIGHT);
        black_knights = board.getBitboard(Piece.BLACK_KNIGHT);
        white_rooks = board.getBitboard(Piece.WHITE_ROOK);
        black_rooks = board.getBitboard(Piece.BLACK_ROOK);
        white_queens = board.getBitboard(Piece.WHITE_QUEEN);
        black_queens = board.getBitboard(Piece.BLACK_QUEEN);
        white_king = board.getBitboard(Piece.WHITE_KING);
        black_king = board.getBitboard(Piece.BLACK_KING);

        //count of pieces
        centipawns +=
                (Long.bitCount(white_pawns) - Long.bitCount(black_pawns)) * Constants.VALUE_PAWN +
                        (Long.bitCount(white_bishops) - Long.bitCount(black_bishops)) * Constants.VALUE_BISHOP +
                        (Long.bitCount(white_knights) - Long.bitCount(black_knights)) * Constants.VALUE_KNIGHT +
                        (Long.bitCount(white_rooks) - Long.bitCount(black_rooks)) * Constants.VALUE_ROOK +
                        (Long.bitCount(white_queens) - Long.bitCount(black_queens)) * Constants.VALUE_QUEEN;

        //positional advantages
        //PAWNS
        //TODO Check whether this check is worth it or make it worth
        centipawns +=
                (Long.bitCount(white_pawns & Constants.white_pawns_3) - Long.bitCount(black_pawns & Constants.black_pawns_3)) * 3 +
                        (Long.bitCount(white_pawns & Constants.white_pawns_5) - Long.bitCount(black_pawns & Constants.black_pawns_5)) * 5 +
                        (Long.bitCount(white_pawns & Constants.white_pawns_10) - Long.bitCount(black_pawns & Constants.black_pawns_10)) * 10 +
                        (Long.bitCount(white_pawns & Constants.white_pawns_20) - Long.bitCount(black_pawns & Constants.black_pawns_20)) * 20 +
                        (Long.bitCount(white_pawns & Constants.white_pawns_25) - Long.bitCount(black_pawns & Constants.black_pawns_25)) * 25 +
                        (Long.bitCount(white_pawns & Constants.white_pawns_100) - Long.bitCount(black_pawns & Constants.black_pawns_100)) * 100;

        //KNIGHTS
        //TODO Check whether this check is worth it or make it worth
        if(!((white_knights | black_knights) == 0))
            centipawns +=
                    (Long.bitCount(white_knights & Constants.white_knights_n30) - Long.bitCount(black_knights & Constants.black_knights_n30)) * -30 +
                            (Long.bitCount(white_knights & Constants.white_knights_n40) - Long.bitCount(black_knights & Constants.black_knights_n40)) * -40 +
                            (Long.bitCount(white_knights & Constants.white_knights_n50) - Long.bitCount(black_knights & Constants.black_knights_n50)) * -50;

        //ROOKS
        centipawns +=
                (Long.bitCount(white_rooks & Constants.white_rooks_50) - Long.bitCount(black_rooks & Constants.black_rooks_50)) * 50;

        //KINGS
        if(turn < 30)
            centipawns +=
                    (Long.bitCount(white_king & Constants.white_king_n20) - Long.bitCount(black_king & Constants.black_king_n20)) * -20 +
                            (Long.bitCount(white_king & Constants.white_king_n40) - Long.bitCount(black_king & Constants.black_king_n40)) * -40 +
                            (Long.bitCount(white_king & Constants.white_king_n100) - Long.bitCount(black_king & Constants.black_king_n100)) * -100 +
                            (Long.bitCount(white_king & Constants.white_king_n200) - Long.bitCount(black_king & Constants.black_king_n200)) * -200 +
                            (Long.bitCount(white_king & Constants.white_king_n300) - Long.bitCount(black_king & Constants.black_king_n300)) * -300 +
                            (Long.bitCount(white_king & Constants.white_king_n400) - Long.bitCount(black_king & Constants.black_king_n400)) * -400 +
                            (Long.bitCount(white_king & Constants.white_king_n500) - Long.bitCount(black_king & Constants.black_king_n500)) * -500;


        return centipawns;

    }


    @Override
    public void onChatMessage(LiChessGame game, String message, String author, String room) {

    }

    @Override
    public void onFinish(LiChessGame game) {

    }
}
