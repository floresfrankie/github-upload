/* Written by Frankie Flores for CS2336.003
   NetID: fxf180009
*/

import java.io.*;
import java.util.*;

public class Auditorium {
    //Class Members
    Node<Seat> head = new Node<Seat>();
    LinkedList<Seat> theatre;
    int rows = 1;
    int columns = -1;
    boolean continueUser;

    //Constructors
    Auditorium(String theaterFilename) throws IOException {
        theatre = new LinkedList<Seat>();                                        //Make the auditorium a linked list of seats
        Scanner fileScanner = new Scanner(new FileInputStream(theaterFilename)); //File scanner to help fill the auditorium
        String currentRow; //String variable used for
        continueUser = true;

        while(fileScanner.hasNextLine()) {
            currentRow = fileScanner.nextLine(); //Store the current row of the auditorium
            columns = currentRow.length();       //Keep the length to know the number of columns

            if(rows == 1) { //There is one row so far
                head.setRight(fillRow(currentRow, rows, null)); //Fill the first row
            }
            else { //There is more than one row
                Node<Seat> bottom = new Node<Seat>();
                bottom = head.right;

                for(int i = 2; i < rows; i++) { //Traverse to the bottom of the auditorium
                    bottom = bottom.down;
                }

                bottom.setDown(fillRow(currentRow, rows, bottom)); //Vertically connect the beginning of each row
            }

            if(fileScanner.hasNextLine())
                rows += 1;
        }

        //Call function to vertically connect the nodes
        connectVertically();

        fileScanner.close(); //Done using file scanner, now we close it
    }

    //Mutators
    public Node<Seat> fillRow(String currentRow, int row, Node<Seat> rowAboveStart) {
        Node<Seat> start = null;   //Set a node to signify the start of the new row
        Node<Seat> current = null; //Set a node to signify the most recent seat to be added
        Node<Seat> next = null;    //Set a node to signify the next seat to be added

        //Create the first seat of the new row
        Seat addedSeat = new Seat(row, 'A', currentRow.charAt(0));
        current = new Node<Seat>(addedSeat);

        //Connect the first node of the last row to the first node of the new row
        if(rowAboveStart != null) {
            rowAboveStart.setDown(current);
            current.setUp(rowAboveStart);
        }

        start = current; //Only make start = current AFTER the seat has been connected with the row above

        for(int i = 1; i < currentRow.length(); i++) {
            addedSeat = new Seat(row, (char)('A' + i), currentRow.charAt(i));
            next = new Node<Seat>(addedSeat);

            //Connect the nodes
            current.setRight(next);
            next.setLeft(current);
            current = next;
        }

        //Add the end node
        addedSeat = new Seat(row, (char)('A' + (currentRow.length() - 1)), currentRow.charAt(currentRow.length() - 1));
        next = new Node<Seat>(addedSeat);

        //Connect the two horizontally adjacent nodes
        current.setRight(next);
        next.setLeft(current);

        return start;
    }

    public void connectVertically() {
        Node<Seat> topRowStart = head.right;
        Node<Seat> topRowIterator = topRowStart;
        Node<Seat> botRowStart;
        Node<Seat> botRowIterator;

        //ONLY continue with the function IF there is more than one row
        if(topRowStart.down != null) {
            botRowStart = topRowStart.down;
            botRowIterator = botRowStart;

            for(int i = 1; i < rows; i++) {
                //Traverse the row and connect each node after the row's beginning column
                while(topRowIterator.right != null) {
                    topRowIterator = topRowIterator.right;
                    botRowIterator = botRowIterator.right;
                    topRowIterator.setDown(botRowIterator);
                    botRowIterator.setUp(topRowIterator);
                }

                //Move down to the next two rows if possible
                if(botRowStart.down != null) {
                    topRowStart = botRowStart;
                    topRowIterator = topRowStart;
                    botRowStart = botRowStart.down;
                    botRowIterator = botRowStart;
                }
            }
        }
    }

