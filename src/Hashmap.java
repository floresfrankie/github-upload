/* Written by Frankie Flores for CS2336.003
   NetID: fxf180009
*/

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Hashmap {
    Customer[] customerList;

    Hashmap() {
        customerList = new Customer[0];
    }

    Hashmap(String filename) throws IOException {
        customerList = createHashmap(filename).clone();
    }

    public Customer[] createHashmap(String customerFileName) throws IOException {
        Scanner fileScanner = new Scanner(new FileInputStream(customerFileName)); //Scanner to read through the file

        Customer[] tempList = new Customer[10]; //Will be the hashmap returned by the function
        String tempKey;               //The key (username) for the current customer in the file
        String tempPassword;          //The password for the current customer in the file
        String tempReader;            //Will read through the line to get the key and password
        String currentLine;           //The current line of the file
        boolean isKey;                //Flag used to determine if the key or password is being read in
        int nextIndex;

        while(fileScanner.hasNextLine()) {
            isKey = true;
            tempKey = "";
            tempReader = "";
            currentLine = fileScanner.nextLine();

            for(int i = 0; i < currentLine.length(); i++) {
                if(currentLine.toUpperCase().charAt(i) != 32 && currentLine.toUpperCase().charAt(i) != '\0' && currentLine.toUpperCase().charAt(i) != '\n') {
                    tempReader += currentLine.charAt(i);
                }
                else {
                    if(isKey) {
                        tempKey = tempReader;
                        tempReader = "";
                        isKey = false;
                    }
                    else
                        tempReader += currentLine.charAt(i);
                }
            }

            tempPassword = tempReader;
            Customer nextCustomer = new Customer(tempKey, tempPassword);
            nextIndex = tempKey.charAt(0) % tempList.length;

            if(tempList[nextIndex] != null)
                tempList[findNextIndex(nextIndex, tempList)] = nextCustomer;
            else
                tempList[nextIndex] = nextCustomer;

            if(needsResizing(tempList, 0.5))
                tempList = resizeList(tempList).clone();
        }

        return tempList;
    }

    public int getNextPrimeNum(int currentSize) {
        int newSize = currentSize * 2; //Size variable that will be returned
        int currentSqrt;               //Where the loop will iterate up to
        boolean isPrime;               //Flag variable used to exit the do while loop when a prime number is found

        do {
            isPrime = true;                        //Set the flag to true for the current iteration
            currentSqrt = (int)Math.sqrt(newSize); //Find the right-bounds for the for loop

            for(int i =2; i <= currentSqrt; i++) {
                if (newSize % i == 0) { //If the number can be divided, it's not prime
                    isPrime = false;    //Set the flag to false
                    break;
                }
            }

            if(!isPrime)
                newSize++; //Increment the size if the size has not been found
        } while(!isPrime); //Stop once the loop finds a prime number

        return newSize;
    }

    public boolean needsResizing(Customer[] hashList, double load_Factor) {
        int accumulator = 0; //Will count the number of non-empty indexes in the list

        for (Customer customer : hashList) { //Iterate through the hashtable
            if (customer != null)
                accumulator++;   //Count every index that has been filled with a number
        }

        //Return whether or not the current load factor is greater than 0.5
        return ((double)accumulator/ hashList.length) > load_Factor;
    }

    public Customer[] resizeList(Customer[] hashList) {
        int newListSize = getNextPrimeNum(hashList.length); //Find the new size of the hashtable
        Customer[] newList = new Customer[newListSize];     //Create a new hashtable
        int nextIndex;                                      //Variable used to find the new index for the existing values

        for (Customer customer : hashList) { //Iterate through the hashtable
            if (customer != null) {
                nextIndex = customer.key.charAt(0) % newListSize; //Find the new index

                if (newList[nextIndex] != null) //Collision occurs, find and fill an empty bucket
                    newList[findNextIndex(nextIndex, newList)] = customer;
                else
                    newList[nextIndex] = customer;  //No collision, fill the empty bucket
            }
        }

        return newList; //Return the newly resized hashtable
    }

    public int findNextIndex(int collisionIndex, Customer[] hashList) {
        int newIndex;      //Variable used to find a new index in the event of a collision
        int iIterator = 0; //Iterator used to get another index

        while(hashList[(collisionIndex + (iIterator*iIterator)) % hashList.length] != null) { //While the next potential bucket is not empty
            iIterator++; //Increment the iterator
        }

        //Once the loop stops, an empty bucket has been found and will be filled
        newIndex = (collisionIndex + (iIterator*iIterator)) % hashList.length;
        return newIndex; //Return the index of the empty bucket
    }

    public Customer[] addAnOrder(Customer[] previousList, Customer addedOrder, Orders orderItself) {
        Customer[] newList = previousList.clone();         //Prepare the new list to be edited
        int addIndex = findACustomer(addedOrder.getKey()); //Get the index of the customer whose order will be edited

        newList[addIndex].addOrder(orderItself);   //Add the order to the hashmap
        if(needsResizing(newList, 0.5))  //If resizing is necessary
            newList = resizeList(newList).clone(); //Then resize the hashmap

        return newList; //Return the hashmap
    }

    public int findACustomer(String searchKey) {
        int foundIndex = -1; //Variable giving the index of the customer if they are found

        for (int i = 0; i < customerList.length; i++) { //Go through the customer array list
            if (customerList[i] != null && customerList[i].getKey().compareTo(searchKey) == 0) {
                foundIndex = i; //Once the user is found, store the index and break the loop
                break;
            }
        }

        return foundIndex; //If the index exists, it will be > -1
    }

    public boolean validatePassword(int customerIndex, String passwordToCheck) {
        return customerList[customerIndex].getPassword().compareTo(passwordToCheck) == 0; //Compares password with given value and returns the result
    }

    public Customer[] getCustomerList() {
        return customerList;
    }

}

