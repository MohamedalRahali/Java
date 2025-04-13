package models;

public class TypeEvent {

    int evenment_id;
    int t_id;
public TypeEvent() {}
    public TypeEvent(int evenment_id, int t_id) {
        this.evenment_id = evenment_id;
        this.t_id = t_id;
    }

    public int getEvenment_id() {
        return evenment_id;
    }

    public void setEvenment_id(int evenment_id) {
        this.evenment_id = evenment_id;
    }

    public int getT_id() {
        return t_id;
    }

    public void setT_id(int t_id) {
        this.t_id = t_id;
    }

    @Override
    public String toString() {
        return "TypeEvent{" +
                "evenment_id=" + evenment_id +
                ", t_id=" + t_id +
                '}';
    }
}
