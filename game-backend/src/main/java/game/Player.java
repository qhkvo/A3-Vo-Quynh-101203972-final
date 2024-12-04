package game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.*;

public class Player {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private List<Card> hand;
    private int totalShields;
    private int previousStageValue;
    private List<Card> selectedAttackCards;
    private List<String> messages;
    private Set<Integer> globalSelectedCardPositions = new HashSet<>(); // Tracks selected card positions across all stages
    private Set<Integer> selectedCardPositions = new HashSet<>(); // Tracks selected card positions for the current stage
    private int currentStage = 0;
    private boolean questSetupComplete = false;

    public Player() {
        // Initialize an empty hand
        hand = new ArrayList<>();
        this.totalShields = 0;
        this.previousStageValue = 0;
        this.selectedAttackCards = new ArrayList<>();
        messages = new ArrayList<>();
    }

    public void addCardToHand(Card card) {
        hand.add(card);
    }

    // Display only the newly drawn cards
    public String displayNewlyDrawnCards(int initialHandSize) {
        messages.add("Newly drawn cards:");
        for (int i = initialHandSize; i < hand.size(); i++) {
            messages.add((i + 1) + ". " + hand.get(i).toString());
        }
        return String.join("\n", messages);
    }

    // Display the player's hand
    public List<String> displayHand() {
        List<String> handDetails = new ArrayList<>();
        int cardNumber = 1;
        for (Card card : hand) {
            handDetails.add(cardNumber + ". " + card.toString());
            cardNumber++;
        }
        return handDetails;
    }


    public String displayUpdatedHand() {
        List<String> cards = new ArrayList<>();
        for (Card card : hand) {
            cards.add(card.toString());
        }
        messages.add("Your current hand: " + cards);
        return String.join("\n", messages);
    }

    // Sort cards in the player's hand (F -> W (S before H))
    public void sortHand() {
        hand.sort((c1, c2) -> {
            if (c1.getType().startsWith("F") && !c2.getType().startsWith("F")) return -1;
            if (!c1.getType().startsWith("F") && c2.getType().startsWith("F")) return 1;
            if (c1.getType().startsWith("F") && c2.getType().startsWith("F")) return Integer.compare(c1.getValue(), c2.getValue());
            if (c1.getType().startsWith("S") && c2.getType().startsWith("H")) return -1;
            if (c1.getType().startsWith("H") && c2.getType().startsWith("S")) return 1;
            return Integer.compare(c1.getValue(), c2.getValue());
        });
    }

    public void loseShields(int shieldsLost) {
        // Ensure the player doesn't lose more shields than they have
        totalShields = Math.max(0, totalShields - shieldsLost);
    }

    public void addShields(int shieldsGained) {
        totalShields += shieldsGained;
    }

