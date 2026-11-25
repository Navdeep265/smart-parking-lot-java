package parking;
public final class HourlyFeeStrategy implements FeeStrategy {
    public int hourlyRate(VehicleType type){return switch(type){case MOTORCYCLE->20;case CAR->50;case BUS->100;};}
    public int calculate(long entry,long exit,int rate){if(exit<entry||rate<0)throw new IllegalArgumentException("Invalid duration or rate");long duration=exit-entry;long hours=Math.max(1,duration/3600000+(duration%3600000==0?0:1));return Math.toIntExact(Math.multiplyExact(hours,rate));}
}
