package ass4;

public class SharedMemory {
    private byte[] contents;
    private boolean empty = true;

    /**
     * Puts an object in the box.  This method returns when
     * the object has been put into the box.
     *
     * @param msg The object to be put in the box.
     */
    public synchronized void put(byte[] msg) {
        while (!empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                return;
            }
        }
        contents = msg;
        empty = false;
        notifyAll();
    }

    /**
     * Gets an object from the box.  This method returns once the
     * object has been removed from the box.
     *
     * @return The object taken from the box.
     */
    public synchronized byte[] get() {
        while (empty) {
            try {
                wait();
            } catch (InterruptedException e) {
                return null;
            }
        }
        byte[] msg = contents;
        contents = null;
        empty = true;
        notifyAll();
        return msg;
    }
}
