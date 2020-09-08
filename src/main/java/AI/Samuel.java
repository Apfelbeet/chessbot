package AI;

import api.LiChessAccess;
import api.handlers.AIHandler;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

import java.util.List;

/**
 * Testing class (performance, algorithms, quality of results, etc).
 * Also the class where my 2 lines of code for all the handling of Lichess connection (with the help of your API <3 ) are.
 */
public class Samuel {

    public static void main(String[] args) {

        var access = new LiChessAccess(Constants.AccessToken);
        access.acceptAndJoinGames(new AIHandler());
    }


    public void moveMakeAndUndoPerformance(Board board) throws MoveGeneratorException {
        var moves = MoveGenerator.generateLegalMoves(board);
        int l = 10_000_000 / moves.size();
        var t1 = System.currentTimeMillis();
        for (int i = 0; i < l; i++) {
            for (Move move : moves) {
                board.doMove(move, false);
                board.undoMove();
            }
        }
        var t2 = System.currentTimeMillis();

        System.out.printf("Move execution and undo took %g seconds for %d repetitions%n", (t2 - t1) / 1000d, l * moves.size());
    }

    public void legalMoveCreationPerformance(Board board) throws MoveGeneratorException {
        int l = 1_000_000;
        var t1 = System.currentTimeMillis();
        for (int i = 0; i < l; i++) {
            MoveGenerator.generateLegalMoves(board);
        }
        var t2 = System.currentTimeMillis();

        System.out.printf("Legal move creation took %g seconds for %d repetitions%n", (t2 - t1) / 1000d, l);
    }

    public void listOrArrayOrBB(Board board, boolean drawCheck) {
        int l = 2_000_000;

//        System.out.println("\"Aufwärmen\"");

        for (int i = 0; i < 10000; i++) {
            var a1 = board.getPieceLocation(Piece.WHITE_PAWN);
            var a2 = board.getPieceLocation(Piece.WHITE_BISHOP);
            var a4 = board.getPieceLocation(Piece.WHITE_KNIGHT);
            var a5 = board.getPieceLocation(Piece.WHITE_QUEEN);
            var a6 = board.getPieceLocation(Piece.WHITE_ROOK);

            var b1 = board.boardToArray();
        }

        Side side = board.getSideToMove();

//        System.out.println("Piece-List");
        var t1 = System.currentTimeMillis();
        for (int i = 0; i < l; i++) {
            long eval = evalList(board, side, drawCheck);
        }
        long eval1 = evalList(board, side, drawCheck);
        var t2 = System.currentTimeMillis();

//        System.out.println("Piece-Array");
        var t3 = System.currentTimeMillis();
        for (int i = 0; i < l; i++) {
            long eval = evalArray(board, side, drawCheck);
        }
        long eval2 = evalArray(board, side, drawCheck);
        var t4 = System.currentTimeMillis();

        var t5 = System.currentTimeMillis();
        for (int i = 0; i < l; i++) {
            long eval = evalBB(board, side);
        }
        long eval3 = evalBB(board, side);
        var t6 = System.currentTimeMillis();

        System.out.printf("Square-List took %g seconds --- Piece-Array took %g seconds --- BB took %g seconds%n",
                (t2 - t1) / 1000d, (t4 - t3) / 1000d, (t6 - t5) / 1000d);
        System.out.printf("Square-List yielded %d --- Piece-Array yielded %d --- BB yielded %d%n", eval1, eval2, eval3);

    }

