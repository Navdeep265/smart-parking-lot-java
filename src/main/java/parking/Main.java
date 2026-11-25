package parking;
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.nio.file.*;
public final class Main {
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("data"));
        ParkingService service=new ParkingService(new JdbcParkingRepository("jdbc:hsqldb:file:data/parking"),new BestFitAllocation(),new HourlyFeeStrategy(),Clock.systemUTC());
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",args.length>0?Integer.parseInt(args[0]):8080),0);
        ExecutorService pool=Executors.newFixedThreadPool(8); server.setExecutor(pool);
        server.createContext("/api/",e->{try {String path=e.getRequestURI().getPath(),method=e.getRequestMethod();String result;int status=200;
            if(path.equals("/api/check-in")&&method.equals("POST")){var p=form(e);result=json(service.checkIn(required(p,"plate"),VehicleType.valueOf(required(p,"type").toUpperCase(Locale.ROOT))));status=201;}
            else if(path.equals("/api/check-out")&&method.equals("POST"))result=json(service.checkOut(required(form(e),"ticket")));
            else if(path.startsWith("/api/tickets/")&&method.equals("GET"))result=json(service.getTicket(path.substring(13)));
            else if(path.equals("/api/availability")&&method.equals("GET")){var rows=service.availability();result="["+String.join(",",rows.stream().map(a->String.format("{\"floor\":%d,\"sizeRank\":%d,\"total\":%d,\"available\":%d}",a.floor(),a.size(),a.total(),a.available())).toList())+"]";}
            else {send(e,404,"{\"error\":\"Unknown endpoint or method\"}");return;}send(e,status,result);
        }catch(Conflict ex){send(e,409,error(ex.getMessage()));}catch(NoSuchElementException ex){send(e,404,error(ex.getMessage()));}catch(IllegalArgumentException ex){send(e,400,error("Invalid input: "+ex.getMessage()));}catch(Exception ex){ex.printStackTrace();send(e,500,error("Internal server error"));}finally{e.close();}});
        server.createContext("/",e->{try{if(!e.getRequestURI().getPath().equals("/")){send(e,404,error("Not found"));return;}byte[] b=Files.readAllBytes(Path.of("web/index.html"));e.getResponseHeaders().set("Content-Type","text/html; charset=utf-8");e.sendResponseHeaders(200,b.length);e.getResponseBody().write(b);}finally{e.close();}});
        Runtime.getRuntime().addShutdownHook(new Thread(()->{server.stop(1);pool.shutdown();try{service.close();}catch(Exception ex){ex.printStackTrace();}}));server.start();System.out.println("Parking API: http://localhost:"+server.getAddress().getPort());
    }
    private static Map<String,String> form(HttpExchange e) throws IOException {byte[] bytes=e.getRequestBody().readNBytes(8193);if(bytes.length>8192)throw new IllegalArgumentException("Request too large");Map<String,String> m=new HashMap<>();for(String pair:new String(bytes,StandardCharsets.UTF_8).split("&")){String[] kv=pair.split("=",2);if(kv.length==2)m.put(URLDecoder.decode(kv[0],StandardCharsets.UTF_8),URLDecoder.decode(kv[1],StandardCharsets.UTF_8));}return m;}
    private static String required(Map<String,String> m,String key){String s=m.get(key);if(s==null||s.isBlank())throw new IllegalArgumentException("Missing "+key);return s;}
    private static String quote(String s){return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replaceAll("[\\x00-\\x1f]"," ")+"\"";}
    private static String error(String s){return "{\"error\":"+quote(s)+"}";}
    private static String json(Ticket t){return "{\"ticketId\":"+quote(t.id())+",\"plate\":"+quote(t.plate())+",\"vehicleType\":"+quote(t.type().name())+",\"spot\":"+t.spot()+",\"floor\":"+t.floor()+",\"entryTime\":"+quote(Instant.ofEpochMilli(t.entry()).toString())+",\"exitTime\":"+(t.exit()==null?"null":quote(Instant.ofEpochMilli(t.exit()).toString()))+",\"feeINR\":"+t.fee()+",\"status\":"+quote(t.exit()==null?"ACTIVE":"COMPLETED")+"}";}
    private static void send(HttpExchange e,int status,String s)throws IOException{byte[] b=s.getBytes(StandardCharsets.UTF_8);e.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");e.sendResponseHeaders(status,b.length);e.getResponseBody().write(b);}
}