//----------------------------------------------------------------------------------------------------------------------//
class Customer {
    String key;                  //The username will be the key for the hashmap
    String password;             //The password will also be stored in the hashmap
    ArrayList<Orders> orderList; //The customer's orders will be stored in an arraylist

    Customer() {                                    //Default Constructor
        key = "";
        password = "";
        orderList = new ArrayList<>();
    }

    Customer(String userKey, String userPassword) { //Overloaded constructor
        key = userKey;
        password = userPassword;
        orderList = new ArrayList<>();
    }

    public void addOrder(Orders newOrder) { //Append a new order to the end of the list
        orderList.add(newOrder);
    }

    public String[][] addToEndOfOrder(int whereIsOrder, int row, char col, int totalAdded) {
        String[][] updatedSeats = orderList.get(whereIsOrder).orderSeats.clone(); //Copy the customer's order array

        for(int i = col - 65; i < (col - 65) + (totalAdded); i++) {
            updatedSeats[row - 1][i] = "";              //First clear the index
            updatedSeats[row - 1][i] += row;            //Then give the row number
            updatedSeats[row - 1][i] += (char)(i + 65); //Then add the seat letter
        }

        return updatedSeats; //Return the updated customer order array
    }

    public int[] modifySelection(Scanner userScanner) {
        int[] orderOption = getOrderToModify(userScanner).clone(); //Get the user's choice on which order to modify
        int whatToDo = -1;             //User decision between adding/deleting/canceling
        String userInput = null;       //Initialize string variable the user will enter values to
        boolean validInput;            //Flag used to determine if input was valid or not
        int[] selections = new int[3]; //Selections stores the info necessary for modifying an order

        System.out.println("\n1. Add tickets to order");
        System.out.println("2. Delete tickets from order");
        System.out.println("3. Cancel Order");

        do {
            validInput = true;
            try {
                userInput = userScanner.nextLine();     //Take the user input
                whatToDo = Integer.parseInt(userInput); //Attempt to parse as an integer
                if(whatToDo < 1 || whatToDo > 3)        //If parsed, make sure it's between 1 & 3
                    throw new Exception();              //Otherwise throw an exception
            } catch(Exception e) {
                //Give exception message and iterate through the do while loop again
                System.out.println("ERROR. Please give a valid response");
                validInput = false;
            }
        } while(!validInput);

        selections[0] = whatToDo;       //Tells us if we are adding, removing, or canceling
        selections[1] = orderOption[0]; //Location of the order
        selections[2] = orderOption[1]; //Room the order is in
        return selections;
    }

