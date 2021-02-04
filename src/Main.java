/* Written by Frankie Flores for CS2336.003
   NetID: fxf180009
*/

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner kb = new Scanner(System.in);                      //Scanner to read the user input
        String[] successfulLogin;                                 //Tells us if the user was able to successfully login
        boolean exitProgram = false;                              //Case used by the admin that tells the program to exit
        Hashmap customerList = new Hashmap("userdb.dat");         //Customer Hashmap
        Auditorium[] movieRooms = new Auditorium[3];              //Array to hold the 3 rooms
        movieRooms[0] = new Auditorium("A1.txt");    //First room
        movieRooms[1] = new Auditorium("A2.txt");    //Second room
        movieRooms[2] = new Auditorium("A3.txt");    //Third room

        do {
            do { //Loop until there is a successful login
                successfulLogin = loginScreen(kb, customerList).clone();
            } while(!successfulLogin[0].equals("true"));

            if(successfulLogin[1].equals("true")) { //Admin menu
                //Loop until the admin either selects logout or exit program
                while((movieRooms[0].continueUser && movieRooms[1].continueUser && movieRooms[0].continueUser) && !exitProgram) {
                    exitProgram = printAdminMenu(kb, movieRooms);
                }

                //Reset logout flags
                movieRooms[0].continueUser = true;
                movieRooms[1].continueUser = true;
                movieRooms[2].continueUser = true;
            }
            else { //Customer menu
                //Loop until the user selects logout
                while(movieRooms[0].continueUser && movieRooms[1].continueUser && movieRooms[0].continueUser) {
                    movieRooms = printOrderMenu(kb, successfulLogin[2], movieRooms, customerList).clone();
                }

                //Reset logout flags
                movieRooms[0].continueUser = true;
                movieRooms[1].continueUser = true;
                movieRooms[2].continueUser = true;
            }
        } while(!exitProgram);

        kb.close(); //Done using the io scanner, close it
    }

    public static String[] loginScreen(Scanner userScanner, Hashmap userList) {
        String username;           //User input for the customer username
        String password;           //User input for the customer password
        int counter = 1;           //Counter of how many times the password has been asked for
        boolean userExists = true; //Store whether or not the user was found
        String[] loginCase = new String[3];
            //The first index is if the login was successful or not
            //The second index is if the user is the admin

        System.out.println("Enter your username: "); //Prompt user for username
        username = userScanner.nextLine();

        if(userList.findACustomer(username) > -1) { //If the username exists in the list
            System.out.println("Please enter your password, " + username + ": "); //Get password
            password = "";
            password = userScanner.nextLine();

            //Loop until either correct password is given or the password was input incorrectly 3 times
            while(!userList.validatePassword(userList.findACustomer(username), password) && counter < 3) {
                counter++;
                System.out.println("Invalid password");
                System.out.println("Please enter your password, " + username + ": ");
                password = userScanner.nextLine();
            }

            if(counter == 3) {
                //If the user could not login, give an error and return to the login screen
                userExists = false;
                loginCase[0] = "false";
                System.out.println("User " + username + " could not login properly");
            }
        }
        else { //If the username does not exist
            //Give an error and return to the login screen
            userExists = false;
            loginCase[0] = "false";
            System.out.println("Username " + username + " not found");
        }

        if(userExists && username.compareTo("admin") == 0) { //User logged in as admin
            loginCase[0] = "true";
            loginCase[1] = "true";
        }
        else if(userExists) {                                //User logged in as a customer
            loginCase[0] = "true";
            loginCase[1] = "false";
            loginCase[2] = username; //Store the customer's username
        }

        return loginCase;
    }

    public static Auditorium[] printOrderMenu(Scanner userScanner, String userKey, Auditorium[] theaters, Hashmap customer_orders) throws IOException {
        Auditorium[] rooms = theaters.clone();                  //Store the 3 movie rooms into an array that will be updated
        String inputTemp;                                       //User input variable, will be used to process information
        String secondChoiceAnswer;                              //Used if the user is offered a best available
        int userSelection = -1;                                 //User's selection at the main menu
        boolean validMenuSelection;                             //Flag for if the user gave a valid menu choice
        int[] userInput;                                        //Stores the user input necessary to reserve a seat
        int roomNumber = 0;                                     //The movie room the user wants to reserve a seat in
        int totalTickets;                                       //The number of tickets the user wants to reserve
        int userIndex = customer_orders.findACustomer(userKey); //Index of the customer making the order

        do {
            validMenuSelection = true;

            //Prompt the user with the main menu
            System.out.println("Select one of the following:");
            System.out.println("1. Reserve Seats");
            System.out.println("2. View Orders");
            System.out.println("3. Update Order");
            System.out.println("4. Display Receipt");
            System.out.println("5. Log Out");
            inputTemp = userScanner.nextLine();

            try { //Get a valid menu selection
                userSelection = Integer.parseInt(inputTemp);
                if(userSelection < 1 || userSelection > 5 || inputTemp.isEmpty())
                    throw new Exception();
            } catch (Exception e) {
                //Invalid menu selection, output an error
                validMenuSelection = false;
                System.out.println("ERROR. Please select a number between 1 and 5.");
            }
        } while(!validMenuSelection);

        //Now that a valid selection has been made, process the user's requests
        switch(userSelection) {
            case 1: //User wants to reserve seats
                roomNumber = getRoomNumber(userScanner); //First ask the user for the room number

                //Then ask the user to give the info to reserve seats
                rooms[roomNumber].printAuditorium(rooms[roomNumber].getHead(), rooms[roomNumber].getColumns(), rooms[roomNumber].getRows()); //Show them the room
                userInput = orderValidation(userScanner, rooms[roomNumber].getRows(), rooms[roomNumber].getColumns()).clone();               //Validate their input
                totalTickets = userInput[2] + userInput[3] + userInput[4];                                                                   //Store their input

                if (rooms[roomNumber].checkAvailability(totalTickets, userInput[0], (char) userInput[1])) {
                    rooms[roomNumber].updateRoom(userInput); //Update linked list if available

                    //Then update the hashmap with the new orders
                    Orders addedOrder = new Orders(roomNumber, userInput[0], userInput[2], userInput[3], userInput[4], (char) userInput[1], rooms[roomNumber].getRows() ,rooms[roomNumber].getColumns());
                    customer_orders.customerList = customer_orders.addAnOrder(
                            customer_orders.customerList, customer_orders.customerList[customer_orders.findACustomer(userKey)], addedOrder).clone();
                }
                else {                                       //Otherwise, find the best available seats (if there are any)
                    int[] secondChoice = rooms[roomNumber].bestAvailable(totalTickets).clone();
                    if (secondChoice[0] == -1) {             //There are no seats available
                        System.out.println("no seats available");
                    } else {                                 //Tell the user which seats are available
                        System.out.println("Seats " + secondChoice[0] + (char) secondChoice[1] + " - " +
                                secondChoice[0] + (char) (secondChoice[1] + (secondChoice[2] - 1)) + " are available.");
                        System.out.println("Would you like to reserve these seats instead? Y/N");

                        secondChoiceAnswer = userScanner.nextLine(); //Store the user's input

                        if (secondChoiceAnswer.toUpperCase().equals("Y")) { //The user answered yes, so reserve the best available seats
                            userInput[0] = secondChoice[0];
                            userInput[1] = secondChoice[1];
                            rooms[roomNumber].updateRoom(userInput);
                            //Lastly update the user hashmap with the new order
                            Orders addedOrder = new Orders(roomNumber, userInput[0], userInput[2], userInput[3], userInput[4], (char) userInput[1], rooms[roomNumber].getRows() ,rooms[roomNumber].getColumns());
                            customer_orders.customerList = customer_orders.addAnOrder(
                                    customer_orders.customerList, customer_orders.customerList[customer_orders.findACustomer(userKey)], addedOrder).clone();
                        } else {
                            System.out.println("no seats available");
                        }
                    }
                }
                break;
            case 2: //User wants to view their orders
                customer_orders.customerList = customer_orders.getCustomerList().clone(); //Get the orders
                int customerIndex = customer_orders.findACustomer(userKey);               //Find the index with the logged in customer
                if(customer_orders.customerList[customerIndex].orderList.size() > 0) {
                    customer_orders.customerList[customerIndex].printOrders();            //Call the function to print the customer's orders
                }
                else {
                    System.out.println("No orders to view.");
                }
                break;
            case 3: //User wants to update an order
                customer_orders.customerList = customer_orders.getCustomerList().clone(); //Get the orders
                customerIndex = customer_orders.findACustomer(userKey);                   //Find the index with the logged in customer
                if(customer_orders.customerList[customerIndex].orderList.size() > 0) {
                    rooms = customer_orders.customerList[customerIndex].modifyOrder(userScanner, rooms); //Call the function to modify the order
                }
                else {
                    System.out.println("No orders to modify.");
                }
                //Give the user the option to update an order if they have made one
                break;
            case 4: //User wants to display their receipt
                customer_orders.customerList = customer_orders.getCustomerList().clone(); //Get the orders
                customerIndex = customer_orders.findACustomer(userKey);                   //Find the index of the logged in customer
                if(customer_orders.customerList[customerIndex].orderList.size() > 0) {
                    customer_orders.customerList[customerIndex].printReceipt(); //Call the function to print the customer's receipt
                }
                else {
                    System.out.println("Customer Total: $0.00"); //No orders, so no money spent
                }
                break;
            default: //User wants to log out
                System.out.println("Logging out...");
                rooms[roomNumber].continueUser = false; //Set login menu flag to false
                break;
        }

        return rooms; //Return the updated movie rooms
    }

    public static boolean printAdminMenu(Scanner userScanner, Auditorium[] rooms) throws IOException {
        String inputTemp;                      //Admin input variable
        int userSelection = -1;                //Which number the admin chose
        boolean validMenuSelection;            //Flag for if the input was valid
        boolean exitProgram = false;           //Flag to exit the program completely
        double[] individualResults;            //Array to store the results of each auditorium
        double[] totalResults = new double[7]; //Array to store the results of all auditoriums

        do {
            validMenuSelection = true;

            //Prompt the admin for input
            System.out.println("Select one of the following:");
            System.out.println("1. Print Report");
            System.out.println("2. Logout");
            System.out.println("3. Exit");

            try {
                inputTemp = userScanner.nextLine();          //Get input
                userSelection = Integer.parseInt(inputTemp); //Attempt to parse
                if(userSelection < 1 || userSelection > 3 || inputTemp.isEmpty())
                    throw new Exception();
            } catch (Exception e) {
                //Could not parse properly, give error and iterate through do while again
                validMenuSelection = false;
                System.out.println("ERROR. Please select a number between 1 and 3.");
            }
        } while(!validMenuSelection);

        switch(userSelection) { //Now that the input is valid
            case 1:  //Print final report
                System.out.println("Printing final report...");
                for(int i = 0; i < 3; i++) { //Print the report for each room, and write the data to their respective files
                    individualResults = rooms[i].printReport(rooms[i].getHead(), rooms[i].getColumns(), rooms[i].getRows(), i).clone();
                    for(int j = 0; j < 7; j++) {
                        totalResults[j] += individualResults[j]; //Add to the grand total of the theater
                    }
                }
                printOverallSummary(totalResults); //Print overall details of the theater
                break;
            case 2:  //Log out
                System.out.println("Logging out...");
                rooms[0].continueUser = false; //Set logout flag to true
                break;
            default: //Exit
                exitProgram = true; //Set program exit flag to true
                break;
        }

        return exitProgram;
    }

    public static int[] orderValidation(Scanner inputScanner, int numRows, int numCols) {
        boolean isValid;            //Flag variable for each input the user gives
        String user_input;          //Variable to hold the user's input
        int tempInputHolder = 0;    //Variable where the user's input will be converted into an int
        char tempSeatLetter = '\0'; //Variable where the user's input will be converted into a char
        int[] choices = new int[5]; //Choices will store the user's input about their tickets

        //Validate row number
        do {
            isValid = true;
            System.out.println("Select the row number you wish to sit in: ");

            try {
                user_input = inputScanner.nextLine();           //Store the input
                tempInputHolder = Integer.parseInt(user_input); //Try to parse the input into an integer
                if(tempInputHolder < 1 || tempInputHolder > numRows)
                    throw new Exception();
            } catch(Exception e) {
                //Could not parse properly, give an error and iterate through do while again
                isValid = false;
                System.out.println("ERROR. Please select a valid row. ");
            }
        } while (!isValid);

        //Store the row number the user wants to sit in
        choices[0] = tempInputHolder;

        //Validate seat letter
        do {
            isValid = true;
            System.out.println("Select the starting seat letter: ");

            try {
                user_input = inputScanner.nextLine();

                //Make sure that the user only input one character
                if(user_input.length() != 1) {
                    throw new Exception();
                }

                tempSeatLetter = user_input.toUpperCase().charAt(0); //Store the input as a character since there is only one char in the string

                if(tempSeatLetter < 65 || tempSeatLetter > (65 + numCols))
                    throw new Exception();

            } catch (Exception e) {
                isValid = false;
                System.out.println("ERROR. Please select a valid starting seat letter.");
            }
        } while (!isValid);

        //Store the starting column the user wants to sit at
        choices[1] = tempSeatLetter;
        int charChoice = tempSeatLetter - 64;

        //Validate number of adult tickets
        do {
            isValid = true;
            System.out.println("How many adult tickets would you like? They are $10 each.");

            user_input = inputScanner.nextLine();

            try {
                tempInputHolder = Integer.parseInt(user_input);

                if(tempInputHolder < 0 || tempInputHolder > (numCols + 1 - charChoice ))
                    throw new Exception();
            } catch (Exception e) {
                isValid = false;
                System.out.println("ERROR. Please enter a valid number of tickets.");
            }
        } while (!isValid);

        choices[2] = tempInputHolder; //Store tbe number of adult tickets the user wants

        //Validate number of child tickets
        do {
            isValid = true;
            System.out.println("How many child tickets would you like? They are $5 each.");

            user_input = inputScanner.nextLine();

            try {
                tempInputHolder = Integer.parseInt(user_input);

                if(tempInputHolder < 0 || tempInputHolder > (numCols + 1 - (charChoice + choices[2])))
                    throw new Exception();
            } catch (Exception e) {
                isValid = false;
                System.out.println("ERROR. Please enter a valid number of tickets.");
            }
        } while (!isValid);

        choices[3] = tempInputHolder; //Store the number of child tickets the user wants

        //Validate number of senior tickets
        do {
            isValid = true;
            System.out.println("How many senior tickets would you like? They are $7.50 each.");

            user_input = inputScanner.nextLine();

            try {
                tempInputHolder = Integer.parseInt(user_input);

                if(tempInputHolder < 0 || tempInputHolder > (numCols + 1 - (charChoice + choices[2] + choices[3])))
                    throw new Exception();
            } catch (Exception e) {
                isValid = false;
                System.out.println("ERROR. Please enter a valid number of tickets.");
            }
        } while (!isValid);

        choices[4] = tempInputHolder; //Store the number of senior tickets the user wants

        return choices;
    }

    public static void printOverallSummary(double[] results) {
        System.out.print("Total");
        System.out.print("\t\t\t" + (int)results[1]); //Total open seats
        System.out.print("\t" + (int)results[2]);     //Total reserved seats
        System.out.print("\t" + (int)results[3]);     //Total adult tickets
        System.out.print("\t" + (int)results[4]);     //Total child tickets
        System.out.print("\t" + (int)results[5]);     //Total senior tickets
        System.out.print("\t$");
        System.out.printf("%.2f", results[6]);        //Total money made
        System.out.print("\n\n");
    }

    public static int getRoomNumber(Scanner userScanner) {
        int selectedRoom = -1;      //Which room the user chose
        boolean validInput = false; //Flag for if user input was valid
        String input;               //User input variable

        do {
            //Ask for the room
            System.out.println("Enter which auditorium you wish to reserve a seat in:");
            System.out.println("1. Auditorium 1");
            System.out.println("2. Auditorium 2");
            System.out.println("3. Auditorium 3");
            input = userScanner.nextLine();

            try {
                selectedRoom = Integer.parseInt(input); //Attempt to parse
                if(selectedRoom < 1 || selectedRoom > 3)
                    throw new Exception();
                else
                    validInput = true;
            } catch(Exception e) {
                //Could not parse, give error and try again
                System.out.println("ERROR. Please give a valid response");
            }
        } while(!validInput);

        return selectedRoom - 1;
    }
}
