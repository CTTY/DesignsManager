import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.*;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;
import java.nio.file.Files;


public class DesignManagerVerticle extends AbstractVerticle {

    private HttpServer server;
    private Router router;

    @Override
    public void start(Future<Void> startFuture) {
        // Expose static resources
        router = Router.router(vertx);

        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads")); //set upload directory

        router
                .route("/*")
                .handler(StaticHandler.create("upload"));

        /* Routing by HTTP method*/

        // GET
        router
                .post("/message")
                .handler(this::GETHandler);


        // POST
        router
                .postWithRegex("/upload/*")
                .handler(this::POSTHandler);

        server = vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, res -> {
                    if (res.succeeded()) {
                    System.out.println("Server succeed deployed");
                    startFuture.complete();
                    } else {
                        startFuture.fail(res.cause());
                    }
                });
    }


    private void GETHandler(RoutingContext ctx){
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

    private void POSTHandler(RoutingContext ctx){

        ctx.response().putHeader("Content-Type", "text/html");

        ctx.response().setChunked(true);

        System.out.println("POST Handler!");

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
            ctx.response().write("<h3>" + "Actual Name: " + file.uploadedFileName() + "</h3>");
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

            s3.putDesign(uploadFile.toPath(), description);
        }
        ctx.response().end();
    }


}