    public String setUpQuest(String input, List<List<Card>> stages, int stageCount, int gameStage) {
        messages.clear();
        List<Card> discardedCards = new ArrayList<>();

        if (currentStage == 0) {  // Initialize
            currentStage = 1;
            previousStageValue = 0;
            globalSelectedCardPositions.clear();
            questSetupComplete = false;
        }

//        if (currentStage > stageCount) {
//            hand.removeAll(discardedCards);
//            messages.add("Quest setup complete.");
//            questSetupComplete = true;
//            previousStageValue = 0;
//            return String.join("\n", messages);
//        }

        while (stages.size() < currentStage) stages.add(new ArrayList<>());
        List<Card> stageCards = stages.get(currentStage - 1);

        if (input == null) {
            messages.add("---> SETTING UP STAGE " + currentStage + " OF " + stageCount + " <---");
            sortHand();
            messages.addAll(displayHand());
            messages.add("Enter the position of a card to include in this stage, or type 'Quit' to finish the stage:");
            return String.join("\n", messages);
        }

        if (input.equalsIgnoreCase("Quit")) {
            if (stageCards.isEmpty()) {
                messages.add("A stage cannot be empty. Please select at least one card.");
            } else if (!validateStage(stageCards)) {
                messages.add("Invalid stage: Each stage must contain exactly 1 Foe card and zero or more unique Weapon cards.");
            } else {
                int currentStageValue = calculateStageValue(stageCards);
                if (currentStageValue <= previousStageValue) {
                    messages.add("Insufficient value for this stage. Stage value must be higher than the previous stage.");
                } else {
                    previousStageValue = currentStageValue;
                    messages.add("Stage " + (currentStage) + " successfully set with the following cards:");
                    for (Card card : stageCards) {
                        messages.add(card.toString());
                    }
                    discardedCards.addAll(stageCards);
                    selectedCardPositions.clear();
                    currentStage++;

                    if (currentStage > stageCount) {
                        hand.removeAll(discardedCards);
                        messages.add("Quest setup complete.");
                        questSetupComplete = true;
                        previousStageValue = 0;
                        return String.join("\n", messages);
                        //return setUpQuest(null, stages, stageCount, gameStage); // Pass null to trigger final check
                    } else {
                        messages.add("---> SETTING UP STAGE " + currentStage + " OF " + stageCount + " <---");
                        sortHand();
                        messages.addAll(displayHand());
                        messages.add("Enter the position of a card to include in this stage, or type 'Quit' to finish the stage:");
                    }
                }
            }
        } else {
            try {
                int cardPosition = Integer.parseInt(input) - 1;

                if (cardPosition < 0 || cardPosition >= hand.size()) {
                    messages.add("Invalid input. Please enter a valid card position.");
                } else if ( selectedCardPositions.contains(cardPosition) || globalSelectedCardPositions.contains(cardPosition)) {
                    messages.add("This card has already been selected for this stage or another stage. Please choose a different card.");
                } else {
                    Card selectedCard = hand.get(cardPosition);
                    if (isInvalidCardForStage(selectedCard, stageCards)) {
                        messages.add("Invalid card: You can only have one Foe and no repeated Weapon cards in a stage.");
                    } else {
                        stageCards.add(selectedCard);
                        selectedCardPositions.add(cardPosition);  // Track the selected card position for this stage
                        globalSelectedCardPositions.add(cardPosition);  // Track the selected card position across all stages
                        messages.add("Added " + selectedCard + " to stage " + currentStage);
                    }
                }
            } catch (NumberFormatException e) {
                messages.add("Invalid input. Please enter a valid card position.");
            }

        }
        previousStageValue = 0;
        return String.join("\n", messages);
    }



    // Calculate the total value of a stage (Foe + Weapon values)
    private int calculateStageValue(List<Card> stageCards) {
        return stageCards.stream().mapToInt(Card::getValue).sum();
    }

    // Validate if a selected card is valid for the current stage
    private boolean isInvalidCardForStage(Card selectedCard, List<Card> stageCards) {
        if (selectedCard.getType().startsWith("F")) {
            // Ensure only one Foe per stage
            for (Card card : stageCards) {
                if (card.getType().startsWith("F")) {
                    return true;  // Invalid, because a Foe card already exists
                }
            }
        } else if (selectedCard.getType().matches("[DSHBEL].*")) {
            // Check if there's a repeated Weapon card in this stage
            for (Card card : stageCards) {
                if (card.getType().equals(selectedCard.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Validate that the stage contains exactly 1 Foe card and unique Weapon cards
    private boolean validateStage(List<Card> stageCards) {
        int foeCount = 0;
        Set<String> weaponTypes = new HashSet<>();

        for (Card card : stageCards) {
            if (card.getType().startsWith("F")) {
                foeCount++;
            } else if (card.getType().matches("[DSHBEL].*")) {
                if (!weaponTypes.add(card.getType())) {
                    return false;  // Duplicate Weapon card found
                }
            }
        }

        // Ensure there is exactly 1 Foe card and zero or more unique Weapon cards
        return foeCount == 1 && weaponTypes.size() == (stageCards.size() - 1);
    }

    public boolean canSponsorQuest(int stageCount) {
        long foeCardCount = hand.stream()
                .filter(card -> card.getType().startsWith("F"))
                .count();

        // Check if there are enough Foe cards for the number of stages
        return foeCardCount >= stageCount;
    }

    // Add a card to the selected attack cards
    public void addToAttack(Card card) {
        selectedAttackCards.add(card);
    }
    public void removeHand(){
        hand.clear();
    }

    // Clear the selected attack cards after each stage
    public void clearAttackCards() {
        selectedAttackCards.clear();
    }



    // Getters
    public int getHandSize() {
        return hand.size();
    }
    public List<Card> getHand() {
        return hand;
    }
    public int getTotalShield() {
        return totalShields;
    }
    public boolean hasFinishedQuestSetup() {
        return questSetupComplete;
    }
    public int getCurrentStage() {
        return currentStage;
    }

    // Calculate the player's attack value based on the selected attack cards
    public int getAttackValue() {
        return selectedAttackCards.stream().mapToInt(Card::getValue).sum();
    }

    public List<Card> getSelectedAttackCards() {
        return selectedAttackCards;
    }

}
