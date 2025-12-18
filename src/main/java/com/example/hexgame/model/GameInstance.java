package com.example.hexgame.model;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.messaging.simp.SimpMessagingTemplate;

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
    private boolean isWaitingForPlayerRessourceChange = false;
    private boolean isWaitingForFreeRoadPlacement = false;
    private boolean isWaitingForMovingRobber = false;

    private MostKnightsCard mostKnightsCard;

    private TradeOffer currentTradeOffer;

    //TODO can be changed in future; not tested yet
    private boolean canTradeMultipleRessourcesAtOnce = false;

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
        this.mostKnightsCard = new MostKnightsCard();
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
        if (!players.containsKey(playerId)) return;
        switch (state) {
            case WAITING_FOR_PLAYERS:
                needReady.remove(playerId);
                if (needReady.size() == 0) {
                    state = GameState.ROLL_FOR_POSITION;
                    initializeGame();
                }
                break;
            case ROLL_FOR_POSITION:
                needReady.remove(playerId);
                if (needReady.size() == 0) {
                    state = GameState.PLACEMENT;
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
    }

    public void assignColors() {
        List<String> colors = new ArrayList<>();
        colors.add("red");
        colors.add("blue");
        colors.add("green");
        colors.add("yellow");
        for (Player player : players.values()) {
            player.addRes(TileType.wood, 5);
            player.addRes(TileType.clay, 5);
            player.addRes(TileType.wheat, 5);
            player.addRes(TileType.wool, 5);
            player.addRes(TileType.stone, 5);
            int i = random.nextInt(colors.size());
            player.setColor(colors.get(i));
            colors.remove(i);
        }
    }

    public void initializeGame() {
        throwInitialDice();
        assignColors();
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
            sendMessage("INITIAL_ROLL", "" + d[0] + "" + d[1], player.getUserId(), player.getName());            
        }
        currentPlayer = playerOrder.lastEntry().getValue();
        initialPlacementOrder = new Player[playerOrder.size() * 2];
        int i = 0;
        for (Player player : playerOrder.values()) {
            player.setNextPlayer(currentPlayer);
            currentPlayer = player;
            initialPlacementOrder[playerOrder.size() - 1 - i] = player;
            initialPlacementOrder[playerOrder.size() + i] = player;
            player.setPlayerIndex(playerOrder.size() - 1 - i);
            i++;
        }
    }

    public void sendMessage(String type, String message, String targetPlayerId, String causer) {
        System.out.println(type + " for: " + ("".equals(targetPlayerId) ? "all" : targetPlayerId) + ": " + message);
        messagingTemplate.convertAndSend(
            "/topic/games/" + id,
            Map.of("type", type, "message", message, "playerId", targetPlayerId, "game", this, "playerName", causer)
        );
    }

    public void nextInitialBuild() {
        if (initialPlacementIndex >= initialPlacementOrder.length) {
            state = GameState.IN_PROGRESS;
            startTurn();
            return;
        }
        currentPlayer = initialPlacementOrder[initialPlacementIndex];
        initialPlacementIndex++;
        sendMessage("INITIAL_PLACE", "", currentPlayer.getUserId(), currentPlayer.getName());    
    }


    public boolean build(String playerId, int row, int col) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        Player player = players.get(playerId);
        if (board.getNodes().length <= row || board.getNodes()[row].length <= col ) return false;
        Node spot = board.getNodes()[row][col];
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        if (spot.getBuildFactor() > 1) return false;
        if (null == state) return false; else switch (state) {
            case PLACEMENT:
                if (initialIsPlacingRoad) return false;
                if (!spot.canBuildInitialVillage(player) || !player.canBuildFreeVillage()) return false;
                player.buildFreeVillage();
                boolean isSecondPlaceRound = initialPlacementIndex > players.size();
                spot.buildInitialVillage(player, isSecondPlaceRound);
                initialIsPlacingRoad = true;
                sendMessage("BUILD", playerId + " at " + row + ", " + col, "", player.getName());
                break;
            case IN_PROGRESS:
                if (spot.getBuildFactor() == 0) {
                    if (!player.canBuildVillage() || !spot.canBuildVillage(player)) return false;
                    player.buildVillage();
                    spot.buildVillage(player);
                    board.checkLongestRoad(); //since villages could break up roads
                    sendMessage("BUILD", playerId + " at " + row + ", " + col, "", player.getName());
                } else {
                    if (!player.canBuildCity() || !spot.canBuildCity(player)) return false;
                    player.buildCity();
                    spot.buildCity(player);
                    sendMessage("BUILD", playerId + " at " + row + ", " + col, "", player.getName());
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean buildRoad(String playerId, int row, int col) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        Player player = players.get(playerId);
        if (board.getPaths().length <= row || board.getPaths()[row].length <= col ) return false;
        Path path = board.getPaths()[row][col];
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        if (path.getOwner() != null) return false;
        if (null == state) return false; else switch (state) {
            case PLACEMENT:
                if (!initialIsPlacingRoad) return false;
                if (!path.canBuildInitialRoad(player) || !player.canBuildFreeRoad(true)) return false;
                player.buildFreeRoad(true);
                path.buildInitialRoad(player);
                initialIsPlacingRoad = false;
                sendMessage("BUILD_ROAD", playerId + " at " + row + ", " + col, "", player.getName());
                nextInitialBuild();
                break;
            case IN_PROGRESS:
                boolean isFree = player.canBuildFreeRoad(false);
                if (!isFree && !player.canBuildRoad()) return false;
                if (!path.canBuildRoad(player)) return false;
                if (isFree) player.buildFreeRoad(false);
                else player.buildRoad();
                isWaitingForFreeRoadPlacement = player.canBuildFreeRoad(false);
                path.buildRoad(player);
                sendMessage("BUILD_ROAD", playerId + " at " + row + ", " + col, "", player.getName());
                break;
            default:
                return false;
        }
        this.board.checkLongestRoad();
        return true;
    }

    public boolean buyDevelopment(String playerId) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        Player player = players.get(playerId);
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                if (!player.canBuyDevelopment()) return false;
                player.buyDevelopment();
                sendMessage("BOUGTH_DEVELOPMENT", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean playDevelopment(String playerId, String type, String resType) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        Player player = players.get(playerId);
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                DevelopmentItem card = player.getDevelopmentCard(type);
                player.playDevelopmentCard(card);
                switch (card.getType()) {
                    case knight:
                        this.isWaitingForMovingRobber = true;
                        this.mostKnightsCard.checkOwnerChange(player);
                        break;
                    case development:
                        int takeAmount = this.bank.getTotalResBalance() < 2 ? this.bank.getTotalResBalance() : 2;
                        player.addToResDebt(-takeAmount);
                        this.upDateWaitingForResDebt();
                        break;
                    case roadwork:
                        player.addFreeRoads(2);
                        isWaitingForFreeRoadPlacement = player.canBuildFreeRoad(false);
                        break;
                    case monopoly:
                        TileType choosenRes = null;
                        switch (resType) {
                            case "wood":
                                choosenRes = TileType.wood;
                                break;
                            case "clay":
                                choosenRes = TileType.clay;
                                break;
                            case "wheat":
                                choosenRes = TileType.wheat;
                                break;
                            case "wool":
                                choosenRes = TileType.wool;
                                break;
                            case "stone":
                                choosenRes = TileType.stone;
                                break;
                            default: return false;
                        }
                        for (Player victim : players.values()) {
                            if (victim == player) continue;
                            int amount = victim.getResBalance().get(choosenRes);
                            victim.takeRes(choosenRes, amount);
                            player.addRes(choosenRes, amount);
                        }
                        break;
                    case victoryPoint:
                        player.addVictoryPoints(1);
                        break;
                }                
                sendMessage("PLAYED_DEVELOPMENT", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public void sendWin(Player player) {
        //TODO handle in frontend
        sendMessage("WON", player.getUserId(), "", player.getName());
    }

    public boolean bankTrade(String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        Player player = players.get(playerId);
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                HashMap<TileType, Integer> tradeRes = new HashMap<TileType, Integer>();
                tradeRes.put(TileType.wood, wood);
                tradeRes.put(TileType.clay, clay);
                tradeRes.put(TileType.wheat, wheat);
                tradeRes.put(TileType.wool, wool);
                tradeRes.put(TileType.stone, stone);

                int takenRessources = 0;
                int canTakeRessources = 0;
                double canTakeRessourcesFromOverFlow = 0;
                for (TileType res: tradeRes.keySet()) {
                    int playerGetsAmount = tradeRes.get(res);
                    if (playerGetsAmount > 0) {
                        if (!bank.hasRes(res, playerGetsAmount)) return false;
                        takenRessources += playerGetsAmount;
                    } else if (playerGetsAmount < 0) {
                        int playerGivesAmount = -playerGetsAmount;
                        if (!player.hasRes(res, playerGivesAmount)) return false;
                        int overflow = playerGivesAmount % player.getTradeFactor(res);
                        playerGivesAmount-= overflow;
                        canTakeRessourcesFromOverFlow += overflow / player.getTradeFactor(res);
                        canTakeRessources += playerGivesAmount / player.getTradeFactor(res);
                    }
                }
                if (canTradeMultipleRessourcesAtOnce) {
                    canTakeRessources += canTakeRessourcesFromOverFlow;
                } else {
                    if (canTakeRessourcesFromOverFlow != 0) return false;
                }
                if (takenRessources != canTakeRessources) {
                    return false;
                }
                for (TileType res: tradeRes.keySet()) {
                    int amount = tradeRes.get(res);
                    if (amount > 0) {
                        player.addRes(res, amount);
                    } else if (amount < 0) {
                        player.takeRes(res, -amount);
                    }
                }
                sendMessage("BANK_TRADE", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean askPlayerTrade(String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        Player player = players.get(playerId);
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                if (wood < 0 && !player.hasRes(TileType.wood, -wood)) return false;
                if (clay < 0 && !player.hasRes(TileType.clay, -clay)) return false;
                if (wheat < 0 && !player.hasRes(TileType.wheat, -wheat)) return false;
                if (wool < 0 && !player.hasRes(TileType.wool, -wool)) return false;
                if (stone < 0 && !player.hasRes(TileType.stone, -stone)) return false;
                this.currentTradeOffer = new TradeOffer(playerId, wood, clay, wheat, wool, stone);
                sendMessage("NEW_TRADE_OFFER", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean cancelPlayerTrade(String playerId) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        Player player = players.get(playerId);
        if (currentTradeOffer == null) return false;
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                this.currentTradeOffer = null;
                sendMessage("CANCLED_TRADE_OFFER", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean acceptPlayerTrade(String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        Player player = players.get(playerId);
        if (currentTradeOffer == null) return false;
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                if (wood > 0 && !player.hasRes(TileType.wood, wood)) return false;
                if (clay > 0 && !player.hasRes(TileType.clay, clay)) return false;
                if (wheat > 0 && !player.hasRes(TileType.wheat, wheat)) return false;
                if (wool > 0 && !player.hasRes(TileType.wool, wool)) return false;
                if (stone > 0 && !player.hasRes(TileType.stone, stone)) return false;
                if (!this.currentTradeOffer.hasValues(wood, clay, wheat, wool, stone)) return false;
                this.currentTradeOffer.accept(playerId);
                sendMessage("ACCEPTED_TRADE_OFFER", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean declinePlayerTrade(String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        Player player = players.get(playerId);
        if (currentTradeOffer == null) return false;
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                if (!this.currentTradeOffer.hasValues(wood, clay, wheat, wool, stone)) return false;
                this.currentTradeOffer.decline(playerId);
                sendMessage("DECLINED_TRADE_OFFER", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean finishPlayerTrade(String playerId, String partnerId) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (currentPlayer.getUserId().equals(partnerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        if (currentTradeOffer == null) return false;
        if (!currentTradeOffer.getAcceptersId().get(partnerId)) return false;
        Player player = players.get(playerId);
        Player partner = players.get(partnerId);
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                int wood = this.currentTradeOffer.getWood();
                int clay = this.currentTradeOffer.getClay();
                int wheat = this.currentTradeOffer.getWheat();
                int wool = this.currentTradeOffer.getWool();
                int stone = this.currentTradeOffer.getStone();

                if (wood < 0 && !player.hasRes(TileType.wood, -wood)) return false;
                if (clay < 0 && !player.hasRes(TileType.clay, -clay)) return false;
                if (wheat < 0 && !player.hasRes(TileType.wheat, -wheat)) return false;
                if (wool < 0 && !player.hasRes(TileType.wool, -wool)) return false;
                if (stone < 0 && !player.hasRes(TileType.stone, -stone)) return false;

                if (wood > 0 && !partner.hasRes(TileType.wood, wood)) return false;
                if (clay > 0 && !partner.hasRes(TileType.clay, clay)) return false;
                if (wheat > 0 && !partner.hasRes(TileType.wheat, wheat)) return false;
                if (wool > 0 && !partner.hasRes(TileType.wool, wool)) return false;
                if (stone > 0 && !partner.hasRes(TileType.stone, stone)) return false;

                if (wood != 0) if (wood > 0) {
                    partner.takeRes(TileType.wood, wood);
                    player.addRes(TileType.wood, wood);
                } else {
                    player.takeRes(TileType.wood, -wood);
                    partner.addRes(TileType.wood, -wood);
                }
                if (clay != 0) if (clay > 0) {
                    partner.takeRes(TileType.clay, clay);
                    player.addRes(TileType.clay, clay);
                } else {
                    player.takeRes(TileType.clay, -clay);
                    partner.addRes(TileType.clay, -clay);
                }
                if (wheat != 0) if (wheat > 0) {
                    partner.takeRes(TileType.wheat, wheat);
                    player.addRes(TileType.wheat, wheat);
                } else {
                    player.takeRes(TileType.wheat, -wheat);
                    partner.addRes(TileType.wheat, -wheat);
                }
                if (wool != 0) if (wool > 0) {
                    partner.takeRes(TileType.wool, wool);
                    player.addRes(TileType.wool, wool);
                } else {
                    player.takeRes(TileType.wool, -wool);
                    partner.addRes(TileType.wool, -wool);
                }
                if (stone != 0) if (stone > 0) {
                    partner.takeRes(TileType.stone, stone);
                    player.addRes(TileType.stone, stone);
                } else {
                    player.takeRes(TileType.stone, -stone);
                    partner.addRes(TileType.stone, -stone);
                }
                this.currentTradeOffer = null;
                sendMessage("FINISHED_TRADE_OFFER", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean settleDebt(String playerId, int wood, int clay, int wheat, int wool, int stone) {
        if (isWaitingForFreeRoadPlacement) return false;
        if (!isWaitingForPlayerRessourceChange) return false;
        Player player = players.get(playerId);
        if (player.getResDebt() == 0) return false;
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                HashMap<TileType, Integer> tradeRes = new HashMap();
                tradeRes.put(TileType.wood, wood);
                tradeRes.put(TileType.clay, clay);
                tradeRes.put(TileType.wheat, wheat);
                tradeRes.put(TileType.wool, wool);
                tradeRes.put(TileType.stone, stone);

                int takenRessources = 0;
                int givenRessources = 0;
                System.out.println("a");

                for (TileType res: tradeRes.keySet()) {
                    int playerGetsAmount = tradeRes.get(res);
                    if (playerGetsAmount > 0) {
                        if (!bank.hasRes(res, playerGetsAmount)) return false;
                        System.out.println("ab");
                        takenRessources += playerGetsAmount;
                    } else if (playerGetsAmount < 0) {
                        int playerGivesAmount = -playerGetsAmount;
                        System.out.println("ac");
                        if (!player.hasRes(res, playerGivesAmount)) return false;
                        givenRessources += playerGivesAmount;
                    }
                }
                System.out.println("acd");
                if (player.getResDebt() < 0) {
                    System.out.println("acdbbbb");
                    if (givenRessources != 0) return false;
                    System.out.println("acdbbbb");
                    if (-player.getResDebt() != takenRessources) return false;
                    System.out.println("acdbbbb");
                } else {
                    System.out.println("acdaaa");
                    if (takenRessources != 0) return false;
                    System.out.println("acdaaa");
                    if (player.getResDebt() != givenRessources) return false;
                    System.out.println("acdaaa");
                }
                for (TileType res: tradeRes.keySet()) {
                    int amount = tradeRes.get(res);
                    if (amount > 0) {
                        player.addRes(res, amount);
                        player.addToResDebt(amount);
                    } else if (amount < 0) {
                        player.takeRes(res, -amount);
                        player.addToResDebt(amount);
                    }
                }
                this.upDateWaitingForResDebt();
                sendMessage("SETTLED_DEBT", playerId, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }

    public void upDateWaitingForResDebt() {
        for (Player player: this.players.values()) {
            if (player.getResDebt() != 0) {
                this.isWaitingForPlayerRessourceChange = true;
                return;
            }
        }
        this.isWaitingForPlayerRessourceChange = false;
    }

    public boolean moveRobber(String playerId, int oldRow, int oldCol, int row, int col) {
        System.out.println("a");
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        System.out.println("ab");
        Player player = players.get(playerId);
        if (board.getTiles().length <= oldRow || board.getTiles()[oldRow].length <= oldCol ) return false;
        System.out.println("aw");
        if (board.getTiles().length <= row || board.getTiles()[row].length <= col ) return false;
        System.out.println("ad");
        Tile oldTile = board.getTiles()[oldRow][oldCol];
        Tile tile = board.getTiles()[row][col];
        if (isWaitingForFreeRoadPlacement) return false;
        System.out.println("ah");
        if (isWaitingForPlayerRessourceChange) return false;
        System.out.println("aw");
        if (!isWaitingForMovingRobber) return false;
        System.out.println("ak");
        if (!oldTile.hasRobber()) return false;
        System.out.println("al");
        if (tile.hasRobber()) return false;
        System.out.println("aö");
        if (null == state) return false; else switch (state) {
            case IN_PROGRESS:
                boolean success = oldTile.moveRobber(tile);
                if (!success) return false;
                System.out.println("an");
                this.isWaitingForMovingRobber = false;
                //TODO handle ressource steal
                sendMessage("MOVED_ROBBER", playerId + " to " + row + ", " + col, "", player.getName());
                break;
            default:
                return false;
        }
        return true;
    }


    public boolean endTurn(String playerId) {
        if (!currentPlayer.getUserId().equals(playerId)) return false;
        if (isWaitingForFreeRoadPlacement) return false;
        if (isWaitingForPlayerRessourceChange) return false;
        if (isWaitingForMovingRobber) return false;
        this.currentTradeOffer = null;
        currentPlayer = currentPlayer.getNextPlayer();
        startTurn();
        //sendMessage("END_TURN", "", currentPlayer.getUserId());
        return true;       
    }

    //TODO handle endturn -> phase before turn to play knigth -> start turn

    public void startTurn() {
        int[] d = throw2Dice();
        board.handleDice(d[0] + d[1]);
        if (d[0] + d[1] == 7) {
            this.isWaitingForMovingRobber = true;
            for (Player player: players.values()) {
                int numberCards = player.getTotalResBalance();
                if (numberCards > 7) {
                    player.addToResDebt(numberCards / 2);
                    this.isWaitingForPlayerRessourceChange = true;
                }
            }
        }
        sendMessage("START_TURN", "" + d[0] + "" + d[1], currentPlayer.getUserId(), currentPlayer.getName());            
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

    public TradeOffer getCurrentTradeOffer() {
        return currentTradeOffer;
    }

    

    public boolean isWaitingForMovingRobber() {
        return isWaitingForMovingRobber;
    }

    public String getId() { return id; }
    public Board getBoard() { return board; }
    public Map<String, Player> getPlayers() { return players; }
    public GameState getState() { return state; }
    public Instant getLastActive() { return lastActive; }
    public void touch() { this.lastActive = Instant.now(); }


    public String getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    
}
