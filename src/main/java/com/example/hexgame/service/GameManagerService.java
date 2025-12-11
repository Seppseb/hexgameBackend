package com.example.hexgame.service;

import com.example.hexgame.model.*;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class GameManagerService {
    private final ConcurrentMap<String, GameInstance> games = new ConcurrentHashMap<>();
    // optional index of userId -> gameId (fast lookup)
    private final ConcurrentMap<String, String> playerToGame = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, String> playerToName = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public GameManagerService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public GameInstance createGame() {
        String id = UUID.randomUUID().toString();
        GameInstance g = new GameInstance(id, messagingTemplate);
        games.put(id, g);
        return g;
    }

    public Optional<GameInstance> getGame(String gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    public List<GameInstance> listGames() {
        return new ArrayList<>(games.values());
    }

    public JoinResult joinGame(String gameId, String requestedName) {
        GameInstance gi = games.get(gameId);
        if (gi == null) return JoinResult.error("Game not found");

        gi.getLock().lock();
        try {
            gi.touch();
            if (gi.getState() != GameState.WAITING_FOR_PLAYERS) {
                return JoinResult.error("Game already started");
            }
            if (gi.getPlayers().size() >= 4) {
                return JoinResult.error("Game is full");
            }
            String userId = UUID.randomUUID().toString();
            if (gi.getPlayers().size() == 0) {
                gi.setOwnerId(userId);
            }
            Player p = new Player(userId, requestedName, gi.getBank(), gi);
            gi.getPlayers().put(userId, p);
            playerToGame.put(userId, gameId);
            playerToName.put(userId, requestedName);
            gi.sendMessage("JOINED_GAME", requestedName, "", requestedName);
            return JoinResult.success(userId, gameId, requestedName);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean startGame(String gameId) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (gi.getState() != GameState.WAITING_FOR_PLAYERS) return false;
            if (gi.getPlayers().size() < 2) return false;
            gi.startGame();
            gi.sendMessage("STARTED_GAME", "", "", "");
            return true;
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean readyPlayer(String gameId, String playerId) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            gi.readyPlayer(playerId);
            return true;
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean build(String gameId, String playerId, int row, int col) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.build(playerId, row, col);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean buildRoad(String gameId, String playerId, int row, int col) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.buildRoad(playerId, row, col);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean buyDevelopment(String gameId, String playerId) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.buyDevelopment(playerId);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean playDevelopment(String gameId, String playerId, String type) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.playDevelopment(playerId, type);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean bankTrade(String gameId, String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.bankTrade(playerId, wood, clay, wheat, wool, stone);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean askPlayerTrade(String gameId, String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.askPlayerTrade(playerId, wood, clay, wheat, wool, stone);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean acceptPlayerTrade(String gameId, String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.acceptPlayerTrade(playerId, wood, clay, wheat, wool, stone);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean cancelPlayerTrade(String gameId, String playerId) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.cancelPlayerTrade(playerId);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean declinePlayerTrade(String gameId, String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.declinePlayerTrade(playerId, wood, clay, wheat, wool, stone);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean finishPlayerTrade(String gameId, String playerId, String partnerId) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.finishPlayerTrade(playerId, partnerId);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean settleDebt(String gameId, String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.settleDebt(playerId, wood, clay, wheat, wool, stone);
        } finally {
            gi.getLock().unlock();
        }
    }

    public boolean endTurn(String gameId, String playerId) {
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;
        gi.getLock().lock();
        try {
            if (!gi.getPlayers().containsKey(playerId)) return false;
            return gi.endTurn(playerId);
        } finally {
            gi.getLock().unlock();
        }
    }

    public void sendUpdate(String gameId) {
        GameInstance gi = games.get(gameId);
        gi.sendMessage("UPDATE", "game changed", "", "");
    }

    public boolean leaveGame(String userId) {
        String gameId = playerToGame.get(userId);
        if (gameId == null) return false;
        GameInstance gi = games.get(gameId);
        if (gi == null) return false;

        gi.getLock().lock();
        try {
            gi.getPlayers().remove(userId);
            playerToGame.remove(userId);
            gi.touch();
            if (!gi.getPlayers().isEmpty()) {
                for (var playerId : gi.getPlayers().keySet()) {
                    gi.setOwnerId(playerId);
                    break;
                }
            }
            return true;
        } finally {
            gi.getLock().unlock();
        }
    }

    public String getPlayerName(String playerId) {
        if (playerId == null) return "";
        if (!playerToName.containsKey(playerId)) return "";
        return playerToName.get(playerId);
    }


    // Basic garbage cleanup to remove idle games older than threshold
    public void cleanupIdleGames(Duration maxIdle) {
        Instant cutoff = Instant.now().minus(maxIdle);
        for (Map.Entry<String, GameInstance> e : games.entrySet()) {
            GameInstance g = e.getValue();
            if (g.getLastActive().isBefore(cutoff)) {
                games.remove(e.getKey());
            }
        }
    }

    // helper class
    public static class JoinResult {
        public final boolean ok;
        public final String userId;
        public final String gameId;
        public final String message;
        public final String name;

        private JoinResult(boolean ok, String userId, String gameId, String message, String name) {
            this.ok = ok; this.userId = userId; this.gameId = gameId; this.message = message; this.name = name;
        }

        public static JoinResult success(String userId, String gameId, String name) {
            return new JoinResult(true, userId, gameId, "joined", name);
        }
        public static JoinResult error(String msg) {
            return new JoinResult(false, null, null, msg, null);
        }
    }
}
