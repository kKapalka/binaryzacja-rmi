package server;

import gui.GUI;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.imageio.ImageIO;
import rmi.Binarizer;

public class Server implements Binarizer {
    
    private GUI gui;
    private File loadedFile;
    private String Extension,newFile;
    BufferedImage newImage;
    public Server(GUI g) {
        gui = g;
    }
    
    public void bindStub() {
        try {
            LocateRegistry.createRegistry(1099);
            Binarizer stub = (Binarizer) UnicastRemoteObject.exportObject(this, 1099);
            Registry reg = LocateRegistry.getRegistry();
            reg.bind("Binarizer", stub);
            gui.getTextArea().setText("Załadowano: 100%\nSerwer oczekuje na instrukcje.");
        } catch(Exception e) {
            gui.getTextArea().setText(gui.getTextArea().getText() + "\n>>>>> Wystąpił błąd: " + e.toString());
        }
    }
    @Override
    public void loadFile(File file) throws RemoteException{
        try{
            loadedFile=file;
            Extension=file.getName().substring(file.getName().lastIndexOf(".")+1);
        gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nZaładowano plik: "+file.getCanonicalPath()+" .");
        }catch (IOException ioex){
            gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nNie udało się załadować pliku.");
        }
    }
    @Override
    public String binarize(String mode, int value1, int value2) throws RemoteException{
        if(mode==null) mode="Dolnoprogowa";
        if(value1==0 && "Dolnoprogowa".equals(mode)){
                            gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nBlad: nie ustawiono dolnego progu.");
                            return null;
                        }
        if(value2==0 && "Gornoprogowa".equals(mode)){
                            gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nBlad: nie ustawiono gornego progu.");
                            return null;
                        }
        if(value1==0 && value2==0 && ("Dwuprogowa".equals(mode) || "Warunkowa".equals(mode))){
            gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nBlad: nie ustawiono progów.");
                            return null;
        }
        gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nRozpoczęto binaryzację pliku. Tryb: "+mode+ ". Progi: "+value1+", "+value2+".");
        switch(Extension){
            case "jpg": case "jpeg":
            case "bmp":
            case "png":
                try{
           newFile=loadedFile.getPath().substring(0,loadedFile.getPath().lastIndexOf("."))+"-binarized."+Extension;
            gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nUtworzono sciezke do nowego pliku.");
            BufferedImage loadedImg =ImageIO.read(loadedFile);
gui.getTextArea().setText(gui.getTextArea().getText()+
                "\ndebug");
            WritableRaster raster= (WritableRaster) loadedImg.getData();
             gui.getTextArea().setText(gui.getTextArea().getText()+
                "\ndebug");
            int pixel,blue,green,red,greyscale;
            int[] black={0,0,0};
            int[] white={255,255,255};
            int[] warunkowa={0,0,0};
                 
            for(int i=0;i<raster.getWidth();i++){
                for(int j=0;j<raster.getHeight();j++){
                    pixel = loadedImg.getRGB(i, j);
                    blue = pixel & 0xff;
                    green = (pixel & 0xff00) >> 8;
                    red = (pixel & 0xff0000) >> 16;
                    greyscale=(blue+green+red)/3;
                    if ("Dolnoprogowa".equals(mode)){
                        
                        if(greyscale<=value1){
                            raster.setPixel(i, j, black);
                        }
                        else {
                            raster.setPixel(i, j, white);
                        }
                    }
                    else if("Gornoprogowa".equals(mode)){
                        
                        if(greyscale>=value2) raster.setPixel(i, j, black);
                        else raster.setPixel(i, j, white);
                    }
                    else if("Dwuprogowa".equals(mode)){
                        if(greyscale<=value1 || greyscale>value2) raster.setPixel(i,j,black);
                        else raster.setPixel(i,j,white);
                    }
                    else if("Warunkowa".equals(mode)){
                        if(greyscale<=value1){
                            raster.setPixel(i,j,black);
                            warunkowa=black;
                        }
                        else if(greyscale>value2){
                            raster.setPixel(i,j,white);
                            warunkowa=white;
                        }
                        else if(i==1){
                            if(greyscale<value1+Math.round((value2-value1)/2)){
                                raster.setPixel(i,j,black);
                                warunkowa=black;
                            }
                            else{ raster.setPixel(i,j,white);
                            warunkowa=white;
                            }
                        }
                        else raster.setPixel(i,j,warunkowa);
                    }
                }
            }
            ColorModel colorModel = new ComponentColorModel(
        ColorSpace.getInstance(ColorSpace.CS_sRGB), 
        new int[]{8, 8, 8}, // bits
        false, // hasAlpha
        false, // isPreMultiplied
        ComponentColorModel.OPAQUE, 
        DataBuffer.TYPE_BYTE);
            newImage=new BufferedImage(colorModel,raster,false,null);
            gui.getTextArea().setText(gui.getTextArea().getText()+
                "\ndebug");
            ImageIO.write((RenderedImage)newImage,Extension,new File (newFile));
            
             gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nUstawiono nowe informacje.");
             break;
                } catch (IOException ioex){
                    gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nNie odczytano pliku. Rozszerzenie: "+Extension+".");
                    return null;
                }
            default:
                gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nNie odczytano pliku. Rozszerzenie: "+Extension+".");
                return null;
        }
        gui.getTextArea().setText(gui.getTextArea().getText()+
                "\nOdsyłam ścieżkę do pliku do klienta: "+newFile+".");
        return newFile;
    }
    
}