    public Auditorium[] modifyOrder(Scanner userScanner, Auditorium[] rooms) {
        boolean validChoice;           //Flag used to determine if the user gave a valid selection
        boolean backToMenu;            //Flag to determine if the user must choose a modify order option again
        int ticketToRemoveRow = 0;     //Initialize the row of a ticket that may be removed
        int whichModification;         //Value that gives what type of modification will be done
        int totalTickets;              //Number of tickets in a customer's order
        char ticketToRemoveLetter = 0; //Initialize the letter of a seat that may be removed
        String userInput;              //Input given by the user, which will then be processed
        int[] orderInformation;        //Stores the info necessary to reserve new seats in an auditorium

        do {
            backToMenu = false;                                      //For now, there is no need to reset the modify orders prompt
            int[] selections = modifySelection(userScanner).clone(); //Store the info necessary for modifying an order
            whichModification = selections[0];                       //Store the type of modification to be done

            switch(whichModification) {
                case 1: { //Add tickets to order
                    int roomNumber = selections[2]; //First get the room number

                    //Then ask the user to give the info to reserve seats
                    rooms[roomNumber].printAuditorium(rooms[roomNumber].getHead(), rooms[roomNumber].getColumns(), rooms[roomNumber].getRows()); //Show them the room
                    orderInformation = Main.orderValidation(userScanner, rooms[roomNumber].getRows(), rooms[roomNumber].getColumns()).clone();   //Validate their input
                    totalTickets = orderInformation[2] + orderInformation[3] + orderInformation[4];                                              //Store the info needed to reserve seats

                    if (rooms[roomNumber].checkAvailability(totalTickets, orderInformation[0], (char) orderInformation[1])) { //If the seat is available
                        rooms[roomNumber].updateRoom(orderInformation); //Update linked list if available

                        //Then update the user hashmap with the new order
                        Orders addedOrder = new Orders(roomNumber, orderInformation[0], orderInformation[2], orderInformation[3], orderInformation[4], (char) orderInformation[1], rooms[roomNumber].getRows() ,rooms[roomNumber].getColumns());

                        //Update the hashmap with the new tickets, both in the array and ticket counter variables
                        orderList.get(selections[1] - 1).orderSeats = addToEndOfOrder(selections[1] - 1, orderInformation[0], (char)orderInformation[1], totalTickets).clone(); //123456
                        orderList.get(selections[1] - 1).numAdults += orderInformation[2];   //Increase the number of adult tickets
                        orderList.get(selections[1] - 1).numChildren += orderInformation[3]; //Increase the number of child tickets
                        orderList.get(selections[1] - 1).numSeniors += orderInformation[4];  //Increase the number of senior tickets
                    }
                    else {
                        System.out.println("no seats available"); //Tell the user the seats were not available
                        backToMenu = true;                        //Go back to the modify menu
                    }
                    break;
                }
                case 2: { //Delete tickets from order
                    do { //Get a valid row from the user
                        validChoice = true;                                                   //For now the input is valid
                        System.out.println("Enter the row of the ticket you wish to remove"); //Prompt user for the row number
                        userInput = userScanner.nextLine();

                        try {
                            ticketToRemoveRow = Integer.parseInt(userInput); //Make sure the user gave an integer
                            if(ticketToRemoveRow < 1 || ticketToRemoveRow > orderList.get(selections[1] - 1).getOrderSeats().length) //Make sure the row is a valid number
                                throw new Exception();

                        } catch(Exception e) {
                            validChoice = false; //Change validChoice so that the do while loop will iterate again
                        }
                    } while(!validChoice);

                    do {
                        validChoice = true; //For now the letter input is valid
                        try {
                            System.out.println("Enter the letter of the ticket you wish to remove"); //Prompt user for the seat letter
                            userInput = userScanner.nextLine().toUpperCase();

                            char lastPossible = orderList.get(selections[1] - 1).getLastSeatInRow(ticketToRemoveRow); //Store the last possible seat that can be entered
                            if (userInput.length() > 1 || userInput.charAt(0) > lastPossible)
                                throw new Exception(); //Throw an exception if the input is not valid
                            else
                                ticketToRemoveLetter = userInput.charAt(0); //Otherwise store the input
                        } catch(Exception e) {
                            validChoice = false; //Bad input; do while loop must iterate again
                        }
                    } while(!validChoice);

                    if(validateTicketChoice(orderList.get(selections[1] - 1), ticketToRemoveRow, ticketToRemoveLetter)) {
                        char kindOfTicket = rooms[selections[2]].getTicketPayload(rooms[selections[2]].getHead(), ticketToRemoveRow, ticketToRemoveLetter); //Get the info of the ticket being removed
                        String seatKey = String.valueOf(ticketToRemoveRow).concat(String.valueOf(ticketToRemoveLetter));                                    //Find the key of the ticket being removed
                        rooms[selections[2]] = deleteSingleTicket(rooms[selections[2]], ticketToRemoveRow, ticketToRemoveLetter);                           //Now remove the ticket from the linked list
                        orderList.get(selections[1] - 1).orderSeats = orderList.get(selections[1] - 1).removeSeatFromOrder(seatKey, kindOfTicket).clone();  //And remove the order from the orders hashmap

                        if(orderList.get(selections[1] - 1).getOrderLength() < 1) {        //Then check if the order is now empty
                            orderList = cancelOrder(orderList.get(selections[1] - 1));     //If the order is empty, remove the order from the arrayList entirely
                        }
                    }

                    break;
                }
                default: //Cancel order
                    rooms[selections[2]] = clearSeats(orderList.get(selections[1] - 1), rooms[selections[2]]); //Clear the order from the linked list
                    orderList = cancelOrder(orderList.get(selections[1] - 1));                                 //Clear the order from the hashmap
                    break;
            }
        } while(backToMenu);
        return rooms;
    }

