package cn.bg7qvu.mtrwebctc.model;

/**
 * 平台 DTO
 */
public class PlatformDTO {
    private long id;
    private long stationId;
    private String name;
    private int color;
    private double x;
    private double y;
    private double z;
    private String dimension;
    private int dwellTime;
    private boolean isOpen;
    
    // Getters
    public long getId() { return id; }
    public long getStationId() { return stationId; }
    public String getName() { return name; }
    public int getColor() { return color; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getDimension() { return dimension; }
    public int getDwellTime() { return dwellTime; }
    public boolean isOpen() { return isOpen; }
    
    // Setters
    public void setId(long id) { this.id = id; }
    public void setStationId(long stationId) { this.stationId = stationId; }
    public void setName(String name) { this.name = name; }
    public void setColor(int color) { this.color = color; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public void setDwellTime(int dwellTime) { this.dwellTime = dwellTime; }
    public void setOpen(boolean open) { isOpen = open; }
}