    /**
     * @deprecated Too slow
     */
    @Deprecated(forRemoval = true)
    public long evalList(Board board, Side side, boolean drawCheck) {

        if(drawCheck)
            if(board.isDraw())  //draw covers insufficient material, stalemate, threefold repetition, etc.
                return 0;

        if(board.isMated())
            return side == Side.WHITE ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        long centipawns = 0;

        if(board.isKingAttacked())
            centipawns += side == Side.WHITE ? 300 : -300;

        List<Square> white_pawns, white_bishops, white_knights, white_queens, white_rooks, black_pawns, black_bishops, black_knights, black_queens, black_rooks;
        Square white_king, black_king;

        white_pawns = board.getPieceLocation(Piece.WHITE_PAWN);
        white_bishops = board.getPieceLocation(Piece.WHITE_BISHOP);
        white_knights = board.getPieceLocation(Piece.WHITE_KNIGHT);
        white_queens = board.getPieceLocation(Piece.WHITE_QUEEN);
        white_rooks = board.getPieceLocation(Piece.WHITE_ROOK);
        white_king = board.getKingSquare(Side.WHITE);

        black_pawns = board.getPieceLocation(Piece.BLACK_PAWN);
        black_bishops = board.getPieceLocation(Piece.BLACK_BISHOP);
        black_knights = board.getPieceLocation(Piece.BLACK_KNIGHT);
        black_queens = board.getPieceLocation(Piece.BLACK_QUEEN);
        black_rooks = board.getPieceLocation(Piece.BLACK_ROOK);
        black_king = board.getKingSquare(Side.BLACK);

        //PAWNS
        for (Square w_p : white_pawns) {
            centipawns += Constants.VALUE_PAWN;
            centipawns += Constants.white_pawns_position[w_p.ordinal()];
        }

        for (Square b_p : black_pawns) {
            centipawns -= Constants.VALUE_PAWN;
            centipawns -= Constants.black_pawns_position[b_p.ordinal()];
        }

        //BISHOPS
        for (Square w_b : white_bishops) {
            centipawns += Constants.VALUE_BISHOP;
            //TODO How many squares are controlled
        }

        for (Square b_b : black_bishops) {
            centipawns -= Constants.VALUE_BISHOP;
            //TODO How many squares are controlled
        }

        //KNIGHTS
        for (Square w_k : white_knights) {
            centipawns += Constants.VALUE_KNIGHT;
            centipawns += Constants.white_knights_position[w_k.ordinal()];
        }

        for (Square b_k : black_knights) {
            centipawns -= Constants.VALUE_KNIGHT;
            centipawns -= Constants.black_knights_position[b_k.ordinal()];
        }

        //ROOKS
        for (Square w_r : white_rooks) {
            centipawns += Constants.VALUE_ROOK;
            centipawns += Constants.white_rooks_position[w_r.ordinal()];
        }

        for (Square b_r : black_rooks) {
            centipawns -= Constants.VALUE_ROOK;
            centipawns -= Constants.black_rooks_position[b_r.ordinal()];
        }

        //QUEENS
        for (Square w_q : white_queens) {
            centipawns += Constants.VALUE_QUEEN;
            //TODO How many squares are controlled
        }

        for (Square b_q : black_queens) {
            centipawns -= Constants.VALUE_QUEEN;
            //TODO How many squares are controlled
        }

        //KINGS

        //we do not need to add a value for the king, only for the loss of it
        centipawns += Constants.white_kings_position[white_king.ordinal()];

        //we do not need to add a value for the king, only for the loss of it
        centipawns -= Constants.black_kings_position[black_king.ordinal()];


        return centipawns;
    }

    /**
     * @deprecated Too slow
     */
    @Deprecated(forRemoval = true)
    public long evalArray(Board board, Side side, boolean drawCheck) {

        if(drawCheck)
            if(board.isDraw())  //draw covers insufficient material, stalemate, threefold repetition, etc.
                return 0;

        if(board.isMated())
            return side == Side.WHITE ? Integer.MAX_VALUE : Integer.MIN_VALUE;

        long centipawns = 0;

        if(board.isKingAttacked())
            centipawns += side == Side.WHITE ? 300 : -300;

        var pieces = board.boardToArray();

        for (int i = 0; i < 64; i++) {
            switch (pieces[i]) {
                case NONE -> {
                }
                case WHITE_PAWN -> {
                    centipawns += Constants.VALUE_PAWN;
                    centipawns += Constants.white_pawns_position[i];
                }
                case BLACK_PAWN -> {
                    centipawns -= Constants.VALUE_PAWN;
                    centipawns -= Constants.black_pawns_position[i];
                }
                case WHITE_BISHOP -> {
                    centipawns += Constants.VALUE_BISHOP;
                }
                case BLACK_BISHOP -> {
                    centipawns -= Constants.VALUE_BISHOP;
                }
                case WHITE_KNIGHT -> {
                    centipawns += Constants.VALUE_KNIGHT;
                    centipawns += Constants.white_knights_position[i];
                }
                case BLACK_KNIGHT -> {
                    centipawns -= Constants.VALUE_KNIGHT;
                    centipawns -= Constants.black_knights_position[i];
                }
                case WHITE_ROOK -> {
                    centipawns += Constants.VALUE_ROOK;
                    centipawns += Constants.white_rooks_position[i];
                }
                case BLACK_ROOK -> {
                    centipawns -= Constants.VALUE_ROOK;
                    centipawns -= Constants.black_rooks_position[i];
                }
                case WHITE_QUEEN -> {
                    centipawns += Constants.VALUE_QUEEN;
                }
                case BLACK_QUEEN -> {
                    centipawns -= Constants.VALUE_QUEEN;
                }
                case WHITE_KING -> {
                    centipawns += Constants.white_kings_position[i];
                }
                case BLACK_KING -> {
                    centipawns -= Constants.black_kings_position[i];
                }
            }
        }

        return centipawns;
    }

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
//        if(turn < 30)
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


}
