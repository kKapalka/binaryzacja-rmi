package client;

import gui.GUI;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.JOptionPane;
import rmi.Binarizer;

public class Client {

    private GUI gui;
    private Binarizer stub;
    
    public Client(GUI g) {
        gui = g;
    }
    
    public Binarizer getStub() {
        return stub;
    }
    
    public void connectServer() {
        try {
            Registry reg = LocateRegistry.getRegistry();
            Binarizer stub = (Binarizer) reg.lookup("Binarizer");
            this.stub = stub;
        } catch(Exception e) {
            JOptionPane.showMessageDialog(gui,
                "Błąd połączenia.",
                "CONN_ERROR",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
