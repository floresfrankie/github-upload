/* Written by Frankie Flores for CS2336.003
   NetID: fxf180009
*/

public class Seat {
    //Class Members
    int row;
    char seat;
    char ticketType;

    //Constructors
    Seat() {
        row = -1;
        seat = '\0';
        ticketType = '\0';
    }

    Seat(int seatRow, char exactSeat, char ticket) {
        row = seatRow;
        seat = exactSeat;
        ticketType = ticket;
    }

    //Mutators
    public void setTicketType(char ticketType) {
        this.ticketType = ticketType;
    }

    public char getTicketType() {
        return ticketType;
    }
}