    public void updateRoom(int[] ticketInfo) {
        /* ticketInfo[0] has the row number
           ticketInfo[1] has the starting seat letter
           ticketInfo[2] has the number of adult tickets
           ticketInfo[3] has the number of child tickets
           ticketInfo[4] has the number of senior tickets
         */
        Node<Seat> seatIterator = head.right;
        int ticketAccumulator = 0;

        //Go the the requested row
        while(seatIterator.payload.row != ticketInfo[0])
            seatIterator = seatIterator.down;

        //Go to the requested column
        while(seatIterator.payload.seat != ticketInfo[1])
            seatIterator = seatIterator.right;

        //Insert adult tickets first
        while(ticketAccumulator < ticketInfo[2]) {
            ticketAccumulator++;
            seatIterator.payload.setTicketType('A');
            if(seatIterator.right != null)
                seatIterator = seatIterator.right; //Only move to the right if the end of the row hasn't been reached
        }

        ticketAccumulator = 0;

        //Insert child tickets second
        while(ticketAccumulator < ticketInfo[3]) {
            ticketAccumulator++;
            seatIterator.payload.setTicketType('C');
            if(seatIterator.right != null)
                seatIterator = seatIterator.right; //Only move to the right if the end of the row hasn't been reached
        }

        ticketAccumulator = 0;

        //Insert senior tickets last
        while(ticketAccumulator < ticketInfo[4]) {
            ticketAccumulator++;
            seatIterator.payload.setTicketType('S');
            if(seatIterator.right != null)
                seatIterator = seatIterator.right; //Only move to the right if the end of the row hasn't been reached
        }
    }

    public void removeTicket(Node<Seat> whereToStart, int row, char seatLetter) {
        Node<Seat> startNode = head.right; //Node that will be used to check availability

        //Get to the requested row number
        while(startNode.payload.row != row) {
            startNode = startNode.down;
        }

        //Get to the starting seat letter
        while(startNode.payload.seat != seatLetter) {
            startNode = startNode.right;
        }

        startNode.payload.setTicketType('.');
    }

    public char getTicketPayload(Node<Seat> whereToStart, int row, char seatLetter) {
        Node<Seat> startNode = head.right; //Node that will be used to check availability

        //Get to the requested row number
        while(startNode.payload.row != row) {
            startNode = startNode.down;
        }

        //Get to the starting seat letter
        while(startNode.payload.seat != seatLetter) {
            startNode = startNode.right;
        }

        return startNode.payload.getTicketType();
    }

