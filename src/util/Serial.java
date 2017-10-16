/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

/**
 *
 * @author rafael
 */
public class Serial implements SerialPortEventListener {

    private SerialPort serialPort;
    private boolean staus = false;

    /**
     * Um BufferedReader que será alimentado por um InputStreamReader
     * convertendo o      * bytes em caracteres que tornam a página de códigos
     * de resultados exibida independente
     */
    private BufferedReader input;
    /**
     * O fluxo de saída para a porta
     */
    private OutputStream output;
    /**
     * Milisegundos para bloquear enquanto aguarda a abertura da porta
     */
    private static final int TIME_OUT = 2000;
    /**
     * Bits padrão por segundo para a porta COM.
     */
    private static final int DATA_RATE = 9600;

    private String inputLine;

    public void initialize(String namePort) {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //Primeiro, encontre uma instância de porta serial configurada em PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            if (namePort.equals(currPortId.getName())) {
                portId = currPortId;
                break;
            }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // abrir a porta serial e usar o nome da classe para o appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // definir parâmetros de porta
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // abra os fluxos
            if (input == null) {
                input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            }
            output = serialPort.getOutputStream();

            // adicionar ouvintes de eventos
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException e) {
            System.err.println(e.toString());
        }
    }

    public void send(String data) {
        try {
            output.write(data.getBytes());
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    public String read() {
        return inputLine;
    }

    public void reset() {
        inputLine = null;
    }

    public void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Isso deve ser chamado quando você parar de usar a porta. Isso evitará
     *      * bloqueio de portas em plataformas como o Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    /**
     * Gerencia um evento na porta serial. Lee os dados e imprime.
     *
     * @param oEvent
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                if (input == null) {
                    input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                }
                if (SerialPortEvent.OUTPUT_BUFFER_EMPTY > 0) {
                    inputLine = input.readLine();
                }
            } catch (IOException e) {
                System.err.println(e.toString()+"  vaiasdf");
            }
        }
        // Ignore todos os outros eventTypes, mas você deve considerar os outros.
    }

}
