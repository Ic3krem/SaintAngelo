package com.stangelo.saintangelo.services;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleIntegerProperty;

public class QueueService {
    private static final SimpleIntegerProperty currentQueueNumber = new SimpleIntegerProperty(0);
    private static final int MAX_QUEUE_NUMBER = 260;

    public static SimpleIntegerProperty currentQueueNumberProperty() {
        return currentQueueNumber;
    }

    public static int getCurrentQueueNumber() {
        return currentQueueNumber.get();
    }

    public static void incrementQueueNumber() {
        int current = currentQueueNumber.get();
        // Loop from 260 back to 1
        currentQueueNumber.set((current % MAX_QUEUE_NUMBER) + 1);
    }

    public static void resetQueue() {
        currentQueueNumber.set(0);
    }

    public static String formatQueueNumber(int n) {
        if (n <= 0 || n > MAX_QUEUE_NUMBER) {
            return "---"; // Placeholder for out of bounds
        }
        char letter = (char) ('A' + (n - 1) / 10);
        int number = (n - 1) % 10 + 1;
        return "" + letter + number;
    }

    public static StringBinding queueNumberAsStringBinding() {
        return Bindings.createStringBinding(() -> {
            int n = currentQueueNumber.get();
            if (n == 0) {
                return "0";
            }
            return formatQueueNumber(n);
        }, currentQueueNumber);
    }
}
