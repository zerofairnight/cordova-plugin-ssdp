package io.zero.cordova.ssdp;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Locale;
import java.util.Scanner;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SSDP extends CordovaPlugin {
    private static final String SSDP_HOST = "239.255.255.250";
    private static final int SSDP_PORT = 1900;
    private static final int SSDP_MULTICAST_PORT = 1901;
    private static final int TIMEOUT = 3000;

    @Override
    public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if ("getNetworkServices".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        callbackContext.success(getNetworkServices(args.getString(0)));
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });

            return true;
        }

        return false;
    }

    /**
     * Discover the network services.
     *
     * @param  service  The service to discover.
     */
    private JSONArray getNetworkServices(final String service) throws JSONException, SocketException, IOException {
        DatagramSocket socket = null;
        DatagramPacket receivePacket;

        // send the discover request
        discover(service);

        try {
            // create a socket to read the incoming packets
            socket = new DatagramSocket(SSDP_MULTICAST_PORT);
            socket.setSoTimeout(TIMEOUT);

            // the return services
            JSONArray services = new JSONArray();

            while (true) {
                try {
                    // receive the ssdp packet
                    receivePacket = new DatagramPacket(new byte[1536], 1536);
                    socket.receive(receivePacket);

                    // read the packet payload
                    services.put(readPayload(receivePacket.getData()));
                } catch (SocketTimeoutException ignore) {
                    break;
                }
            }

            // return the collected services
            return services;
        } finally {
            if (socket != null) {
                socket.disconnect();
                socket.close();
            }
        }
    }

    /**
     * Send the discover packet for the specified serivce.
     *
     * @param  service  The service to discover.
     */
    private void discover(final String service) throws IOException {
        // create the M-Search packet
        StringBuffer sb = new StringBuffer();
        sb.append("M-SEARCH * HTTP/1.1\r\n");
        sb.append("HOST: " + SSDP_HOST + ":" + SSDP_PORT + "\r\n");
        sb.append("ST:"+service+"\r\n");
        sb.append("MAN: \"ssdp:discover\"\r\n");
        sb.append("MX: 2\r\n");
        sb.append("\r\n");

        // create the dgram packet
        byte[] buf = sb.toString().getBytes();
        DatagramPacket discoveryPacket = new DatagramPacket(
            buf,
            buf.length,
            new InetSocketAddress(InetAddress.getByName(SSDP_HOST), SSDP_PORT)
        );

        // broadcast the multi-cast packet
        MulticastSocket multicast = null;
        try {
            multicast = new MulticastSocket(null);
            multicast.bind(new InetSocketAddress(SSDP_MULTICAST_PORT));
            multicast.setTimeToLive(4);
            multicast.send(discoveryPacket);
        } finally {
            if (multicast != null) {
                multicast.disconnect();
                multicast.close();
            }
        }
    }

    /**
     * Read the payload into a JSON object.
     *
     * @param  buffer  The payload buffer to read.
     */
    private static JSONObject readPayload(final byte[] buffer) throws JSONException {
        final String content = new String(buffer);
        final Scanner s = new Scanner(content);

        // the return payload
        final JSONObject payload = new JSONObject();

        // set some default values
        payload.put("usn", JSONObject.NULL);
        payload.put("location", JSONObject.NULL);
        payload.put("st", JSONObject.NULL);
        payload.put("server", JSONObject.NULL);

        // first line is the header
        // M-SEARCH * HTTP/1.1
        s.nextLine();

        // read all the headers, one per line
        while (s.hasNextLine()) {
            String line = s.nextLine();
            int index = line.indexOf(':');

            if (index > 0) {
                String name = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();

                // do not include empty values
                if (name.length() > 0 && value.length() > 0) {
                    payload.put(name.toLowerCase(Locale.ENGLISH), value);
                }
            }
        }

        return payload;
    }
}