    public ArrayList<Orders> cancelOrder(Orders oldOrder) {
        orderList.remove(oldOrder); //Remove the order from the orders array list
        return orderList;
    }

    public Auditorium clearSeats(Orders removeOrder, Auditorium modifiedRoom) {
        //Go to the 2D array and clear the values in the linked list
        String[][] badOrder = removeOrder.getOrderSeats().clone();
        int rowStart; //Gives the first index of the seats being removed
        String temp;  //Used to get the ticket info for removal

        for(int i = 0; i < removeOrder.getOrderSeats().length; i++) {
            for(int j = 0; j < removeOrder.getOrderSeats()[i].length; j++) { //Loop through the orders list
                if(badOrder[i][j] != null && badOrder[i][j].length() > 1) {  //Remove the ticket at this index
                    temp = ""; //The row of the tickets to remove

                    for(int k = 0; k < badOrder[i][j].length() - 1; k++)  //Get the integer value of the row to remove
                        temp += badOrder[i][j].charAt(k);                 //Gives the character value

                    //Remove the ticket from the linked list
                    modifiedRoom.removeTicket(modifiedRoom.getHead(), Integer.parseInt(temp), badOrder[i][j].charAt(badOrder[i][j].length() - 1));
                }
            }
        }

        return modifiedRoom; //Return the updated auditorium
    }

    public boolean validateTicketChoice(Orders modifiedOrder, int row, char col) {
        boolean wasFound = false;             //Tells us if the order exists
        String tempRow = String.valueOf(row); //String version of the row we are searching for

        for(int i = 0; i < modifiedOrder.orderSeats.length; i++) {
            for(int j = 0; j < modifiedOrder.orderSeats[i].length; j++) {
                //If the order exists in the customer's specific order array
                if (modifiedOrder.orderSeats[i][j] != null && (modifiedOrder.orderSeats[i][j].contains(tempRow) && modifiedOrder.orderSeats[i][j].contains(String.valueOf(col)))) {
                    wasFound = true; //Set flag to true
                    break;           //Exit the inner loop
                }
            }

            if(wasFound) //If found, exit the outer loop
                break;
        }

        return wasFound;
    }

    public Auditorium deleteSingleTicket(Auditorium modifiedRoom, int row, char col) {
        modifiedRoom.removeTicket(modifiedRoom.getHead(), row, col); //Remove the ticket from the linked list
        return modifiedRoom;
    }

    public int[] getOrderToModify(Scanner userScanner) {
        int whichOrder = -1;                //Stores the specific order number to be modified
        int totalOrders = orderList.size(); //Gives the number of orders the user has made
        String userInput;                   //User given input to be processed
        boolean validInput;                 //Flag variable
        int whichRoom;                      //The room being modified
        int[] orderDetails = new int[2];    //Has the location of the order and the room the order is in

        System.out.println("Select an order you wish to modify:"); //Prompt user for input, show them their orders
        printOrders();

        do {
            validInput = true;

            try {
                userInput = userScanner.nextLine();       //Take user input
                whichOrder = Integer.parseInt(userInput); //Attempt to parse it into an int
                if(whichOrder < 1 || whichOrder > totalOrders)
                    throw new Exception();
            } catch(Exception e) {
                System.out.println("ERROR. Please give a valid response"); //Could not parse properly, iterate do while again
                validInput = false;
            }
        } while(!validInput);

        whichRoom = orderList.get(whichOrder - 1).getAuditoriumNum(); //Find the room number of the order
        orderDetails[0] = whichOrder; //Tells us the location of the order
        orderDetails[1] = whichRoom;  //Tells us the room the order is in
        return orderDetails;
    }

