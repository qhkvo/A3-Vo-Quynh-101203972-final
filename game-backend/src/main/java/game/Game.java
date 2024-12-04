package game;

import java.io.PrintWriter;
import java.util.*;

public class Game {
    private List<Player> players;
    private Deck adventureDeck;
    private Deck eventDeck;
     private int currentPlayerIndex;
    private int initialPlayerIndex;
    private List<Card> discardAPile;
    private List<Card> discardEPile;
    private int sponsorIndex;
    private List<Player> withdrawnParticipants;
    private int stageCount;
    private List<List<Card>> totalStagesCards;
    private List<Player> winners;

    // Constructor to initialize players and set up decks
    public Game() {
        players = new ArrayList<>();
        discardAPile = new ArrayList<>();
        discardEPile = new ArrayList<>();
        withdrawnParticipants = new ArrayList<>();
        stageCount = 0;
        totalStagesCards = new ArrayList<>();
        winners = new ArrayList<>();

        // Create 4 players
        for (int i = 0; i < 4; i++){
            players.add(new Player());
        }

        // Initialize the decks
        setUpDecks();
    }

    public void start(PrintWriter output, Scanner input) {
        setUpDecks();
        distributeCards();
        while (winners.isEmpty()) {
            playTurn(output, input);
            checkForWinnersOrProceed( output, input);
        }
    }

    public void startTest(PrintWriter output, Scanner input) {
        while (winners.isEmpty()) {
            playTurn(output, input);
            checkForWinnersOrProceed( output, input);
        }
    }

    // Set up adventure and event decks
    public void setUpDecks() {
        // Initialize the adventure deck with 100 cards
        adventureDeck = new Deck("Adventure", 100);
        adventureDeck.shuffle();
        // Initialize the event deck with 12 quest cards and 5 event cards
        eventDeck = new Deck("Event", 17);
        eventDeck.shuffle();
    }

    // Distribute 12 cards
    public void distributeCards () {
        for (Player player : players) {
            for (int i = 0; i < 12; i++) {
                player.addCardToHand(adventureDeck.drawCard());
            }
        }
    }

    // Display current player’s turn, hand and the drawn event card
    public void playTurn(PrintWriter printWriter, Scanner input) {
        Player currentPlayer = players.get(initialPlayerIndex);
        printWriter.println("Player " + (players.indexOf(currentPlayer) + 1) + "'s turn");

        currentPlayer.sortHand();
        currentPlayer.displayHand(printWriter);

        // Draw the next event card from the event deck
        Card eventCard = eventDeck.drawCard();
        EventCard eventCard1 = new EventCard(eventCard.getType(), eventCard.getValue());

        printWriter.println("Event card drawn: " + eventCard);
        resolveEvent(eventCard1, printWriter, input);

        printWriter.flush();
    }

    // Resolve the event or quest card drawn
    public void resolveEvent(EventCard card, PrintWriter printWriter, Scanner input) {
        if (card.isQuest()) {
            printWriter.println("---> A Quest is drawn <---");
            stageCount = card.getValue();
            promptForSponsorship(printWriter, input, stageCount);
        } else {
            printWriter.println("---> A " + card.getType() + " event is drawn <---");
            card.applyEvent(players.get(initialPlayerIndex), players, adventureDeck, printWriter);
            trimHandForAll(printWriter, input);
        }
        printWriter.flush();
        discardEPile.add(card);
    }

    public void trimHandForAll(PrintWriter printWriter, Scanner input) {
        for (Player player : getPlayers()) {
            if (player.getHandSize() > 12) {
                trimHandTo12(player, printWriter, input);
            }
        }
    }

