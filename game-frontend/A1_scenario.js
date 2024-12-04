const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const { Key } = require('selenium-webdriver');

async function A1_scenario() {
    // Setup ChromeDriver (you can adjust the path to your chromedriver if needed)
    let options = new chrome.Options();
    let driver = await new Builder().forBrowser('chrome').setChromeOptions(options).build();

    try {
        // Open the game webpage
        await driver.get('http://localhost:8081'); // Adjust the port if necessary

        // Wait for the page to load and the 'Start Game' button to appear
        await driver.wait(until.elementLocated(By.id('start-game-btn')), 10000);

        // Click the Start Game button
        let startButton = await driver.findElement(By.id('start-game-btn'));
        await startButton.click();

        // Wait for the game to start and display the first message
        await driver.wait(until.elementTextContains(driver.findElement(By.id('display-screen')), 'Welcome to the Card Game'), 10000);

        await driver.sleep(2000);  // Wait a couple of seconds for the game state to change

        let displayScreen = await driver.findElement(By.id('display-screen'));
                let displayText = await displayScreen.getText();
                        if (displayText.includes("Player 1's turn")) {
                    console.log("Test Passed: 'Player 1's turn' is displayed on the screen.");
                } else {
                    console.log("Test Failed: 'Player 1's turn' not found on the screen.");
                }

         const expectedText = " 1. F5
         2. F5
         3. F20
         4. F20
         5. F20
         6. F25
         7. F30
         8. S10
         9. S10
         10. B15
         11. L20
         12. E30
         Event card drawn: Q4
         ---> A Quest is drawn <---
         Player 1, would you like to sponsor this quest? (yes/no)".trim();


                 // Compare the actual text with the expected text
                 assert.strictEqual(actualText, expectedText, 'The displayed text does not match the expected output.');

                 console.log('Test Passed: The full text was displayed correctly.');
             } catch (error) {
                 console.error('Test Failed:', error);
             } finally {
                 // Quit the browser
                 await driver.quit();
             }
         })();

        let inputField = await driver.findElement(By.id('input'));

        // ???
        await inputField.sendKeys('yes', Key.RETURN);

        await driver.wait(until.elementTextContains(driver.findElement(By.id('display-screen')), 'Resolving event'), 10000);

        await driver.sleep(1000);

    } catch (error) {
        console.error('Error during the game play:', error);
    } finally {
        // Close the browser after the game is played
        await driver.quit();
    }
}

let A1ScenarioInputs = [
                        "no\n" + "yes\n" +
                        "1\n" + "8\n" + "quit\n" +              // Stage 1
                        "4\n" + "7\n" + "quit\n" +              // Stage 2
                        "3\n" + "6\n" + "10\n" + "quit\n" +     // Stage 3
                        "5\n" + "11\n" + "quit\n" +             // Stage 4
                        "c\n" + "1\n" + "c\n" + "1\n" + "c\n" + "1\n" + // Players accept quest and trim to 12 cards
                        "1\n" + "1\n" + "quit\n" + "2\n" + "1\n" + "quit\n" + "1\n" + "3\n" + "quit\n" + // P1,3,4 set up attack
                        "c\n" + "c\n" + "c\n"  + // 3 Players continue to stage 2
                        "2\n" + "1\n" + "quit\n" + "6\n" + "1\n" + "quit\n" + "3\n" + "3\n" + "quit\n" + // P1,3,4 set up attack
                        "c\n" + "c\n" +  // P3,4 succeeded
                        "6\n" + "3\n" + "1\n" + "quit\n" + "4\n" + "2\n" + "3\n" + "quit\n" +  // P3,4 set up attack
                        "c\n" + "c\n" +
                        "3\n" + "2\n" + "2\n" + "quit\n" + "1\n" + "1\n" + "1\n" + "2\n" + "quit\n" +
                        "11\n" + "11\n" + "11\n" + "11\n";  // Sponsor trims to 12 cards
                        ]
playGame();
