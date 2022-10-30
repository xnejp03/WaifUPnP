/*
 * Copyright (C) 2015 Federico Dossena (adolfintel.com).
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.dosse.upnp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 * This class contains static methods that allow quick access to UPnP Port Mapping.<br>
 * Commands will be sent to the default gateway.
 * 
 * @author Federico
 */
public class UPnP {

    private final InetAddress ip;

    private Gateway defaultGW = null;
    private final GatewayFinder finder;

    public UPnP(InetAddress ip) {
        this.ip = ip;

        finder = new GatewayFinder(ip) {
            @Override
            public void gatewayFound(Gateway g) {
                synchronized (finder) {
                    if (defaultGW == null) {
                        defaultGW = g;
                    }
                }
            }
        };
    }

    /**
     * Waits for UPnP to be initialized (takes ~3 seconds).<br>
     * It is not necessary to call this method manually before using UPnP functions
     */
    public void waitInit() {
        while (finder.isSearching()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    /**
     * Is there an UPnP gateway?<br>
     * This method is blocking if UPnP is still initializing<br>
     * All UPnP commands will fail if UPnP is not available
     * 
     * @return true if available, false if not
     */
    public boolean isUPnPAvailable(){
        waitInit();
        return defaultGW!=null;
    }

    /**
     * Opens a TCP port on the gateway
     * 
     * @param internalPort TCP port (0-65535)
     * @param externalPort TCP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public boolean openPortTCP(String name, int leaseDuration, int internalPort, int externalPort) {
        if(!isUPnPAvailable()) return false;
        return defaultGW.openPort(name, leaseDuration, internalPort, externalPort, false);
    }
    
    /**
     * Opens a UDP port on the gateway
     * 
     * @param internalPort UDP port (0-65535)
     * @param externalPort UDP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public boolean openPortUDP(String name, int leaseDuration, int internalPort, int externalPort) {
        if(!isUPnPAvailable()) return false;
        return defaultGW.openPort(name, leaseDuration, internalPort, externalPort, true);
    }
    
    /**
     * Closes a TCP port on the gateway<br>
     * Most gateways seem to refuse to do this
     * 
     * @param externalPort TCP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public boolean closePortTCP(int externalPort) {
        if(!isUPnPAvailable()) return false;
        return defaultGW.closePort(externalPort, false);
    }
    
    /**
     * Closes a UDP port on the gateway<br>
     * Most gateways seem to refuse to do this
     * 
     * @param externalPort UDP port (0-65535)
     * @return true if the operation was successful, false otherwise
     */
    public boolean closePortUDP(int externalPort) {
        if(!isUPnPAvailable()) return false;
        return defaultGW.closePort(externalPort, true);
    }
    
    /**
     * Checks if a TCP port is mapped<br>
     * 
     * @param port TCP port (0-65535)
     * @return true if the port is mapped, false otherwise
     */
    public boolean isMappedTCP(int port) {
        if(!isUPnPAvailable()) return false;
        return defaultGW.isMapped(port, false);
    }
    
    /**
     * Checks if a UDP port is mapped<br>
     * 
     * @param port UDP port (0-65535)
     * @return true if the port is mapped, false otherwise
     */
    public boolean isMappedUDP(int port) {
        if(!isUPnPAvailable()) return false;
        return defaultGW.isMapped(port, true);
    }
    
    /**
     * Gets the external IP address of the default gateway
     * 
     * @return external IP address as string, or null if not available
     */
    public String getExternalIP(){
        if(!isUPnPAvailable()) return null;
        return defaultGW.getExternalIP();
    }
    
    /**
     * Gets the internal IP address of this machine
     * 
     * @return internal IP address as string, or null if not available
     */
    public String getLocalIP(){
        if(!isUPnPAvailable()) return null;
        return defaultGW.getLocalIP();
    }

    /**
     * Gets the  IP address of the router
     *
     * @return internal IP address as string, or null if not available
     */
    public String getDefaultGatewayIP(){
        if(!isUPnPAvailable()) return null;
        return defaultGW.getGatewayIP();
    }

    public static Inet4Address[] getLocalIPs() {
        LinkedList<Inet4Address> ret = new LinkedList<Inet4Address>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                try {
                    NetworkInterface iface = ifaces.nextElement();
                    if (!iface.isUp() || iface.isLoopback() || iface.isVirtual() || iface.isPointToPoint() || iface.getDisplayName().toLowerCase().contains("virtual")) {
                        continue;
                    }
                    Enumeration<InetAddress> addrs = iface.getInetAddresses();
                    if (addrs == null) {
                        continue;
                    }
                    while (addrs.hasMoreElements()) {
                        InetAddress addr = addrs.nextElement();
                        if (addr instanceof Inet4Address) {
                            ret.add((Inet4Address) addr);
                        }
                    }
                } catch (Throwable t) {
                }
            }
        } catch (Throwable t) {
        }
        return ret.toArray(new Inet4Address[]{});
    }

}
