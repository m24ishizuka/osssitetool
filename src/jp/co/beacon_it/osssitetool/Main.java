package jp.co.beacon_it.osssitetool;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class Main {

    public static void main(String[] args) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream("makecsv.properties"));
            Properties prop = new Properties();
            prop.load(in);
            
            String dirPath = prop.getProperty("directory_path");
            String oldCSVName = prop.getProperty("old_csv_name");
            String newCSVName = prop.getProperty("new_csv_name");
            String orderPatterns[] = prop.getProperty("order_patterns").split(",");
            boolean isUseCSVHeader = "true".equals(prop.getProperty("use_csv_header")) ? true : false;
            
            FileInfoCSVMaker ficm = new FileInfoCSVMaker(dirPath, oldCSVName, newCSVName, orderPatterns, isUseCSVHeader);
            
            System.out.println("start");
            ficm.exec();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch(SecurityException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.print("end");  
        }
    }
}
