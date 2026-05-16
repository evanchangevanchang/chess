package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{
    private final static HashMap<String, GameData> GAME_MAP = new HashMap<>();

    public void clearGame() {
        GAME_MAP.clear();
    }
}

