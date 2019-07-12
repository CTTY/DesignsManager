import java.io.File;

public class Design {

    private String title;
    private String url; //Can only get url from S3!!!
    private File file;


    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public File getFile() {
        return file;
    }

    Design(){

    }

    Design(String title, File file){

    }




    @Override
    public String toString(){
         return title;
     }
}