    public String getKey() {
        return key;
    }

    public String getPassword() {
        return password;
    }

    public void printOrders() {
        for (int i = 0; i < orderList.size(); i++) { //Loop through the orders array list
            Orders orders = orderList.get(i);        //Get the order at the current index
            int numOrders = orderList.get(i).numAdults + orderList.get(i).numChildren + orderList.get(i).numSeniors; //Store the user's number of orders
            int orderCount = 1; //The order number currently being used

            System.out.print("\nAuditorium " + (orders.getAuditoriumNum() + 1) + ", ");

            for (int j = 0; j < orders.orderSeats.length; j++) {       //Loop through the specific order's 2D array
                for(int k = 0; k < orders.orderSeats[j].length; k++) {
                    //Print the individual tickets
                    if (orders.orderSeats[j][k] != null && orders.orderSeats[j][k].length() > 1) {
                        System.out.print(j + 1);           //Print the row
                        System.out.print((char) (65 + k)); //Print the seat letter
                        if (orderCount < numOrders)        //If there is another ticket, print a comma
                            System.out.print(",");
                        orderCount++;
                    }
                }
            }

            //Give basic order details, don't include cost
            System.out.print("\n" + orders.getNumAdults() + " adult, ");
            System.out.print(orders.getNumChildren() + " child, ");
            System.out.print(orders.getNumSeniors() + " senior");
            System.out.print("\n");

        }

        System.out.print("\n");
    }

    public void printReceipt(){
        int orderCount;          //Used for formatting the current order
        double orderTotal;       //Total spent on the current order
        double finalTotal = 0.0; //Total spent on all orders

        for (int i = 0; i < orderList.size(); i++) { //Loop through the orders array list
            Orders orders = orderList.get(i);        //Get the order at the current index
            int numOrders = orderList.get(i).numAdults + orderList.get(i).numChildren + orderList.get(i).numSeniors; //Get the total number of orders made
            orderCount = 1;

            System.out.print("\nAuditorium " + (orders.getAuditoriumNum() + 1) + ", ");

            for (int j = 0; j < orders.orderSeats.length; j++) {       //Loop through the specific order's 2D arrat
                for(int k = 0; k < orders.orderSeats[j].length; k++) {
                    //Print the individual tickets
                    if (orders.orderSeats[j][k] != null && orders.orderSeats[j][k].length() > 1) {
                        System.out.print(j + 1);           //Print the row
                        System.out.print((char) (65 + k)); //Print the column
                        if (orderCount < numOrders)
                            System.out.print(",");         //If there is another ticket, print a comma too
                        orderCount++;
                    }
                }
            }

            //Basic order details
            System.out.print("\n" + orders.getNumAdults() + " adult, ");
            System.out.print(orders.getNumChildren() + " child, ");
            System.out.print(orders.getNumSeniors() + " senior");
            System.out.print("\n");

            //Calculate the current order total and print it
            orderTotal = ((orders.getNumAdults() * 10.0) + (orders.getNumChildren() * 5.0) + (orders.getNumSeniors() * 7.5));
            System.out.print("Order Total: $");
            System.out.printf("%.2f", orderTotal);
            System.out.print("\n");
            finalTotal += orderTotal;
        }

        //Calculate final order total and print it
        System.out.print("\nCustomer Total: $");
        System.out.printf("%.2f", finalTotal);
        System.out.print("\n\n");
    }
}

//----------------------------------------------------------------------------------------------------------------------//
class Orders {
    int auditoriumNum;      //Which auditorium the order is in
    int rowNumber;          //The row number the seats are in
    int numAdults;          //Number of adult tickets in the order
    int numChildren;        //Number of child tickets in the order
    int numSeniors;         //Number of senior tickets in the order
    char startSeat;         //Starting seat letter
    String[][] orderSeats;  //Array with the seats the user has reserved

    //Default constructor
    Orders() {
        auditoriumNum = -1;
        rowNumber = -1;
        numAdults = -1;
        numChildren = -1;
        numSeniors = -1;
        startSeat = '\0';
        orderSeats = new String[0][0];
    }

