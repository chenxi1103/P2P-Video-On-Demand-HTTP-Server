import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class test {
    public static void main(String[] args) {
        File file = new File("src/main/resources/index.html");
        try{
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] byteFile = new byte[(int)file.length()];
            fileInputStream.read(byteFile);
            fileInputStream.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e1) {

        }

    }
}
