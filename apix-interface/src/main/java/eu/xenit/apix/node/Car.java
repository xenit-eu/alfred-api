package eu.xenit.apix.node;

public class Car {
    public String color;
    public int id;

    public Car(){

    }

    public Car(String color, int id) {
        this.color = color;
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