    //Overloaded constructor
    Orders(int whichRoom, int startRow, int adults, int kids, int seniors, char firstSeat, int rows, int rowLen) {
        auditoriumNum = whichRoom;
        rowNumber = startRow;
        numAdults = adults;
        numChildren = kids;
        numSeniors = seniors;
        startSeat = firstSeat;
        orderSeats = new String[rows][rowLen];
        orderSeats = fillOrderedSeats(rows, rowLen).clone(); //Fill the array with the user's ordered seats
    }

    public String[][] fillOrderedSeats(int numRows, int numCols) {
        String[][] orderRow = new String[numRows][numCols]; //Create a copy of the specific order's 2D array

        for(int i = startSeat - 65; i < (startSeat - 65) + (numAdults + numChildren + numSeniors); i++) { //Loop through the row with reserved tickets
            orderRow[rowNumber - 1][i] = "";              //First empty the string
            orderRow[rowNumber - 1][i] += (rowNumber);    //Then give the row number
            orderRow[rowNumber - 1][i] += (char)(i + 65); //Then add the seat letter
        }

        return orderRow; //Return the updated order 2D array
    }

    public int getAuditoriumNum() {
        return auditoriumNum;
    }

    public char getStartSeat() {
        return startSeat;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getTotalTickets() {
        return (numAdults + numChildren + numSeniors);
    }

    public int getNumAdults() {
        return numAdults;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public int getNumSeniors() {
        return numSeniors;
    }

    public String[][] getOrderSeats() {
        return orderSeats;
    }

    public char getLastSeatInRow(int row) {
        char lastSeat = '\0';

        for(int i = 0; i < orderSeats[row - 1].length; i++) {                         //Loop through the specific row of the 2D order array
            if(orderSeats[row - 1][i] != null && orderSeats[row - 1][i].length() > 1) //Store the value - if there is any - at the current index
                lastSeat = orderSeats[row - 1][i].charAt(orderSeats[row - 1][i].length() - 1);
        }

        return lastSeat; //Return the last possible character value that can be given
    }

    public int getOrderLength() {
        int counter = 0;

        for (String[] orderSeat : orderSeats) {  //Go through each row of the order array
            for (String s : orderSeat) {         //Go through each column of the order array
                if (s != null && s.length() > 1) //If there is an order, increment the counter
                    counter++;
            }
        }

        return counter;
    }

    public String[][] removeSeatFromOrder(String searchSeat, char ticketType) {
        boolean wasFirst = true; //Tells us if we need to change the starting seat of the order

        for(int i = 0; i < orderSeats.length; i++) {
            for(int j = 0; j < orderSeats[i].length; j++) {
                if(orderSeats[i][j] != null) {
                    if(orderSeats[i][j].compareTo(searchSeat) == 0 && wasFirst) {
                        orderSeats[i][j] = null; //Set the current starting seat to null
                        findNextStartingSeat();  //Find the next starting seat
                    }
                    else if(orderSeats[i][j].compareTo(searchSeat) == 0) {
                        orderSeats[i][j] = null; //Set the seat to null
                    }
                    if(wasFirst) {
                        wasFirst = false;
                    }
                }
            }
        }

        //Decrement the ticket type by 1: i.e. if an adult, decrement numAdults by 1.
        switch(ticketType) {
            case 'A':  //Decrement numAdults
                numAdults--;
                break;
            case 'C':  //Decrement numChildren
                numChildren--;
                break;
            default:   //Decrement numSeniors
                numSeniors--;
                break;
        }

        return orderSeats;
    }

    public void findNextStartingSeat() {
        boolean newStartFound = false;

        for(int i = rowNumber; i < orderSeats.length; i++) {
            for(int j = startSeat - 65; j < orderSeats[i].length; j++) {
                //If there are other tickets (in the current order) on this row
                if(orderSeats[i][j] != null && !orderSeats[i][j].contains(String.valueOf(startSeat))) {
                    startSeat = (char)(j + 65); //Store the first ticket we find
                    newStartFound = true;       //Set flag to true, exit the inner loop
                    break;
                }
            }

            if(newStartFound) //Exit the outer loop if ticket was found
                break;
        }

        if(!newStartFound) {
            for (String[] orderSeat : orderSeats) {           //Search the entire 2D array for a ticket
                for (int k = 0; k < orderSeat.length; k++) {
                    if (orderSeat[k] != null && !orderSeat[k].contains(String.valueOf(startSeat))) {
                        startSeat = (char) (k + 65); //Store the first ticket we find
                        newStartFound = true;        //Set flag to true, exit the inner loop
                        break;
                    }
                }

                if (newStartFound)
                    break;
            }
        }
    }
}