package rmi;

import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.awt.image.BufferedImage;
public interface Binarizer extends Remote {

    /**
     *
     * @param file
     * @throws RemoteException
     * @throws IOException
     */
    public void loadFile(File file) throws RemoteException;
    public String binarize(String mode, int value1, int value2) throws RemoteException;

}