    // Trim the player's hand to 12 cards
    public void trimHandTo12(Player player, PrintWriter printWriter, Scanner input) {
        while (player.getHandSize() > 12) {
            printWriter.println("!ATTENTION PLAYER " + (players.indexOf(player) + 1) + " : Your hand contains more than 12 cards. Please choose a card number (1 to " + player.getHandSize() + ") to discard:");
            player.sortHand();
            player.displayHand(printWriter);
            printWriter.flush();

            int cardToDiscard;
            try {
                String line = input.nextLine();
                if (line.trim().isEmpty()) {
                    printWriter.println("Invalid input. Please enter a number.");
                    continue;
                }
                cardToDiscard = Integer.parseInt(line); // Parse input
            } catch (NumberFormatException e) {
                printWriter.println("Invalid input. Please enter a number.");
                continue;
            }

            // Validate the input. If valid, remove the card from hand
            if (cardToDiscard >= 1 && cardToDiscard <= player.getHandSize()) {
                Card removedCard = player.getHand().remove(cardToDiscard - 1);
                printWriter.println("You have discarded: " + removedCard);
                discardChosenCards(Collections.singletonList(removedCard));
            } else {
                printWriter.println("Invalid input. Please choose a valid card number (1 to " + player.getHandSize() + "):");
                continue;
            }

            player.displayHand(printWriter);
            printWriter.println();
            printWriter.flush();
        }

        printWriter.println("Your hand has been trimmed to 12 cards.");
        printWriter.flush();
    }

    // A player chooses to sponsor the quest or not, starting from current player
    public boolean promptForSponsorship(PrintWriter printWriter, Scanner input, int stageCount) {
        // Starting with the current player
        int playerToAnswerIndex = initialPlayerIndex;

        // Loop through all players starting with the current player
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(playerToAnswerIndex);

            // Prompt the player for sponsorship decision
            printWriter.println("Player " + (playerToAnswerIndex + 1) + ", would you like to sponsor this quest? (yes/no)");
            printWriter.flush();

            String response;
            try {
                response = input.nextLine().trim().toLowerCase();
            } catch (NoSuchElementException e) {
                // Exit gracefully if there's no more input available
                printWriter.println("No more input available. Exiting quest setup.");
                printWriter.flush();
                return false;
            }

            // If player agrees to sponsor, check if they have enough cards
            if (response.equals("yes")) {
                printWriter.println("Player " + (playerToAnswerIndex + 1) + " has decided to sponsor the quest.");
                printWriter.flush();

                // Use Player's method to check if the player can sponsor the quest
                if (player.canSponsorQuest(stageCount)) {
                    List<List <Card>> stages = new ArrayList<>();
                    printWriter.println("Player " + (playerToAnswerIndex + 1) + " has enough cards to sponsor the quest.");
                    printWriter.println("---> A SPONSOR FOUND: " + "Player " + (playerToAnswerIndex + 1));
                    sponsorIndex = playerToAnswerIndex;  // Set the sponsor index
                    printWriter.println();
                    player.setUpQuest(printWriter, input, stages, stageCount);

                    totalStagesCards = stages;
                    startQuest(printWriter, input, totalStagesCards);
                    return true;
                } else {
                    printWriter.println("Player " + (playerToAnswerIndex + 1) + " does not have enough cards to sponsor the quest.");
                    printWriter.println();
                }
                printWriter.flush();
            } else {
                printWriter.println("Player " + (playerToAnswerIndex + 1) + " has decided to decline the quest.");
                printWriter.flush();
            }

            // Move to the next player in turn
            playerToAnswerIndex = (playerToAnswerIndex + 1) % players.size();
        }

