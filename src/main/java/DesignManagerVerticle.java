import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.*;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;


public class DesignManagerVerticle extends AbstractVerticle {

    private HttpServer server;
    private Router router;

    @Override
    public void start(Future<Void> startFuture) {
        // Expose static resources
        router = Router.router(vertx);

        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads")); //???

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
        String option = ctx.request().getParam("Action");

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
//                    System.out.println(ctx.fileUploads().toString());
        for(FileUpload file : ctx.fileUploads()){
            ctx.response().write("<h3>" + "Filename: " + file.fileName() + "</h3>");
            ctx.response().write("<h3>" + "ContentType: " + file.contentType() + "</h3>");
            ctx.response().write("<h3>" + "Name: " + file.name() + "</h3>");
            ctx.response().write("<h3>" + "Actual Name: " + file.uploadedFileName() + "</h3>");
            File testFile = new File(file.uploadedFileName());
            if(testFile.renameTo(new File("uploads/hello.txt"))){
                System.out.println("Rename Successful");
            }else{
                System.out.println("Failed");
            }
            ctx.response().write("<h3>" + "Test File: " + testFile.getPath() + "</h3>");
        }
        ctx.response().end();
    }


}