    //Accessors
    public Node<Seat> getHead() {
        return head;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public void printAuditorium(Node<Seat> headSeat, int numColumns, int numRows) {
        Node<Seat> startOfRow = headSeat.right; //Node to represent the start of each row
        Node<Seat> tempSeat = startOfRow;       //Node that gets printed
        int currentRow = 1;                     //Row iterator

        //Print the seat letters
        System.out.print("   ");
        for(int letterIterator = 0; letterIterator < numColumns; letterIterator++) {
            System.out.print((char)('A' + letterIterator));
        }
        System.out.print("\n");

        //Loop through the linkedList
        while(currentRow <= numRows) {
            for(int i = 0; i <= numColumns; i++) {
                if(i == 0) {
                    System.out.print(currentRow + "  ");
                }
                else {
                    if(tempSeat.payload.ticketType != '.') //If the ticket is occupied, print "#"
                        System.out.print('#');
                    else
                        System.out.print('.');             //Otherwise print "."

                    if(tempSeat.right != null)
                        tempSeat = tempSeat.right;
                }
            }

            System.out.print("\n");       //Move to a newline
            currentRow++;                 //Increment row iterator
            if(startOfRow.down != null) {
                startOfRow = startOfRow.down; //Move down to next row
                tempSeat = startOfRow;
            }
        }
    }

    public boolean checkAvailability(int numTickets, int rowNumber, char startingLetter) {
        boolean isAvailable = true;        //Flag variable for availability
        Node<Seat> startNode = head.right; //Node that will be used to check availability

        //Get to the requested row number
        while(startNode.payload.row != rowNumber) {
            startNode = startNode.down;
        }

        //Get to the starting seat letter
        while(startNode.payload.seat != startingLetter) {
            startNode = startNode.right;
        }

        //Check that there are numTickets tickets available in the row. If any ticket is taken, set isAvailable to false
        for(int i = 0; i < numTickets; i++) {
            if(startNode.payload.ticketType != '.') {
                isAvailable = false;
            }
            startNode = startNode.right; //Move on to the next node
        }

        return isAvailable;
    }

    public int[] bestAvailable(int numTickets) {
        Node<Seat> rowStart = head.right;         //Node for the beginning of each row
        Node<Seat> rowIterator = rowStart;        //Node for the current column in the row
        Node<Seat> seatChecker = rowIterator;     //Node that checks current column + number of tickets requested
        int[] bestAvailableSeat = new int[3];     //Array where the best available values will be stored and returned
        int openTickets = 0;                      //Accumulator variable for open tickets
        int currentRow = 1;                       //Row Iterator
        int startingRow = -1;                     //If there is a best available, this is the row of the best available seats
        int startingSeat = -1;                    //If there is a best available, this is the starting seat of the best available seats
        int tempX;                                //Temporary X variable used in tempDistance calculation
        int tempY;                                //Temporary Y variable used in tempDistance calculation
        double midPointColumn = (columns+1)/ 2.0; //Find the middle column
        double midPointRow = (rows+1) / 2.0;      //Find the middle row
        double closestDistance = 1000;            //Variable for the closest distance to the middle
        double tempDistance;                      //Any open seats will have this variable to compare their distance to the current best available
        boolean wasInMiddle = false;

        while(currentRow <= rows) { //Check each row
            for(int i = 0; i <= columns - numTickets; i++) { //Start at the first element of each row
                for(int j = 0; j < numTickets; j++) {       //Check to see if (i + numTickets) seats are available
                    if(seatChecker.payload.ticketType != '.')
                        break;
                    else {
                        openTickets++; //Increment the number of open seats
                        if(seatChecker.right != null && openTickets!= numTickets) //Make sure that there is another seat in the row, and numTickets hasn't been reached
                            seatChecker = seatChecker.right;
                    }
                }

                if(openTickets == numTickets) {
                    tempY = currentRow;                            //Set the x value to the current row number
                    int leftMost = rowIterator.payload.seat - 64;  //Store the left most available seat
                    int rightMost = seatChecker.payload.seat - 64; //Store the right most available seat

                    if(Math.abs((double)leftMost - midPointColumn) <= Math.abs((double)rightMost - midPointColumn)) {
                        tempX = leftMost; //Take the data from the left most point (rowIterator)
                    }
                    else {
                        tempX = rightMost; //Take the data from the right most point (seatChecker)
                    }

                    //Calculate the distance to the middle of the auditorium
                    tempDistance = Math.sqrt(Math.pow((double)tempX - midPointColumn, 2) + Math.pow((double)tempY - midPointRow, 2));

                    //Check to see if the potential distance is either smaller OR directly in the middle
                    if(((double)(leftMost + rightMost)/2.0) == midPointColumn && Math.abs((double)tempY - midPointRow) <= 1) {
                        startingRow = tempY;            //Store the row
                        startingSeat = leftMost + 64;   //Store the starting index of the best available seats
                        closestDistance = tempDistance; //Save the new closest distance
                        wasInMiddle = true;
                    }
                    if(tempDistance < closestDistance && !wasInMiddle) {
                        startingRow = tempY;            //Store the row
                        startingSeat = leftMost + 64;   //Store the starting index of the best available seats
                        closestDistance = tempDistance; //Save the new closest distance
                    }
                    else if((tempDistance == closestDistance && (Math.abs((double)tempY - midPointRow) < Math.abs((double)startingRow - midPointRow))) && !wasInMiddle) {
                        //Reserve the seats closest to the middle of the auditorium, if there happens to be a tie
                        startingRow = tempY;
                        startingSeat = tempX + 64;
                    }
                }

                //Start the check at the next index
                openTickets = 0;
                rowIterator = rowIterator.right;
                seatChecker = rowIterator;
            }

            currentRow++; //Move to the next row for the loop
            if(rowStart.down != null) {
                rowStart = rowStart.down; //Move to the next row for the nodes
                rowIterator = rowStart;   //Reset the iterator node
                seatChecker = rowIterator;//Reset the seat checker node
            }
        }

        //Set the best available seats into the array: starting row, starting seat, and number of seats)
        bestAvailableSeat[0] = startingRow;
        bestAvailableSeat[1] = startingSeat;
        bestAvailableSeat[2] = numTickets;

        return bestAvailableSeat;
    }

    public double[] printReport(Node<Seat> headSeat, int numColumns, int numRows, int whichFile) throws IOException {
        Node<Seat> startOfRow = headSeat.right; //Node to represent the start of each row
        Node<Seat> tempSeat = startOfRow;       //Node that gets printed
        int currentRow = 1;                     //Row iterator
        int occupiedSeats = 0;                  //Number of tickets sold
        int numAdults = 0;                      //Number of adult tickets sold
        int numChildren = 0;                    //Number of children tickets sold
        int numSeniors = 0;                     //Number of senior tickets sold
        double moneyMade = 0.0;                 //Total amount of sales made
        double[] roomResults = new double[7];   //Details about the room to be returned by the function

        //Write all data to the output file
        FileOutputStream outFile = new FileOutputStream("A" + (whichFile + 1) + "Final.txt");
        PrintWriter outputWriter = new PrintWriter(outFile);

        //Loop through the linkedList
        while(currentRow <= numRows) {
            for(int i = 1; i <= numColumns; i++) {
                outputWriter.print(tempSeat.payload.ticketType); //Write the ticket type

                if(tempSeat.payload.ticketType == 'A') { //Current seat is an adult ticket
                    moneyMade += 10.0;
                    occupiedSeats++;
                    numAdults++;
                }
                else if (tempSeat.payload.ticketType == 'C') { //Current seat is a child ticket
                    moneyMade += 5.0;
                    occupiedSeats++;
                    numChildren++;
                }
                else if (tempSeat.payload.ticketType == 'S') { //Current seat is a senior ticket
                    moneyMade += 7.5;
                    occupiedSeats++;
                    numSeniors++;
                }

                if(tempSeat.right != null)
                    tempSeat = tempSeat.right;
            }

            outputWriter.print("\n");     //Move to the next row
            currentRow++;                 //Increment row iterator
            if(startOfRow.down != null) {
                startOfRow = startOfRow.down; //Move down to next row
                tempSeat = startOfRow;
            }
        }

        //Print a report to the console and store the results to be returned
        System.out.print("Auditorium " + (whichFile + 1));
        roomResults[0] = (rows * columns);                  //Total seats in the room
        System.out.print("\t" + ((rows * columns) - occupiedSeats));
        roomResults[1] = (rows*columns) - occupiedSeats;    //Number of open seats
        System.out.print("\t" + occupiedSeats);
        roomResults[2] = occupiedSeats;                     //Number of reserved seats
        System.out.print("\t" + numAdults);
        roomResults[3] = numAdults;                         //Number of adult seats
        System.out.print("\t" + numChildren);
        roomResults[4] = numChildren;                       //Number of child seats
        System.out.print("\t" + numSeniors + "\t$");
        roomResults[5] = numSeniors;                        //Number of senior tickets
        System.out.printf("%.2f", moneyMade);
        roomResults[6] = moneyMade;                         //Amount made from this room
        System.out.print("\n");

        //Close printWriter, and then close the output file
        outputWriter.close();
        outFile.close();

        return roomResults;
    }
}