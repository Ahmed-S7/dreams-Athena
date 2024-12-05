package com.example.ahmed_first_app.Interfaces;

/**
 * Interface representing a model in the observer pattern, allowing observers to be added, removed, and notified.
 */
public interface Model {
    public void addObserver(Observer observer);
    public void removeObserver(Observer observer);
    public void notifyObservers();
}