        // If no players agree to sponsor, return false
        handleNoSponsorship(printWriter, input);
        return false;
    }

    public void handleNoSponsorship(PrintWriter printWriter, Scanner input) {
        printWriter.println("No one agreed to sponsor the quest. The quest has failed.");
        endCurrentPlayerTurn(printWriter, input);
        printWriter.flush();
    }

    // End the current player's turn and move to the next player
    public void endCurrentPlayerTurn(PrintWriter printWriter, Scanner input) {
        printWriter.println("Player " + (initialPlayerIndex + 1) + "'s turn is over.");
        printWriter.println("Press <return> to end your turn.");
        printWriter.flush();

        // Wait for the player to press <return>
        input.nextLine();
        initialPlayerIndex = (initialPlayerIndex + 1) % players.size();
    }

    // Handles when a player continues the quest
    public void handlePlayerContinuesQuest(Player player, PrintWriter printWriter, Scanner input, Deck adventureDeck) {
        // Step 1: Draw an adventure card
        Card drawnCard = adventureDeck.drawCard();
        player.addCardToHand(drawnCard);
        printWriter.println("Player " + (players.indexOf(player) + 1) + " drew: " + drawnCard);
        player.sortHand();

        // Step 2: Trim hand if the player has more than 12 cards
        if (player.getHandSize() > 12) {
            printWriter.println("Player " + (players.indexOf(player) + 1) + " has more than 12 cards and needs to trim their hand.");
            trimHandForAll(printWriter, input);
        }

        printWriter.println("Player " + (players.indexOf(player) + 1) + " is setting up their attack.");
    }

    public void promptParticipantsForQuestStage(PrintWriter printWriter, Scanner input, List<Player> eligibleParticipants, int stageNumber) {
        List<Player> playersToRemove = new ArrayList<>();
        printWriter.flush();
        // Sort eligible participants so Player 1 goes first, then Player 2, etc.
        eligibleParticipants.sort(Comparator.comparingInt(player -> players.indexOf(player)));
        for (Player player : eligibleParticipants) {
            printWriter.println("Player " + (players.indexOf(player) + 1) + ", would you like to continue or withdraw from the quest? (c/w)");
            printWriter.flush();

            String decision = input.nextLine().trim().toLowerCase();

            // Keep asking until a valid response is given
            while (!decision.equals("c") && !decision.equals("w")) {
                printWriter.println("Invalid input. Please choose 'c' to continue or 'w' to withdraw.");
                printWriter.flush();

                decision = input.nextLine().trim().toLowerCase();
            }

            // Process the decision
            if (decision.equals("w")) {
                // If player decides to withdraw, add them to the withdrawn list
                printWriter.println("Player " + (players.indexOf(player) + 1) + " has withdrawn from the quest.");
                withdrawnParticipants.add(player);
                playersToRemove.add(player);
                printWriter.println();
            } else {
                printWriter.println("Player " + (players.indexOf(player) + 1) + " is tackling the current stage.");
                handlePlayerContinuesQuest(player, printWriter, input, adventureDeck);
            }

        }
        eligibleParticipants.removeAll(playersToRemove);
        printWriter.flush();
    }

    // Method to resolve the attack for the current stage
    public void resolveStage(PrintWriter printWriter, List<Player> eligibleParticipants, List<Card> stageCards, int stageNumber, int totalStages, Scanner input) {
        List<Player> successfulParticipants = new ArrayList<>();
        int stageValue = calculateStageValue(stageCards);

        printWriter.println("Resolving attacks for Stage " + (stageNumber) + " (Stage value: " + stageValue + "):");
        printWriter.flush();

        for (Player player : eligibleParticipants) {
            player.clearAttackCards();
            buildAttackForParticipant(printWriter, input, player);
            int attackValue = player.getAttackValue();
            discardUsedAttackCards(player, printWriter);

            if (attackValue < stageValue) {
                printWriter.println("Player " + (players.indexOf(player) + 1) + " is eliminated with an attack value of " + attackValue);
                printWriter.println("Player " + (players.indexOf(player) + 1) + " earns " + 0 + " shields.");
                withdrawnParticipants.add(player);
                player.displayUpdatedHand(printWriter);
            } else {
                printWriter.println("Player " + (players.indexOf(player) + 1) + " succeeds with an attack value of " + attackValue);
                player.displayUpdatedHand(printWriter);
                if (stageNumber == totalStages) {
                    // If this is the last stage, the player wins the quest
                    player.addShields(totalStages);
                    withdrawnParticipants.clear();
                    printWriter.println("Player " + (players.indexOf(player) + 1) + " wins the quest.");
                    printWriter.println("Player " + (players.indexOf(player) + 1) + " earns " + stageCount + " shields.");
                } else {
                    // If not the last stage, player proceeds to the next stage
                    successfulParticipants.add(player);
                }
            }
        }

        if (withdrawnParticipants.size() == 4) {
            printWriter.println("All participants have been eliminated.");
            eligibleParticipants.clear();
        } else {
            eligibleParticipants.clear();
            eligibleParticipants.addAll(successfulParticipants);  // Update eligible participants for the next stage
            printWriter.println("Participants remaining for the next stage:");
            for (Player player : eligibleParticipants) {
                printWriter.println("Player " + (players.indexOf(player) + 1));
            }
        }

        printWriter.flush();
    }

    // Helper method to discard used attack cards after each stage
    private void discardUsedAttackCards(Player player, PrintWriter printWriter) {
        List<Card> usedCards = player.getSelectedAttackCards();
        discardChosenCards(usedCards);
        player.getHand().removeAll(usedCards);

        printWriter.println("Player " + (players.indexOf(player) + 1) + " discarded the following attack cards:");
        for (Card card : usedCards) {
            printWriter.println("- " + card);
        }
        // player.clearAttackCards();
        printWriter.flush();
    }

    public void startQuest(PrintWriter printWriter, Scanner input, List<List<Card>> sponsorBuiltStages) {
        int totalStages = sponsorBuiltStages.size();

        // Get the participants for the first stage
        List<Player> eligibleParticipants = new ArrayList<>(players);
        Player sponsor = players.get(sponsorIndex);
        eligibleParticipants.remove(sponsor);
        withdrawnParticipants.add(sponsor);

        if (eligibleParticipants.isEmpty()) {
            printWriter.println("No players are eligible to participate in this quest.");
            return;
        }

        displayEligibleParticipants(printWriter, eligibleParticipants);

        // Loop through stages
        for (int stageNumber = 1; stageNumber <= totalStages; stageNumber++) {
            List<Card> stageCards = sponsorBuiltStages.get(stageNumber - 1); // Get the cards for the current stage

            // Prompt players to continue or withdraw after the stage
            promptParticipantsForQuestStage(printWriter, input, eligibleParticipants, stageNumber);

            if (eligibleParticipants.isEmpty()) {
                printWriter.println("All participants have withdrawn. The quest ends!");
                endQuest(printWriter, sponsorBuiltStages, adventureDeck, input);
                break;
            }
            resolveStage(printWriter, eligibleParticipants, stageCards, stageNumber, totalStages, input);

            // End the quest if it's the last stage
            if (stageNumber == totalStages) {
                endQuest(printWriter, sponsorBuiltStages, adventureDeck, input);
            }

        }
    }

    // Calculate the total value of a stage based on its cards
    private int calculateStageValue(List<Card> stageCards) {
        return stageCards.stream().mapToInt(Card::getValue).sum(); // Sum up the card values
    }

    public void displayEligibleParticipants(PrintWriter printWriter, List<Player> eligibleParticipants) {
        if (eligibleParticipants.isEmpty()) {
            printWriter.println("No players are eligible to participate in this quest.");
        } else {
            printWriter.println("Eligible participants for the quest:");
            for (Player player : eligibleParticipants) {
                int playerNumber = players.indexOf(player) + 1;
                printWriter.println("Player " + playerNumber);
            }
            printWriter.println();
        }
        printWriter.flush();
    }

    public void endQuest(PrintWriter printWriter, List<List<Card>> stages, Deck adventureDeck, Scanner input) {
        printWriter.println("---> THE QUEST HAS ENDED <---");
        printWriter.flush();
        Player sponsor = players.get(sponsorIndex);

        // Sponsor discards all cards used to build the quest
        for (List<Card> stage : stages) {
            discardChosenCards(stage);
        }
        printWriter.println("Sponsor discarded: " + stages);

        // Calculate the total number of cards discarded by the sponsor
        int totalDiscardedCards = stages.stream().mapToInt(List::size).sum();
        int cardsToDraw = totalDiscardedCards + stages.size();  // Cards to draw = discarded cards + number of stages

        printWriter.println("Sponsor will draw " + cardsToDraw + " new cards.");
        printWriter.flush();

        // Sponsor draws the cards
        for (int i = 0; i < cardsToDraw; i++) {
            Card drawnCard = adventureDeck.drawCard();
            if (drawnCard != null) {
                sponsor.addCardToHand(drawnCard);
                printWriter.println("Sponsor drew: " + drawnCard);
            } else {
                // If the deck is empty, reshuffle the discard pile and put it back into the adventure deck
                printWriter.println("No more cards left in the adventure deck to draw. Reshuffling the discard pile.");
                adventureDeck.reshuffle(discardAPile);
                i--;
            }
        }
        sponsor.sortHand();
        if (sponsor.getHandSize() > 12) {
            printWriter.println("Sponsor has more than 12 cards and needs to trim their hand.");
            trimHandForAll(printWriter, input);
        }
        withdrawnParticipants.clear();
        printWriter.flush();
    }

    // Participate build attack
    public void buildAttackForParticipant(PrintWriter printWriter, Scanner input, Player player) {
        List<Card> weaponCards = filterWeaponCards(player.getHand());
        if (weaponCards.isEmpty()) {
            printWriter.println("You have no weapon cards available. Attack value will be 0.");
            printWriter.flush();
            return;
        }
        printWriter.println("Player " + (players.indexOf(player) + 1) + "'s turn:");
        displayWeaponCards(printWriter, weaponCards);

        int attackValue = 0;
        List<Card> chosenCards = new ArrayList<>();
        while (!weaponCards.isEmpty()) {
            Card chosenCard = handleCardSelection(printWriter, input, weaponCards, chosenCards);
            if (chosenCard == null) {
                break;  // Player chose to quit
            }

            // Add selected card to the player's attack
            player.addToAttack(chosenCard);  // Add to the player's selected attack cards
            chosenCards.add(chosenCard);
            attackValue += chosenCard.getValue();
            weaponCards.remove(chosenCard);

            printWriter.println("You have added " + chosenCard + " to your attack. Current attack value: " + attackValue);
            printWriter.println("Your current attack includes the following cards:");
            for (Card card : chosenCards) {
                printWriter.println("- " + card);
            }
            printWriter.flush();

            if (!weaponCards.isEmpty()) {
                printWriter.println("Remaining weapon cards:");
                displayWeaponCards(printWriter, weaponCards);
            } else {
                printWriter.println("No more weapon cards left to choose.");
                printWriter.flush();
            }
        }

        // Display the final attack list
        printWriter.println("Your final attack includes the following cards:");
        for (Card card : chosenCards) {
            printWriter.println("- " + card);
        }
        printWriter.println("Your final attack value is: " + attackValue);
        printWriter.flush();
    }

    // Filter weapon cards from the player's hand
    private List<Card> filterWeaponCards(List<Card> hand) {
        List<Card> weaponCards = new ArrayList<>();
        for (Card card : hand) {
            if (isWeaponCard(card)) {
                weaponCards.add(card);
            }
        }
        return weaponCards;
    }

    // Check if a card is a weapon card
    private boolean isWeaponCard(Card card) {
        return card.getType().startsWith("D") || card.getType().startsWith("H") ||
                card.getType().startsWith("S") || card.getType().startsWith("B") ||
                card.getType().startsWith("L") || card.getType().startsWith("E");
    }

    // Display available weapon cards
    private void displayWeaponCards(PrintWriter printWriter, List<Card> weaponCards) {
        printWriter.println("Weapon cards in your hand:");
        for (int i = 0; i < weaponCards.size(); i++) {
            printWriter.println((i + 1) + ". " + weaponCards.get(i));
        }
        printWriter.flush();
    }

    // Handle card selection from the player with validation for non-repeated and valid cards
    private Card handleCardSelection(PrintWriter printWriter, Scanner input, List<Card> weaponCards, List<Card> chosenCards) {
        while (true) {  // Loop until a valid card is selected or the player quits
            printWriter.println("Enter the position of the weapon card you want to include in your attack, or type 'Quit':");
            printWriter.flush();

            String inputLine = input.nextLine().trim().toLowerCase();

            if (inputLine.equals("quit")) {
                return null;  // Player chose to quit
            }

            try {
                int cardPosition = Integer.parseInt(inputLine);

                // Validate card position
                if (cardPosition < 1 || cardPosition > weaponCards.size()) {
                    printWriter.println("Invalid position. Please choose a valid card number.");
                    printWriter.flush();
                } else {
                    Card selectedCard = weaponCards.get(cardPosition - 1);
                    boolean alreadyChosen = false;
                    for (Card chosenCard : chosenCards) {
                        if (chosenCard.getType().equals(selectedCard.getType())) {
                            alreadyChosen = true;
                            break;
                        }
                    }

                    if (alreadyChosen) {
                        printWriter.println("You have already selected this card. Please choose a different card.");
                        printWriter.flush();
                    } else {
                        return selectedCard;  // Return the valid, non-repeated card
                    }
                }
            } catch (NumberFormatException e) {
                printWriter.println("Invalid input. Please enter a number or type 'Quit'.");
                printWriter.flush();
            }
        }
    }

    // Discard the selected cards after building the attack
    private void discardChosenCards(List<Card> chosenCards) {
        discardAPile.addAll(chosenCards);
    }

    public void checkForWinnersOrProceed(PrintWriter printWriter, Scanner input) {
        for (Player player : players) {
            if (player.getTotalShield() >= 7) {
                winners.add(player);
            }
        }

        if (!winners.isEmpty()) {
            printWriter.println("---> We have one or more winners! <---");
            for (Player winner : winners) {
                printWriter.println("Player " + (players.indexOf(winner) + 1) + " has won the game with " + winner.getTotalShield() + " shields!");
            }

            printWriter.println();
            printWriter.println("---> All players' total shields after Quest game: ");
            for (Player player : players) {
                printWriter.println("Player " + (players.indexOf(player) + 1) + ": " + player.getTotalShield() + " shields");
            }
            printWriter.flush();
        } else {
            printWriter.println("No players have won yet. Moving to the next player's turn.");
            endCurrentPlayerTurn(printWriter, input);
            printWriter.flush();
        }
    }

    //Getters
    public int getAdventureDeckSize() { return adventureDeck.size(); }
    public int getEventDeckSize() { return eventDeck.size(); }
    public Deck getAdventureDeck() { return adventureDeck; }
    public Deck getEventDeck() { return eventDeck; }
    public List<Card> getDiscardAPile() { return discardAPile; }
    public int getTotalStagesCount() { return stageCount;}
    public List<List<Card>> getTotalStagesCards() { return totalStagesCards;}

    // Getter for players
    public List<Player> getPlayers() { return players; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }

    // Setters
    public void setCurrentPlayerIndex(int index) {
        // Ensure the index is within the valid range of players
        if (index >= 0 && index < players.size()) {
            this.currentPlayerIndex = index;
        } else {
            throw new IllegalArgumentException("Invalid player index: " + index);
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        Scanner input = new Scanner(System.in);
        PrintWriter output = new PrintWriter(System.out, true);

        game.start(output, input);
    }
}