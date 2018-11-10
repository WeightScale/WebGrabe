package com.kostya.webscaleslibrary.module;


import java.io.Serializable;

/**
 * @author Kostya  on 24.07.2016.
 */
public class ObjectCommand implements Serializable {
    private static final long serialVersionUID = 3124433923323779155L;
    private final Commands command;
    private String value;
    private boolean isResponse;

    public ObjectCommand(Commands command, String value){
        this.command = command;
        this.value = value;
    }

    public Commands getCommand() {return command;}
    public String getValue() {return value;}
    public void setResponse(boolean response) {isResponse = response;}
    public boolean isResponse() { return isResponse; }
    public void setValue(String value) {this.value = value;}
}

class ResponseCommand{
    private final String command;
    private boolean isResponse = false;

    public ResponseCommand(String command){
        this.command = command;
    }

    public String getCommand() {return command;}
    public void setResponse(boolean response) {isResponse = response;}
    public boolean isResponse() { return isResponse; }
}