import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.util.List;


public class DesignManagerVerticle extends AbstractVerticle {

    private HttpServer server;
    private Router router;

    @Override
    public void start(Future<Void> startFuture) {

        router = Router.router(vertx);

        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads")); //set upload directory

        // Expose html page as main page, don't need this in the future
        router
                .route("/*")
                .handler(StaticHandler.create("upload"));

        /* Routing by HTTP method*/

        router
                .post("/message")
                .handler(this::TestHandler);


        // Route to handlers
        router
                .post("/api/upload")
                .handler(this::uploadHandler);

        router
                .post("/api/delete")
                .handler(this::deleteHandler);

        router
                .post("/api/list")
                .handler(this::listHandler);

        router
                .post("/api/download")
                .handler(this::downloadHandler);

        server = vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(9000, res -> {
                    if (res.succeeded()) {
                    System.out.println("Server succeed deployed");
                    startFuture.complete();
                    } else {
                        startFuture.fail(res.cause());
                    }
                });
    }


    private void TestHandler(RoutingContext ctx){
        ctx.response().putHeader("content-type", "text/html");
        ctx.response().setChunked(true);
        System.out.println("GET Handler!");
        String option = ctx.request().getParam("action");

        System.out.println(option);
        if(option.equals("ListFiles")){
            System.out.println("I'm Here");
            ctx.response().write("<h3>" + "Hello Vert.x---List Files!" + "</h3>");
        }else if(option.equals("Nope")){
            ctx.response().write("<h3>Hello World--- Nothing happens!</h3>");
        }else{
            System.out.println("There should be a webpage");
            ctx.response().write("Something wrong happens in GET");
        }
        ctx.response().end();
    }

    /** Request params:
     * "File"
     * description */
    private void uploadHandler(RoutingContext ctx){

        ctx.response().putHeader("Content-Type", "text/html");

        ctx.response().setChunked(true);

        System.out.println("Upload Handler!");

        if(ctx.fileUploads().size()>1){
            System.out.println("Error: can only upload 1 file once");
            return;
        }

        String description = ctx.request().getParam("description");

        System.out.println("Description:" + description);

        S3Verticle s3 = new S3Verticle();

        for(FileUpload file : ctx.fileUploads()){
            ctx.response().write("<h3>" + "Filename: " + file.fileName() + "</h3>");
            ctx.response().write("<h3>" + "ContentType: " + file.contentType() + "</h3>");
            ctx.response().write("<h3>" + "Name: " + file.name() + "</h3>");
            ctx.response().write("<h3>" + "Actual Name: " + file.uploadedFileName() + "</h3>"); // diff b/w actual name and filename
            File testFile = new File(file.uploadedFileName());
            File uploadFile = new File("uploads/" + file.fileName());
            if(!testFile.renameTo(uploadFile)){
                System.out.println("Rename Failed");
            }

            try{
                uploadFile.createNewFile();
                System.out.println("Rename Success");
            }catch(Exception e){
                e.printStackTrace();
            }
            ctx.response().write("<h3>" + "Upload File: " + uploadFile.getPath() + "</h3>");

            try{
                s3.putDesign(uploadFile.toPath(), description);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        // Need to clean the /uploads directory here

        // Response with a html page, don't need this in the future
        ctx.response().end();
    }

    /** Request params
     * id
     * title */
    private void deleteHandler(RoutingContext ctx){
        System.out.println("Delete Handler!");
        String title  = ctx.request().getParam("title");
        S3Verticle s3 = new S3Verticle();
        try{
            s3.deleteDesign(title);
        }catch(Exception e){
            e.printStackTrace();
            ctx.response()
                    .putHeader("Content-Type","text/plain")
                    .end("Delete failed");
        }
        ctx.response().setStatusCode(200).end();
        System.out.println("Delete Finished");
    }


    /** Request params:
     * (null)
     * Return a JSON file with information of all designs in HTTP response */
    private void listHandler(RoutingContext ctx){
        System.out.println("List Handler!");
        S3Verticle s3 = new S3Verticle();

        ctx.response().putHeader("Content-Type", "application/json");
        ctx.response().setChunked(true);

        List<S3Object> list;
        try{
            list = s3.listDesign();
        }catch(Exception e){
            e.printStackTrace();
            ctx.response().end("List Error");
            return;
        }

        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        JsonObject tempJsonObj = new JsonObject();
        System.out.println("Building JsonArray  " + list.size());
        // Iterate objects list, build JsonArray that store information of all design
        for(S3Object obj: list){
            tempJsonObj.clear();
            String key = obj.key();
            System.out.println(obj.key());
            String URLStr = s3.getDesignURL(obj.key()).toString();
            System.out.println("URL: "+ URLStr);
            tempJsonObj.put("title", key)
                    .put("URL", URLStr);
            array.add(tempJsonObj);
        }

        System.out.println("Array: " + array.toString());
        json.put("arrayOfDesign", array);
        System.out.println("JSON: " + json.toString());

        ctx.response()
                .write(json.toBuffer())
                .end();
    }

    /**Request params:
     * key*/
    private void downloadHandler(RoutingContext ctx){
        System.out.println("Download Handler!");
        String key = ctx.request().getParam("key");
        String descriptionKey = key + "-description.txt";
        S3Verticle s3 = new S3Verticle();
        try{
            s3.getDesign(key);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Download design failed!");
        }
        ctx.response()
                .sendFile("./download/design/" + key)
                .sendFile("./download/description/" + descriptionKey);


    }

    private void cleanUpHandler(RoutingContext ctx){
        System.out.println("I'm here!");

        boolean result = deleteDirectory(new File("./download"));
        System.out.println("Result: " + result);
        ctx.response().end();
    }

    public boolean deleteDirectory(File directoryToBeDeleted){
        File[] allContents = directoryToBeDeleted.listFiles();
        if(allContents != null){
            for(File file : allContents){
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }


}
