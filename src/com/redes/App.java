package com.redes;

public class App {

    public static void main(String[] args) {
	    try {
            new RouterManager().run();
        } catch (Exception e) {
	        System.err.println(e.getMessage());
        }
    }

}
