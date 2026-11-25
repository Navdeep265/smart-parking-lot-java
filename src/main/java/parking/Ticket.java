package parking;
public record Ticket(String id,String plate,VehicleType type,int spot,int floor,long entry,Long exit,Integer fee,int rate) {}
