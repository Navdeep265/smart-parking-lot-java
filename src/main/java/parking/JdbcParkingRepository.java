package parking;
import java.sql.*;
import java.util.*;
public final class JdbcParkingRepository implements ParkingRepository {
    private final Connection db;
    public JdbcParkingRepository(String url) throws SQLException,ClassNotFoundException {
        Class.forName("org.hsqldb.jdbcDriver"); db=DriverManager.getConnection(url,"sa","");
        try(Statement s=db.createStatement()) {
            if(!db.getMetaData().getTables(null,null,"SPOTS",new String[]{"TABLE"}).next()) {
                s.executeUpdate("CREATE CACHED TABLE spots(id INTEGER PRIMARY KEY, floor_no INTEGER NOT NULL, size_rank INTEGER NOT NULL)");
                s.executeUpdate("CREATE CACHED TABLE vehicles(plate VARCHAR(20) PRIMARY KEY, kind VARCHAR(15) NOT NULL)");
                s.executeUpdate("CREATE CACHED TABLE tickets(id VARCHAR(36) PRIMARY KEY, plate VARCHAR(20) NOT NULL, spot INTEGER NOT NULL, entry_ms BIGINT NOT NULL, exit_ms BIGINT, rate INTEGER NOT NULL, fee INTEGER, FOREIGN KEY(plate) REFERENCES vehicles(plate), FOREIGN KEY(spot) REFERENCES spots(id))");
                s.executeUpdate("CREATE CACHED TABLE active_parking(spot INTEGER PRIMARY KEY, plate VARCHAR(20) NOT NULL, ticket VARCHAR(36) NOT NULL, UNIQUE(plate), UNIQUE(ticket), FOREIGN KEY(spot) REFERENCES spots(id), FOREIGN KEY(plate) REFERENCES vehicles(plate), FOREIGN KEY(ticket) REFERENCES tickets(id))");
                s.executeUpdate("CREATE INDEX spot_priority ON spots(size_rank,floor_no,id)");
                for(int f=1;f<=3;f++) for(int i=1;i<=11;i++) s.executeUpdate("INSERT INTO spots VALUES("+(f*100+i)+","+f+","+(i<=4?1:i<=9?2:3)+")");
            }
        }
    }
    public synchronized <T> T transaction(Work<T> work) throws SQLException {db.setAutoCommit(false);try{T result=work.run();db.commit();return result;}catch(SQLException|RuntimeException|Error e){db.rollback();throw e;}finally{db.setAutoCommit(true);}}
    public synchronized boolean isParked(String plate)throws SQLException {try(PreparedStatement q=db.prepareStatement("SELECT plate FROM active_parking WHERE plate=?")){q.setString(1,plate);try(ResultSet r=q.executeQuery()){return r.next();}}}
    public synchronized List<ParkingSpot> freeSpots()throws SQLException {List<ParkingSpot> list=new ArrayList<>();try(Statement q=db.createStatement();ResultSet r=q.executeQuery("SELECT s.id,s.floor_no,s.size_rank FROM spots s WHERE NOT EXISTS(SELECT 1 FROM active_parking a WHERE a.spot=s.id)")){while(r.next())list.add(new ParkingSpot(r.getInt(1),r.getInt(2),r.getInt(3)));}return list;}
    public synchronized void register(Vehicle v)throws SQLException {try(PreparedStatement q=db.prepareStatement("SELECT kind FROM vehicles WHERE plate=?")){q.setString(1,v.plate());try(ResultSet r=q.executeQuery()){if(r.next()){if(!r.getString(1).equals(v.type().name()))throw new Conflict("Vehicle type differs from registration");}else try(PreparedStatement insert=db.prepareStatement("INSERT INTO vehicles VALUES(?,?)")){insert.setString(1,v.plate());insert.setString(2,v.type().name());insert.executeUpdate();}}}}
    public synchronized void save(Ticket t)throws SQLException {try(PreparedStatement q=db.prepareStatement("INSERT INTO tickets(id,plate,spot,entry_ms,rate) VALUES(?,?,?,?,?)")){q.setString(1,t.id());q.setString(2,t.plate());q.setInt(3,t.spot());q.setLong(4,t.entry());q.setInt(5,t.rate());q.executeUpdate();}try(PreparedStatement q=db.prepareStatement("INSERT INTO active_parking VALUES(?,?,?)")){q.setInt(1,t.spot());q.setString(2,t.plate());q.setString(3,t.id());q.executeUpdate();}}
    public synchronized void complete(Ticket t)throws SQLException {try(PreparedStatement q=db.prepareStatement("UPDATE tickets SET exit_ms=?,fee=? WHERE id=?")){q.setLong(1,t.exit());q.setInt(2,t.fee());q.setString(3,t.id());q.executeUpdate();}try(PreparedStatement q=db.prepareStatement("DELETE FROM active_parking WHERE ticket=?")){q.setString(1,t.id());q.executeUpdate();}}
    public synchronized Ticket getTicket(String id) throws SQLException {
        try(PreparedStatement q=db.prepareStatement("SELECT t.plate,v.kind,t.spot,s.floor_no,t.entry_ms,t.exit_ms,t.fee,t.rate FROM tickets t JOIN vehicles v ON t.plate=v.plate JOIN spots s ON t.spot=s.id WHERE t.id=?")){q.setString(1,id);try(ResultSet r=q.executeQuery()){if(!r.next())throw new NoSuchElementException("Ticket not found");long exit=r.getLong(6);Long end=r.wasNull()?null:exit;int fee=r.getInt(7);Integer amount=r.wasNull()?null:fee;return new Ticket(id,r.getString(1),VehicleType.valueOf(r.getString(2)),r.getInt(3),r.getInt(4),r.getLong(5),end,amount,r.getInt(8));}}
    }
    public synchronized List<Availability> availability() throws SQLException {List<Availability> list=new ArrayList<>();try(Statement q=db.createStatement();ResultSet r=q.executeQuery("SELECT s.floor_no,s.size_rank,COUNT(*),COUNT(a.spot) FROM spots s LEFT JOIN active_parking a ON s.id=a.spot GROUP BY s.floor_no,s.size_rank ORDER BY s.floor_no,s.size_rank")){while(r.next())list.add(new Availability(r.getInt(1),r.getInt(2),r.getInt(3),r.getInt(3)-r.getInt(4)));}return list;}
    public synchronized void close() throws SQLException {try(Statement s=db.createStatement()){s.execute("SHUTDOWN");}finally{db.close();}}
}

