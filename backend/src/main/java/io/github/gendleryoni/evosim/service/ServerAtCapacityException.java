package io.github.gendleryoni.evosim.service;

public class ServerAtCapacityException extends RuntimeException {

    public ServerAtCapacityException() {
        super("The server is currently at capacity.");
    }
}
