package code;

public class TunnelDto {
    private Position from;
    private Position to;

    public TunnelDto() {}

    public TunnelDto(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Position getFrom() { return from; }
    public void setFrom(Position from) { this.from = from; }

    public Position getTo() { return to; }
    public void setTo(Position to) { this.to = to; }
}