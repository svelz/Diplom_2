package model;

public class Order {
    private String[] ingredients;

    public Order(String[] ingredients) {
        this.ingredients = ingredients;
    }

    public String[] getIngredients() {
        return ingredients;
    }
}
