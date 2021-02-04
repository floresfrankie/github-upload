/* Written by Frankie Flores for CS2336.003
   NetID: fxf180009
*/

public class Node <T>{
    //Class Members
    Node<T> up;
    Node<T> down;
    Node<T> left;
    Node<T> right;
    T payload;

    //Constructors
    Node () {
        up = null;
        down = null;
        left = null;
        right = null;
        payload = null;
    }

    Node (T payload_seat) {
        up = null;
        down = null;
        left = null;
        right = null;
        payload = payload_seat;
    }

    //Mutators
    public void setUp(Node<T> up) {
        this.up = up;
    }

    public void setDown(Node<T> down) {
        this.down = down;
    }

    public void setLeft(Node<T> left) {
        this.left = left;
    }

    public void setRight(Node<T> right) {
        this.right = right;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }
}
