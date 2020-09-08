package AI;

public class Constants {
    public static final int
            VALUE_PAWN = 100,
            VALUE_KNIGHT = 310,
            VALUE_BISHOP = 320,
            VALUE_ROOK = 500,
            VALUE_QUEEN = 900;

    // @formatter:off
    public static final int[]
            white_pawns_position = {
            0,  0,  0,  0,  0,  0,  0,  0,  //can't be on that row anyways
            0,  0,  0,  0,  0,  0,  0,  0,  //no special advantage in being on the starting row
            5,  0,  0,  10, 10, 0,  0,  5,  //3rd rank
            0,  0,  5,  20, 20, 5,  0,  0,  //4th rank
            0,  0,  5,  20, 20, 5,  0,  0,  //5th rank
            3,  3,  10, 25, 25, 10, 3,  3,  //6th rank
            100,100,100,100,100,100,100,100,//7th rank
            900,900,900,900,900,900,900,900 //8th rank  //actually there is no need for this row because pieces are promoted by now anyways
    },
            black_pawns_position = new int[64],           //white_pawns_position mirrored between the 4th and 5th rank

    //The bishops' positions can't be statically declared

    white_knights_position = {          //outer borders are bad
            -50,-50,-50,-50,-50,-50,-50,-50,
            -50,-40,-30,-30,-30,-30,-40,-50,
            -50,-30,0,  0,  0,  0,  -30,-50,
            -50,-30,0,  0,  0,  0,  -30,-50,
            -50,-30,0,  0,  0,  0,  -30,-50,
            -50,-30,0,  0,  0,  0,  -30,-50,
            -50,-40,-30,-30,-30,-30,-40,-50,
            -50,-50,-50,-50,-50,-50,-50,-50,
    },
            black_knights_position = white_knights_position,

    white_rooks_position = {
            0,  0,  0,  0,  0,  0,  0,  0,  //1st rank
            0,  0,  0,  0,  0,  0,  0,  0,  //2nd rank
            0,  0,  0,  0,  0,  0,  0,  0,  //3rd rank
            0,  0,  0,  0,  0,  0,  0,  0,  //4th rank
            0,  0,  0,  0,  0,  0,  0,  0,  //5th rank
            0,  0,  0,  0,  0,  0,  0,  0,  //6th rank
            50, 50, 50, 50, 50, 50, 50, 50, //7th rank
            0,  0,  0,  0,  0,  0,  0,  0,  //8th rank
    },
            black_rooks_position = new int[64],             //white_rooks_position mirrored between the 4th and 5th rank

    //The queens' positions can't be statically declared


    white_kings_position = {                //only take this into account within the first 30 moves
            0,  0,  0,  0,  0,  0,  0,  0,  //1st rank
            -20,-20,-20,-20,-20,-20,-20,-20,//2nd rank
            -40,-40,-40,-40,-40,-40,-40,-40,//3rd rank
            -100,-100,-100,-100,-100,-100,-100,-100,//4th rank
            -200,-200,-200,-200,-200,-200,-200,-200,//5th rank
            -300,-300,-300,-300,-300,-300,-300,-300,//6th rank
            -400,-400,-400,-400,-400,-400,-400,-400,//7th rank
            -500,-500,-500,-500,-500,-500,-500,-500,//8th rank
    },
            black_kings_position = new int[64];             //white_kings_position mirrored between the 4th and 5th rank
    // @formatter:on

    //Masks - be aware that the masks are reversed in order when read compared to the arrays (Last value [0b...0001] is A1)
    public static final long
            black_pawns_3 = 0b00000000_00000000_00000000_00000000_00000000_11000011_00000000_00000000L,
            black_pawns_5 = 0b00000000_00000000_10000001_00100100_00100100_00000000_00000000_00000000L,
            black_pawns_10 = 0b00000000_00000000_00011000_00000000_00000000_00000000_00000000_00000000L,
            black_pawns_20 = 0b00000000_00000000_00000000_00011000_00011000_00000000_00000000_00000000L,
            black_pawns_25 = 0b00000000_00000000_00000000_00000000_00000000_00011000_00000000_00000000L,
            black_pawns_100 = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000L,
//            black_pawns_900 = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L,
            white_pawns_3 = Long.reverseBytes(black_pawns_3),
            white_pawns_5 = Long.reverseBytes(black_pawns_5),
            white_pawns_10 = Long.reverseBytes(black_pawns_10),
            white_pawns_20 = Long.reverseBytes(black_pawns_20),
            white_pawns_25 = Long.reverseBytes(black_pawns_25),
            white_pawns_100 = Long.reverseBytes(black_pawns_100),
//            white_pawns_900 = Long.reverseBytes(black_pawns_900),
            black_knights_n50 = 0b11111111_10000001_10000001_10000001_10000001_10000001_10000001_11111111L,
            black_knights_n40 = 0b00000000_01000010_00000000_00000000_00000000_00000000_01000010_00000000L,
            black_knights_n30 = 0b00000000_00111100_01000010_01000010_01000010_01000010_00111100_00000000L,
            white_knights_n50 = black_knights_n50,
            white_knights_n40 = black_knights_n40,
            white_knights_n30 = black_knights_n30,
            black_rooks_50 = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000L,
            white_rooks_50 = Long.reverseBytes(black_rooks_50),
            black_king_n20 = 0b00000000_11111111_00000000_00000000_00000000_00000000_00000000_00000000L,
            black_king_n40 = 0b00000000_00000000_11111111_00000000_00000000_00000000_00000000_00000000L,
            black_king_n100 = 0b00000000_00000000_00000000_11111111_00000000_00000000_00000000_00000000L,
            black_king_n200 = 0b00000000_00000000_00000000_00000000_11111111_00000000_00000000_00000000L,
            black_king_n300 = 0b00000000_00000000_00000000_00000000_00000000_11111111_00000000_00000000L,
            black_king_n400 = 0b00000000_00000000_00000000_00000000_00000000_00000000_11111111_00000000L,
            black_king_n500 = 0b00000000_00000000_00000000_00000000_00000000_00000000_00000000_11111111L,
            white_king_n20 = Long.reverseBytes(black_king_n20),
            white_king_n40 = Long.reverseBytes(black_king_n40),
            white_king_n100 = Long.reverseBytes(black_king_n100),
            white_king_n200 = Long.reverseBytes(black_king_n200),
            white_king_n300 = Long.reverseBytes(black_king_n300),
            white_king_n400 = Long.reverseBytes(black_king_n400),
            white_king_n500 = Long.reverseBytes(black_king_n500);

    public static final byte depth = 6;

    public static String[] rooms = {"player", "spectator"};

    public static String AccessToken = "ivlEbRfPbVLydRUa";

    static {
        for (int i = 0, j = 7; i < 8; i++, j--) {
            for (int k = 0; k < 8; k++) {
                int b = i * 8 + k;
                int w = j * 8 + k;
                black_pawns_position[b] = white_pawns_position[w];
                black_rooks_position[b] = white_rooks_position[w];
                black_kings_position[b] = white_kings_position[w];
            }
        }
    }
}
