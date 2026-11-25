package parking;
public interface FeeStrategy { int hourlyRate(VehicleType type); int calculate(long entry,long exit,int rate); }
