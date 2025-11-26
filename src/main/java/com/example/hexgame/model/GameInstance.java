package com.example.hexgame.model;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.example.hexgame.dto.GameEvent;

public class GameInstance {
    private String id; // game id (UUID)
    private Board board;
    private Bank bank;
    private Map<String, Player> players = new HashMap<>();
    private Set<String> needReady = new HashSet<>();
    private String ownerId;
    private Player currentPlayer;
    private Player[] initialPlacementOrder;
    private int initialPlacementIndex = 0;
    private boolean initialIsPlacingRoad = false;

    public boolean isInitialIsPlacingRoad() {
        return initialIsPlacingRoad;
    }

    private final SimpMessagingTemplate messagingTemplate;

    private Random random;

    private GameState state = GameState.WAITING_FOR_PLAYERS;
    private Instant lastActive = Instant.now();

    // Lock for per-game concurrency
    private final transient ReentrantLock lock = new ReentrantLock();

    public GameInstance(String id, SimpMessagingTemplate messagingTemplate) {
        this.random = new Random();
        this.id = id;
        this.board = new Board(this.random);
        this.bank = new Bank(this.random);
        this.messagingTemplate = messagingTemplate;
    }

    public int[] throw2Dice() {
        int die1 = random.nextInt(6) + 1;
        int die2 = random.nextInt(6) + 1;
        return new int[]{die1, die2};
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public boolean isGameOwner(String userId) {
        if (players.size() == 0) return false;
        return userId.equals(ownerId);
    }

    public void startGame() {
        for (Player player : players.values()) {
            needReady.add(player.getUserId());
        }
    }

    public void readyPlayer(String playerId) {
        //System.out.println(needReady);
        //System.out.println(playerId);
        if (!players.containsKey(playerId)) return;
        switch (state) {
            case WAITING_FOR_PLAYERS:
                needReady.remove(playerId);
                if (needReady.size() == 0) {
                    state = GameState.ROLL_FOR_POSITION;
                    throwInitialDice();
                    assignColors();
                }
                break;
            case ROLL_FOR_POSITION:
                needReady.remove(playerId);
                if (needReady.size() == 0) {
                    state = GameState.PLACEMENT;
                    //System.out.println(currentPlayer);
                    if (currentPlayer != null) {
                        nextInitialBuild();        
                    }
                }
                break;
            case PLACEMENT:
                break;
            case IN_PROGRESS:
                break;
            case FINISHED:
                break;
        }
        //System.out.println(needReady);
    }

    public void assignColors() {
        List<String> colors = new ArrayList<>();
        colors.add("red");
        colors.add("blue");
        colors.add("green");
        colors.add("yellow");
        for (Player player : players.values()) {
            int i = random.nextInt(colors.size());
            player.setColor(colors.get(i));
            colors.remove(i);
        }
    }

    public void throwInitialDice() {
        TreeMap<Integer, Player> playerOrder = new TreeMap<>();
        for (Player player : players.values()) {
            needReady.add(player.getUserId());
            int[] d = throw2Dice();
            for (int i = 0; i < 20; i++) {
                if (!playerOrder.containsKey(d[0] + d[1])) break;
                d = throw2Dice();
            }
            playerOrder.put(d[0] + d[1], player);
            sendMessage("INITIAL_ROLL", "" + d[0] + "" + d[1], player.getUserId());            
        }
        currentPlayer = playerOrder.lastEntry().getValue();
        initialPlacementOrder = new Player[playerOrder.size() * 2];
        int i = 0;
        for (Player player : playerOrder.values()) {
            player.setNextPlayer(currentPlayer);
            currentPlayer = player;
            initialPlacementOrder[playerOrder.size() - 1 - i] = player;
            initialPlacementOrder[playerOrder.size() + i] = player;
            i++;
        }
    }

    public void sendMessage(String type, String message, String targetPlayerId) {
        System.out.println(type + " for: " + ("".equals(targetPlayerId) ? "all" : targetPlayerId) + ": " + message);
        messagingTemplate.convertAndSend(
            "/topic/games/" + id,
            Map.of("type", type, "message", message, "playerId", targetPlayerId, "game", this)
        );
    }

    public void nextAction() {

    }

    // getters/setters for id, board, players, state, lastActive
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Board getBoard() { return board; }
    public Map<String, Player> getPlayers() { return players; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Instant getLastActive() { return lastActive; }
    public void touch() { this.lastActive = Instant.now(); }


    public String getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }


    public boolean build(String playerId, int row, int col) {
        //check player turn
        //check game phase
        //check player ressources only if gamestat erunning not initial build
        //check player building avaliable -> implemnet
        //check building spot valid
        //check row, col

        if (!currentPlayer.getUserId().equals(playerId)) return false;
        Player player = players.get(playerId);
        if (initialIsPlacingRoad) return false;
        if (board.getNodes().length <= row || board.getNodes()[row].length <= col ) return false;
        Node spot = board.getNodes()[row][col];
        if (spot.getBuildFactor() > 1) return false;
        if (null == state) return false; else switch (state) {
            case PLACEMENT:
                if (!spot.canBuildFreeVillage(player)) return false;
                player.buildFreeVillage();
                spot.buildVillage(player);
                initialIsPlacingRoad = true;
                sendMessage("BUILD", playerId + " at " + row + ", " + col, "");
                break;
            case IN_PROGRESS:
                if (spot.getBuildFactor() == 0) {
                    if (!player.canBuildVillage() || !spot.canBuildVillage(player)) return false;
                    player.buildVillage();
                    bank.buildVillage();
                    spot.buildVillage(player);
                    sendMessage("BUILD", playerId + " at " + row + ", " + col, "");
                } else {
                    if (!player.canBuildCity() || !spot.canBuildCity(player)) return false;
                    player.buildCity();
                    bank.buildCity();
                    spot.buildCity(player);
                    sendMessage("BUILD", playerId + " at " + row + ", " + col, "");
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean buildRoad(String playerId, int row, int col) {
        //check player turn
        //check game phase
        //check player ressources only if gamestat erunning not initial build
        //check player building avaliable -> implemnet
        //check building spot valid
        //check row, col
        System.out.println(row + " " + col);

        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (!initialIsPlacingRoad) return false;
        Player player = players.get(playerId);
        if (board.getPaths().length <= row || board.getPaths()[row].length <= col ) return false;
        Path path = board.getPaths()[row][col];
        if (path.getOwner() != null) return false;
        if (null == state) return false; else switch (state) {
            case PLACEMENT:
                if (!path.canBuildFreeRoad(player)) return false;
                player.buildFreeRoad();
                path.buildRoad(player);
                sendMessage("BUILD_ROAD", playerId + " at " + row + ", " + col, "");
                initialIsPlacingRoad = false;
                nextInitialBuild();
                break;
            case IN_PROGRESS:
                if (!player.canBuildRoad() || !path.canBuildRoad(player)) return false;
                player.buildRoad();
                bank.buildRoad();
                path.buildRoad(player);
                sendMessage("BUILD_ROAD", playerId + " at " + row + ", " + col, "");
                break;
            default:
                return false;
        }
        return true;
    }

    public void nextInitialBuild() {
        if (initialPlacementIndex >= initialPlacementOrder.length) {
            state = GameState.IN_PROGRESS;
            startTurn();
            return;
        }
        currentPlayer = initialPlacementOrder[initialPlacementIndex];
        initialPlacementIndex++;
        sendMessage("INITIAL_PLACE", "", currentPlayer.getUserId());    
    }

    public void startTurn() {
        int[] d = throw2Dice();
        sendMessage("START_TURN", "" + d[0] + "" + d[1], currentPlayer.getUserId());            
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
