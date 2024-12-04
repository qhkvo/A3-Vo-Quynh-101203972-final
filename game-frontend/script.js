const apiBaseUrl = "http://localhost:8080/api/game";
let playersToTrimList = []
let playerIndex = 0

async function apiRequest(endpoint, options = {}) {
    try {
        const response = await fetch(`${apiBaseUrl}${endpoint}`, options);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        return await response.text();
    } catch (error) {
        console.error(`Error in ${endpoint}:`, error);
        throw error;
    }
}

function updateDisplayScreen(message) {
    const displayScreen = document.getElementById("display-screen");
    // Split the message into lines and append each line separately
    const lines = message.split('\n').filter(line => line.trim() !== '');
    lines.forEach(line => {
        displayScreen.innerHTML += `<div>${line}</div>`;
    });
    displayScreen.scrollTop = displayScreen.scrollHeight; // Auto-scroll to the latest message
}

async function startGame() {
    try {
        const result = await apiRequest('/start');
//        console.log("Start Game Response:", result);
//        updateDisplayScreen(result);

        await resolveEvent();
    } catch (error) {
        console.error("Error in startGame:", error);
        updateDisplayScreen("Error starting the game. Please try again.");
    }
}

async function checkForWinnersOrProceed() {
    try {
        const result = await apiRequest('/checkForWinnersOrProceed');
        console.log("Check for Winners Response:", result);
        updateDisplayScreen(result);

        // If no winners, proceed to the next turn automatically
        if (result.includes("Moving to the next player's turn")) {
            await playTurn();
        }
    } catch (error) {
        console.error("Error in checkForWinnersOrProceed:", error);
        updateDisplayScreen("Error checking for winners. Please try again.");
    }
}

async function resolveEvent() {
    try {
        const result = await apiRequest('/resolveEvent');
        console.log("Resolve Event Response:", result);
        updateDisplayScreen(result);

        // await checkForWinnersOrProceed();
    } catch (error) {
        console.error("Error in resolveEvent:", error);
        updateDisplayScreen("Error resolving event. Please try again.");
    }
}

async function resolveEventInput() {
    const inputValue = document.getElementById("input").value.trim();
    if (!inputValue) {
        updateDisplayScreen("Please enter a valid input.");
        return;
    }

    try {
        const result = await apiRequest('/resolveEventInput', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ input: inputValue }),
        });

        console.log("Resolve Event Input Response:", result);
        updateDisplayScreen(result);
    } catch (error) {
        console.error("Error in resolveEventInput:", error);
        updateDisplayScreen("Error processing input. Please try again.");
    } finally {
        document.getElementById("input").value = '';
        document.getElementById("input").focus();
    }
}

document.getElementById("input").addEventListener("keypress", function (event) {
    if (event.key === "Enter") {
        event.preventDefault();
        resolveEventInput();
    }
});
document.getElementById("start-game-btn").addEventListener("click", startGame);
