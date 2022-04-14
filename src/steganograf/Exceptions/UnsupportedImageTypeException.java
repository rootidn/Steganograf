/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganograf.Exceptions;

/**
 *
 * @author ACER
 */
public class UnsupportedImageTypeException extends Exception{
    public UnsupportedImageTypeException(String message) {super(message);}
    public UnsupportedImageTypeException(String message, Throwable cause) {super(message, cause);}
}
