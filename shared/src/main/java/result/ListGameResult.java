package result;

import model.GameData;

import java.util.Collection;

public record ListGameResult(
        Collection<GameData> gameDataCollection
) {
}
