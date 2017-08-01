FXCM Demo application

Summary
================================================================
This demo replicates the basic functions of FXCM desktop application in eclipse console.
The demo performs the following actions:
1. Prompts user for login details. 
2. Displays current dealing rates and open positions for EUR/USD, USD/JPY and GBP/USD.
3. Allows the user to open and close market orders.
4. Logs out when the user quits the application.

Assumptions
================================================================
1. The application only deals with EUR/USD, USD/JPY and GBP/USD, as required by the assignment specifications.
2. It is assumed that the user has only one account associated with their login. The functionality that grabs all the accounts of the user is present in the application but the user is not presented with any option to choose different accounts.

Running the application
================================================================

1.Create an eclipse project using the instructions given in the link http://fxcodebase.com/wiki/index.php/How_to_Start_Using_ForexConnect_in_Java
2. Right-click on the src folder and select Import
3. In the Import dialog, expand General and select Archive File. Click Next.
4. Choose the given jar file and click Finish
5. Run the application by choosing Run as Java Application.

Running the Unit Tests
================================================================
1. To run the unit tests, add JUnit library to the project by configuring the build path.
2. Configure the JUnit tests run configurations with the same settings as required for the eclipse project above
3. Run the unit tests by choosing Run as JUnit Tests.

Further Improvements
=================================================================
1. O2GOfferRow returned in the response from server sometimes has 0.0 for High and Low values. Need more insight into how the O2GOfferRow response varibales works. 
2. Could not find a definite formula for Spread and hence was left out while displaying dealing rates.
